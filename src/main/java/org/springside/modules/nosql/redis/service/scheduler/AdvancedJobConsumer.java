package org.springside.modules.nosql.redis.service.scheduler;

import com.google.common.collect.Lists;
import java.util.List;
import org.springside.modules.nosql.redis.JedisScriptExecutor;
import org.springside.modules.nosql.redis.JedisTemplate;
import org.springside.modules.nosql.redis.pool.JedisPool;
import org.springside.modules.utils.Threads;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class AdvancedJobConsumer
{
  public static final String DEFAULT_BATCH_POP_LUA_FILE_PATH = "classpath:/redis/batchpop.lua";
  public static final String DEFAULT_SINGLE_POP_LUA_FILE_PATH = "classpath:/redis/singlepop.lua";
  public static final int DEFAULT_CONNECTION_RETRY_MILLS = 5000;
  public static final boolean DEFAULT_RELIABLE = false;
  public static final int DEFAULT_BATCH_SIZE = 10;
  private boolean reliable = false;
  private int batchSize = 10;
  private JedisTemplate jedisTemplate;
  private JedisScriptExecutor singlePopScriptExecutor;
  private JedisScriptExecutor batchPopScriptExecutor;
  private String batchPopScriptPath = "classpath:/redis/batchpop.lua";
  private String singlePopScriptPath = "classpath:/redis/singlepop.lua";
  private String readyJobKey;
  private String lockJobKey;
  private List<String> keys;
  
  public AdvancedJobConsumer(String jobName, JedisPool jedisPool)
  {
    this.readyJobKey = Keys.getReadyJobKey(jobName);
    this.lockJobKey = Keys.getLockJobKey(jobName);
    this.keys = Lists.newArrayList(new String[] { this.readyJobKey, this.lockJobKey });
    
    this.jedisTemplate = new JedisTemplate(jedisPool);
    this.singlePopScriptExecutor = new JedisScriptExecutor(jedisPool);
    this.batchPopScriptExecutor = new JedisScriptExecutor(jedisPool);
  }
  
  public void init()
  {
    this.singlePopScriptExecutor.loadFromFile(this.singlePopScriptPath);
    this.batchPopScriptExecutor.loadFromFile(this.batchPopScriptPath);
  }
  
  public String popupJob()
  {
    String job = null;
    try
    {
      long currTime = System.currentTimeMillis();
      List<String> args = Lists.newArrayList(new String[] { String.valueOf(currTime), String.valueOf(this.reliable) });
      job = (String)this.singlePopScriptExecutor.execute(this.keys, args);
    }
    catch (JedisConnectionException e)
    {
      Threads.sleep(5000L);
    }
    return job;
  }
  
  public List<String> popupJobs()
  {
    List<String> jobs = null;
    try
    {
      long currTime = System.currentTimeMillis();
      List<String> args = Lists.newArrayList(new String[] { String.valueOf(currTime), String.valueOf(this.batchSize), String.valueOf(this.reliable) });
      
      jobs = (List)this.batchPopScriptExecutor.execute(this.keys, args);
    }
    catch (JedisConnectionException e)
    {
      Threads.sleep(5000L);
    }
    return jobs;
  }
  
  public void ackJob(String job)
  {
    this.jedisTemplate.zrem(this.lockJobKey, job);
  }
  
  public void setBatchPopScriptPath(String batchPopScriptPath)
  {
    this.batchPopScriptPath = batchPopScriptPath;
  }
  
  public void setSinglePopScriptPath(String singlePopScriptPath)
  {
    this.singlePopScriptPath = singlePopScriptPath;
  }
  
  public void setReliable(boolean reliable)
  {
    this.reliable = reliable;
  }
  
  public void setBatchSize(int batchSize)
  {
    this.batchSize = batchSize;
  }
}
