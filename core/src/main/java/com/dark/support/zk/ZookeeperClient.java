package com.dark.support.zk;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:zuiwoxing@gmail.com">刘德建</a>
 * @version Ver 1.0
 * @description: TODO add description
 * @Date 2013-5-20 下午5:55:28
 */
public interface ZookeeperClient {

    /**
     * @param path      路径
     * @param ephemeral 是否持久
     * @param isSequeue 是否有序
     * @param acls      权限
     */
    String create(String path, boolean ephemeral, boolean isSequeue, byte[] data, ArrayList<ACL> acls);

    void delete(String path);

    byte[] getData(String path);

    /**
     * @param path
     * @return
     * @description 检查节点是否存在
     */
    boolean isExist(String path);

    void setData(String path, byte[] data);

    List<String> getChildren(String path);

    List<String> addChildListener(String path, ChildListener listener);

    void removeChildListener(String path, ChildListener listener);

    void addStateListener(StateListener listener);

    void removeStateListener(StateListener listener);

    boolean isConnected();

    void close();

    ZooKeeper getZookeeper();


}
