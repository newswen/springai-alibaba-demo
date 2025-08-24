package com.yw.springaialibaba.api;

import reactor.core.publisher.Flux;

/**
 * 聊天服务接口
 * 定义同步和流式两种聊天方法
 */
public interface IChatService {

    /**
     * 基于输入内容获取同步聊天回答
     *
     * @param query 用户输入内容
     * @return 聊天回复
     */
    String call(String query);

    /**
     * 基于输入内容获取流式聊天回答（SSE）
     *
     * @param query 用户输入内容
     * @return 聊天回复流
     */
    Flux<String> chatStream(String query);
}
