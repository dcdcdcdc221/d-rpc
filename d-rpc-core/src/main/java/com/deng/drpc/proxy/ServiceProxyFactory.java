package com.deng.drpc.proxy;



import com.deng.drpc.RpcApplication;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂（用户创建对象）
 */
public class ServiceProxyFactory{
    /**
     * 动态代理
     *
     * @param serviceClass
     * @return
     * Proxy.newProxyInstance需要三个参数
     * 类加载器
     * 参数
     * 类
     */

    public static  <T> T getProxy(Class<T> serviceClass){
        if(RpcApplication.getRpcConfig().isMock()){
            return getMockProxy(serviceClass);
        }
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());
    }
    public static <T> T getMockProxy(Class<T> serviceClass){
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy());
    }

}
