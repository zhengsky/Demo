package org.springside.modules.nosql.redis.pool;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolBuilder
{
  public static final String DIRECT_POOL_PREFIX = "direct:";
  public static final String SENTINEL_POOL_PREFIX = "direct:";
  private static Logger logger = LoggerFactory.getLogger(JedisPoolBuilder.class);
  private String poolName;
  private String[] sentinelHostAndPorts;
  private String[] hostAndPorts;
  private String[] masterNames;
  private int poolSize = -1;
  private int database = 0;
  private String password = ConnectionInfo.DEFAULT_PASSWORD;
  private int timeout = 2000;
  
  public JedisPoolBuilder setUrl(String url)
  {
    URI uri;
    try
    {
      uri = new URI(url);
    }
    catch (URISyntaxException ex)
    {
      logger.error("Incorrect URI for initializing Jedis pool", ex);
      return this;
    }
    Properties prop = new Properties();
    String query = uri.getQuery();
    if (query != null)
    {
      try
      {
        prop.load(new StringReader(query.replace("&", "\n")));
      }
      catch (IOException ex)
      {
        logger.error("Failed to load the URI query string as stream", ex);
        return this;
      }
    }
    else
    {
      logger.error("No redis pool information set in query part of URI");
      return this;
    }
    String authority = uri.getAuthority();
    if ("direct".equals(uri.getScheme())) {
      setShardedDirectHosts(authority);
    } else {
      setSentinelHosts(authority);
    }
    if (prop.getProperty("masterName") != null)
    {
      String masterName = prop.getProperty("masterName");
      setShardedMasterNames(masterName);
    }
    if (prop.getProperty("poolName") != null) {
      setPoolName(prop.getProperty("poolName"));
    }
    if (prop.getProperty("poolSize") != null) {
      setPoolSize(Integer.parseInt(prop.getProperty("poolSize")));
    }
    if (prop.getProperty("database") != null) {
      setDatabase(Integer.parseInt(prop.getProperty("database")));
    }
    if (prop.getProperty("password") != null) {
      setPassword(prop.getProperty("password"));
    }
    if (prop.getProperty("timeout") != null) {
      setTimeout(Integer.parseInt(prop.getProperty("timeout")));
    }
    return this;
  }
  
  public JedisPoolBuilder setPoolName(String poolName)
  {
    this.poolName = poolName;
    return this;
  }
  
  public JedisPoolBuilder setSentinelHosts(String[] sentinelHostsAndPorts)
  {
    this.sentinelHostAndPorts = sentinelHostsAndPorts;
    return this;
  }
  
  public JedisPoolBuilder setSentinelHosts(String sentinelHostsAndPorts)
  {
    if (sentinelHostsAndPorts != null) {
      this.sentinelHostAndPorts = sentinelHostsAndPorts.split(",");
    }
    return this;
  }
  
  public JedisPoolBuilder setMasterName(String masterName)
  {
    this.masterNames = new String[] { masterName };
    return this;
  }
  
  public JedisPoolBuilder setShardedMasterNames(String[] shardedMasterNames)
  {
    this.masterNames = shardedMasterNames;
    return this;
  }
  
  public JedisPoolBuilder setShardedMasterNames(String shardedMasterNames)
  {
    if (shardedMasterNames != null) {
      this.masterNames = shardedMasterNames.split(",");
    }
    return this;
  }
  
  public JedisPoolBuilder setDirectHost(String hostAndPort)
  {
    this.hostAndPorts = new String[] { hostAndPort };
    return this;
  }
  
  public JedisPoolBuilder setShardedDirectHosts(String[] shardedHostAndPorts)
  {
    this.hostAndPorts = shardedHostAndPorts;
    return this;
  }
  
  public JedisPoolBuilder setShardedDirectHosts(String shardedHostAndPorts)
  {
    if (shardedHostAndPorts != null) {
      this.hostAndPorts = shardedHostAndPorts.split(",");
    }
    return this;
  }
  
  public JedisPoolBuilder setPoolSize(int poolSize)
  {
    this.poolSize = poolSize;
    return this;
  }
  
  public JedisPoolBuilder setDatabase(int database)
  {
    this.database = database;
    return this;
  }
  
  public JedisPoolBuilder setPassword(String password)
  {
    this.password = password;
    return this;
  }
  
  public JedisPoolBuilder setTimeout(int timeout)
  {
    this.timeout = timeout;
    return this;
  }
  
  public JedisPool buildPool()
  {
    if ((this.poolName == null) || (this.poolName.length() == 0)) {
      throw new IllegalArgumentException("poolName is null or empty");
    }
    if (this.poolSize < 1) {
      throw new IllegalArgumentException("poolSize is less then one");
    }
    JedisPoolConfig config = JedisPool.createPoolConfig(this.poolSize);
    ConnectionInfo connectionInfo = new ConnectionInfo(this.database, this.password, this.timeout);
    if (isDirect()) {
      return buildDirectPool(this.hostAndPorts[0], connectionInfo, config);
    }
    if ((this.sentinelHostAndPorts == null) || (this.sentinelHostAndPorts.length == 0)) {
      throw new IllegalArgumentException("sentinelHostsAndPorts is null or empty");
    }
    if ((this.masterNames == null) || (this.masterNames.length == 0)) {
      throw new IllegalArgumentException("masterNames is null or empty");
    }
    return buildSentinelPool(this.masterNames[0], connectionInfo, config);
  }
  
  public List<JedisPool> buildShardedPools()
  {
    if ((this.poolName == null) || (this.poolName.length() == 0)) {
      throw new IllegalArgumentException("poolName is null or empty");
    }
    if (this.poolSize < 1) {
      throw new IllegalArgumentException("poolSize is less then one");
    }
    JedisPoolConfig config = JedisPool.createPoolConfig(this.poolSize);
    ConnectionInfo connectionInfo = new ConnectionInfo(this.database, this.password, this.timeout);
    
    List<JedisPool> jedisPools = new ArrayList();
    if (isDirect())
    {
      for (String hostAndPort : this.hostAndPorts) {
        jedisPools.add(buildDirectPool(hostAndPort, connectionInfo, config));
      }
    }
    else
    {
      if ((this.sentinelHostAndPorts == null) || (this.sentinelHostAndPorts.length == 0)) {
        throw new IllegalArgumentException("sentinelHostsAndPorts is null or empty");
      }
      if ((this.masterNames == null) || (this.masterNames.length == 0)) {
        throw new IllegalArgumentException("masterNames is null or empty");
      }
      for (String masterName : this.masterNames) {
        jedisPools.add(buildSentinelPool(masterName, connectionInfo, config));
      }
    }
    return jedisPools;
  }
  
  private JedisPool buildDirectPool(String hostAndPort, ConnectionInfo connectionInfo, JedisPoolConfig config)
  {
    logger.info("Building JedisDirectPool, on redis server {}", hostAndPort);
    String[] hostPort = hostAndPort.split(":");
    HostAndPort masterAddress = new HostAndPort(hostPort[0], Integer.parseInt(hostPort[1]));
    return new JedisDirectPool(this.poolName, masterAddress, connectionInfo, config);
  }
  
  private JedisPool buildSentinelPool(String masterName, ConnectionInfo connectionInfo, JedisPoolConfig config)
  {
    logger.info("Building JedisSentinelPool, on sentinel sentinelHosts:" + Arrays.toString(this.sentinelHostAndPorts) + ", masterName is " + masterName);
    

    HostAndPort[] sentinelAddresses = new HostAndPort[this.sentinelHostAndPorts.length];
    for (int i = 0; i < this.sentinelHostAndPorts.length; i++)
    {
      String[] hostPort = this.sentinelHostAndPorts[i].split(":");
      sentinelAddresses[i] = new HostAndPort(hostPort[0], Integer.parseInt(hostPort[1]));
    }
    return new JedisSentinelPool(this.poolName, sentinelAddresses, masterName, connectionInfo, config);
  }
  
  private boolean isDirect()
  {
    return (this.hostAndPorts != null) && (this.hostAndPorts.length > 0);
  }
}
