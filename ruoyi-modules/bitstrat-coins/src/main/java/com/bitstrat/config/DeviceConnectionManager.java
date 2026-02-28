package com.bitstrat.config;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ConnectionStatus;
import com.bitstrat.constant.MessageType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.DeviceInfo;
import com.bitstrat.domain.ExchangeData;
import com.bitstrat.domain.msg.AccountData;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.server.Message;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.store.RoleCenter;
import com.bitstrat.utils.NettyUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.bitstrat.constant.ServiceConstant.*;

@Slf4j
@Component
public class DeviceConnectionManager {

    @Autowired
        @Lazy
    CommonServce commonServce;
    private final Map<String, DeviceInfo> devices = new ConcurrentHashMap<>();
    private final Map<String, Channel> deviceChannels = new ConcurrentHashMap<>();
    private final Map<String,String> channelExchange = new ConcurrentHashMap<>();

    @Autowired
    RoleCenter roleCenter;
    public synchronized void handleDeviceConnection(String clientId, Message message, Channel channel) {
        log.info("Device connected: {}", clientId);

        // 创建或更新设备信息
        DeviceInfo deviceInfo = devices.computeIfAbsent(clientId, k -> new DeviceInfo());
        deviceInfo.setClientId(clientId);
        deviceInfo.setExchangeName(message.getExchangeName());
        deviceInfo.setStatus(ConnectionStatus.CONNECTED);
        deviceInfo.setIp(NettyUtils.getClientIp(channel));
        deviceInfo.setLastConnectTime(System.currentTimeMillis());



        // 保存Channel
        deviceChannels.put(clientId, channel);
        channelExchange.put(message.getExchangeName()+":"+NettyUtils.getClientIp(channel),clientId);
        channel.attr(CLIENT_ID_ATTR).set(clientId);
        channel.attr(EXCHANGE_ATTR).set(message.getExchangeName());

//        List<Account> userAccountByExchange = commonServce.getUserAccountByExchange(message.getExchangeName());
        List<Account> userAccountByExchange = commonServce.getUserAccountByNodeClient(clientId);
        this.initAccount(message.getExchangeName(), userAccountByExchange,channel);
    }
    public synchronized void refreshDevice(Channel channel) {
        String clientId = channel.attr(CLIENT_ID_ATTR).get();
        if(StringUtils.isEmpty(clientId)) {
            log.error("refresh device failed, clientId is null");
            return;
        }
        if(!devices.containsKey(clientId)) {
            //需要auth
            Message message = new Message();
            message.setType(MessageType.AUTH);
            channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
            return;
        }

        // 创建或更新设备信息
        DeviceInfo deviceInfo = devices.computeIfAbsent(clientId, k -> new DeviceInfo());
        deviceInfo.setClientId(clientId);
        deviceInfo.setStatus(ConnectionStatus.CONNECTED);
        deviceInfo.setIp(NettyUtils.getClientIp(channel));
        deviceInfo.setLastConnectTime(System.currentTimeMillis());


        // 保存Channel
        deviceChannels.put(clientId, channel);
    }

    private void initAccount(String exchangeName, List<Account> userAccountByExchange, Channel channel) {
        for (Account account : userAccountByExchange) {
            Message message = new Message();
            message.setExchangeName(exchangeName);
            message.setType(MessageType.INIT_EXCHANGE);
            message.setTimestamp(System.currentTimeMillis());
            AccountData accountData = new AccountData();
            accountData.setAccount(account);
            message.setData(accountData);
            channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
        }

    }


    public synchronized void handleDeviceDisconnection(String clientId, Channel channel) {
        log.info("Device disconnected: {}", clientId);

        // 更新设备状态
        DeviceInfo deviceInfo = devices.get(clientId);
        if (deviceInfo != null) {
            deviceInfo.setStatus(ConnectionStatus.DISCONNECTED);
            deviceInfo.setLastConnectTime(System.currentTimeMillis());
        }

        // 移除Channel
        deviceChannels.remove(clientId);
        Iterator<Map.Entry<String, String>> iterator = channelExchange.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry.getValue().equals(clientId)) {
                iterator.remove(); // 安全删除
            }
        }
    }

    public boolean isDeviceConnected(String clientId) {
        DeviceInfo deviceInfo = devices.get(clientId);
        return deviceInfo != null && deviceInfo.getStatus() == ConnectionStatus.CONNECTED;
    }

    public DeviceInfo getDeviceInfo(String clientId) {
        return devices.get(clientId);
    }

    public Channel getDeviceChannel(String clientId) {
        return deviceChannels.get(clientId);
    }


    public int getConnectedDeviceCount() {
        return (int) devices.values().stream()
            .filter(device -> device.getStatus() == ConnectionStatus.CONNECTED)
            .count();
    }

    public List<DeviceInfo> getAllDeviceInfo() {
        return new ArrayList<>(devices.values());
    }
    public List<ExchangeData> getAllExchangeInfo() {
        ArrayList<ExchangeData> reslut = new ArrayList<>();
        for (String key : channelExchange.keySet()) {
            String exchangeName = key.split(":")[0];
            String ip = key.split(":")[1];
            String clientId = channelExchange.get(key);
            DeviceInfo deviceInfo = devices.get(clientId);
            ExchangeData exchangeData = new ExchangeData();
            exchangeData.setNodeName(deviceInfo.getClientId());
            List<ActiveLossPoint> activeLossPoints = roleCenter.getNodeLossPointMap().getOrDefault(clientId, new ArrayList<>());
            exchangeData.setExchangeName(exchangeName);
            exchangeData.setClientId(clientId);
//            exchangeData.setIp(ip);
            exchangeData.setMaxRoleSize(deviceInfo.getMaxRoleSize());
            exchangeData.setCurrRoleSize((long) activeLossPoints.size());
            exchangeData.setStatus(deviceInfo.getStatus().getConnectionStatus());
            exchangeData.setDelay(deviceInfo.getDelay());
            reslut.add(exchangeData);
        }
        return reslut;
    }

    public Channel getExchangeNode(String exchange) {
        for (String key : channelExchange.keySet()) {
            String exchangeName = key.split(":")[0];
            String clientId = channelExchange.get(key);
            if (exchangeName.equalsIgnoreCase(exchange)) {
                return deviceChannels.get(clientId);
            }
        }
        return null;
    }
}
