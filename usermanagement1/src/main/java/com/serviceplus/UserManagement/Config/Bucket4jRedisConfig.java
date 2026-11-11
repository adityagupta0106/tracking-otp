package com.serviceplus.UserManagement.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class Bucket4jRedisConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	@Value("${spring.data.redis.password}")
	private String redisPassword;

	@Value("${spring.data.redis.connect-timeout:1000}")
	private int connectTimeout;

	@Value("${spring.data.redis.timeout:1000}")
	private int commandTimeout;

	@Bean
	public JedisPool bucket4jJedisPool() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		return new JedisPool(poolConfig, redisHost, redisPort, connectTimeout, commandTimeout,null, 0,
				null
		);
	}

	@Bean
	public ProxyManager<byte[]> bucket4jProxyManager(JedisPool bucket4jJedisPool) {
		return JedisBasedProxyManager.builderFor(bucket4jJedisPool).build();
	}
}