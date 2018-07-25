package com.remoting.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author liyebing created on 16/10/5.
 * @version $Id$
 */
public class MainClient {

    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    public static void main(String[] args) throws Exception {

        //引入远程服务
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ares-client.xml");
        //获取远程服务
        final HelloService helloService = (HelloService) context.getBean("remoteHelloService");

        long count = 1000000000000000000L;

        User user = new User();
        user.setUsername("123");
        User user1 = helloService.findUser(user);
        String s = helloService.sayHello("netty   111");
        System.out.println(user1);
        System.out.println(s);
//        //调用服务并打印结果
//        for (int i = 0; i < 100; i++) {
//            try {
//                String result = helloService.sayHello("liyebing,i=" + i);
//                System.out.println(result);
//
//            } catch (Exception e) {
//                logger.warn("--------", e);
//            }
//        }

        //关闭jvm
        System.exit(0);
    }
}
