package com.bitstrat.ai.distuptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static com.bitstrat.ai.utils.DistuptorUtils.nextPowerOfTwo;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 15:20
 * @Content
 */


public class MarketPriceDisruptorDemo {

    private final Disruptor<MarketPriceEvent> disruptor;
    private final RingBuffer<MarketPriceEvent> ringBuffer;
    private final int bufferSize;

    public MarketPriceDisruptorDemo(int bufferSize) {
        this.bufferSize = bufferSize;

        disruptor = new Disruptor<>(
            new MarketPriceEventFactory(),
            bufferSize,
            Executors.defaultThreadFactory(),
            ProducerType.SINGLE,
            new BlockingWaitStrategy()
        );

        disruptor.handleEventsWith(new MarketPriceEventHandler());
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

    public void shutdown() {
        disruptor.shutdown();
    }

    // 测试用例
    public static void main(String[] args) throws InterruptedException {
        MarketPriceDisruptorDemo demo = new MarketPriceDisruptorDemo(nextPowerOfTwo(6)); // 环形大小 5

        for (int i = 1; i <= 80; i++) {
            demo.publishPrice(new MarketPrice(System.currentTimeMillis(), new BigDecimal(100.0 + i)));
            Thread.sleep(50); // 模拟时间间隔
        }

        List<MarketPrice> all = demo.getAllPrices();
        all.forEach(System.out::println); // 只保留最后5条

        demo.shutdown();
    }
}
