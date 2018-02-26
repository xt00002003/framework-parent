package com.dark.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

;

/**
 * spring 扫描工具类
 *
 * @author : <a href="mailto:dejianliu@ebnew.com">liudejian</a>
 * @version : Ver 1.0
 * @date : 2015-2-12 下午2:57:38
 */
public class SpringScanUtils {

    private static final Logger logger = LoggerFactory.getLogger(SpringScanUtils.class);

    /**
     *
     */
    private static PathMatchingResourcePatternResolver RESOUCE_HANDLER = new PathMatchingResourcePatternResolver();

    static {
        RESOUCE_HANDLER.setPathMatcher(new AntPathMatcher());
    }

    @SuppressWarnings("unchecked")
    public static List<Resource> scan(List<String> parttens) throws IOException {
        List<Resource> lists = new ArrayList<Resource>();
        for (String pattern : parttens) {
            Resource[] res = RESOUCE_HANDLER.getResources(pattern);
            lists.addAll(CollectionUtils.arrayToList(res));
        }
        return lists;
    }

    /**
     * 将resource 转为properties
     *
     * @param reses
     * @param comparator
     * @return
     * @author : <a href="mailto:dejianliu@ebnew.com">liudejian</a>  2015-2-12 下午3:10:25
     */
    public static Properties loadResourceToProperties(List<Resource> reses,
                                                      Comparator<Resource> comparator) {
        Properties p = new Properties();
        if (reses == null || reses.isEmpty()) {
            return p;
        }
        List<Resource> listFile = new ArrayList<>();
        List<Resource> listJarFile = new ArrayList<>();

        for (Resource resource : reses) {
            try {
                String propPath = resource.getURL().toString();
                if (StringUtils.startsWithIgnoreCase(propPath, "jar:file")) {
                    listJarFile.add(resource);
                }
                if (StringUtils.startsWithIgnoreCase(propPath, "file:")) {
                    listFile.add(resource);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        listJarFile.addAll(listFile);
      /*  if (comparator == null) {
            Collections.sort(reses, new ResourceComparator());
        } else {
            Collections.sort(reses, comparator);
        }*/

        reses = listJarFile; //重新赋值
        for (Resource resource : reses) {
            if (resource.isReadable() && resource.getFilename().endsWith("properties")) {
                try {
                    logger.debug(resource.getURL().toExternalForm());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                p.putAll(readInputToProperties(resource));
            }
        }
        return p;
    }

    /**
     * 将resource 转为 properties
     *
     * @param resource
     * @return
     * @author : <a href="mailto:dejianliu@ebnew.com">liudejian</a>  2015-2-12 下午3:10:43
     */
    public static Properties readInputToProperties(Resource resource) {
        Properties pp = new Properties();
        InputStream input = null;
        try {
            input = resource.getInputStream();
            pp.load(input);
        } catch (IOException e) {
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }
        return pp;
    }

}
