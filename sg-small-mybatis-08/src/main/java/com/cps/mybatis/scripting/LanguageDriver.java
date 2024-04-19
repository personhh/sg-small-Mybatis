package com.cps.mybatis.scripting;

import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * @description: 脚本语言驱动器
 * @author cps
 * @date 2024/1/25 12:50
 * @OtherDescription: Other things
 */
public interface LanguageDriver {

    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);
}
