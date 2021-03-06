package com.remoting.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 服务引入自定义标签
 */
public class RemoteReferenceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("reference",new RevokerFactoryBeanDefinitionParser());
    }
}
