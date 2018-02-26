package com.dark.support.zk.curator;

import com.dark.support.zk.ZookeeperClient;
import org.apache.curator.framework.CuratorFramework;


/**
 * 
 * @author dejianliu
 *
 */
public interface CuratorZookeeperClient extends ZookeeperClient {
	
	/**
	 * @description curator实现时使用
	 * @return
	 */
	CuratorFramework getCuratorFramework();

}
