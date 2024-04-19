package com.cps.mybatis.executor.result;

import com.cps.mybatis.reflection.factory.ObjectFactory;
import com.cps.mybatis.session.ResultContext;
import com.cps.mybatis.session.ResultHandler;

import java.util.List;

/**
 * @author cps
 * @description: 结果集收集器
 * @date 2024/1/28 10:47
 * @OtherDescription: 这里封装了一个非常简单的结果集对象，默认情况下都会写入到这个对象的 list 集合中。
 */
public class DefaultResultHandler implements ResultHandler {

    //list集合
    private final List<Object> list;

    //构造器
    public DefaultResultHandler(List<Object> list) {
        this.list = list;
    }

    //生成list对象工厂
    @SuppressWarnings("unchecked")
    public DefaultResultHandler(ObjectFactory objectFactory){
        this.list = objectFactory.create(List.class);
    }

    //增加集合内对象
    @Override
    public void handleResult(ResultContext context) {
        list.add(context.getResultObject());
    }

    public List<Object> getResultList(){
        return list;
    }
}
