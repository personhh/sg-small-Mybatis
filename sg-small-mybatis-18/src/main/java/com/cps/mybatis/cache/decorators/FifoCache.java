package com.cps.mybatis.cache.decorators;

import com.cps.mybatis.cache.Cache;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author cps
 * @description: 二级缓存中的fifo
 * @date 2024/2/23 15:09
 * @OtherDescription: FIFO：先进先出，按对象进入缓存的顺序移除过期对象
 */
public class FifoCache implements Cache {

    private final Cache delegate;
    private Deque<Object> keyList;
    private int size;

    public FifoCache(Cache delegate) {
        this.delegate = delegate;
        this.keyList = new LinkedList<>();
        this.size = 1024;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    public void setSize(int size) {
        this.size = size;
    }

    //存放
    @Override
    public void putObject(Object key, Object value) {
        cycleKeyList(key);
        delegate.putObject(key, value);
    }

    //获取
    @Override
    public Object getObject(Object key) {
        return delegate.getObject(key);
    }

    //移除
    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    //清空
    @Override
    public void clear() {
        delegate.clear();
        keyList.clear();
    }

    //作用是在增加记录时判断记录是否超过size值，一次移除链表的第一个元素
    private void cycleKeyList(Object key) {
        keyList.addLast(key);
        if (keyList.size() > size) {
            Object oldestKey = keyList.removeFirst();
            delegate.removeObject(oldestKey);
        }
    }

}
