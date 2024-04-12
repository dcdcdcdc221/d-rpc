package com.deng.drpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MockServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }

    /**
     * 生成指定类型的默认值对象
     * @param type
     * @return
     */
    private Object getDefaultObject(Class<?> type){
        if(type.isPrimitive()){
            if(type == boolean.class){
                return false;
            }else if(type ==short.class){
                return (short)0;
            }else if(type == int.class){
                return 0;
            } else if (type == long.class) {
                return (long) 0;
            }else if(type == double.class){
                return (double) 0;
            }
        }
        return null;
    }
}
