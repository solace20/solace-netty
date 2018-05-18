package com.solace.rpc.server;


import com.solace.common.common.Constant;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: ${todo}(这里用一句话描述这个类的作用)
 * @date 2018/5/16
 * @CopyRight lengbar.cn
 */
@Component

public class ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    @Value("${netty.rpc.registryAddress}")
    private String registryAddress;

    @Autowired(required = false)
    private ZookeeperClientUtil zookeeperClientUtil;

    public ServiceRegistry(){

    }

    public void register(String data){
        if (data!=null){
            ZooKeeper zk = zookeeperClientUtil.getAliveZk();
            if (zk!=null){
                zookeeperClientUtil.addRootNode(Constant.ZK_REGISTRY_PATH);
                zookeeperClientUtil.createNode(data);
            }
        }
    }
}
