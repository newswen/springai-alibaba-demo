package com.yw.springaialibabademobiz.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RagConfig {

//    @Bean
//    VectorStore vectorStore(EmbeddingModel embeddingModel) {
//        //SimpleVectorStore 是将向量保存在内存 ConcurrentHashmap 中，Spring AI 提供了多种存储方式，如 Redis、MongoDB 等，可以根据实际情况选择适合的存储方式。
//        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel)
//                .build();
//
//        // 生成一个机器人产品说明书的文档
//        List<Document> documents = List.of(
//                new Document("产品说明书:产品名称：智能机器人\n" +
//                        "产品描述：智能机器人是一个智能设备，能够自动完成各种任务。\n" +
//                        "功能：\n" +
//                        "1. 自动导航：机器人能够自动导航到指定位置。\n" +
//                        "2. 自动抓取：机器人能够自动抓取物品。\n" +
//                        "3. 自动放置：机器人能够自动放置物品。\n"));
//
//        simpleVectorStore.add(documents);
//        return simpleVectorStore;
//    }


}