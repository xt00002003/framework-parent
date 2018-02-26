package com.dark.support.zk;

import com.dark.support.zk.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;


/**
 * @author <a href="mailto:zuiwoxing@gmail.com">刘德建</a>
 * @version Ver 1.0
 * @description: TODO add description
 * @Date 2013-5-20 下午6:10:10
 */
public interface ZookeeperTransporter {


    public CuratorZookeeperClient connect(String connections);

    public CuratorZookeeperClient connect(String connections, byte[] auths);

    public CuratorZookeeperClient connect(String connections, byte[] auths, RetryPolicy retryPolicy, int connectTimeout);
}
