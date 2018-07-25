package com.remoting.helper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;


public class IPHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPHelper.class);

    private static String hostIp = StringUtils.EMPTY;

    /**
     * 获取本地的Ip
     */
    public static String localIp(){
        return hostIp;
    }

    public static String getRealIp(){
        String localip = null;  //本地IP,如果没有配置外网IP则返回它
        String netip = null; //外网IP
        try {
            Enumeration<NetworkInterface> networkInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            boolean findFlag = false;  //是否找到外网IP
            while (networkInterfaces.hasMoreElements() && !findFlag){
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> address = networkInterface.getInetAddresses();
                while (address.hasMoreElements()){
                    ip = address.nextElement();
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")){    //外网IP
                        netip = ip.getHostAddress();
                        findFlag = true;
                        break;
                    }else if (ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")){  //内网IP
                        localip = ip.getHostAddress();
                    }
                }
            }
            if (netip != null && !"".equals(netip)){
                return netip;
            }else {
                return localip;
            }
        }catch (Exception e){
            LOGGER.warn("获取本地IP失败:异常信息: "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    static {
        String ip = null;
        Enumeration<NetworkInterface> allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = allNetInterfaces.nextElement();
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress address : interfaceAddresses){
                    InetAddress Ip = address.getAddress();
                    if (Ip != null && Ip instanceof Inet4Address){
                        if (StringUtils.equals(Ip.getHostAddress(),"127.0.0.1"))
                            continue;
                        ip = Ip.getHostAddress();
                        break;
                    }
                }
            }
        }catch (Exception e){
            LOGGER.warn("获取本地IP失败:异常信息: "+e.getMessage());
            throw new RuntimeException(e);
        }
        hostIp = ip;
    }

    /**
     * 获取主机第一个有效ip
     * 如果没有，返回空串
     * @return
     */
    public static String getHostFirstIp(){
        return hostIp;
    }

    public static void main(String[] args) {
        System.out.println(getRealIp());
        System.out.println(getHostFirstIp());
    }
}
