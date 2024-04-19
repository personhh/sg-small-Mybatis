package com.cps.mybatis.builder;

import com.cps.mybatis.mapping.ResultMap;
import com.cps.mybatis.mapping.ResultMapping;

import java.util.List;

/**
 * @author cps
 * @description: 结果映射解析器
 * @date 2024/2/19 10:26
 * @OtherDescription: Other things
 */
public class ResultMapResolver {

    private final MapperBuilderAssistant assistant;
    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;


    public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, List<ResultMapping> resultMappings) {
        this.assistant = assistant;
        this.id = id;
        this.type = type;
        this.resultMappings = resultMappings;
    }

    public ResultMap resolve(){
        return assistant.addResultMap(this.id,this.type,this.resultMappings);
    }
}
