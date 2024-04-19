package com.deng.drpc.registry.cache;

import com.deng.drpc.model.ServiceMetaInfo;

import java.util.List;

public class RegistryServiceCache {
    /**
     * 服务缓存
     */
    private List<ServiceMetaInfo> serviceCache;

    /**
     * 写缓存
     * @param serviceCache
     */
    public void writeCache(List<ServiceMetaInfo> serviceCache){
        this.serviceCache = serviceCache;
    }

    /**
     * 读缓存
     * @return
     */
    public List<ServiceMetaInfo> readCache(){
        return serviceCache;
    }

    /**
     * 清空缓存
     */
    public void clearCache(){
        serviceCache.clear();
    }
}
