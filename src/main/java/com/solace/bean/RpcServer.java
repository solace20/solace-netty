package com.solace.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: ${todo}(这里用一句话描述这个类的作用)
 * @date 2018/5/16
 * @CopyRight lengbar.cn
 */
@Component
public class RpcServer implements ApplicationContextAware,InitializingBean{
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    @Value("${netty.rpc.serverAddress}")
    private String serverAddress;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.error(serverAddress);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
