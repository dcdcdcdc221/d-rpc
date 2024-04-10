package com.deng.drpc.proxy;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂（用户创建对象）
 */
public class ServiceProxyFactory {
    /**
     * 动态代理
     *
     * @param serviceClass
     * @return
     */
    public static Object getProxy(Class<?> serviceClass){
        /**
         * Proxy.newProxyInstance需要三个参数
         * 类加载器
         * 参数
         * 类
         */
        return Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());
    }
}
