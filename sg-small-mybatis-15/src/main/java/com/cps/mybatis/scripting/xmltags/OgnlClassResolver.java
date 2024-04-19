package com.cps.mybatis.scripting.xmltags;

import com.cps.mybatis.io.Resources;
import ognl.ClassResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cps
 * @description: TODO
 * @date 2024/2/21 11:08
 * @OtherDescription: Other things
 */
public class OgnlClassResolver implements ClassResolver {
    private Map<String, Class<?>> classes = new HashMap<String, Class<?>>(101);

    @Override
    public Class classForName(String className, Map context) throws ClassNotFoundException {
        Class<?> result = null;
        if ((result = classes.get(className)) == null) {
            try {
                result = Resources.classForName(className);
            } catch (ClassNotFoundException e1) {
                if (className.indexOf('.') == -1) {
                    result = Resources.classForName("java.lang." + className);
                    classes.put("java.lang." + className, result);
                }
            }
            classes.put(className, result);
        }
        return result;
    }

}
