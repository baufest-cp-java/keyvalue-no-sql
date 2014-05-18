package utils;

import play.Logger;

import com.typesafe.config.ConfigFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 * @author rfanego
 */
public class RedisUtil {
	private static final JedisPool jedisPool;
	private static final int JEDIS_MAX_MGET = ConfigFactory.load().getInt("redis.mget.max");
	private static Integer MAX_CONNECTIONS = 12;

	static {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
        // Tests whether connection is dead when connection
        // retrieval method is called
        poolConfig.setTestOnBorrow(true);
        /* Some extra configuration */
        // Tests whether connection is dead when returning a
        // connection to the pool
        poolConfig.setTestOnReturn(true);
        // Number of connections to Redis that just sit there
        // and do nothing
        poolConfig.setMaxIdle(8);
        // Minimum number of idle connections to Redis
        // These can be seen as always open and ready to serve
        poolConfig.setMinIdle(8);
        // Tests whether connections are dead during idle periods
        poolConfig.setTestWhileIdle(true);
        // Maximum number of connections to test in each idle check
        poolConfig.setNumTestsPerEvictionRun(12);
        // Idle connection checking period
        poolConfig.setTimeBetweenEvictionRunsMillis(100);
        //max time to wait for a connection to become available in the pool when it is exhausted
        poolConfig.setMaxWaitMillis(10000l);
        
		jedisPool = new JedisPool(poolConfig, ConfigFactory.load().getString("redis.host"), ConfigFactory.load().getInt("redis.port"), 60000);
	}

	public synchronized static Jedis getResource(){
		Jedis j = null;
		int times = 0;
		while(j == null && times++ < MAX_CONNECTIONS){
			try{
				j = jedisPool.getResource();
			}catch (JedisConnectionException e){
				Logger.warn("Couldn't get pool ("+times+"/"+MAX_CONNECTIONS+"): ",e);
			}catch (JedisDataException e){
				Logger.warn("Couldn't get pool ("+times+"/"+MAX_CONNECTIONS+"): ",e);
			}
		}
		return j;
	}
	
	public synchronized static void returnResource(Jedis jedis){
		jedisPool.returnResource(jedis);
	}
}
