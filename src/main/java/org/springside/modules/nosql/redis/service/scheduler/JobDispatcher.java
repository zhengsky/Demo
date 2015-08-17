package org.springside.modules.nosql.redis.service.scheduler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.nosql.redis.JedisScriptExecutor;
import org.springside.modules.nosql.redis.pool.JedisPool;
import org.springside.modules.utils.Threads;
import org.springside.modules.utils.Threads.WrapExceptionRunnable;

public class JobDispatcher
  implements Runnable
{
  public static final String DEFAULT_DISPATCH_LUA_FILE = "classpath:/redis/dispatch.lua";
  public static final long DEFAULT_INTERVAL_MILLIS = 1000L;
  public static final boolean DEFAULT_RELIABLE = false;
  public static final long DEFAULT_JOB_TIMEOUT_SECONDS = 60L;
  private static Logger logger = LoggerFactory.getLogger(JobDispatcher.class);
  private ScheduledExecutorService internalScheduledThreadPool;
  private ScheduledFuture dispatchJob;
  private long intervalMillis = 1000L;
  private boolean reliable = false;
  private long jobTimeoutSecs = 60L;
  private JedisScriptExecutor scriptExecutor;
  private String scriptPath = "classpath:/redis/dispatch.lua";
  private String jobName;
  private List<String> keys;
  
  public JobDispatcher(String jobName, JedisPool jedisPool)
  {
    this.jobName = jobName;
    
    String scheduledJobKey = Keys.getScheduledJobKey(jobName);
    String readyJobKey = Keys.getReadyJobKey(jobName);
    String dispatchCounterKey = Keys.getDispatchCounterKey(jobName);
    String lockJobKey = Keys.getLockJobKey(jobName);
    String retryCounterKey = Keys.getRetryCounterKey(jobName);
    
    this.keys = Lists.newArrayList(new String[] { scheduledJobKey, readyJobKey, dispatchCounterKey, lockJobKey, retryCounterKey });
    
    this.scriptExecutor = new JedisScriptExecutor(jedisPool);
  }
  
  public void start()
  {
    this.internalScheduledThreadPool = Executors.newScheduledThreadPool(1, Threads.buildJobFactory("Job-Dispatcher-" + this.jobName + "-%d"));
    

    start(this.internalScheduledThreadPool);
  }
  
  public void start(ScheduledExecutorService scheduledThreadPool)
  {
    this.scriptExecutor.loadFromFile(this.scriptPath);
    
    this.dispatchJob = scheduledThreadPool.scheduleAtFixedRate(new Threads.WrapExceptionRunnable(this), 0L, this.intervalMillis, TimeUnit.MILLISECONDS);
  }
  
  public void stop()
  {
    this.dispatchJob.cancel(false);
    if (this.internalScheduledThreadPool != null) {
      Threads.normalShutdown(this.internalScheduledThreadPool, 5, TimeUnit.SECONDS);
    }
  }
  
  public void run()
  {
    try
    {
      long currTime = System.currentTimeMillis();
      List<String> args = Lists.newArrayList(new String[] { String.valueOf(currTime), String.valueOf(this.reliable), String.valueOf(this.jobTimeoutSecs) });
      
      this.scriptExecutor.execute(this.keys, args);
    }
    catch (Throwable e)
    {
      logger.error("Unexpected error occurred in task", e);
    }
  }
  
  public void setScriptPath(String scriptPath)
  {
    this.scriptPath = scriptPath;
  }
  
  public void setIntervalMillis(long intervalMillis)
  {
    this.intervalMillis = intervalMillis;
  }
  
  public void setReliable(boolean reliable)
  {
    this.reliable = reliable;
  }
  
  public void setJobTimeoutSecs(long jobTimeoutSecs)
  {
    this.jobTimeoutSecs = jobTimeoutSecs;
  }
}
