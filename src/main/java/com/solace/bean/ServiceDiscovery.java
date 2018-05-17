package com.solace.bean;

import com.solace.common.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author: Solace
 * @Description: 服务发现
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
@Component
public class ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    /**
     * 服务提供列表
     */
    private volatile List<String> dataList = new ArrayList<>();

    @Value("${netty.rpc.registryAddress}")
    private String registryAddress;

    @Autowired(required = false)
    private ZookeeperClientUtil zookeeperClientUtil;

    public ServiceDiscovery(){
        ZooKeeper zk = zookeeperClientUtil.getAliveZk();
        if (zk!=null){
            watchNode(zk);
        }
    }

    /**
     * 添加实时更新服务提供列表监听
     * @param zooKeeper
     */
    private void watchNode(ZooKeeper zooKeeper){
        try {

            List<String> nodeList = zooKeeper.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType()==Event.EventType.NodeChildrenChanged){
                        watchNode(zooKeeper);
                    }
                }
            });
            List<String> dataList = new ArrayList<>();
            for (String node:nodeList){
                byte[] bytes = zooKeeper.getData(Constant.ZK_REGISTRY_PATH+"/"+node,false,null);
                dataList.add(new String(bytes));
            }
            logger.info("node data:{}",dataList);
            this.dataList = dataList;
            updateConnectedServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private void updateConnectedServer(){
        RpcServerConnectManager.getInstance().updateConnectedServer(this.dataList);
    }

    public void stop(){
        ZooKeeper zk = zookeeperClientUtil.getAliveZk();
        if (zk!=null){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
