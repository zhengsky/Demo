package org.springside.modules.nosql.redis.service.elector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.nosql.redis.JedisTemplate;
import org.springside.modules.nosql.redis.JedisTemplate.JedisActionNoResult;
import org.springside.modules.nosql.redis.JedisUtils;
import org.springside.modules.nosql.redis.pool.JedisPool;
import org.springside.modules.utils.Threads;
import org.springside.modules.utils.Threads.WrapExceptionRunnable;
import redis.clients.jedis.Jedis;

public class MasterElector
  implements Runnable
{
  public static final String DEFAULT_MASTER_KEY = "master";
  private static Logger logger = LoggerFactory.getLogger(MasterElector.class);
  private ScheduledExecutorService internalScheduledThreadPool;
  private ScheduledFuture electorJob;
  private int intervalSecs;
  private int expireSecs;
  private JedisTemplate jedisTemplate;
  private String hostId;
  private String masterKey = "master";
  private AtomicBoolean master = new AtomicBoolean(false);
  
  public MasterElector(JedisPool jedisPool, int intervalSecs)
  {
    this.jedisTemplate = new JedisTemplate(jedisPool);
    this.intervalSecs = intervalSecs;
    this.expireSecs = (intervalSecs + intervalSecs / 2);
  }
  
  public boolean isMaster()
  {
    return this.master.get();
  }
  
  public void start()
  {
    this.internalScheduledThreadPool = Executors.newScheduledThreadPool(1, Threads.buildJobFactory("Master-Elector-" + this.masterKey + "-%d"));
    
    start(this.internalScheduledThreadPool);
  }
  
  public void start(ScheduledExecutorService scheduledThreadPool)
  {
    this.hostId = generateHostId();
    this.electorJob = scheduledThreadPool.scheduleAtFixedRate(new Threads.WrapExceptionRunnable(this), 0L, this.intervalSecs, TimeUnit.SECONDS);
    
    logger.info("masterElector for {} start, hostName:{}.", this.masterKey, this.hostId);
  }
  
  public void stop()
  {
    if (this.master.get()) {
      this.jedisTemplate.del(new String[] { this.masterKey });
    }
    this.electorJob.cancel(false);
    if (this.internalScheduledThreadPool != null) {
      Threads.normalShutdown(this.internalScheduledThreadPool, 5, TimeUnit.SECONDS);
    }
  }
  
  protected String generateHostId()
  {
    String host = "localhost";
    try
    {
      host = InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e)
    {
      logger.warn("can not get hostName, use localhost as default.", e);
    }
    host = host + "-" + new SecureRandom().nextInt(10000);
    
    return host;
  }
  
  public void run()
  {
    try
    {
      this.jedisTemplate.execute(new JedisTemplate.JedisActionNoResult()
      {
        public void action(Jedis jedis)
        {
          String masterFromRedis = jedis.get(MasterElector.this.masterKey);
          
          MasterElector.logger.debug("master {} is {}", MasterElector.this.masterKey, masterFromRedis);
          if (masterFromRedis == null)
          {
            if (JedisUtils.isStatusOk(jedis.set(MasterElector.this.masterKey, MasterElector.this.hostId, "NX", "EX", MasterElector.this.expireSecs)))
            {
              MasterElector.this.master.set(true);
              MasterElector.logger.info("master {} is changed to {}.", MasterElector.this.masterKey, MasterElector.this.hostId);
              return;
            }
            MasterElector.this.master.set(false);
            return;
          }
          if (MasterElector.this.hostId.equals(masterFromRedis))
          {
            jedis.expire(MasterElector.this.masterKey, MasterElector.this.expireSecs);
            MasterElector.this.master.set(true);
          }
          else
          {
            MasterElector.this.master.set(false);
          }
        }
      });
    }
    catch (Throwable e)
    {
      logger.error("Unexpected error occurred in task", e);
    }
  }
  
  public void setMasterKey(String masterKey)
  {
    this.masterKey = masterKey;
  }
  
  void setHostId(String hostId)
  {
    this.hostId = hostId;
  }
}
