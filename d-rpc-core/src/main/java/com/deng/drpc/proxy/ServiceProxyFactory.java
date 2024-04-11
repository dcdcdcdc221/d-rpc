package com.deng.drpc.proxy;



import java.lang.reflect.Proxy;

/**
 * 服务代理工厂（用户创建对象）
 */
public class ServiceProxyFactory implements ProxyFactory {
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
    @Override
    public  <T> T getProxy(Class<T> serviceClass){
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());
    }
}
