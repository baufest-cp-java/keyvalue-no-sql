/**
 * 
 */
package models;

import java.util.Set;

import redis.clients.jedis.Jedis;
import utils.RedisUtil;

/**
 * @author rfanego
 */
public class LeaderBoard {
	
	public static void updateLeaderBoard(String userName) {
		Jedis jedis = RedisUtil.getResource();
		jedis.zincrby("leaderboard.posts", 1, userName);
		RedisUtil.returnResource(jedis);
	}
	
	public static String getPosition(String userName){
		Jedis jedis = RedisUtil.getResource();
		Long position = jedis.zrevrank("leaderboard.posts", userName);
		RedisUtil.returnResource(jedis);
		return position != null ? String.valueOf(position.intValue() + 1) : "-";
	}
	
	public static Set<String> getUsersPositions(){
		Jedis jedis = RedisUtil.getResource();
		Set<String> users = jedis.zrevrange("leaderboard.posts", 0, 99);
		RedisUtil.returnResource(jedis);
		return users;
	}
}
