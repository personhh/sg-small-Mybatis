package com.cps.mybatis.executor.result;

import com.cps.mybatis.session.ResultContext;

/**
 * @author cps
 * @description: TODO
 * @date 2024/1/28 15:43
 * @OtherDescription: Other things
 */
public class DefaultResultContext implements ResultContext {


    private Object resultObject;
    private int resultCount;

    public DefaultResultContext() {
        this.resultObject = null;
        this.resultCount = 0;
    }

    @Override
    public Object getResultObject() {
        return resultObject;
    }

    @Override
    public int getResultCount() {
        return resultCount;
    }

    public void nextResultObject(Object resultObject) {
        resultCount++;
        this.resultObject = resultObject;
    }


}
