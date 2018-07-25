package com.remoting.spring;

import com.remoting.revoker.RevokerFactoryBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

public class RevokerFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RevokerFactoryBeanDefinitionParser.class);

    @Override
    protected Class<?> getBeanClass(Element element) {
        return RevokerFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        try {
            String timeOut = element.getAttribute("timeout");
            String targetInterface = element.getAttribute("interface");
            String clusterStrategy = element.getAttribute("clusterStrategy");
            String remoteAppKey = element.getAttribute("remoteAppKey");
            String groupName = element.getAttribute("groupName");

            builder.addPropertyValue("timeout",Integer.parseInt(timeOut));
            builder.addPropertyValue("targetInterface",Class.forName(targetInterface));
            builder.addPropertyValue("remoteAppKey",remoteAppKey);

            if (StringUtils.isNotBlank(clusterStrategy)){
                builder.addPropertyValue("clusterStrategy",clusterStrategy);
            }

            if (StringUtils.isNotBlank(groupName)){
                builder.addPropertyValue("groupName",groupName);
            }
        }catch (Exception e){
            LOGGER.error("RevokerFactoryBeanDefinitionParser error" ,e);
            throw new RuntimeException(e);
        }
    }
}
