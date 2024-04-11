package com.deng.drpc;

import com.deng.drpc.serializer.JdkSerializer;
import com.deng.drpc.serializer.Serializer;

public class test1 {
    public void test1() throws Exception {
        Serializer serializer = new JdkSerializer();
        System.out.println(serializer.getClass().getName());
    }
}
