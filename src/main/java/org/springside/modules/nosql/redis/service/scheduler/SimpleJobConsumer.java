package org.springside.modules.nosql.redis.service.scheduler;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.nosql.redis.JedisTemplate;
import org.springside.modules.nosql.redis.JedisTemplate.JedisAction;
import org.springside.modules.nosql.redis.pool.JedisPool;
import org.springside.modules.utils.Threads;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class SimpleJobConsumer
{
  public static final int DEFAULT_POPUP_TIMEOUT_SECONDS = 5;
  public static final int DEFAULT_CONNECTION_RETRY_MILLS = 5000;
  private static Logger logger = LoggerFactory.getLogger(SimpleJobConsumer.class);
  private JedisTemplate jedisTemplate;
  private String readyJobKey;
  private int popupTimeoutSecs = 5;
  
  public SimpleJobConsumer(String jobName, JedisPool jedisPool)
  {
    this.jedisTemplate = new JedisTemplate(jedisPool);
    this.readyJobKey = Keys.getReadyJobKey(jobName);
  }
  
  public String popupJob()
  {
    List<String> nameValuePair = null;
    try
    {
      nameValuePair = (List)this.jedisTemplate.execute(new JedisTemplate.JedisAction()
      {
        public List<String> action(Jedis jedis)
        {
          return jedis.brpop(SimpleJobConsumer.this.popupTimeoutSecs, SimpleJobConsumer.this.readyJobKey);
        }
      });
    }
    catch (JedisConnectionException e)
    {
      Threads.sleep(5000L);
    }
    if ((nameValuePair != null) && (!nameValuePair.isEmpty())) {
      return (String)nameValuePair.get(1);
    }
    return null;
  }
  
  public void setPopupTimeoutSecs(int popupTimeoutSecs)
  {
    this.popupTimeoutSecs = popupTimeoutSecs;
  }
}
