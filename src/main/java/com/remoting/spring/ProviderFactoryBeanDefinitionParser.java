package com.remoting.spring;

import com.remoting.provider.ProviderFactoryBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

public class ProviderFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderFactoryBeanDefinitionParser.class);

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ProviderFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        try {
            String serviceItf = element.getAttribute("interface");
            String timeout = element.getAttribute("timeout");
            String serverPort = element.getAttribute("serverPort");
            String ref = element.getAttribute("ref");
            String weight = element.getAttribute("weight");
            String workerThreads = element.getAttribute("workerThreads");
            String appKey = element.getAttribute("appKey");
            String groupName = element.getAttribute("groutName");

            builder.addPropertyValue("serverPort", Integer.parseInt(serverPort));
            builder.addPropertyValue("timeout", Integer.parseInt(timeout));
            builder.addPropertyValue("serviceItf", Class.forName(serviceItf));
            builder.addPropertyReference("serviceObject", ref);
            builder.addPropertyValue("appKey", appKey);

            if (NumberUtils.isNumber(weight)) {
                builder.addPropertyValue("weight", Integer.parseInt(weight));
            }
            if (NumberUtils.isNumber(workerThreads)) {
                builder.addPropertyValue("workerThreads", Integer.parseInt(workerThreads));
            }
            if (StringUtils.isNotBlank(groupName)) {
                builder.addPropertyValue("groupName", groupName);
            }
        }catch (Exception e){
            LOGGER.error("ProviderFactoryBeanDefinitionParser error.", e);
            throw new RuntimeException(e);
        }
    }
}
