package com.dark.support.zk.curator;





import com.dark.support.zk.AbstractZookeeperClient;
import com.dark.support.zk.ChildListener;
import com.dark.support.zk.StateListener;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @description: TODO add description
 * @version Ver 1.0
 * @author <a href="mailto:dejian.liu@9icaishi.net">刘德建</a>
 * @Date 2013-5-20 下午5:58:13
 */
public class CuratorZookeeperClientImpl extends
		AbstractZookeeperClient<CuratorWatcher> implements
		CuratorZookeeperClient {

	private final CuratorFramework client;


	/**
	 *
	 * @param connections
	 * @param authoritys
	 * @param retryPolicy
	 * @param connectTimeout
	 * @param namespace
	 */
	public CuratorZookeeperClientImpl(final String connections, byte[] authoritys,RetryPolicy retryPolicy,int connectTimeout,String namespace) {
		super(connections, authoritys);
		try {
			Builder builder = CuratorFrameworkFactory.builder()
					.connectString(connections);
			  if (retryPolicy == null) {
				  builder.retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 2000));
			  } else {
				  builder.retryPolicy(retryPolicy); // 设置为永久重试，每隔2秒重试一次
			  }
			  if(connectTimeout <= 0){
				  builder.connectionTimeoutMs(30000);// 30秒连接超时
			  } else {
				  builder.connectionTimeoutMs(connectTimeout);// 30秒连接超时
			  }
			
			if (authoritys != null && authoritys.length > 0) {
				builder = builder.authorization("digest", authoritys);
			}
            if(!StringUtils.isEmpty(namespace)) {
				builder = builder.namespace(namespace);
			}

			client = builder.build();

			client.getConnectionStateListenable().addListener(
					new ConnectionStateListener() {
						@Override
						public void stateChanged(CuratorFramework client,
								ConnectionState state) {
							if (state == ConnectionState.LOST) {
								logger.info(connections+"========="+ ConnectionState.LOST);
								CuratorZookeeperClientImpl.this
										.stateChanged(StateListener.DISCONNECTED);
							} else if (state == ConnectionState.CONNECTED) {
								CuratorZookeeperClientImpl.this
										.stateChanged(StateListener.CONNECTED);
								logger.info(connections+"========="+ ConnectionState.CONNECTED);
							} else if (state == ConnectionState.RECONNECTED) {
								CuratorZookeeperClientImpl.this
										.stateChanged(StateListener.RECONNECTED);
								logger.info(connections+"========="+ ConnectionState.RECONNECTED);
							}else if (state == ConnectionState.SUSPENDED) {
								CuratorZookeeperClientImpl.this
										.stateChanged(StateListener.SUSPENDED);
								logger.info(connections+"========="+ ConnectionState.SUSPENDED);
								
							}
						}
					});
			client.start();
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	public CuratorZookeeperClientImpl(final String connections, byte[] authoritys) {
 	   this(connections,authoritys,new RetryNTimes(Integer.MAX_VALUE, 2000),30000,null);
	}

	/**
	 * @param path
	 *            路径
	 * @param acls ZooDefs
	 *            .Ids.OPEN_ACL_UNSAFE 权限
	 */
	public void createPersistent(String path, ArrayList<ACL> acls) {
		try {
			client.create().creatingParentsIfNeeded()// 如果指定的节点的父节点不存在，递归创建父节点
					.withMode(CreateMode.PERSISTENT)// 存储类型（临时的还是持久的）
					.withACL(acls)// 访问权限
					.forPath(path);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/**
	 * @param path
	 *            路径
	 * @param acls ZooDefs
	 *            .Ids.OPEN_ACL_UNSAFE 权限
	 */
	public void createEphemeral(String path, ArrayList<ACL> acls) {
		try {
			client.create().creatingParentsIfNeeded()
					.withMode(CreateMode.EPHEMERAL).withACL(acls).forPath(path);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	@Override
	public void delete(String path) {
		try {
			client.delete().forPath(path);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	@Override
	public List<String> getChildren(String path) {
		try {
			return client.getChildren().forPath(path);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	@Override
	public boolean isConnected() {
		return client.getZookeeperClient().isConnected();
	}
	@Override
	public void doClose() {
		client.close();
	}

	private class CuratorWatcherImpl implements CuratorWatcher {

		private volatile ChildListener listener;

		public CuratorWatcherImpl(ChildListener listener) {
			this.listener = listener;
		}

		public void unwatch() {
			this.listener = null;
		}
		@Override
		public void process(WatchedEvent event) throws Exception {
			if (listener != null) {
				listener.childChanged(event.getPath(), client.getChildren()
						.usingWatcher(this).forPath(event.getPath()));
			}
		}
	}
	@Override
	public CuratorWatcher createTargetChildListener(String path,
			ChildListener listener) {
		return new CuratorWatcherImpl(listener);
	}
	@Override
	public List<String> addTargetChildListener(String path,
			CuratorWatcher listener) {
		try {
			return client.getChildren().usingWatcher(listener).forPath(path);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	@Override
	public void removeTargetChildListener(String path, CuratorWatcher listener) {
		((CuratorWatcherImpl) listener).unwatch();
	}

	@Override
	protected String createPersistentSequence(String path, byte[] data,
			ArrayList<ACL> acls) {
		try {
			return client.create().creatingParentsIfNeeded()// 如果指定的节点的父节点不存在，递归创建父节点
					.withMode(CreateMode.PERSISTENT_SEQUENTIAL)// 存储类型（临时的还是持久的）
					.withACL(acls)// 访问权限
					.forPath(path, data);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	protected String createPersistentNoSequence(String path, byte[] data,
			ArrayList<ACL> acls) {
		try {
			return client.create().creatingParentsIfNeeded()// 如果指定的节点的父节点不存在，递归创建父节点
					.withMode(CreateMode.PERSISTENT)// 存储类型（临时的还是持久的）
					.withACL(acls)// 访问权限
					.forPath(path, data);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	protected String createEphemeralSequence(String path, byte[] data,
			ArrayList<ACL> acls) {
		try {
			return client.create().creatingParentsIfNeeded()// 如果指定的节点的父节点不存在，递归创建父节点
					.withMode(CreateMode.EPHEMERAL_SEQUENTIAL)// 存储类型（临时的还是持久的）
					.withACL(acls)// 访问权限
					.forPath(path, data);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	protected String createEphemeralNoSequence(String path, byte[] data,
			ArrayList<ACL> acls) {
		try {
			return client.create().creatingParentsIfNeeded()// 如果指定的节点的父节点不存在，递归创建父节点
					.withMode(CreateMode.EPHEMERAL)// 存储类型（临时的还是持久的）
					.withACL(acls)// 访问权限
					.forPath(path, data);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public byte[] getData(String path) {
		try {
			return client.getData().forPath(path);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void setData(String path, byte[] data) {
		try {
			client.setData().forPath(path, data);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public ZooKeeper getZookeeper() {
		try {
			return client.getZookeeperClient().getZooKeeper();
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public boolean isExist(String path) {
		try {
			Stat stat = client.checkExists().forPath(path);
			if (stat == null) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public CuratorFramework getCuratorFramework() {
		return client;
	}

}
