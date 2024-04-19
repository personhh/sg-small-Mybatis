package com.cps.mybatis.reflection;

import com.cps.mybatis.reflection.factory.ObjectFactory;
import com.cps.mybatis.reflection.property.PropertyTokenizer;
import com.cps.mybatis.reflection.wrapper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author cps
 * @description: TODO
 * @date 2024/1/20 17:42
 * @OtherDescription: Other things
 */
public class MetaObject {

    //原对象
    private Object originalObject;
    //对象包装器
    private ObjectWrapper objectWrapper;
    //对象工厂
    private ObjectFactory objectFactory;
    //对象包装工厂
    private ObjectWrapperFactory objectWrapperFactory;


    //初始化元对象中的包装器，判断元对象类型，
    public MetaObject(Object originalObject, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        this.originalObject = originalObject;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;

        if(originalObject instanceof ObjectWrapper){
            // 如果对象本身已经是ObjectWrapper型，则直接赋给objectWrapper
            this.objectWrapper = (ObjectWrapper) originalObject;
        }else if(objectWrapperFactory.hasWrapperFor(originalObject)){
            // 如果有包装器,调用ObjectWrapperFactory.getWrapperFor
            this.objectWrapper = objectWrapperFactory.getWrapperFor(this,originalObject);
        }else if(originalObject instanceof Map){
            // 如果是Map型，返回MapWrapper
            this.objectWrapper = new MapWrapper(this, (Map) originalObject);
        }else if(originalObject instanceof Collection){
            // 如果是Collection型，返回CollectionWrapper
            this.objectWrapper = new CollectionWrapper(this,(Collection)originalObject);
        }else{
            // 除此以外，返回BeanWrapper
            this.objectWrapper = new BeanWrapper(this, originalObject);
        }
    }


    public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory defaultWrapperFactory) {
        if(object == null){
            //处理一下null，将null包装来
            return SystemMetaObject.NULL_META_OBJECT;
        }else {
            //不是null，生产元对象
            return new MetaObject(object, objectFactory, defaultWrapperFactory);
        }
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public Object getOriginalObject() {
        return originalObject;
    }


    /*===============以下方法都是委托给 ObjectWrapper=============*/

    //查找属性
    public String findProperty(String propName, boolean useCameCaseMapping){
        return objectWrapper.findProperty(propName,useCameCaseMapping);
    }

    // 取得getter的名字列表
    public String[] getGetterNames() {
        return objectWrapper.getGetterNames();
    }

    // 取得setter的名字列表
    public String[] getSetterNames() {
        return objectWrapper.getSetterNames();
    }

    // 取得setter的类型列表
    public Class<?> getSetterType(String name) {
        return objectWrapper.getSetterType(name);
    }

    // 取得getter的类型列表
    public Class<?> getGetterType(String name) {
        return objectWrapper.getGetterType(name);
    }

    //是否有指定的setter
    public boolean hasSetter(String name) {
        return objectWrapper.hasSetter(name);
    }

    // 是否有指定的getter
    public boolean hasGetter(String name) {
        return objectWrapper.hasGetter(name);
    }



    //取得值 如班级[0].学生.成绩
    public Object getValue(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if(prop.hasNext()){
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            if(metaValue == SystemMetaObject.NULL_META_OBJECT){
                //如果上层是null，就结束了，返回null
                return null;
            }else {
                //否则继续看下一层
                return metaValue.getValue(prop.getChildren());
            }
        }else {
            return objectWrapper.get(prop);
        }
    }


    public void setValue(String name ,Object value){
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
        if(propertyTokenizer.hasNext()){
            MetaObject metaValue = metaObjectForProperty(propertyTokenizer.getIndexedName());
            if(metaValue == SystemMetaObject.NULL_META_OBJECT) {
                if (value == null && propertyTokenizer.getChildren() != null) {
                    //如果上层就是null了，还得看有没有儿子，没有那就结束
                    return;
                } else {
                    // 否则还得 new 一个，委派给 ObjectWrapper.instantiatePropertyValue
                    metaValue = objectWrapper.instantiatePropertyValue(name, propertyTokenizer, objectFactory);
                }
            }
            //递归调用setValue
            metaValue.setValue(propertyTokenizer.getChildren(), value);
        }else {
            //到了最后一层了，所有委派 ObjectWrapper.set
            objectWrapper.set(propertyTokenizer,value);
        }
    }

    public MetaObject metaObjectForProperty(String name){
        //实际是递归调用
        Object value = getValue(name);
        return MetaObject.forObject(value, objectFactory, objectWrapperFactory);
    }

    public ObjectWrapper getObjectWrapper(){
        return objectWrapper;
    }
    public boolean isCollection(){
        return objectWrapper.isCollection();
    }

    //添加属性
    public void add(Object element){
        objectWrapper.add(element);
    }

    public <E> void addAll(List<E> list){
        objectWrapper.addAll(list);
    }

}
