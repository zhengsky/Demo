package org.springside.modules.utils;

import java.util.Date;

public abstract interface Clock
{
  public static final Clock DEFAULT = new DefaultClock();
  
  public abstract Date getCurrentDate();
  
  public abstract long getCurrentTimeInMillis();
  
  public static class DefaultClock
    implements Clock
  {
    public Date getCurrentDate()
    {
      return new Date();
    }
    
    public long getCurrentTimeInMillis()
    {
      return System.currentTimeMillis();
    }
  }
  
  public static class MockClock
    implements Clock
  {
    private long time;
    
    public MockClock()
    {
      this(0L);
    }
    
    public MockClock(Date date)
    {
      this.time = date.getTime();
    }
    
    public MockClock(long time)
    {
      this.time = time;
    }
    
    public Date getCurrentDate()
    {
      return new Date(this.time);
    }
    
    public long getCurrentTimeInMillis()
    {
      return this.time;
    }
    
    public void update(Date newDate)
    {
      this.time = newDate.getTime();
    }
    
    public void update(long newTime)
    {
      this.time = newTime;
    }
    
    public void increaseTime(int millis)
    {
      this.time += millis;
    }
    
    public void decreaseTime(int millis)
    {
      this.time -= millis;
    }
  }
}
