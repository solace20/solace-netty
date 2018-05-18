package com.solace.rpc.client;

import com.alibaba.druid.util.StringUtils;
import com.solace.common.common.Constant;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: zookeeper工具类 (这里用一句话描述这个类的作用)
 * @date 2018/5/17
 * @CopyRight lengbar.cn
 */
@Component
public class ZookeeperClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClientUtil.class);

    @Value("${netty.rpc.registryAddress}")
    private String registryAddress;
    @Value("${netty.rpc.sessionTimeOut}")
    private  int sessionTimeOut;
    private final String defaultPath = "/registry";

    private CountDownLatch latch = new CountDownLatch(1);

    private ZooKeeper zk;

    public ZookeeperClientUtil(){

    }

    public ZooKeeper getAliveZk(){
        ZooKeeper aliveZk = zk;
        if (aliveZk!=null&&aliveZk.getState().isAlive()){
            return aliveZk;
        }else {
            zkReconnect();
            return zk;
        }
    }

    private synchronized void zkConnect() throws IOException {
        if (zk==null&& !StringUtils.isEmpty(registryAddress)){
            zk = new ZooKeeper(registryAddress, sessionTimeOut, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState()==Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
        }
    }

    public synchronized void zkReconnect(){
        zkClose();
        try {
            zkConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized void zkClose(){
        if (zk!=null){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                zk = null;
            }
        }
    }

    public String getData(String path){
        String result = null;
        try {
            byte[] data = getAliveZk().getData(path,Boolean.TRUE,null);
            if (null!=data){
                result = new String(data,"UTF-8");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String> getChildren(String path){
        List<String> data = null;
        try {
            data = getAliveZk().getChildren(path,Boolean.TRUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void addRootNode(String path){
        try{
            ZooKeeper zk = getAliveZk();
            Stat s = zk.exists(path,false);
            if (s==null){
                zk.create(path,new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public void createNode(String data){
        try {
            byte[] bytes =data.getBytes();
            String path = zk.create(Constant.ZK_DATA_PATH,bytes,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.info("create zookeeper node({} => {})",path,data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }


}
