package com.yw.springaialibabademobiz.controller;

import com.yw.springaialibabademobiz.entity.BeanEntity;
import com.yw.springaialibabademobiz.entity.QaPair;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: yw
 * @Date: 2025/8/7 20:18
 * @Description:
 **/
@RestController
@RequestMapping("/copy")
public class CopyController {


    private static final String DEFAULT_PROMPT = "你是一名专业的亚马逊产品文案专家，擅长撰写提升转化率的问答内容。请以自然、友好、富有帮助性的语气，引导用户了解产品的特点、优势及使用建议。";
    ;

    private final ChatClient chatClient;

    public CopyController(ChatClient.Builder builder) {
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

    @GetMapping("/call")
    public List<QaPair> call() {
        String QA_User_Prompt =
                "请根据以下产品标题，生成 5 组英文风格的亚马逊 Q&A 问答内容。\n" +
                        "- 每个问题必须包含一个关键词，围绕产品功能、优势、用途或适用人群进行提问；\n" +
                        "- 问题语气需积极自然，避免使用否定表达（如包含 'no' 的句式）；\n" +
                        "- 回答应简洁明了，突出产品卖点，语言自然易懂；\n" +
                        "- 输出顺序应与输入产品标题顺序一致。\n\n" +
                        "以下是产品标题：\n" +
                        "Wireless Bluetooth Noise Cancelling Headphones with Mic\n" +
                        "\n" +
                        "Stainless Steel Insulated Travel Coffee Mug - 16oz\n" +
                        "\n" +
                        "Adjustable Ergonomic Office Chair with Lumbar Support\n" +
                        "\n" +
                        "Smart LED Light Bulb Compatible with Alexa and Google Home\n" +
                        "\n" +
                        "Waterproof Fitness Tracker Watch with Heart Rate Monitor\n" +
                        "\n";
        return chatClient.prompt(QA_User_Prompt).call().entity(new ParameterizedTypeReference<List<QaPair>>() {});
    }

}
