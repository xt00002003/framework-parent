package com.dark.support.config;



import com.dark.scan.ResourceComparator;
import com.dark.util.DarkConfigUtils;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author : <a href="mailto:dejian.liu@9icaishi.net">dejian.liu</a>
 * @version : Ver 1.0
 * @date :  2016/11/28 14:58
 */
public class DarkPropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    /**
     * 配置路径
     */
    private String propPattern;

    /**
     * 对扫描出来的路径进行排序(从jar包扫描的排在前面)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Properties mergeProperties() throws IOException {
        Field f = ReflectionUtils.findField(this.getClass(), "locations");
        try {
            f.setAccessible(true);
            Resource[] rs = (Resource[]) f.get(this);
            List<Resource> resList = CollectionUtils.arrayToList(rs);
            Collections.sort(resList, new ResourceComparator());
            Resource[] newRs = new Resource[resList.size()];
            for (int i = 0; i < resList.size(); i++) {
                newRs[i] = resList.get(i);
            }
            f.set(this, newRs);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }


        /**
         * dejianliu 2016-06-12 add 配置扫描增强
         */
        Properties props = super.mergeProperties();
        if (props == null) {
            props = new Properties();
        }

        if (!StringUtils.isEmpty(this.getPropPattern())) {
            List<String> propPatterns = new ArrayList<String>();
            propPatterns.add(getPropPattern());
            Properties scanP = DarkConfigUtils.scanProp(propPatterns);
            //将配置的扫描覆盖以前的数据
            DarkConfigUtils.getProperties().putAll(scanP);
        }
       //重新覆盖spring properties
        props.putAll(DarkConfigUtils.getProperties());
        //回填YinliConfigUtils
        DarkConfigUtils.getProperties().putAll(props);
        return props;
    }

    public String getPropPattern() {
        return propPattern;
    }

    public DarkPropertySourcesPlaceholderConfigurer setPropPattern(String propPattern) {
        this.propPattern = propPattern;
        return this;
    }


}
