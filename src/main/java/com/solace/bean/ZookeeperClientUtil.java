package com.solace.bean;

import com.alibaba.druid.util.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: ${todo}(这里用一句话描述这个类的作用)
 * @date 2018/5/17
 * @CopyRight lengbar.cn
 */
@Component
public class ZookeeperClientUtil {
    @Value("${netty.rpc.registryAddress}")
    private String registryAddress;
    @Value("${netty.rpc.sessionTimeOut}")
    private  int sessionTimeOut;
    private final String defaultPath = "/registry";

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
            zk = new ZooKeeper(registryAddress,sessionTimeOut,null);
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


}
