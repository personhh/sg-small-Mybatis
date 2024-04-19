package com.cps.mybatis.mapping;

import com.cps.mybatis.session.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author cps
 * @description: 结果映射
 * @date 2024/1/26 20:36
 * @OtherDescription: Other things
 */
public class ResultMap {

    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;
    private Set<String> mappedColumns;

    private ResultMap(){

    }

    public static class Builder{
        private ResultMap resultMap = new ResultMap();

        public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings){
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
        }

        public ResultMap build(){
            resultMap.mappedColumns = new HashSet<>();
            return resultMap;
        }
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }

    public Set<String> getMappedColumns() {
        return mappedColumns;
    }
}
