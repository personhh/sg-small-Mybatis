package com.cps.mybatis.reflection;

import com.cps.mybatis.reflection.invoker.GetFieldInvoker;
import com.cps.mybatis.reflection.invoker.Invoker;
import com.cps.mybatis.reflection.invoker.MethodInvoker;
import com.cps.mybatis.reflection.property.PropertyTokenizer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author cps
 * @description: 元类包装反射器
 * @date 2024/1/20 16:18
 * @OtherDescription: Reflector 反射器类提供的是最基础的核心功能，很多方法也都是私有的，
 * 为了更加方便的使用，还需要做一层元类的包装。
 * 在元类 MetaClass 提供必要的创建反射器以及使用反射器获取 get/set 的 Invoker 反射方法。
 */
public class MetaClass {

    private Reflector reflector;

    public MetaClass(Class<?> type) {
        this.reflector  = Reflector.forClass(type);
    }

    //获取类
    public static MetaClass forClass(Class<?> type){
        return new MetaClass(type);
    }

    //检查是否有缓存里的类
    public static boolean isClassCacheEnabled(){
      return Reflector.isClassCacheEnabled();
    }

    public static void setClassCacheEnabled(boolean classCacheEnabled){
        Reflector.setClassCacheEnabled(classCacheEnabled);
    }



    public MetaClass metaClassForProperty(PropertyTokenizer prop){
        Class<?> propType = getGetterType(prop);
        return MetaClass.forClass(propType);
    }

    public MetaClass metaClassForProperty(String name) {
        Class<?> propType = reflector.getGetterType(name);
        return MetaClass.forClass(propType);
    }


    public String findProperty(String name){
       StringBuilder prop =  buildProperty(name, new StringBuilder());
       return prop.length() > 0 ? prop.toString() : null;
    }

    public String findProperty(String name, boolean userCamelCaseMapping){
        if(userCamelCaseMapping) {
            name = name.replace("_", "");
        }
        return findProperty(name);
    }

    public String[] getGetterNames() {
        return reflector.getGetablePropertyNames();
    }

    public String[] getSetterNames() {
        return reflector.getSetablePropertyNames();
    }

    public Class<?> getSetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop.getName());
            return metaProp.getSetterType(prop.getChildren());
        } else {
            return reflector.getSetterType(prop.getName());
        }
    }

    public Class<?> getGetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop);
            return metaProp.getGetterType(prop.getChildren());
        }
        // issue #506. Resolve the type inside a Collection Object
        return getGetterType(prop);
    }

    private StringBuilder buildProperty(String name, StringBuilder stringBuilder) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if(prop.hasNext()){
            String propertyName = reflector.findPropertyName(prop.getName());
            if(propertyName != null){
                stringBuilder.append(propertyName);
                stringBuilder.append('.');
                MetaClass metaProp = metaClassForProperty(propertyName);
                metaProp.buildProperty(prop.getChildren(), stringBuilder);
            }
        }else{
            String propertyName = reflector.findPropertyName(name);
            if(propertyName != null){
                stringBuilder.append(propertyName);
            }
        }
        return stringBuilder;
    }


    private Class<?> getGetterType(PropertyTokenizer prop) {
        Class<?> type = reflector.getGetterType(prop.getName());
        if (prop.getIndex() != null && Collection.class.isAssignableFrom(type)) {
            Type returnType = getGenericGetterType(prop.getName());
            if (returnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnType = actualTypeArguments[0];
                    if (returnType instanceof Class) {
                        type = (Class<?>) returnType;
                    } else if (returnType instanceof ParameterizedType) {
                        type = (Class<?>) ((ParameterizedType) returnType).getRawType();
                    }
                }
            }
        }
        return type;
    }

    //Generic通用的
    private Type getGenericGetterType(String propertyName) {
        try {
            Invoker invoker = reflector.getGetInvoker(propertyName);
            if (invoker instanceof MethodInvoker) {
                Field _method = MethodInvoker.class.getDeclaredField("method");
                _method.setAccessible(true);
                Method method = (Method) _method.get(invoker);
                return method.getGenericReturnType();
            } else if (invoker instanceof GetFieldInvoker) {
                Field _field = GetFieldInvoker.class.getDeclaredField("field");
                _field.setAccessible(true);
                Field field = (Field) _field.get(invoker);
                return field.getGenericType();
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return null;
    }

    public boolean hasSetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (reflector.hasSetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop.getName());
                return metaProp.hasSetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasSetter(prop.getName());
        }
    }

    public boolean hasGetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (reflector.hasGetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop);
                return metaProp.hasGetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasGetter(prop.getName());
        }
    }
    public Invoker getGetInvoker(String name) {
        return reflector.getGetInvoker(name);
    }

    public Invoker getSetInvoker(String name) {
        return reflector.getSetInvoker(name);
    }

    public boolean hasDefaultConstructor() {
        return reflector.hasDefaultConstructor();
    }
}
