
## 项目简介

[![GitHub](https://img.shields.io/github/stars/tokyohost/BitStrat.svg?style=social&label=Stars)](https://github.com/tokyohost/BitStrat)

[![RuoYi-Vue-Plus](https://img.shields.io/badge/RuoYi_Vue_Plus-5.3.0-success.svg)](https://gitee.com/dromara/RuoYi-Vue-Plus)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-blue.svg)]()
[![JDK-17](https://img.shields.io/badge/JDK-17-green.svg)]()
[![JDK-21](https://img.shields.io/badge/JDK-21-green.svg)]()

BitStrat 是基于 RuoYi-Vue-Plus 框架下，开发的一款支持多AI Agent 策略允许AI对加密货币交易所进行高度自定义操控的平台，支持多租户集群化部署，支持高并发大数据量执行。

> 系统演示: [传送门](https://bitstrat.com)

> 前端项目地址: [bitstrat-ui](https://github.com/tokyohost/bitstrat-ui)<br>


# 支持的交易所


| 交易所     | API 管理 | Websocket 支持     | 合约操盘 | 现货操盘 | 支持持仓模式     |
|---------|--------|------------------|------|------|------------|
| bitget  | 支持     | 支持 |  支持   | 暂未支持        | 单向持仓模式(合约) | 
| okx     | 支持     | 支持 |  支持   | 暂未支持        | 双向持仓模式(合约)     | 
| binance | 支持     | 支持 |  暂未支持   | 暂未支持        | 双向持仓模式(合约)     | 
| bybit   | 支持     | 支持 |  暂未支持   | 暂未支持        | 单向持仓模式(合约)     | 
# 功能模块
| 功能      | 详情                              |
|---------|---------------------------------|
| AI 策略   | 支持市面绝大部分最新AI Agent 直接操盘         |
| API 管理  | 已支持okx、bitget模拟盘以及实盘合约操作        |
| AI 策略统计 | 已支持每日统计不限于盈亏比、多空比、胜率、平均持仓时长等等维度 |
| 实时持仓    | 已支持交易所实时仓位同步显示                  |
| 快速强平    | 已支持交易所实时仓位快速平仓                  |
| 通知配置    | 已支持钉钉群机器人、telegram              |
| 国际化支持   | 已支持简体中文、English、韩语              |
| 更多功能    | 更多功能敬请期待                        |
## 快速体验
```bash
curl -L https://raw.githubusercontent.com/tokyohost/bitstrat/master/service-init/docker-compose.yml | docker compose -f - up -d
```
访问 ```http://localhost```<br>
默认账号密码 ```admin/admin@2026```

## 部署文档

使用框架前请仔细阅读文档重点注意事项
<br>

>[部署文档 Wiki](https://github.com/tokyohost/BitStrat/wiki/%E6%9C%80%E5%B0%8F%E5%8C%96%E9%83%A8%E7%BD%B2%E9%A1%B9%E7%9B%AE)

## 系统架构图

![img_17.png](imgs/img_17.png)
## 演示图例

|                                                                                           |                                                                                            |
|-------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
|        ![img_1.png](imgs/img_1.png)                                                                                   |      ![img_2.png](imgs/img_2.png)                                                                                      |
|        ![img_3.png](imgs/img_3.png)                                                                                   |      ![img_4.png](imgs/img_4.png)                                                                                   |
|        ![img_5.png](imgs/img_5.png)                                                                                   |      ![img_6.png](imgs/img_6.png)                                                                                   |
|        ![img_8.png](imgs/img_8.png)                                                                                 |             ![img_9.png](imgs/img_9.png)                                                                           |
|        ![img_7.png](imgs/img_7.png)                                                                                   |         ![img_10.png](imgs/img_10.png)                                                                               |
|        ![img_12.png](imgs/img_12.png)                                                                                   |           ![img_11.png](imgs/img_11.png)                                                                              |
|        ![img_13.png](imgs/img_13.png)                                                                                   |           ![img_14.png](imgs/img_14.png)                                                                          |
|        ![img_15.png](imgs/img_15.png)                                                                                   |              ![img_16.png](imgs/img_16.png)                                                                       |













