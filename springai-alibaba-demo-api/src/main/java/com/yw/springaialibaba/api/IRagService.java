package com.yw.springaialibaba.api;

public interface IRagService {

    /**
     * 基于用户输入，通过向量检索增强生成回答
     * @param userInput 用户输入
     * @return AI 生成的回答
     */
    String generateAnswer(String userInput);

    /**
     * 检索知识库并返回检索结果的文本集合
     * @param query 检索关键词
     * @param topK 返回前 K 个文档
     * @return 文档文本列表
     */
    String searchDocuments(String query, int topK);
}
