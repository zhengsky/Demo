package org.springside.modules.nosql.redis.pool;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

public class JedisDirectPool
  extends JedisPool
{
  public JedisDirectPool(String poolName, HostAndPort address, JedisPoolConfig config)
  {
    this(poolName, address, new ConnectionInfo(), config);
  }
  
  public JedisDirectPool(String poolName, HostAndPort address, ConnectionInfo connectionInfo, JedisPoolConfig config)
  {
    initInternalPool(address, connectionInfo, config);
    this.poolName = poolName;
  }
}
