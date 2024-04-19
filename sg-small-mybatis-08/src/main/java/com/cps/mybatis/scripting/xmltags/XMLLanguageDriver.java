package com.cps.mybatis.scripting.xmltags;

import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * @author cps
 * @description: xml语言驱动器
 * @date 2024/1/25 13:50
 * @OtherDescription: Other things
 */
public class XMLLanguageDriver implements LanguageDriver {

    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        //用XML脚本构建器解析
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }
}
