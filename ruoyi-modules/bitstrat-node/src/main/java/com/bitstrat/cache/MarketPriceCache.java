package com.bitstrat.cache;

import com.bitstrat.cache.model.PriceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MarketPriceCache {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    public MarketPriceCache(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.objectMapper = new ObjectMapper();
    }

    public PriceDTO getLatestPrice(String exchange, String symbol) {
        try {
            String redisKey = "price:" + exchange.toLowerCase(); // 例如 price:okx
            RMap<String, String> priceMap = redissonClient.getMap(redisKey);

            String json = priceMap.get(symbol.toUpperCase()); // 例如 BTC-USDT

            if (json != null) {
                return objectMapper.readValue(json, PriceDTO.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public void updatePrice(String exchange, String symbol, String price) {
        try {
            String redisKey = "price:" + exchange.toLowerCase();
            RMap<String, String> priceMap = redissonClient.getMap(redisKey);

            PriceDTO dto = new PriceDTO();
            dto.setPrice(price);
            dto.setTimestamp(System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(dto);
            priceMap.put(symbol.toUpperCase(), json);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
