package uap.web.cache.redis;

import org.springside.modules.nosql.redis.pool.JedisPool;
import org.springside.modules.nosql.redis.pool.JedisPoolBuilder;

public class RedisPoolFactory
{
  public static JedisPool createJedisPool(String url)
  {
    return new JedisPoolBuilder().setUrl(url).buildPool();
  }
}
