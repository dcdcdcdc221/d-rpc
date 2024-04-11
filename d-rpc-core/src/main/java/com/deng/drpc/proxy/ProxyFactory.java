package com.deng.drpc.proxy;

public interface ProxyFactory {

    <T> T getProxy(Class<T> serviceClass);
}