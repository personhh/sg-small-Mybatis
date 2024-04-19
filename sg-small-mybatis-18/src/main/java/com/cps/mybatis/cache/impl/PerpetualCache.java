package com.cps.mybatis.cache.impl;

import com.alibaba.fastjson.JSON;
import com.cps.mybatis.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cps
 * @description: 缓存接口实现类：一级缓存，在Session生命周期内一直保持，每创建新的 OpenSession 都会创建一个缓存器 PerpetualCache
 * @date 2024/2/23 11:16
 * @OtherDescription: Other things
 */
public class PerpetualCache implements Cache {

    private String id;

    // 使用HashMap存放一级缓存数据，session 生命周期较短，正常情况下数据不会一直在缓存存放
    private Map<Object, Object> cache = new HashMap<>();

    public PerpetualCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        Object obj = cache.get(key);
        return obj;
    }

    @Override
    public Object removeObject(Object key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int getSize() {
        return cache.size();
    }


}
