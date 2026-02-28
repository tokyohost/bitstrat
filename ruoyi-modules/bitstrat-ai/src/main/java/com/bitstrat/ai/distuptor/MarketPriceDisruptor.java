package com.bitstrat.ai.distuptor;

import com.bitstrat.ai.domain.ExtConfig;
import com.bitstrat.config.wsClient.ConnectionOtherConfig;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import static com.bitstrat.ai.utils.DistuptorUtils.nextPowerOfTwo;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 15:20
 * @Content
 */


public class MarketPriceDisruptor {
    @Getter
    @Setter
    private String symbol;
    @Getter
    @Setter
    private String exchangeName;

    @Getter
    @Setter
    private Channel channel;

    @Getter
    @Setter
    private volatile Long dely = 0L;

    /**
     * 1-A
     * 2-B
     */
    @Getter
    @Setter
    private int side;
    @Getter
    @Setter
    ConnectionOtherConfig<ExtConfig> connectionOtherConfig;
    private final Disruptor<MarketPriceEvent> disruptor;
    private final RingBuffer<MarketPriceEvent> ringBuffer;
    private final int bufferSize;

    public MarketPriceDisruptor(int bufferSize) {
        this.bufferSize = bufferSize;

        disruptor = new Disruptor<>(
            new MarketPriceEventFactory(),
            bufferSize,
            Executors.defaultThreadFactory(),
            ProducerType.SINGLE,
            new BlockingWaitStrategy()
        );
        disruptor.setDefaultExceptionHandler(new DefaultExceptionHandler<MarketPriceEvent>());
        disruptor.handleEventsWith(new MarketPriceEventHandler());
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
    }
    public MarketPriceDisruptor(int bufferSize, EventHandler eventHandler) {
        this.bufferSize = bufferSize;

        disruptor = new Disruptor<>(
            new MarketPriceEventFactory(),
            bufferSize,
            Executors.defaultThreadFactory(),
            ProducerType.SINGLE,
            new BlockingWaitStrategy()
        );

        disruptor.handleEventsWith(eventHandler);
        disruptor.setDefaultExceptionHandler(new DefaultExceptionHandler<MarketPriceEvent>());
        disruptor.start();

        ringBuffer = disruptor.getRingBuffer();
    }

    // 添加价格（队列满则自动覆盖最旧数据）
    public void publishPrice(MarketPrice price) {
        long sequence = ringBuffer.next(); // 获取下一个可用序号（覆盖模式）
        try {
            MarketPriceEvent event = ringBuffer.get(sequence);
            event.setMarketPrice(price);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
    public void publishPrice(MarketPrice price,int side) {
        long sequence = ringBuffer.next(); // 获取下一个可用序号（覆盖模式）
        try {
            MarketPriceEvent event = ringBuffer.get(sequence);
            event.setMarketPrice(price);
            event.setSide(side);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    // 快速读取 RingBuffer 中的所有数据成 List
    public List<MarketPrice> getAllPrices() {
        List<MarketPrice> prices = new ArrayList<>();
        long cursor = ringBuffer.getCursor(); // 当前最新位置
        long minSeq = Math.max(0, cursor - bufferSize + 1);

        for (long i = minSeq; i <= cursor; i++) {
            MarketPriceEvent event = ringBuffer.get(i);
            if (event != null && event.getMarketPrice() != null) {
                prices.add(event.getMarketPrice());
            }
        }
        return prices;
    }

    /**
     * 获取最新多条
     * @param limit
     * @return
     */
    public List<MarketPrice> getLatestPrices(int limit) {
        long cursor = ringBuffer.getCursor();
        if (cursor < 0) return Collections.emptyList();

        // 计算起始序号，确保不会越界
        long start = Math.max(0, cursor - limit + 1);

        // 预分配 List 容量，避免扩容开销
        List<MarketPrice> prices = new ArrayList<>(limit);

        for (long i = start; i <= cursor; i++) {
            MarketPriceEvent event = ringBuffer.get(i);
            if (event != null) {
                MarketPrice price = event.getMarketPrice();
                if (price != null) {
                    prices.add(price);
                }
            }
        }

        return prices;
    }

    /**
     * 获取最新一条
     * @return
     */
    public MarketPrice getLatestPrice() {
        long cursor = ringBuffer.getCursor(); // 获取最新序号
        if (cursor < 0) {
            return null; // 没有数据
        }

        MarketPriceEvent event = ringBuffer.get(cursor);
        return (event != null) ? event.getMarketPrice() : null;
    }

    public void shutdown() {
        connectionOtherConfig.close();
        disruptor.shutdown();
    }

    // 测试用例
    public static void main(String[] args) throws InterruptedException {
        MarketPriceDisruptor demo = new MarketPriceDisruptor(nextPowerOfTwo(6)); // 环形大小 5

        for (int i = 1; i <= 82; i++) {
            demo.publishPrice(new MarketPrice(System.currentTimeMillis(), new BigDecimal(100.0 + i)));
            Thread.sleep(50); // 模拟时间间隔
        }

        List<MarketPrice> all = demo.getAllPrices();
        all.forEach(System.out::println); // 只保留最后5条

        demo.shutdown();
    }


}
