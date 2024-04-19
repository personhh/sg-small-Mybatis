package com.cps.mybatis.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/*通过类加载器获得resource辅助类*/
public class Resources {

    //将资源resource解析成reader对象
    public static Reader getResourceAsReader(String resource) throws IOException{
        //把InputStream转成Reader
        return new InputStreamReader(getResourceAsStream(resource));
    }

    //将资源resource解析成InputStream（字节输出流）
    private static InputStream getResourceAsStream(String resource) throws IOException{
        //获得类加载器数组
        ClassLoader[] classLoaders = getClassLoaders();
        //遍历
        for(ClassLoader classLoader : classLoaders){
            //通过加载器的方法将resource文件转换成字节流
            InputStream inputStream = classLoader.getResourceAsStream(resource);
            if(null != inputStream){
                return inputStream;
            }
        }
        throw new IOException("Could not find resource " + resource);
    }

    //获得系统类型加载和该线程的上下文类加载器
    private static ClassLoader[] getClassLoaders(){
        return new ClassLoader[]{
                ClassLoader.getSystemClassLoader(),
                //返回该线程的上下文类加载器
                Thread.currentThread().getContextClassLoader()
        };
    }

    //根据类名返回一个类的对象的引用
    public static Class<?> classForName(String className) throws ClassNotFoundException{
        //根据类的全类名返回一个class对象的引用
        return Class.forName(className);
    }
}
