package com.yw.springaialibabademo.http;

import com.yw.springaialibaba.api.IRagService;
import com.yw.springaialibaba.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Slf4j
@CrossOrigin(origins = "*")
public class RagController implements IRagService {

    private ChatClient chatClient;

    public RagController(ChatClient.Builder builder) {
        this.chatClient = builder.
                defaultSystem("你是一个智能机器人，请根据用户输入，给出一个回答")
                .build();
    }

    @Resource
    private PgVectorStore pgVectorStore;

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    /**
     * 从外部放入文件 进行知识切割存入知识库
     *http://localhost:8080/ai/upload?
     * @param ragTag 知识库元数据标签
     */
    @PostMapping("/upload")
    public Response<String> uploadFile(@RequestParam String ragTag, @RequestParam("files") List<MultipartFile> files) {
        log.info("上传知识库开始 {}", ragTag);
        for (MultipartFile file : files) {
            //读取为DocMent知识库
            TikaDocumentReader reader = new TikaDocumentReader(file.getResource());
            List<Document> documents = reader.read();
            //给整个文档添加元数据
            documents.forEach(document -> document.getMetadata().put("knowledge", ragTag));
            List<Document> splitDocuments = tokenTextSplitter.apply(documents);
            pgVectorStore.accept(splitDocuments);
        }
        return Response.<String>builder().code("200").info("上传成功").build();
    }


    @GetMapping(value = "/chat", produces = "text/plain; charset=UTF-8")
    public String generateAnswer(@RequestParam(value = "userInput", defaultValue = "你好，袁文今年几岁") String userInput) {
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(pgVectorStore)
                        .build())
                .build();
        // 发起聊天请求并处理响应
        return chatClient.prompt()
                .user(userInput)
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .content();
    }

    @GetMapping("/search")
    public String searchDocuments(@RequestParam(value = "query") String query, @RequestParam(value = "topK") int topK) {
        log.info("start search data");
        //向量相似度搜索
        List<Document> documents = pgVectorStore.similaritySearch(SearchRequest
                .builder()
                //会调用ai的embedding模型进行向量编码 进行相似度维度搜索
                .query(query)
                //取前五个
                .topK(topK)
                //会过滤到向量库中knowledge == '知识库名称'
                .filterExpression("knowledge == '知识库名称'")
                .build());
        //将搜索到的集合到一起
        String documentCollectors = documents.stream().map(Document::getText).toList().toString();
        String systemPrompt = "你是一个智能机器人，请根据用户输入，给出一个回答:{documents}";
        //SystemPromptTemplate 是一个提示词模板类，它可以根据提供的数据，把模板里的 {占位符} 替换成实际值。
        Message ragMessage = new SystemPromptTemplate(systemPrompt).createMessage(Map.of("documents", documentCollectors));
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("袁文今年几岁"));
        messages.add(ragMessage);
        log.info("messages : {}", messages);
//        ChatResponse chatResponse = chatClient.prompt(Prompt.builder()
//                .messages(messages)
//                .build()
//        ).call().content();
        //将得到的知识库和提问一起拼接成提示词 进行提问
        String content = chatClient.prompt(Prompt.builder()
                .messages(messages)
                .build()
        ).call().content();
        log.info("search result: {}", content);
        return content;
    }

    /**
     * 查询知识库名称
     */
    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     *  http://localhost:1104/ai/knowledge
     * @return
     */
    @GetMapping("/knowledge")
    public List<String> getKnowledge() {
        String sql = "SELECT DISTINCT metadata->>'knowledge' AS ragTag FROM vector_store WHERE metadata ? 'knowledge'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

}