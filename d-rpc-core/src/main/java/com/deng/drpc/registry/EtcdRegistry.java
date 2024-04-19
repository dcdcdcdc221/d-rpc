package com.deng.drpc.registry;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.deng.drpc.config.RegistryConfig;
import com.deng.drpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * etcd registry
 * 参考官方示例 @see <a href = "https://github.com/etcd-io/jetcd">github url</a>
 *  @see <a href = "https://etcd.io/docs/v3.6/learning/api/">相关API</a>
 */
@Slf4j
public class EtcdRegistry implements Registry{
    private Client client;
    private KV kvClient;

    private Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints("http://localhost:2379").build();

        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from("test_key".getBytes());
        ByteSequence value = ByteSequence.from("test_value".getBytes());

        // put the key-value
        kvClient.put(key, value).get();

        // get the CompletableFuture
        CompletableFuture<GetResponse> getFuture = kvClient.get(key);

        System.out.println(kvClient.get(key).get());

        // get the value from CompletableFuture
        GetResponse response = getFuture.get();

        // delete the key
        kvClient.delete(key).get();
    }

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
            return keyValues.stream().map(keyValue ->{
                String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                return JSONUtil.toBean(value,ServiceMetaInfo.class);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        if(kvClient != null) {
            kvClient.close();
        }else{
            System.out.println("kvClient is null");
        }
        if(client != null) {
            client.close();
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
}
