package com.cps.mybatis.cache;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cps
 * @description: 缓存key，一般缓存框架的数据接口基本上都是key -》value 方式存储
 * @date 2024/2/23 11:19
 * @OtherDescription: 通常使用 HashMap 的时候 Key 都是一个 String 的值，那么这里因为需要对查询的信息以及 SQL 做一个 ID 使用，
 * 但这样都拼装下来就太长了，所以在缓存 Key 的实现中，基于这些信息创建了一个新的 HashCode 作为 KEY 使用。
 */
public class CacheKey implements Cloneable, Serializable {
    private static final long serialVersionUID = 1146682552656046210L;

    public static final CacheKey NULL_CACHE_KEY = new NullCacheKey();

    private static final int DEFAULT_MULTIPLYER = 37;
    private static final int DEFAULT_HASHCODE = 17;

    private int multiplier;
    private int hashcode;
    private long checksum;
    private int count;
    private List<Object> updateList;

    public CacheKey() {
        this.hashcode = DEFAULT_HASHCODE;
        this.multiplier = DEFAULT_MULTIPLYER;
        this.count = 0;
        this.updateList = new ArrayList<>();
    }

    public CacheKey(Object[] objects) {
        this();
        updateAll(objects);
    }

    public int getUpdateCount() {
        return updateList.size();
    }

    public void update(Object object) {
        if (object != null && object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int i =0; i < length; i ++){
                Object element = Array.get(object, i);
                doUpdate(element);
            }
        } else {
            doUpdate(object);
        }
    }

    private void doUpdate(Object object) {
        // 计算Hash值，校验码
        int baseHashCode = object == null ? 1 : object.hashCode();

        count++;
        checksum += baseHashCode;
        baseHashCode *= count;

        hashcode = multiplier * hashcode + baseHashCode;

        updateList.add(object);
    }

    public void updateAll(Object[] objects) {
        for (Object o : objects) {
            update(o);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof CacheKey)) {
            return false;
        }

        final CacheKey cacheKey = (CacheKey) object;

        if (hashcode != cacheKey.hashcode) {
            return false;
        }
        if (checksum != cacheKey.checksum) {
            return false;
        }
        if (count != cacheKey.count) {
            return false;
        }

        for (int i = 0; i < updateList.size(); i++) {
            Object thisObject = updateList.get(i);
            Object thatObject = cacheKey.updateList.get(i);
            if (thisObject == null) {
                if (thatObject != null) {
                    return false;
                }
            } else {
                if (!thisObject.equals(thatObject)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder().append(hashcode).append(':').append(checksum);
        for (Object obj : updateList) {
            returnValue.append(':').append(obj);
        }

        return returnValue.toString();
    }

    @Override
    public CacheKey clone() throws CloneNotSupportedException {
        CacheKey clonedCacheKey = (CacheKey) super.clone();
        clonedCacheKey.updateList = new ArrayList<>(updateList);
        return clonedCacheKey;
    }


}
