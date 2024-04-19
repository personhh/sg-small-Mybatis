package com.cps.mybatis.cache;

/**
 * @author cps
 * @description: 缓存接口
 * @date 2024/2/23 10:53
 * @OtherDescription: 缓存接口主要提供了数据的存放、获取、删除、情况，以及数量大小的获取。这样的实现方式和我们通常做业务开发时，定义的数据存放都是相似的。
 */
public interface Cache {
    /**
     * 获取ID，每个缓存都有唯一ID标识
     */
    String getId();

    /**
     * 存入值
     */
    void putObject(Object key, Object value);

    /**
     * 获取值
     */
    Object getObject(Object key);

    /**
     * 删除值
     */
    Object removeObject(Object key);

    /**
     * 清空
     */
    void clear();

    /**
     * 获取缓存大小
     */
    int getSize();

}
