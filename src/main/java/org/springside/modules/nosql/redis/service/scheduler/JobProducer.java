package org.springside.modules.nosql.redis.service.scheduler;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.nosql.redis.JedisTemplate;
import org.springside.modules.nosql.redis.pool.JedisPool;

public class JobProducer
{
  private static Logger logger = LoggerFactory.getLogger(JobProducer.class);
  private JedisTemplate jedisTemplate;
  private String scheduledJobKey;
  private String readyJobKey;
  
  public JobProducer(String jobName, JedisPool jedisPool)
  {
    this.jedisTemplate = new JedisTemplate(jedisPool);
    this.scheduledJobKey = Keys.getScheduledJobKey(jobName);
    this.readyJobKey = Keys.getReadyJobKey(jobName);
  }
  
  public void queue(String job)
  {
    this.jedisTemplate.lpush(this.readyJobKey, new String[] { job });
  }
  
  public void schedule(String job, long delay, TimeUnit timeUnit)
  {
    long delayTimeMillis = System.currentTimeMillis() + timeUnit.toMillis(delay);
    this.jedisTemplate.zadd(this.scheduledJobKey, delayTimeMillis, job);
  }
  
  public boolean cancel(String job)
  {
    boolean removed = this.jedisTemplate.zrem(this.scheduledJobKey, job).booleanValue();
    if (!removed) {
      logger.warn("Can't cancel scheduld job by value {}", job);
    }
    return removed;
  }
}
