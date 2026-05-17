package com.procurement.common.config;

import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

@Configuration
@EnableCaching
public class CacheConfig {

  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  @Bean
  RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisSerializer<String> keySerializer = RedisSerializer.string();

    RedisSerializer<Object> valueSerializer = RedisSerializer.json();

    RedisSerializationContext.SerializationPair<String> keyPair =
      RedisSerializationContext.SerializationPair.fromSerializer(keySerializer);

    RedisSerializationContext.SerializationPair<Object> valuePair =
      RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer);

    RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
      .entryTtl(CACHE_TTL)
      .disableCachingNullValues()
      .serializeKeysWith(keyPair)
      .serializeValuesWith(valuePair);

    return RedisCacheManager.builder(connectionFactory).cacheDefaults(cacheConfig).build();
  }
}
