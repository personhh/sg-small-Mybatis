package com.cps.mybatis.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cps
 * @description: 拦截器链
 * @date 2024/2/21 14:26
 * @OtherDescription: Other things
 */
public class InterceptorChain {
    private final List<Interceptor> interceptors = new ArrayList<>();

    public Object pluginAll(Object target) {
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<Interceptor> getInterceptors(){
        return Collections.unmodifiableList(interceptors);
    }


}
