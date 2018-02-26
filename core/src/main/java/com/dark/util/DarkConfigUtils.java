package com.dark.util;



import com.dark.scan.ResourceComparator;
import com.dark.scan.SpringScanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ConfigUtils
 *
 * @author <a href="mailto:dejian.liu@9icaishi.net">刘德建</a>
 * @version Ver 1.0
 * @description: configure 工具类
 * @Date 2013-4-3 下午2:34:41
 */
public class DarkConfigUtils {

    private final static Logger logger = LoggerFactory.getLogger(DarkConfigUtils.class);
    private static volatile Properties properties = new Properties();
    private static AtomicBoolean IS_LOADED = new AtomicBoolean(false);

    private DarkConfigUtils() {
    }

    private static List<String> defaultScanPatterns = new ArrayList<String>();

    public static List<String> getDefaultScanPatterns() {
        return defaultScanPatterns;
    }

    public static void setDefaultScanPatterns(List<String> defaultScanPatterns) {
        DarkConfigUtils.defaultScanPatterns = defaultScanPatterns;
    }

    /**
     * 加载配置文档
     *
     * @author : <a href="mailto:dejian.liu@9icaishi.net">liudejian</a> 2014-12-15
     * 下午2:57:41
     */
    public static synchronized void reload() {
        List<String> rootScan = new ArrayList<>();
        rootScan.add("classpath*:**/*.properties");// 扫描二级目录
        rootScan.add("classpath*:*.properties");// 扫描一级目录
        defaultScanPatterns.addAll(rootScan);
        // 注册默认classpath 文件路径
        properties = initClassPath();
    }


    /**
     * 获取key startWidth 的配置文件
     *
     * @param prefix
     * @return
     * @author : <a href="mailto:dejian.liu@9icaishi.net">heigong</a>  2016-7-9 下午9:27:30
     */
    public static Properties getStartKeyProp(String prefix) {
        Properties startwidthProp = new Properties();
        Set<Object> keys = getProperties().keySet();
        if (keys != null) {
            for (Object key : keys) {
                if (key != null && key.toString().startsWith(prefix)) {
                    startwidthProp.put(key, getProperties().get(key));
                }
            }
        }
        return startwidthProp;
    }

    /**
     * 扫描指定配置路径下的配置
     *
     * @param patterns example:   classpath*:*.properties
     * @return
     */
    public static Properties scanProp(List<String> patterns) {
        Properties properties = new Properties();
        try {
            List<Resource> resources = SpringScanUtils
                    .scan(patterns);
            properties.putAll(
                    SpringScanUtils.loadResourceToProperties(
                            resources, new ResourceComparator())
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return properties;
    }

    private static Properties initClassPath() {
        Properties allProp = new Properties();
        try {
            // 【1】获取系统配置文件信息
            Properties systemProperties = new Properties();
            systemProperties.putAll(System.getProperties());
            // 添加环境变量获取
            systemProperties.putAll(System.getenv());
            allProp.putAll(systemProperties);
            if (!defaultScanPatterns.isEmpty()) {
                allProp.putAll(scanProp(defaultScanPatterns));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return allProp;
    }

    public static Properties getProperties() {
        if (!IS_LOADED.get()) {
            synchronized (DarkConfigUtils.class) {
                if (!IS_LOADED.get()) {
                    reload();
                }
            }
            IS_LOADED.set(true);
        }
        return properties;
    }

    /**
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String get(String key, String defaultValue) {
        return getProperties().getProperty(key,defaultValue);
    }

    /**
     * 两边对齐
     *
     * @param key
     * @return
     * @author : <a href="mailto:dejian.liu@9icaishi.net">liudejian</a> 2014-12-11
     * 下午2:04:59
     */
    public static String getTrim(String key) {
        String v = get(key);
        if (v != null) {
            return v.trim();
        }
        return v;
    }

    public static String getTrim(String key, String defaultValue) {
        String v = get(key, defaultValue);
        if (v != null) {
            return v.trim();
        }
        return v;
    }


}
