package com.yw.springaialibabademo.biz;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RAGTest {

    @Resource
    private TokenTextSplitter tokenTextSplitter;
    //    @Resource
//    private SimpleVectorStore simpleVectorStore;

    @Resource
    private PgVectorStore pgVectorStore;

    @Test
    public void upload() {
        // 从IO流中读取文件
        TikaDocumentReader reader = new TikaDocumentReader("./data/test.md");
        // 将文件转为Document  读到的是完整大文本 Document
        List<Document> documents = reader.read();
        //将文本内容划分为更小的片段 是把大文本切割成小块 Document 基于 token 数做限制，默认约256~512 token 级别
        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);
        //给整个大文本添加元数据
        documents.forEach(doc -> doc.getMetadata().put("knowledge", "文案违禁词"));
        //给切割后的每一个文档添加元数据 保持大统一
        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "文案违禁词"));
        // 存入向量数据库，这个过程会自动调用embeddingModel,将文本变成向量再存入
        pgVectorStore.accept(documentSplitterList);
        log.info("上传完成");
    }

    @Test
    public void testBasic() {
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
        // Add the documents to PGVector
        pgVectorStore.add(documents);

        // Retrieve documents similar to a query
        //      //这部分是通过大模型对查询进行向量匹配，然后返回最相似的文档 简单的文本相似度检索
        //根据传入的自然语言文本 "Spring" 自动调用内置的 embedding 模型，将文本转成向量。
        //然后在 pgvector 向量库里找 最相似的前 5 条（topK=5）文档。
        //返回的是包含内容、元数据的 Document 列表。
        List<Document> results = this.pgVectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        log.info("results: {}", results);
    }

}