package com.wj5633.rpc.registry;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 2:48
 * @description
 */

@Component
@Slf4j
public class ServiceRegistry {

    @Value("${rpc.registry-address}")
    private String zkAddress;

    private ZkClient zkClient;

    @PostConstruct
    public void init() {
        zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        log.debug("connect to zookeeper.");
    }

    public void register(String serviceName, String serviceAddress) {
        String registryPath = Constant.ZK_REGISTRY_PATH;

        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
            log.debug("create registry node: {}", registryPath);
        }

        String servicePath = registryPath + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            log.debug("create service node: {}", servicePath);
        }
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
        log.debug("create address node: {}", addressNode);
    }
}
