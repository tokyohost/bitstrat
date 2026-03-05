<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> 
</p>

## Project Introduction

[![RuoYi-Vue-Plus](https://img.shields.io/badge/RuoYi_Vue_Plus-5.3.0-success.svg)](https://gitee.com/dromara/RuoYi-Vue-Plus)
[![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-3.4-blue.svg)]()
[![JDK-17](https://img.shields.io/badge/JDK-17-green.svg)]()
[![JDK-21](https://img.shields.io/badge/JDK-21-green.svg)]()
[![GitHub](https://img.shields.io/github/stars/tokyohost/bitstrat.svg?style=social&label=Stars)](https://github.com/tokyohost/bitstrat)

BitStrat is a platform built on the **RuoYi-Vue-Plus framework** that
enables **multiple AI Agents to execute cryptocurrency trading
strategies** with a high level of customization.\
It supports **multi-tenant cluster deployment** and is designed to
handle **high concurrency and large-scale data execution**.
<br>**The core purpose of this project is to lower the barrier to quantitative trading, enabling users to quickly build their own strategies using AI and natural language. It supports multi-tenant cluster deployment and is designed for high-concurrency and large-scale data execution.**

> System Demo: [Visit Here](https://bitstrat.com)

> Frontend Repository:
> [bitstrat-ui](https://github.com/tokyohost/bitstrat-ui)<br>

------------------------------------------------------------------------

# Supported Exchanges

| Exchange     | API Management | Websocket Support     | Futures Trading | Spot Trading         | Position Mode          | Paper Trading Supported|
|---------|--------|------------------|----------|---------------|------------------------|------------------------|
| bitget  | Supported     | Supported | Supported       | Not Supported | One-way Position Mode (Futures) | Supported|
| okx     | Supported     | Supported | Supported       | Not Supported |  Hedge Mode(Futures)             | Supported|
| binance | Supported     | Supported |Not Supported     | Not Supported | Hedge Mode(Futures)            | Not Supported|
| bybit   | Supported     | Supported |Not Supported     | Not Supported | One-way Position Mode (Futures)             | Not Supported|


# Feature Modules

|Feature                | Description                                                                                              |
  |----------------------|----------------------------------------------------------------------------------------------------------|
| AI Strategies          | Supports most modern AI Agent frameworks to directlycontrol trading                                      |
| API Management       | Supports OKX and Bitget simulation accounts and live futures trading                                     |
| AI Strategy Analytics | Provides daily analytics including PnL ratio, long/short ratio, win rate, average holding time, and more |
| Real-time Positions   | Synchronizes and displays real-time positions from exchanges                                             |
| Quick Liquidation    | Allows fast closing of exchange positions                                                                |
| Notification System   | Supports DingTalk group bots and Telegram notifications                                                  |
| Internationalization  | Supports Simplified Chinese, English, and Korean. Language can be switched from the top navigation bar.|
|More Features          | More features coming soon                                                                                |

## Quick Start

``` bash
git clone https://github.com/tokyohost/bitstrat.git && \
cd bitstrat/service-init && \
docker compose up -d
```

Visit: `http://localhost`

Default credentials:

    admin / admin@2026

------------------------------------------------------------------------

## Deployment Documentation

Please carefully read the documentation and important notes before using
the framework.

[Deployment
Wiki](https://github.com/tokyohost/BitStrat/wiki/%E6%9C%80%E5%B0%8F%E5%8C%96%E9%83%A8%E7%BD%B2%E9%A1%B9%E7%9B%AE)

------------------------------------------------------------------------

## System Architecture

![img_17.png](imgs/img_17.png)

------------------------------------------------------------------------

## Demo Screenshots

|             |            |
|--------|------------|
|![img_1.png](imgs/img_1.png)     |   ![img_2.png](imgs/img_2.png)|
|![img_3.png](imgs/img_3.png)      |  ![img_4.png](imgs/img_4.png)|
|![img_5.png](imgs/img_5.png)    |    ![img_6.png](imgs/img_6.png)|
|![img_8.png](imgs/img_8.png)   |     ![img_9.png](imgs/img_9.png)|
|![img_7.png](imgs/img_7.png)   |     ![img_10.png](imgs/img_10.png)|
|![img_12.png](imgs/img_12.png)  |    ![img_11.png](imgs/img_11.png)|
|![img_13.png](imgs/img_13.png)   |   ![img_14.png](imgs/img_14.png)|
|![img_15.png](imgs/img_15.png)   |   ![img_16.png](imgs/img_16.png)|
