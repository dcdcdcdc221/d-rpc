package com.deng.drpc.registry;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.deng.drpc.config.RegistryConfig;
import com.deng.drpc.model.ServiceMetaInfo;
import com.deng.drpc.registry.cache.RegistryServiceCache;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * etcd registry
 * 参考官方示例 @see <a href = "https://github.com/etcd-io/jetcd">github url</a>
 *  @see <a href = "https://etcd.io/docs/v3.6/learning/api/">相关API</a>
 */
@Slf4j
public class EtcdRegistry implements Registry {
    private Client client;
    private KV kvClient;

    private Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 本地缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * 监听集合(使用ConcurrentHashSet防止重复)
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 初始化
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder().endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        log.info("执行检测");
//        ByteSequence key = ByteSequence.from(registryConfig.getRegistry(), StandardCharsets.UTF_8);
//        ByteSequence value = ByteSequence.from(registryConfig.getAddress(), StandardCharsets.UTF_8);

        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        //创建Lease 和 KV 客户端
        Lease leaseClient = client.getLeaseClient();

        //创建一个30S的租约
        long leaseId = leaseClient.grant(30).get().getID();

        //设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        log.info("key: " + key + " value: " +value);
        //将键值对与租约联系起来
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key,value,putOption).get();
        boolean add = localRegisterNodeKeySet.add(registerKey);
        if(add){
            log.info("Register successfully added");
        }else {
            log.info("register failed");
        }

    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence
                .from(registerKey,StandardCharsets.UTF_8));
        boolean remove = localRegisterNodeKeySet.remove(registerKey);
        if(remove){
            log.info("remove succeed");
        }
    }

    /**
     * 服务发现，根据前缀搜索
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey){
        //优先从缓存中获取服务
        List<ServiceMetaInfo> serviceMetaInfos = registryServiceCache.readCache();
        if (serviceMetaInfos != null) {
            return serviceMetaInfos;
        }
        //前缀搜索，结尾一定要加/
        String searchPrefix = ETCD_ROOT_PATH+ serviceKey + "/";
        GetOption getOption = GetOption.builder().isPrefix(true).build();
        List<KeyValue> keyValues = null;
        try {
            keyValues = kvClient.get(
                        ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                        getOption)
                    .get()
                    .getKvs();
            //解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                watch(key);
                return JSONUtil.toBean(value, ServiceMetaInfo.class);
            }).collect(Collectors.toList());
            registryServiceCache.writeCache(serviceMetaInfoList);

            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        //下线节点
        //遍历本节点所有的key
        for(String key: localRegisterNodeKeySet){
            try{
                kvClient.delete(ByteSequence.from(key,StandardCharsets.UTF_8));
            }catch (Exception e){
                throw new RuntimeException(key + "节点下线异常");
            }
        }
        if(kvClient != null) {
            kvClient.close();
            log.info("kvClient Closing");
        }else{
            System.out.println("kvClient is null");
        }
        if(client != null) {
            client.close();
            log.info("client Closing");
        }else{
            System.out.println("client is null");
        }

    }

    /**
     * 心跳检测
     */
    @Override
    public void heartBeat() {
        //10S 续签一次
        CronUtil.schedule("*/10 * * * * *",new Task(){
            @Override
            public void execute() {
                //遍历所有节点的key
                for(String key:localRegisterNodeKeySet){
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        //节点未过期（需要重启节点才能重新注册）
                        if(CollUtil.isEmpty(keyValues)){
                            continue;
                        }
                        //节点未过期，重新注册（相当于续签）
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                        log.info("续签成功");
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败" +e.getMessage());
                    }
                }
            }
        });
        //支持秒级别的任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        //之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if(newWatch){
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response ->{
               for (WatchEvent event : response.getEvents()){
                   switch (event.getEventType()){
                       //key删除时出发
                       case DELETE:
                           //清理注册缓存
                           registryServiceCache.clearCache();
                           break;
                       //其他情况不做任何处理
                       case PUT:
                       default:
                           break;
                   }
               }
            });
        }
    }
}
