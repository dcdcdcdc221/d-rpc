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
     * 系统 SPI 目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system";

    /**
     * 用户自定义 SPI 目录
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
     * 获取某个接口的实例
     *
     * @param tClass
     * @param key
     * @return
     * @param <T>
     */
    public static <T> T getInstance(Class<?> tClass,String key) {
        String name = tClass.getName();
        System.err.println("name:" + name);
        Map<String, Class<?>> keyClassMap = loaderMap.get(name);
        System.out.println(loaderMap);
        log.info("keyClassMap:",keyClassMap);
        System.err.println("keyClassMap:" + keyClassMap);
        log.info("key:",key);
        System.err.println(key);
        log.error(String.format("key:%s",key));
        System.out.println("loaderMap:" + loaderMap);;
        //打印日志
        log.info(String.format("SpiLoader: %s key = %s",tClass,key));
        if(keyClassMap == null) {
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型",name));
        }
        Class<?> implClass = keyClassMap.get(key);
        System.out.println(implClass);
        if(!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key = %s 的类型",tClass,key));
        }
        //获取要加载的实现类的类型
        //从实例缓存中加载指定的实现类
        String className = implClass.getName();
        if(!instanceCache.containsKey(className)){
                //把对象放入并实例化
            try {
                instanceCache.put(className,implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(String.format("%s类实例化失败",className),e);
            }
        }
        log.info(String.format("%s类实例化成功",className));
        return (T) instanceCache.get(className);
    }

    /**
     * 负荷
     * 加载某个类型
     *
     * @param loadClass 负载等级
     * @return {@link Map}<{@link String}, {@link Class}<{@link ?}>>
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为 {} 的 SPI", loadClass.getName());
        // 扫描路径，用户自定义的 SPI 优先级高于系统 SPI
        Map<String, Class<?>> keyClassMap = new HashMap<>();

        for (String scanDir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(scanDir + "/"+ loadClass.getName());
            // 读取每个资源文件
            System.out.println("scanDir:" + scanDir);
            System.out.println("loadClass:" + loadClass.getName());
            System.out.println("resources:" + resources);
            System.out.println(resources.size());
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        log.info("while语句被执行");
                        String[] strArray = line.split("=");
                        if (strArray.length > 1) {
                            String key = strArray[0];
                            String className = strArray[1];
                            System.out.println("key:" + key + "className:" + className);
                            Class<?> aClass = Class.forName(className);
                            log.info("forName执行成功");
                            System.out.println("forName执行成功");
                            keyClassMap.put(key, aClass);
                            System.out.println("执行赋值key"+key);
                            System.out.println("keyClassMap_2:" + keyClassMap);
                        }
                    }
                    System.out.println("while之后");
                } catch (Exception e) {
                    log.error("spi resource load error", e);
                }
            }
        }
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }
}
