package com.yw.springaialibaba.api;


import com.yw.springaialibaba.model.QaPair;

import java.util.List;

/**
 * 亚马逊产品文案服务接口
 */
public interface ICopyService {

    /**
     * 根据产品标题生成问答内容-实体结构化输出List
     *
     * @return Q&A 列表
     */
    List<QaPair> generateQaList();
}
