package com.cps.mybatis.builder.annotation;

import com.cps.mybatis.annotations.Delete;
import com.cps.mybatis.annotations.Insert;
import com.cps.mybatis.annotations.Select;
import com.cps.mybatis.annotations.Update;
import com.cps.mybatis.binding.MapperMethod;
import com.cps.mybatis.builder.MapperBuilderAssistant;
import com.cps.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.cps.mybatis.executor.keygen.KeyGenerator;
import com.cps.mybatis.executor.keygen.NoKeyGenerator;
import com.cps.mybatis.mapping.SqlCommandType;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.ResultHandler;
import com.cps.mybatis.session.defaults.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author cps
 * @description: 注册配置构建器Mapper
 * @date 2024/2/18 14:47
 * @OtherDescription: Other things
 */
public class MapperAnnotationBuilder {

    private final Set<Class<? extends Annotation>> sqlAnnotationTypes = new HashSet<>();
    private Configuration configuration;

    private MapperBuilderAssistant assistant;

    //注解类型（增删改查）
    private Class<?> type;

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        String resource = type.getName().replace(".", "/" + ".java (best guess)");
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;

        sqlAnnotationTypes.add(Select.class);
        sqlAnnotationTypes.add(Insert.class);
        sqlAnnotationTypes.add(Update.class);
        sqlAnnotationTypes.add(Delete.class);
    }

    public void parse(){
        String resource = type.toString();
        if(!configuration.isResourceLoaded(resource)){
            assistant.setCurrentNameSpace(type.getName());

            Method[] methods = type.getMethods();
            for(Method method : methods){
                if(!method.isBridge()){
                    //解析语句
                    parseStatement(method);
                }
            }
        }
    }

    private void parseStatement(Method method) {
        //获取方法的参数类型
        Class<?> parameterTypesClass = getParameterType(method);
        //获取脚本语言注册器
        LanguageDriver languageDriver = getLanguageDriver(method);
        //获取SQL源码
        SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypesClass, languageDriver);

        //判断sql源码是否为空
        if(sqlSource != null){
            //将获取映射语句id
            final String mappedStatementId = type.getName() + "." + method.getName();
            //获取语句指令类型
            SqlCommandType sqlCommandType = getSqlCommandType(method);

            // step-14 新增
            KeyGenerator keyGenerator;
            String keyProperty = "id";
            if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
                keyGenerator = configuration.isUseGeneratedKeys() ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
            } else {
                keyGenerator = new NoKeyGenerator();
            }

            //判断语句指令类型是不是select
            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

            String resultMapId = null;
            if(isSelect){
                resultMapId = parseResultMap(method);
            }

            //调用助手类
            assistant.addMappedStatement(mappedStatementId,sqlSource,sqlCommandType,parameterTypesClass,resultMapId,getReturnType(method),false, false, keyGenerator,keyProperty,languageDriver);
        }
    }

    private SqlCommandType getSqlCommandType(Method method){
        Class<? extends Annotation> type = getSqlAnnotationType(method);
        if(type == null){
            return SqlCommandType.UNKNOWN;
        }
        return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
    }

    private String parseResultMap(Method method) {
        // generateResultMapName
        StringBuilder suffix = new StringBuilder();
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }
        if (suffix.length() < 1) {
            suffix.append("-void");
        }
        String resultMapId = type.getName() + "." + method.getName() + suffix;

        // 添加 ResultMap
        Class<?> returnType = getReturnType(method);
        assistant.addResultMap(resultMapId, returnType, new ArrayList<>());
        return resultMapId;
    }


    /**
     * 重点：DAO 方法的返回类型，如果为 List 则需要获取集合中的对象类型
     */
    private Class<?> getReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Collection.class.isAssignableFrom(returnType)) {
            Type returnTypeParameter = method.getGenericReturnType();
            if (returnTypeParameter instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnTypeParameter).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (issue #443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        // (issue #525) support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            }
        }
        return returnType;
    }



    private Class<?> getParameterType(Method method){
        Class<?> parameterType = null;
        //获得方法的参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        //每个参数类型进行处理
        for(Class<?> clazz : parameterTypes){
            //判断参数类型是不是继承关系
            if(!RowBounds.class.isAssignableFrom(clazz) && !ResultHandler.class.isAssignableFrom(clazz)){
                if(parameterType == null){
                    parameterType =clazz;
                }else{
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }

    private LanguageDriver getLanguageDriver(Method method){
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        return configuration.getLanguageRegistry().getDriver(langClass);
    }

    public SqlSource getSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver){
        try{
            //获得注解类型
            Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
            //判断类型是否为空，为空直接返回null，不为空就返回sql源码
            if(sqlAnnotationType != null){
                //从方法中获得注解
                Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
                //再从注解中获得value属性的值
                final String[] strings = (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
                //根据value属性的值和参数类型还有语言脚本注册器返回sql源码
                return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
            }
            return null;
        }catch (Exception e){
            throw new RuntimeException("Could not find value method on SQL annotation.  Cause: " + e);
        }
    }

    private Class<? extends Annotation> getSqlAnnotationType(Method method){
        for(Class<? extends Annotation> type : sqlAnnotationTypes){
            Annotation annotation = method.getAnnotation(type);
            if(annotation != null) return type;
        }
        return null;
    }
    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver){
        final StringBuilder sql = new StringBuilder();
        for(String fragment : strings){
            sql.append(fragment);
            sql.append("");
        }
        return languageDriver.createSqlSource(configuration, sql.toString(), parameterTypeClass);
    }

}
