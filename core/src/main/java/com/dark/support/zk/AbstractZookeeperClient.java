package com.dark.support.zk;

import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;



/**
 * @author <a href="mailto:zuiwoxing@gmail.com">刘德建</a>
 * @version Ver 1.0
 * @description:引用dubbo 对zookeeper的部分封装，并且增加了新的API
 * @Date 2013-5-20 下午5:56:53
 */
public abstract class AbstractZookeeperClient<TargetChildListener> implements ZookeeperClient {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractZookeeperClient.class);

    private String connections;

    private byte[] auths;

    private final Set<StateListener> stateListeners = new HashSet<StateListener>();

    private final ConcurrentMap<String, ConcurrentMap<ChildListener, TargetChildListener>> childListeners = new ConcurrentHashMap<String, ConcurrentMap<ChildListener, TargetChildListener>>();

    private volatile boolean closed = false;

    public AbstractZookeeperClient(String connections, byte[] auths) {
        this.connections = connections;
        this.auths = auths;
    }

    public String getConnections() {
        return connections;
    }


    public void setConnections(String connections) {
        this.connections = connections;
    }

    public byte[] getAuths() {
        return auths;
    }


    public void setAuths(byte[] auths) {
        this.auths = auths;
    }


    /**
     * @param path      路径
     * @param isPersist 是否持久
     * @param isSequence 是否有序
     * @param acls
     */
    @Override
    public String create(String path, boolean isPersist, boolean isSequence, byte[] data, ArrayList<ACL> acls) {

        if (isPersist && isSequence) {
            return createPersistentSequence(path, data, acls);
        } else if (isPersist && !isSequence) {
            return createPersistentNoSequence(path, data, acls);
        } else if (!isPersist && isSequence) {
            return createEphemeralSequence(path, data, acls);
        } else if (!isPersist && !isSequence) {
            return createEphemeralNoSequence(path, data, acls);
        }
        return null;
    }

    @Override
    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }
    @Override
    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    public Set<StateListener> getSessionListeners() {
        return stateListeners;
    }
    @Override
    public List<String> addChildListener(String path, final ChildListener listener) {
        ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
        if (listeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildListener, TargetChildListener>());
            listeners = childListeners.get(path);
        }
        TargetChildListener targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return addTargetChildListener(path, targetListener);
    }
    @Override
    public void removeChildListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
        if (listeners != null) {
            TargetChildListener targetListener = listeners.remove(listener);
            if (targetListener != null) {
                removeTargetChildListener(path, targetListener);
            }
        }
    }

    protected void stateChanged(int state) {
        for (StateListener sessionListener : getSessionListeners()) {
            sessionListener.stateChanged(state);
        }
    }
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            doClose();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }

    protected abstract void doClose();

    protected abstract String createPersistentSequence(String path, byte[] data, ArrayList<ACL> acls);

    protected abstract String createPersistentNoSequence(String path, byte[] data, ArrayList<ACL> acls);

    protected abstract String createEphemeralSequence(String path, byte[] data, ArrayList<ACL> acls);

    protected abstract String createEphemeralNoSequence(String path, byte[] data, ArrayList<ACL> acls);

    protected abstract TargetChildListener createTargetChildListener(String path, ChildListener listener);

    protected abstract List<String> addTargetChildListener(String path, TargetChildListener listener);

    protected abstract void removeTargetChildListener(String path, TargetChildListener listener);

}
