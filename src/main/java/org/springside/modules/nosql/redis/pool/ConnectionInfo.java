package org.springside.modules.nosql.redis.pool;

public class ConnectionInfo
{
  public static final String DEFAULT_PASSWORD = null;
  private int database = 0;
  private String password = DEFAULT_PASSWORD;
  private int timeout = 2000;
  
  public ConnectionInfo() {}
  
  public ConnectionInfo(int database, String password, int timeout)
  {
    this.timeout = timeout;
    this.password = password;
    this.database = database;
  }
  
  public int getDatabase()
  {
    return this.database;
  }
  
  public void setDatabase(int database)
  {
    this.database = database;
  }
  
  public String getPassword()
  {
    return this.password;
  }
  
  public void setPassword(String password)
  {
    this.password = password;
  }
  
  public int getTimeout()
  {
    return this.timeout;
  }
  
  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }
  
  public String toString()
  {
    return "ConnectionInfo [database=" + this.database + ", password=" + this.password + ", timeout=" + this.timeout + "]";
  }
}
