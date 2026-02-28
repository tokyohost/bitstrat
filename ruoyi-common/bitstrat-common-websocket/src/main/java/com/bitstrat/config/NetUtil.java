package com.bitstrat.config;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/20 0:21
 * @Content
 */

import java.net.*;
import java.util.Enumeration;
import java.util.Objects;

public class NetUtil {

    /**
     * 获取本机外网 IP，如果配置了指定 IP 则优先返回配置 IP
     */
    public static InetAddress getLocalAddress(String configuredIp) {
        try {
            // 优先使用配置 IP
            if (Objects.nonNull(configuredIp) && !configuredIp.isBlank()) {
                return InetAddress.getByName(configuredIp);
            }

            // 遍历所有网卡，选择第一个有效 IPv4 地址
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                // 跳过不活动和虚拟网卡
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }

            // 如果找不到外网 IP，则 fallback 到本地主机
            return InetAddress.getLocalHost();

        } catch (Exception e) {
            throw new RuntimeException("Failed to determine local host IP", e);
        }
    }

    // 测试
    public static void main(String[] args) {
        InetAddress address = getLocalAddress(null); // 不配置 IP
        System.out.println("Selected IP: " + address.getHostAddress());

        InetAddress configured = getLocalAddress("192.168.1.100"); // 配置 IP
        System.out.println("Configured IP: " + configured.getHostAddress());
    }
}

