package com.cps.mybatis.session;

/**
 * @author cps
 * @description: 结果处理器
 * @date 2024/1/19 13:30
 * @OtherDescription:
 */
public interface ResultHandler {
    void handleResult(ResultContext context);
}
