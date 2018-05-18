package com.solace.rpc;


import com.solace.rpc.server.RpcServer;
import com.solace.rpc.server.ServiceRegistry;
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
public class ProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class,args);
    }

//    @Bean
//    public RpcServer rpcServer(){
//        return new RpcServer();
//    }
//
//    @Bean
//    public ServiceRegistry serviceRegistry(){
//        return new ServiceRegistry();
//    }
//
//    @Bean
//    public ZookeeperClientUtil zookeeperClientUtil(){
//        return new ZookeeperClientUtil();
//    }
}
