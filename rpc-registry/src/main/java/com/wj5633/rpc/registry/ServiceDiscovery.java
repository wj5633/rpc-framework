package com.wj5633.rpc.registry;

import io.netty.util.internal.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 3:16
 * @description
 */

@Component
@Slf4j
public class ServiceDiscovery {

    @Value("${rpc.registry-address}")
    private String zkAddress;

    public String discover(String serviceName) {
        ZkClient zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        log.debug("connect to zookeeper.");

        try {
            String serviePath = Constant.ZK_REGISTRY_PATH + "/" + serviceName;
            if (!zkClient.exists(serviePath)) {
                throw new RuntimeException(String.format("can not find any service node on path: %s", serviePath));
            }
            List<String> addressList = zkClient.getChildren(serviePath);
            if (CollectionUtils.isEmpty(addressList)) {
                throw new RuntimeException(String.format("can not find any service node on path: %s", serviePath));
            }
            String address;
            int size = addressList.size();
            if (size == 1) {
                address = addressList.get(0);
            } else {
                address = addressList.get(ThreadLocalRandom.current().nextInt(size));
            }
            log.debug("get only address node: {}", address);

            String addressPath = serviePath + "/" + address;
            return zkClient.readData(addressPath);
        } finally {
            zkClient.close();
        }
    }
}
