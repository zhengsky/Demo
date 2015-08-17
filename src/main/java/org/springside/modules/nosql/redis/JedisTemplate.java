package org.springside.modules.nosql.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.nosql.redis.pool.JedisPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

public class JedisTemplate {
	private static Logger logger = LoggerFactory.getLogger(JedisTemplate.class);
	private JedisPool jedisPool;

	public JedisTemplate(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public <T> T execute(JedisAction<T> jedisAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = (Jedis) this.jedisPool.getResource();
			return jedisAction.action(jedis);
		} catch (JedisException e) {
			broken = handleJedisException(e);
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	public void execute(JedisActionNoResult jedisAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = (Jedis) this.jedisPool.getResource();
			jedisAction.action(jedis);
		} catch (JedisException e) {
			broken = handleJedisException(e);
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	public List<Object> execute(PipelineAction pipelineAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = (Jedis) this.jedisPool.getResource();
			Pipeline pipeline = jedis.pipelined();
			pipelineAction.action(pipeline);
			return pipeline.syncAndReturnAll();
		} catch (JedisException e) {
			broken = handleJedisException(e);
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	public void execute(PipelineActionNoResult pipelineAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = (Jedis) this.jedisPool.getResource();
			Pipeline pipeline = jedis.pipelined();
			pipelineAction.action(pipeline);
			pipeline.sync();
		} catch (JedisException e) {
			broken = handleJedisException(e);
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	public JedisPool getJedisPool() {
		return this.jedisPool;
	}

	protected boolean handleJedisException(JedisException jedisException) {
		if ((jedisException instanceof JedisConnectionException)) {
			logger.error("Redis connection " + this.jedisPool.getAddress() + " lost.", jedisException);
		} else if ((jedisException instanceof JedisDataException)) {
			if ((jedisException.getMessage() != null) && (jedisException.getMessage().indexOf("READONLY") != -1)) {
				logger.error("Redis connection " + this.jedisPool.getAddress() + " are read-only slave.",
						jedisException);
			} else {
				return false;
			}
		} else {
			logger.error("Jedis exception happen.", jedisException);
		}
		return true;
	}

	protected void closeResource(Jedis jedis, boolean conectionBroken) {
		try {
			if (conectionBroken) {
				this.jedisPool.returnBrokenResource(jedis);
			} else {
				this.jedisPool.returnResource(jedis);
			}
		} catch (Exception e) {
			logger.error("return back jedis failed, will fore close the jedis.", e);
			JedisUtils.destroyJedis(jedis);
		}
	}

	public Boolean del(final String... keys)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        return Boolean.valueOf(jedis.del(keys).longValue() == keys.length);
      }
    });
  }

	public void flushDB() {
		execute(new JedisActionNoResult() {
			public void action(Jedis jedis) {
				jedis.flushDB();
			}
		});
	}

	public String get(final String key)
  {
    (String)execute(new JedisAction()
    {
      public String action(Jedis jedis)
      {
        return jedis.get(key);
      }
    });
  }

	public Long getAsLong(String key) {
		String result = get(key);
		return result != null ? Long.valueOf(result) : null;
	}

	public Integer getAsInt(String key) {
		String result = get(key);
		return result != null ? Integer.valueOf(result) : null;
	}

	public List<String> mget(final String... keys)
  {
    (List)execute(new JedisAction()
    {
      public List<String> action(Jedis jedis)
      {
        return jedis.mget(keys);
      }
    });
  }

	public void set(final String key, final String value) {
		execute(new JedisActionNoResult() {
			public void action(Jedis jedis) {
				jedis.set(key, value);
			}
		});
	}

	public void setex(final String key, final String value, final int seconds) {
		execute(new JedisActionNoResult() {
			public void action(Jedis jedis) {
				jedis.setex(key, seconds, value);
			}
		});
	}

	public Boolean setnx(final String key, final String value)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        return Boolean.valueOf(jedis.setnx(key, value).longValue() == 1L);
      }
    });
  }

	public Boolean setnxex(final String key, final String value, final int seconds)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        String result = jedis.set(key, value, "NX", "EX", seconds);
        return Boolean.valueOf(JedisUtils.isStatusOk(result));
      }
    });
  }

	public String getSet(final String key, final String value)
  {
    (String)execute(new JedisAction()
    {
      public String action(Jedis jedis)
      {
        return jedis.getSet(key, value);
      }
    });
  }

	public Long incr(final String key)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.incr(key);
      }
    });
  }

	public Long incrBy(final String key, final long increment)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.incrBy(key, increment);
      }
    });
  }

	public Double incrByFloat(final String key, final double increment)
  {
    (Double)execute(new JedisAction()
    {
      public Double action(Jedis jedis)
      {
        return jedis.incrByFloat(key, increment);
      }
    });
  }

	public Long decr(final String key)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.decr(key);
      }
    });
  }

	public Long decrBy(final String key, final long decrement)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.decrBy(key, decrement);
      }
    });
  }

	public String hget(final String key, final String fieldName)
  {
    (String)execute(new JedisAction()
    {
      public String action(Jedis jedis)
      {
        return jedis.hget(key, fieldName);
      }
    });
  }

	public List<String> hmget(final String key, final String... fieldsNames)
  {
    (List)execute(new JedisAction()
    {
      public List<String> action(Jedis jedis)
      {
        return jedis.hmget(key, fieldsNames);
      }
    });
  }

	public Map<String, String> hgetAll(final String key)
  {
    (Map)execute(new JedisAction()
    {
      public Map<String, String> action(Jedis jedis)
      {
        return jedis.hgetAll(key);
      }
    });
  }

	public void hset(final String key, final String fieldName, final String value) {
		execute(new JedisActionNoResult() {
			public void action(Jedis jedis) {
				jedis.hset(key, fieldName, value);
			}
		});
	}

	public void hmset(final String key, final Map<String, String> map) {
		execute(new JedisActionNoResult() {
			public void action(Jedis jedis) {
				jedis.hmset(key, map);
			}
		});
	}

	public Boolean hsetnx(final String key, final String fieldName, final String value)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        return Boolean.valueOf(jedis.hsetnx(key, fieldName, value).longValue() == 1L);
      }
    });
  }

	public Long hincrBy(final String key, final String fieldName, final long increment)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.hincrBy(key, fieldName, increment);
      }
    });
  }

	public Double hincrByFloat(final String key, final String fieldName, final double increment)
  {
    (Double)execute(new JedisAction()
    {
      public Double action(Jedis jedis)
      {
        return jedis.hincrByFloat(key, fieldName, increment);
      }
    });
  }

	public Long hdel(final String key, final String... fieldsNames)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.hdel(key, fieldsNames);
      }
    });
  }

	public Boolean hexists(final String key, final String fieldName)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        return jedis.hexists(key, fieldName);
      }
    });
  }

	public Set<String> hkeys(final String key)
  {
    (Set)execute(new JedisAction()
    {
      public Set<String> action(Jedis jedis)
      {
        return jedis.hkeys(key);
      }
    });
  }

	public Long hlen(final String key)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.hlen(key);
      }
    });
  }

	public Long push(final String key, final String... values)
  {
		(Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.lpush(key, values);
      }
    });
  }

	public String rpop(final String key)
  {
    (String)execute(new JedisAction()
    {
      public String action(Jedis jedis)
      {
        return jedis.rpop(key);
      }
    });
  }

	public String brpop(final String key)
  {
    (String)execute(new JedisAction()
    {
      public String action(Jedis jedis)
      {
        List<String> nameValuePair = jedis.brpop(key);
        if (nameValuePair != null) {
          return (String)nameValuePair.get(1);
        }
        return null;
      }
    });
  }

	public String brpop(final int timeout, final String key)
  {
    (String)execute(new JedisAction()
    {
      public String action(Jedis jedis)
      {
        List<String> nameValuePair = jedis.brpop(timeout, key);
        if (nameValuePair != null) {
          return (String)nameValuePair.get(1);
        }
        return null;
      }
    });
  }

	public String rpoplpush(final String sourceKey, final String destinationKey)
  {
    (String)execute(new JedisAction()
    {
      public String action(Jedis jedis)
      {
        return jedis.rpoplpush(sourceKey, destinationKey);
      }
    });
  }

	public String brpoplpush(final String source, final String destination, final int timeout)
  {
    (String)execute(new JedisAction()
    {
      public String action(Jedis jedis)
      {
        return jedis.brpoplpush(source, destination, timeout);
      }
    });
  }

	public Long llen(final String key)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.llen(key);
      }
    });
  }

	public String lindex(final String key, final long index)
  {
    (String)execute(new JedisAction()
    {
      public String action(Jedis jedis)
      {
        return jedis.lindex(key, index);
      }
    });
  }

	public List<String> lrange(final String key, final int start, final int end)
  {
    (List)execute(new JedisAction()
    {
      public List<String> action(Jedis jedis)
      {
        return jedis.lrange(key, start, end);
      }
    });
  }

	public void ltrim(final String key, final int start, final int end) {
		execute(new JedisActionNoResult() {
			public void action(Jedis jedis) {
				jedis.ltrim(key, start, end);
			}
		});
	}

	public void ltrimFromLeft(final String key, final int size) {
		execute(new JedisActionNoResult() {
			public void action(Jedis jedis) {
				jedis.ltrim(key, 0L, size - 1);
			}
		});
	}

	public Boolean lremFirst(final String key, final String value)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        Long count = jedis.lrem(key, 1L, value);
        return Boolean.valueOf(count.longValue() == 1L);
      }
    });
  }

	public Boolean lremAll(final String key, final String value)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        Long count = jedis.lrem(key, 0L, value);
        return Boolean.valueOf(count.longValue() > 0L);
      }
    });
  }

	public Boolean sadd(final String key, final String member)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        return Boolean.valueOf(jedis.sadd(key, new String[] { member }).longValue() == 1L);
      }
    });
  }

	public Set<String> smembers(final String key)
  {
    (Set)execute(new JedisAction()
    {
      public Set<String> action(Jedis jedis)
      {
        return jedis.smembers(key);
      }
    });
  }

	public Boolean zadd(final String key, final double score, String member)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        return Boolean.valueOf(jedis.zadd(key, score, this.val$member).longValue() == 1L);
      }
    });
  }

	public Double zscore(final String key, final String member)
  {
    (Double)execute(new JedisAction()
    {
      public Double action(Jedis jedis)
      {
        return jedis.zscore(key, member);
      }
    });
  }

	public Long zrank(final String key, final String member)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.zrank(key, member);
      }
    });
  }

	public Long zrevrank(final String key, final String member)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.zrevrank(key, member);
      }
    });
  }

	public Long zcount(final String key, final double min, double max)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.zcount(key, min, this.val$max);
      }
    });
  }

	public Set<String> zrange(final String key, final int start, final int end)
  {
    (Set)execute(new JedisAction()
    {
      public Set<String> action(Jedis jedis)
      {
        return jedis.zrange(key, start, end);
      }
    });
  }

	public Set<Tuple> zrangeWithScores(final String key, final int start, final int end)
  {
    (Set)execute(new JedisAction()
    {
      public Set<Tuple> action(Jedis jedis)
      {
        return jedis.zrangeWithScores(key, start, end);
      }
    });
  }

	public Set<String> zrevrange(final String key, final int start, final int end)
  {
    (Set)execute(new JedisAction()
    {
      public Set<String> action(Jedis jedis)
      {
        return jedis.zrevrange(key, start, end);
      }
    });
  }

	public Set<Tuple> zrevrangeWithScores(final String key, final int start, final int end)
  {
    (Set)execute(new JedisAction()
    {
      public Set<Tuple> action(Jedis jedis)
      {
        return jedis.zrevrangeWithScores(key, start, end);
      }
    });
  }

	public Set<String> zrangeByScore(final String key, final double min, double max)
  {
    (Set)execute(new JedisAction()
    {
      public Set<String> action(Jedis jedis)
      {
        return jedis.zrangeByScore(key, min, this.val$max);
      }
    });
  }

	public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, double max)
  {
    (Set)execute(new JedisAction()
    {
      public Set<Tuple> action(Jedis jedis)
      {
        return jedis.zrangeByScoreWithScores(key, min, this.val$max);
      }
    });
  }

	public Set<String> zrevrangeByScore(final String key, final double max, double min)
  {
    (Set)execute(new JedisAction()
    {
      public Set<String> action(Jedis jedis)
      {
        return jedis.zrevrangeByScore(key, max, this.val$min);
      }
    });
  }

	public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, double min)
  {
    (Set)execute(new JedisAction()
    {
      public Set<Tuple> action(Jedis jedis)
      {
        return jedis.zrevrangeByScoreWithScores(key, max, this.val$min);
      }
    });
  }

	public Boolean zrem(final String key, final String member)
  {
    (Boolean)execute(new JedisAction()
    {
      public Boolean action(Jedis jedis)
      {
        return Boolean.valueOf(jedis.zrem(key, new String[] { member }).longValue() == 1L);
      }
    });
  }

	public Long zremByScore(final String key, final double start, double end)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.zremrangeByScore(key, start, this.val$end);
      }
    });
  }

	public Long zremByRank(final String key, final long start, long end)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.zremrangeByRank(key, start, this.val$end);
      }
    });
  }

	public Long zcard(final String key)
  {
    (Long)execute(new JedisAction()
    {
      public Long action(Jedis jedis)
      {
        return jedis.zcard(key);
      }
    });
  }

	public static abstract interface PipelineActionNoResult {
		public abstract void action(Pipeline paramPipeline);
	}

	public static abstract interface PipelineAction {
		public abstract List<Object> action(Pipeline paramPipeline);
	}

	public static abstract interface JedisActionNoResult {
		public abstract void action(Jedis paramJedis);
	}

	public static abstract interface JedisAction<T> {
		public abstract T action(Jedis paramJedis);
	}
}
