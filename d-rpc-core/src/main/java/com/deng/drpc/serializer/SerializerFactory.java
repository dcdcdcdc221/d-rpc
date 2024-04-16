package com.deng.drpc.serializer;

import com.deng.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;


/**
 * 序列化器工厂（用于获取序列化器对象）
 *
 * @author cong
 * @date 2024/03/08
 */
public class SerializerFactory {
    static {
        //SpiLoader.load(Serializer.class);
        SpiLoader.loadAll();
        System.out.println("静态代码块被执行");
    }

    /**
     * 获取实例
     *
     * @param key 钥匙
     * @return {@link Serializer}
     */
    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }
}