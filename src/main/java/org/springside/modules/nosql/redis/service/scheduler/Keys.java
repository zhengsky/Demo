package org.springside.modules.nosql.redis.service.scheduler;

public class Keys
{
  public static String getScheduledJobKey(String jobName)
  {
    return "job:" + jobName + ":scheduled";
  }
  
  public static String getReadyJobKey(String jobName)
  {
    return "job:" + jobName + ":ready";
  }
  
  public static String getLockJobKey(String jobName)
  {
    return "job:" + jobName + ":lock";
  }
  
  public static String getDispatchCounterKey(String jobName)
  {
    return "job:" + jobName + ":dispatch.counter";
  }
  
  public static String getRetryCounterKey(String jobName)
  {
    return "job:" + jobName + ":retry.counter";
  }
}
