package com.cps.mybatis.builder;

import com.cps.mybatis.mapping.*;
import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cps
 * @description: 映射构建器助手，建造者
 * @date 2024/1/26 20:43
 * @OtherDescription: MapperBuilderAssistant 构建器助手专门为创建 MappedStatement 映射语句类而服务的，
 * 在这个类中封装了入参和出参的映射、以及把这些配置信息写入到 Configuration 配置项中。
 */
public class MapperBuilderAssistant extends BaseBuilder{

    private String currentNameSpace;//当前命名空间
    private String resource;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    public String getCurrentNameSpace() {
        return currentNameSpace;
    }

    public void setCurrentNameSpace(String currentNameSpace) {
        this.currentNameSpace = currentNameSpace;
    }

    //添加namespace前缀
    public String applyCurrentNamespace(String base, boolean isReference){
        if(base == null){
            return null;
        }
        if(isReference){
            if(base.contains(".")){
                return base;
            }
        }
        else {
            if(base.startsWith(currentNameSpace + ".")){
                return base;
            }
            if(base.contains(".")){
                throw new RuntimeException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNameSpace + "." + base;
    }
    //添加映射器语句
    public MappedStatement addMappedStatement(String id,
                                              SqlSource sqlSource,
                                              SqlCommandType sqlCommandType,
                                              Class<?> parameterType,
                                              String resultMap,
                                              Class<?> resultType,
                                              LanguageDriver lang){
        //给id加上namespace前缀
        id = applyCurrentNamespace(id,false);

        //生成mapperStatementBuilder对象
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);

        //进行结果映射，给MappedStatement添加resultMaps
        setStatementResultMap(resultMap, resultType, statementBuilder);

        MappedStatement statement = statementBuilder.build();
        //映射语句信息。建造完存放到配置中
        configuration.addMappedStatement(statement);

        return statement;

    }

    //结果映射，设置语句结果集
    private void setStatementResultMap(String resultMap, Class<?> resultType, MappedStatement.Builder statementBuilder) {
        //因为目前值处理resultType，不处理resultMap，所以这个地方返回的是null
        resultMap = applyCurrentNamespace(resultMap,true);

        List<ResultMap> resultMaps = new ArrayList<>();

        //对返回结果集进行判断
        if(resultMap != null){
            // TODO: 这个地方暂时不进行处理，因为没有涉及到结果集的处理
            String[] resultMapNames = resultMap.split(",");
            for(String resultMapName : resultMapNames){
                resultMaps.add(configuration.getResultMap(resultMapName.trim()));
            }
        }

        //一般使用resultType作为返回内容处理就行
        else if(resultType != null){
             ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                    configuration,
                    statementBuilder.id() + "-Inline",
                    resultType,
                    new ArrayList<>()
            );
            resultMaps.add(inlineResultMapBuilder.build());
        }
        statementBuilder.resultMaps(resultMaps);
    }


    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings){
        ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration,id,type,resultMappings);

        ResultMap resultMap = inlineResultMapBuilder.build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }
}
