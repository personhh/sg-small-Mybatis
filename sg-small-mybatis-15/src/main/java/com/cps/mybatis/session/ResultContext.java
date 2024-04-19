package com.cps.mybatis.session;

/**
 * @author cps
 * @description: 结果上下文
 * @date 2024/1/28 10:52
 * @OtherDescription: Other things
 */
public interface ResultContext {

    //获取结果
    Object getResultObject();

    //获取记录数
    int getResultCount();
}
