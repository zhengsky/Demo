package org.springside.modules.nosql.redis.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

public abstract class JedisPool
  extends Pool<Jedis>
{
  protected String poolName;
  protected HostAndPort address;
  protected ConnectionInfo connectionInfo;
  
  public static JedisPoolConfig createPoolConfig(int maxPoolSize)
  {
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(maxPoolSize);
    config.setMaxIdle(maxPoolSize);
    
    config.setTimeBetweenEvictionRunsMillis(600000L);
    
    return config;
  }
  
  protected void initInternalPool(HostAndPort address, ConnectionInfo connectionInfo, JedisPoolConfig config)
  {
    this.poolName = this.poolName;
    this.address = address;
    this.connectionInfo = connectionInfo;
    JedisFactory factory = new JedisFactory(address.getHost(), address.getPort(), connectionInfo.getTimeout(), connectionInfo.getPassword(), connectionInfo.getDatabase());
    

    this.internalPool = new GenericObjectPool(factory, config);
  }
  
  public void returnBrokenResource(Jedis resource)
  {
    if (resource != null) {
      returnBrokenResourceObject(resource);
    }
  }
  
  public void returnResource(Jedis resource)
  {
    if (resource != null)
    {
      resource.resetState();
      returnResourceObject(resource);
    }
  }
  
  public HostAndPort getAddress()
  {
    return this.address;
  }
  
  public ConnectionInfo getConnectionInfo()
  {
    return this.connectionInfo;
  }
}
