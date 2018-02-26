package com.dark.support.zk;

import java.util.List;

/**
 * @author <a href="mailto:zuiwoxing@gmail.com">刘德建</a>
 * @version Ver 1.0
 * @description: TODO add description
 * @Date 2013-6-3 下午2:33:14
 */
public interface ChildListener {

    void childChanged(String path, List<String> children);

}
