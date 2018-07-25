package com.remoting.test;

/**
 * @author liyebing created on 16/10/5.
 * @version $Id$
 */
public class HelloServiceImpl implements HelloService {


    @Override
    public String sayHello(String somebody) {
        return "hello " + somebody + "!";
    }

    @Override
    public User findUser(User user) {
        user.setUsername(user.getUsername()+"123");
        user.setAge(user.getAge()+1);
        user.setSex(user.getSex()+"123");
        return user;
    }


}
