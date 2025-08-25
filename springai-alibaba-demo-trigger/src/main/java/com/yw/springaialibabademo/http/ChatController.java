package com.yw.springaialibabademo.http;

import com.yw.springaialibaba.api.IChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yingzi
 * @date 2025/5/21 10:11
 */
@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
@Slf4j
public class ChatController implements IChatService {

    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

    private final ChatClient chatClient;

    @Resource
    private PgVectorStore pgVectorStore;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem(DEFAULT_PROMPT)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .temperature(0.9)
                                .build()
                )
                .build();
    }

    @Override
    @GetMapping("/call")
    public String call(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {
        return chatClient.prompt(query).call().content();
    }

    /**
     * curl http://localhost:1104/chat/stream?model=gpt-4o-mini&query=1+1
     *
     * @param model
     * @param query 用户输入内容
     * @return
     */
    @Override
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam(value = "model") String model, @RequestParam(value = "ragTag") String ragTag, @RequestParam(value = "query") String query) {
        //作用是在 AI 生成内容之前或期间，为生成过程提供基于检索内容的增强上下文
//        //RAG
//        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
//                .documentRetriever(VectorStoreDocumentRetriever.builder()
//                        .vectorStore(pgVectorStore)
//                        .build())
//                .build();
        //改成知识库检索
        List<Document> documents = pgVectorStore.similaritySearch(SearchRequest
                .builder()
                //会调用ai的embedding模型进行向量编码 进行相似度维度搜索
                .query(query)
                //取前五个
                .topK(5)
                //会过滤到向量库中knowledge == '知识库名称'
                .filterExpression("knowledge == '" + ragTag + "'")
                .build());
        //将搜索到的集合到一起
        String documentCollectors = documents.stream().map(Document::getText).toList().toString();
        String systemPrompt = "你是一个智能机器人，请根据用户输入，给出一个回答:{documents}";
        //SystemPromptTemplate 是一个提示词模板类，它可以根据提供的数据，把模板里的 {占位符} 替换成实际值。
        Message ragMessage = new SystemPromptTemplate(systemPrompt).createMessage(Map.of("documents", documentCollectors));
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(query));
        messages.add(ragMessage);
        log.info("messages : {}", messages);
        return chatClient.prompt(Prompt.builder()
                        .messages(messages)
                        .build())
                .options(
                        OpenAiChatOptions.builder()
                                .model(model)
                                .temperature(1.0)
                                .build()
                )
//                .advisors(retrievalAugmentationAdvisor)
                .stream().content();
    }
}