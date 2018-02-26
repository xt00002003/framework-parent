package com.dark.support.zk;

/**
 * @author <a href="mailto:zuiwoxing@gmail.com">刘德建</a>
 * @version Ver 1.0
 * @description: TODO add description
 * @Date 2013-6-3 下午2:33:09
 */
public interface StateListener {

    int DISCONNECTED = 0;

    int CONNECTED = 1;

    int RECONNECTED = 2;

    int SUSPENDED = 3;

    void stateChanged(int connected);

}
