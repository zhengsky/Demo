package org.springside.modules.nosql.redis;

import org.springside.modules.nosql.redis.pool.JedisPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class JedisUtils
{
  private static final String OK_CODE = "OK";
  private static final String OK_MULTI_CODE = "+OK";
  
  public static boolean isStatusOk(String status)
  {
    return (status != null) && (("OK".equals(status)) || ("+OK".equals(status)));
  }
  
  public static void destroyJedis(Jedis jedis)
  {
    if ((jedis != null) && (jedis.isConnected())) {
      try
      {
        try
        {
          jedis.quit();
        }
        catch (Exception e) {}
        jedis.disconnect();
      }
      catch (Exception e) {}
    }
  }
  
  public static boolean ping(JedisPool pool)
  {
    JedisTemplate template = new JedisTemplate(pool);
    try
    {
      String result = (String)template.execute(new JedisTemplate.JedisAction()
      {
        public String action(Jedis jedis)
        {
          return jedis.ping();
        }
      });
      return (result != null) && (result.equals("PONG"));
    }
    catch (JedisException e) {}
    return false;
  }
}
