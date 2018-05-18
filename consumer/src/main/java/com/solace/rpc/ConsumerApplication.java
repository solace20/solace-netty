package com.solace.rpc;

import com.solace.rpc.client.ZookeeperClientUtil;
import com.solace.rpc.client.RpcClient;
import com.solace.rpc.client.ServiceDiscovery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: ${todo}(这里用一句话描述这个类的作用)
 * @date 2018/5/17
 * @CopyRight lengbar.cn
 */
@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class,args);
    }

//    @Bean
//    public RpcClient rpcClient(){
//        return new RpcClient();
//    }
//
//    @Bean
//    public ServiceDiscovery serviceDiscovery(){
//        return new ServiceDiscovery();
//    }
//
//    @Bean
//    public ZookeeperClientUtil zookeeperClientUtil(){
//        return new ZookeeperClientUtil();
//    }
}
