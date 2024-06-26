package com.cps.mybatis.type;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//别名类型注册器
public class TypeAliasRegistry {

    //别名类型集，k-别名 v-别名类型
    private final Map<String, Class<?>> TYPE_ALIASES = new HashMap<>();

    public TypeAliasRegistry() {
        //构造函数里注册系统内置的类型别名
        registerAlias("string", String.class);


        //基本包装类型
        registerAlias("byte", Byte.class);
        registerAlias("long", Long.class);
        registerAlias("short", Short.class);
        registerAlias("int", Integer.class);
        registerAlias("integer", Integer.class);
        registerAlias("double", Double.class);
        registerAlias("float", Float.class);
        registerAlias("boolean", Boolean.class);
    }

    //注册别名类型
    public void registerAlias(String alias, Class<?> value){
        String key = alias.toLowerCase(Locale.ENGLISH);//给别名转成小写
        TYPE_ALIASES.put(key,value);//放入集合中
    }

    //查询别名类型并返回
    public <T> Class<T> resolveAlias(String string){
        String key = string.toLowerCase(Locale.ENGLISH);
        return (Class<T>) TYPE_ALIASES.get(key);
    }
}
