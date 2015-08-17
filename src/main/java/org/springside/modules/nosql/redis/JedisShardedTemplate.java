package org.springside.modules.nosql.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.springside.modules.nosql.redis.pool.JedisPool;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Hashing;

public class JedisShardedTemplate
{
  private final Hashing algo = Hashing.MURMUR_HASH;
  private TreeMap<Long, JedisTemplate> nodes = new TreeMap();
  private JedisTemplate singleTemplate = null;
  
  public JedisShardedTemplate(JedisPool... jedisPools)
  {
    if (jedisPools.length == 1) {
      this.singleTemplate = new JedisTemplate(jedisPools[0]);
    } else {
      initNodes(jedisPools);
    }
  }
  
  public JedisShardedTemplate(List<JedisPool> jedisPools)
  {
    this((JedisPool[])jedisPools.toArray(new JedisPool[jedisPools.size()]));
  }
  
  private void initNodes(JedisPool... jedisPools)
  {
    for (int i = 0; i != jedisPools.length; i++) {
      for (int n = 0; n < 128; n++)
      {
        JedisPool jedisPool = jedisPools[i];
        this.nodes.put(Long.valueOf(this.algo.hash("SHARD-" + i + "-NODE-" + n)), new JedisTemplate(jedisPool));
      }
    }
  }
  
  public JedisTemplate getShard(String key)
  {
    if (this.singleTemplate != null) {
      return this.singleTemplate;
    }
    SortedMap<Long, JedisTemplate> tail = this.nodes.tailMap(Long.valueOf(this.algo.hash(key)));
    if (tail.isEmpty()) {
      return (JedisTemplate)this.nodes.get(this.nodes.firstKey());
    }
    return (JedisTemplate)tail.get(tail.firstKey());
  }
  
  public <T> T execute(String key, JedisTemplate.JedisAction<T> jedisAction)
    throws JedisException
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.execute(jedisAction);
  }
  
  public void execute(String key, JedisTemplate.JedisActionNoResult jedisAction)
    throws JedisException
  {
    JedisTemplate jedisTemplate = getShard(key);
    jedisTemplate.execute(jedisAction);
  }
  
  public List<Object> execute(String key, JedisTemplate.PipelineAction pipelineAction)
    throws JedisException
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.execute(pipelineAction);
  }
  
  public void execute(String key, JedisTemplate.PipelineActionNoResult pipelineAction)
    throws JedisException
  {
    JedisTemplate jedisTemplate = getShard(key);
    jedisTemplate.execute(pipelineAction);
  }
  
  public Boolean del(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.del(new String[] { key });
  }
  
  public Boolean del(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.del(new String[] { key });
  }
  
  public String get(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.get(key);
  }
  
  public String get(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.get(key);
  }
  
  public Long getAsLong(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.getAsLong(key);
  }
  
  public Long getAsLong(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.getAsLong(key);
  }
  
  public Integer getAsInt(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.getAsInt(key);
  }
  
  public Integer getAsInt(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.getAsInt(key);
  }
  
  public void set(String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(key);
    jedisTemplate.set(key, value);
  }
  
  public void set(String shardingKey, String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    jedisTemplate.set(key, value);
  }
  
  public void setex(String key, String value, int seconds)
  {
    JedisTemplate jedisTemplate = getShard(key);
    jedisTemplate.setex(key, value, seconds);
  }
  
  public void setex(String shardingKey, String key, String value, int seconds)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    jedisTemplate.setex(key, value, seconds);
  }
  
  public Boolean setnx(String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.setnx(key, value);
  }
  
  public Boolean setnx(String shardingKey, String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.setnx(key, value);
  }
  
  public Boolean setnxex(String key, String value, int seconds)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.setnxex(key, value, seconds);
  }
  
  public Boolean setnxex(String shardingKey, String key, String value, int seconds)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.setnxex(key, value, seconds);
  }
  
  public String getSet(String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.getSet(key, value);
  }
  
  public String getSet(String shardingKey, String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.getSet(key, value);
  }
  
  public Long incr(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.incr(key);
  }
  
  public Long incr(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.incr(key);
  }
  
  public Long incrBy(String key, Long increment)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.incrBy(key, increment.longValue());
  }
  
  public Long incrBy(String shardingKey, String key, Long increment)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.incrBy(key, increment.longValue());
  }
  
  public Double incrByFloat(String key, double increment)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.incrByFloat(key, increment);
  }
  
  public Double incrByFloat(String shardingKey, String key, double increment)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.incrByFloat(key, increment);
  }
  
  public Long decr(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.decr(key);
  }
  
  public Long decr(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.decr(key);
  }
  
  public Long decrBy(String key, Long decrement)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.decrBy(key, decrement.longValue());
  }
  
  public Long decrBy(String shardingKey, String key, Long decrement)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.decrBy(key, decrement.longValue());
  }
  
  public String hget(String key, String field)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hget(key, field);
  }
  
  public String hget(String shardingKey, String key, String field)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hget(key, field);
  }
  
  public List<String> hmget(String key, String field)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hmget(key, new String[] { field });
  }
  
  public List<String> hmget(String key, String[] fields)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hmget(key, fields);
  }
  
  public List<String> hmget(String shardingKey, String key, String field)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hmget(key, new String[] { field });
  }
  
  public List<String> hmget(String shardingKey, String key, String[] fields)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hmget(key, fields);
  }
  
  public Map<String, String> hgetAll(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hgetAll(key);
  }
  
  public Map<String, String> hgetAll(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hgetAll(key);
  }
  
  public void hset(String key, String field, String value)
  {
    JedisTemplate jedisTemplate = getShard(key);
    jedisTemplate.hset(key, field, value);
  }
  
  public void hset(String shardingKey, String key, String field, String value)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    jedisTemplate.hset(key, field, value);
  }
  
  public void hmset(String key, Map<String, String> map)
  {
    JedisTemplate jedisTemplate = getShard(key);
    jedisTemplate.hmset(key, map);
  }
  
  public void hmset(String shardingKey, String key, Map<String, String> map)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    jedisTemplate.hmset(key, map);
  }
  
  public Boolean hsetnx(String key, String fieldName, String value)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hsetnx(key, fieldName, value);
  }
  
  public Boolean hsetnx(String shardingKey, String key, String fieldName, String value)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hsetnx(key, fieldName, value);
  }
  
  public Long hincrBy(String key, String fieldName, long increment)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hincrBy(key, fieldName, increment);
  }
  
  public Long hincrBy(String shardingKey, String key, String fieldName, long increment)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hincrBy(key, fieldName, increment);
  }
  
  public Double hincrByFloat(String key, String fieldName, double increment)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hincrByFloat(key, fieldName, increment);
  }
  
  public Double hincrByFloat(String shardingKey, String key, String fieldName, double increment)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hincrByFloat(key, fieldName, increment);
  }
  
  public Long hdel(String key, String fieldsName)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hdel(key, new String[] { fieldsName });
  }
  
  public Long hdel(String key, String[] fieldsNames)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hdel(key, fieldsNames);
  }
  
  public Long hdel(String shardingKey, String key, String fieldsName)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hdel(key, new String[] { fieldsName });
  }
  
  public Long hdel(String shardingKey, String key, String[] fieldsNames)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hdel(key, fieldsNames);
  }
  
  public Boolean hexists(String key, String fieldName)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hexists(key, fieldName);
  }
  
  public Boolean hexists(String shardingKey, String key, String fieldName)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hexists(key, fieldName);
  }
  
  public Set<String> hkeys(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hkeys(key);
  }
  
  public Set<String> hkeys(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hkeys(key);
  }
  
  public Long hlen(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.hlen(key);
  }
  
  public Long hlen(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.hlen(key);
  }
  
  public Long lpush(String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.lpush(key, new String[] { value });
  }
  
  public Long lpush(String key, String[] values)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.lpush(key, values);
  }
  
  public Long lpush(String shardingKey, String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.lpush(key, new String[] { value });
  }
  
  public Long lpush(String shardingKey, String key, String[] values)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.lpush(key, values);
  }
  
  public String rpop(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.rpop(key);
  }
  
  public String rpop(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.rpop(key);
  }
  
  public String brpop(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.brpop(key);
  }
  
  public String brpop(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.brpop(key);
  }
  
  public String brpop(int timeout, String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.brpop(timeout, key);
  }
  
  public String brpop(String shardingKey, int timeout, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.brpop(timeout, key);
  }
  
  public Long llen(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.llen(key);
  }
  
  public Long llen(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.llen(key);
  }
  
  public String lindex(String key, long index)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.lindex(key, index);
  }
  
  public String lindex(String shardingKey, String key, long index)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.lindex(key, index);
  }
  
  public List<String> lrange(String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.lrange(key, start, end);
  }
  
  public List<String> lrange(String shardingKey, String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.lrange(key, start, end);
  }
  
  public void ltrim(String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(key);
    jedisTemplate.ltrim(key, start, end);
  }
  
  public void ltrim(String shardingKey, String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    jedisTemplate.ltrim(key, start, end);
  }
  
  public void ltrimFromLeft(String key, int size)
  {
    JedisTemplate jedisTemplate = getShard(key);
    jedisTemplate.ltrimFromLeft(key, size);
  }
  
  public void ltrimFromLeft(String shardingKey, String key, int size)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    jedisTemplate.ltrimFromLeft(key, size);
  }
  
  public Boolean lremFirst(String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.lremFirst(key, value);
  }
  
  public Boolean lremFirst(String shardingKey, String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.lremFirst(key, value);
  }
  
  public Boolean lremAll(String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.lremAll(key, value);
  }
  
  public Boolean lremAll(String shardingKey, String key, String value)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.lremAll(key, value);
  }
  
  public Boolean sadd(String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.sadd(key, member);
  }
  
  public Boolean sadd(String shardingKey, String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.sadd(key, member);
  }
  
  public Set<String> smembers(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.smembers(key);
  }
  
  public Set<String> smembers(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.smembers(key);
  }
  
  public Boolean zadd(String key, double score, String member)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zadd(key, score, member);
  }
  
  public Boolean zadd(String shardingKey, String key, double score, String member)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zadd(key, score, member);
  }
  
  public Double zscore(String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zscore(key, member);
  }
  
  public Double zscore(String shardingKey, String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zscore(key, member);
  }
  
  public Long zrank(String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrank(key, member);
  }
  
  public Long zrank(String shardingKey, String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrank(key, member);
  }
  
  public Long zrevrank(String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrevrank(key, member);
  }
  
  public Long zrevrank(String shardingKey, String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrevrank(key, member);
  }
  
  public Long zcount(String key, double start, double end)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zcount(key, start, end);
  }
  
  public Long zcount(String shardingKey, String key, double start, double end)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zcount(key, start, end);
  }
  
  public Set<String> zrange(String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrange(key, start, end);
  }
  
  public Set<String> zrange(String shardingKey, String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrange(key, start, end);
  }
  
  public Set<Tuple> zrangeWithScores(String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrangeWithScores(key, start, end);
  }
  
  public Set<Tuple> zrangeWithScores(String shardingKey, String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrangeWithScores(key, start, end);
  }
  
  public Set<String> zrevrange(String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrevrange(key, start, end);
  }
  
  public Set<String> zrevrange(String shardingKey, String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrevrange(key, start, end);
  }
  
  public Set<Tuple> zrevrangeWithScores(String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrevrangeWithScores(key, start, end);
  }
  
  public Set<Tuple> zrevrangeWithScores(String shardingKey, String key, int start, int end)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrevrangeWithScores(key, start, end);
  }
  
  public Set<String> zrangeByScore(String key, double min, double max)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrangeByScore(key, min, max);
  }
  
  public Set<String> zrangeByScore(String shardingKey, String key, double min, double max)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrangeByScore(key, min, max);
  }
  
  public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrangeByScoreWithScores(key, min, max);
  }
  
  public Set<Tuple> zrangeByScoreWithScores(String shardingKey, String key, double min, double max)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrangeByScoreWithScores(key, min, max);
  }
  
  public Set<String> zrevrangeByScore(String key, double max, double min)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrevrangeByScore(key, max, min);
  }
  
  public Set<String> zrevrangeByScore(String shardingKey, String key, double max, double min)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrevrangeByScore(key, max, min);
  }
  
  public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrevrangeByScoreWithScores(key, max, min);
  }
  
  public Set<Tuple> zrevrangeByScoreWithScores(String shardingKey, String key, double max, double min)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrevrangeByScoreWithScores(key, max, min);
  }
  
  public Boolean zrem(String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zrem(key, member);
  }
  
  public Boolean zrem(String shardingKey, String key, String member)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zrem(key, member);
  }
  
  public Long zremByScore(String key, double min, double max)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zremByScore(key, min, max);
  }
  
  public Long zremByScore(String shardingKey, String key, double min, double max)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zremByScore(key, min, max);
  }
  
  public Long zremByRank(String key, long start, long end)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zremByRank(key, start, end);
  }
  
  public Long zremByRank(String shardingKey, String key, long start, long end)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zremByRank(key, start, end);
  }
  
  public Long zcard(String key)
  {
    JedisTemplate jedisTemplate = getShard(key);
    return jedisTemplate.zcard(key);
  }
  
  public Long zcard(String shardingKey, String key)
  {
    JedisTemplate jedisTemplate = getShard(shardingKey);
    return jedisTemplate.zcard(key);
  }
}
