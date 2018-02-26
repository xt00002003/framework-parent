package com.dark.support.zk.curator;


import com.dark.support.zk.ZookeeperTransporter;
import org.apache.curator.RetryPolicy;


/**
 * @description:	 TODO add description
 * @version  Ver 1.0
 * @author   <a href="mailto:dejian.liu@9icaishi.net">刘德建</a>
 * @Date	 2013-5-20 下午6:09:38
 */
public class CuratorZookeeperTransporter implements ZookeeperTransporter {

	@Override
	public CuratorZookeeperClient connect(String connections) {
		return new CuratorZookeeperClientImpl(connections,null);
	}

	@Override
	public CuratorZookeeperClient connect(String connections, byte[] auths) {
		return new CuratorZookeeperClientImpl(connections,auths);
	}

	@Override
	public CuratorZookeeperClient connect(String connections, byte[] auths,
			RetryPolicy retryPolicy, int connectTimeout) {
		return new CuratorZookeeperClientImpl(connections,auths,retryPolicy,connectTimeout,null);
	}

	
 

}
