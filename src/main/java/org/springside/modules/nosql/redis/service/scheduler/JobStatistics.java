package org.springside.modules.nosql.redis.service.scheduler;

import org.springside.modules.nosql.redis.JedisTemplate;
import org.springside.modules.nosql.redis.pool.JedisPool;

public class JobStatistics
{
  private JedisTemplate jedisTemplate;
  private String scheduledJobKey;
  private String readyJobKey;
  private String lockJobKey;
  private String dispatchCounterKey;
  private String retryCounterKey;
  
  public JobStatistics(String jobName, JedisPool jedisPool)
  {
    this.scheduledJobKey = Keys.getScheduledJobKey(jobName);
    this.readyJobKey = Keys.getReadyJobKey(jobName);
    this.lockJobKey = Keys.getLockJobKey(jobName);
    
    this.dispatchCounterKey = Keys.getDispatchCounterKey(jobName);
    this.retryCounterKey = Keys.getRetryCounterKey(jobName);
    
    this.jedisTemplate = new JedisTemplate(jedisPool);
  }
  
  public long getScheduledJobNumber()
  {
    return this.jedisTemplate.zcard(this.scheduledJobKey).longValue();
  }
  
  public long getReadyJobNumber()
  {
    return this.jedisTemplate.llen(this.readyJobKey).longValue();
  }
  
  public long getLockJobNumber()
  {
    return this.jedisTemplate.zcard(this.lockJobKey).longValue();
  }
  
  public long getDispatchCounter()
  {
    return this.jedisTemplate.getAsLong(this.dispatchCounterKey).longValue();
  }
  
  public long getRetryCounter()
  {
    return this.jedisTemplate.getAsLong(this.retryCounterKey).longValue();
  }
  
  public void restCounters()
  {
    this.jedisTemplate.set(this.dispatchCounterKey, "0");
    this.jedisTemplate.set(this.retryCounterKey, "0");
  }
}
