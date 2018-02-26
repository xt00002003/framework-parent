package com.dark.support.config;


import com.dark.support.zk.curator.CuratorZookeeperClient;
import com.dark.support.zk.curator.CuratorZookeeperClientImpl;
import com.dark.util.DarkConfigUtils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * 描述: 远程配置
 *
 * @author : <a href="mailto:dejian.liu@9icaishi.net">dejian.liu</a>
 * @version : Ver 1.0
 * @date : 2017/4/11
 */
public class RemoteConfigUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(RemoteConfigUtil.class);

    /**
     * 根路径
     */
    private static String NAMESPACE = "unify";

    /**
     * 加密方式
     */
    private static String SCHEME = "digest";

    /**
     * 连接超时(ms)
     */
    public static int connectionTimeoutMs = 5000;

    /**
     * 重试时间间隔(ms)
     */
    public static int sleepMsBetweenRetries = 2000;

    /**
     * zk地址
     */
    public final static String ZK_CONNECT_CONSTANT = "zookeeper.addr";

    /**
     * 统一应用名称
     */
    public final static String UNIFY_APP_NAME = "unify.app.name";

    /**
     * 环境
     */
    public final static String UNIFY_APP_ENV = "unify.app.env";


    /**
     * 初始化远程配置监听
     */
    public static void init() {

        String zkAddress = DarkConfigUtils.get(ZK_CONNECT_CONSTANT);
        if (StringUtils.isEmpty(zkAddress)) {
            throw new RuntimeException("zookeeper.connection can't be null!");
        }
        String appName = DarkConfigUtils.get(UNIFY_APP_NAME);
        if (StringUtils.isEmpty(appName)) {
            throw new RuntimeException("unify.app.name can't be null!");
        }

        String appEnv = DarkConfigUtils.get(UNIFY_APP_ENV);
        if (StringUtils.isEmpty(appEnv)) {
            throw new RuntimeException("unify.app.env can't be null!");
        }


        StringBuffer configBuf = new StringBuffer().append("/").append("config").append("/").append(appName).append("/").append(appEnv);
        String APP_CONFIG_PATH = configBuf.toString();
        CuratorZookeeperClient client = createAclCurator(zkAddress, null, null);
        if (!client.isExist(APP_CONFIG_PATH)) {
            client.create(APP_CONFIG_PATH, true, false, null, ZooDefs.Ids.OPEN_ACL_UNSAFE);
        }
        initListener(client.getCuratorFramework(), APP_CONFIG_PATH);
    }


    /**
     * 初始化监听
     *
     * @param curatorFramework
     * @param appPath          监听节点
     */
    private static void initListener(CuratorFramework curatorFramework, String appPath) {
        try {

            PathChildrenCache childrenCache = new PathChildrenCache(curatorFramework, appPath, true);
            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    ChildData data = event.getData();
                    Properties prop = null;
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            prop = convertByteToProp(data.getData());
                            DarkConfigUtils.getProperties().putAll(prop);
                            LOGGER.info("CHILD_ADDED:" + data.getPath());
                            break;
                        case CHILD_REMOVED:
                            prop = convertByteToProp(data.getData());
                            removeProp(DarkConfigUtils.getProperties(), prop);
                            LOGGER.info("CHILD_REMOVED:" + data.getPath());
                            break;
                        case CHILD_UPDATED:
                            prop = convertByteToProp(data.getData());
                            DarkConfigUtils.getProperties().putAll(prop);
                            LOGGER.info("CHILD_UPDATED:" + data.getPath());
                            break;
                        case CONNECTION_SUSPENDED:
                            LOGGER.info("zk CONNECTION_SUSPENDED!");
                            break;
                        case CONNECTION_RECONNECTED:
                            LOGGER.info("zk CONNECTION_RECONNECTED!");
                            break;
                        case INITIALIZED:
                            LOGGER.info("zk INITIALIZED!");
                            break;
                        default:
                            break;
                    }
                }
            };
            childrenCache.getListenable().addListener(childrenCacheListener);
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * @param zkAddress zookeeper 地址，逗号隔开
     * @param username  acl授权使用
     * @param password  acl授权使用
     * @return
     */
    public static CuratorZookeeperClient createAclCurator(String zkAddress, final String username, final String password) {
        StringBuffer buf = new StringBuffer();
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            buf.append(username).append(":").append(password);
        }
        final String aclStr = buf.toString();

        ACLProvider aclProvider = new ACLProvider() {
            private List<ACL> acl;

            @Override
            public List<ACL> getDefaultAcl() {
                if (acl == null) {
                    if (!StringUtils.isEmpty(aclStr)) {
                        ArrayList<ACL> aclNew = ZooDefs.Ids.CREATOR_ALL_ACL;
                        aclNew.clear();
                        aclNew.add(new ACL(ZooDefs.Perms.ALL, new Id("auth", aclStr)));
                        this.acl = aclNew;
                    } else {
                        this.acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
                    }
                }
                return acl;
            }

            @Override
            public List<ACL> getAclForPath(String path) {
                return acl;
            }
        };

        byte[] authData = aclStr.getBytes();

        CuratorZookeeperClient curatorClient = new CuratorZookeeperClientImpl(zkAddress, authData, new RetryNTimes(Integer.MAX_VALUE,
                sleepMsBetweenRetries),
                connectionTimeoutMs, NAMESPACE);
        return curatorClient;
    }


    /**
     * @param allProp 全局配置文件
     * @param delProp 待删除的配置文件
     */
    private static Properties removeProp(Properties allProp, Properties delProp) {
        if (allProp == null) {
            return new Properties();
        }
        if (delProp != null && !delProp.isEmpty()) {
            Set<Object> sets = delProp.keySet();
            for (Object key : sets) {
                allProp.remove(key);
            }
        }
        return allProp;
    }

    /**
     * 加载prop
     *
     * @param datas
     * @return
     */
    private static Properties convertByteToProp(byte[] datas) {
        Properties prop = new Properties();
        try {
            if (datas == null) {
                return prop;
            }
            prop.load(new ByteArrayInputStream(datas));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return prop;
    }


}
