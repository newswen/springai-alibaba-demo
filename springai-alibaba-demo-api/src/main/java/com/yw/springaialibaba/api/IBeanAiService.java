package com.yw.springaialibaba.api;

import com.yw.springaialibaba.model.BeanEntity;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

/**
 * AI问答服务接口
 */
public interface IBeanAiService {


    /**
     * 基于底层的 目前有问题 暂时不用
     *
     * @param query
     * @return
     */
    String call(String query);

    /**
     * 结构化输出 在某些场景下，我们希望得到结构化的数据
     * 这里会自动根据对象的属性 对提问ai的时候 加上结构化提示词 最后会自动处理转换返回
     * @param query
     * @return
     */
    BeanEntity callFormat(String query);

}
