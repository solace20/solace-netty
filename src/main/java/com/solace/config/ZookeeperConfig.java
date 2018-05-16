package com.solace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: ${todo}(这里用一句话描述这个类的作用)
 * @date 2018/5/16
 * @CopyRight lengbar.cn
 */
@Configuration
public class ZookeeperConfig {

    @Value("${netty.rpc.registryAddress}")
    private String registryAddress;


}
