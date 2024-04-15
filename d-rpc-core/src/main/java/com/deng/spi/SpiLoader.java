package com.deng.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.deng.drpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



/**
 * SPI加载器支持键值对映射
 */
@Slf4j
public class SpiLoader {
    /**
     * 存储已加载的类：接口名 => (key => 实现类)
     */
    private static Map<String, Map<String,Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存（避免重复new），类路径 => 对象实例,单例模式
     */
    private static Map<String,Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统SPI目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 用户自定义SPI目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[] {RPC_CUSTOM_SPI_DIR,RPC_SYSTEM_SPI_DIR};

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll() {
        log.info("加载所有SPI");
        for(Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }

    /**
     * 加载某个类型
     * @param loadClass
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为{}的SPI",loadClass.getName());

    }
}
