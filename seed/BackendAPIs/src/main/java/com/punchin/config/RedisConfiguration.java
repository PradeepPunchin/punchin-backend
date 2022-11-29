package com.punchin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Configuration
public class RedisConfiguration {
	@Value("${spring.redis.port}")
	private String port;

	@Value("${spring.redis.host}")
	private String host;
	
	@Value("${spring.redis.jedis.pool.max-active}")
	private String maxTotal;
	
	@Value("${spring.redis.jedis.pool.max-idle}")
	private String maxIdle;
	
	@Value("${spring.redis.jedis.pool.min-idle}")
	private String minIdle;


	private JedisPool pool = null;

	@PostConstruct
	public JedisPool getRedisReceiverClient() {
		try {
			JedisPoolConfig poolConfig = buildPoolConfig();
			pool = new JedisPool(poolConfig, host);
			pool.getResource();
		} catch (JedisConnectionException jex) {
			log.error("ERROR WHILE MAKING REDIS CONNECTION ::{}", jex.getMessage());
			System.exit(0);
		} catch (Exception ex) {
			log.error("JEDIS ERROR::{}", ex.getMessage());
			System.exit(0);
		}
		return pool;
	}


	private JedisPoolConfig buildPoolConfig() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(Integer.parseInt(maxTotal));
		poolConfig.setMinEvictableIdleTimeMillis(60000);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setMaxWaitMillis(5000);
		poolConfig.setMaxIdle(Integer.parseInt(maxIdle));
		poolConfig.setMinIdle(Integer.parseInt(minIdle));
		return poolConfig;
	}

	public void closeConnection() {
		if (pool != null) {
			pool.destroy();
			log.info("RedisReceiverClient::closeConnection::REDIS POOL DESTROYED");
		}
	}

	public Jedis getResource() {
		return pool.getResource();
	}

	@PreDestroy
	public void destroy(){
		closeConnection();
	}
}
