package com.dark.support.config;


import com.dark.util.DarkConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.IOException;
import java.util.Properties;

/**
 * 描述:
 *
 * @author : <a href="mailto:dejian.liu@9icaishi.net">dejian.liu</a>
 * @version : Ver 1.0
 * @date : 2017/4/11
 */
public class SpringContext extends XmlWebApplicationContext {
    private static Logger LOGGER = LoggerFactory.getLogger(SpringContext.class);

    public SpringContext() {
        try {
            LOGGER.debug("config init...");
            RemoteConfigUtil.init();
            LOGGER.debug("config init....");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory)
            throws BeansException, IOException {
        Properties prop = DarkConfigUtils.getProperties();
        PropertiesPropertySource propertySource = new PropertiesPropertySource("remoteProperties", prop);
        this.getEnvironment().getPropertySources().addFirst(propertySource);
        super.loadBeanDefinitions(beanFactory);
    }


}
