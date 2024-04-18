package com.deng.drpc.model;

import cn.hutool.core.util.StrUtil;
import com.deng.drpc.constant.RpcConstant;
import lombok.Data;

/**
 * 服务元信息（注册信息）
 */
@Data
public class ServiceMetaInfo {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 服务地址
     */
    private String serviceAddress;

    /**
     * 服务分组（暂未实现）
     */
    private String serviceGroup = "default";

    /**
     * 获取服务完整地址
     * TODO 动态获取
     */
    private String serviceHost = "http://localhost";

    /**
     * 获取服务端口
     * TODO 动态获取
     */
    private int servicePort = 2380;

    /**
     * 获取服务键值名
     * @return
     */
    public String getServiceKey(){
        //后续拓展
        //return String.format("%s:%s:%s", serviceName,serviceVersion,serviceGroup);

        return String.format("%s:%s", serviceName,serviceVersion);
    }

    /**
     * 获取服务器
     * @return
     */
    public String getServiceNodeKey(){
        return String.format("%s%s",getServiceKey(), serviceAddress);
    }

    /**
     * 获取完整服务地址
     * @return
     */
    public String getServiceAddress(){
        if(!StrUtil.contains(serviceHost,"http")){
            return String.format("http://%s",serviceHost,servicePort);
        }
        return String.format("%s:%s",serviceHost,servicePort);
    }

}
