package org.springside.modules.nosql.redis.pool;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;

public class JedisFactory
  implements PooledObjectFactory<Jedis>
{
  private final String host;
  private final int port;
  private final int timeout;
  private final String password;
  private final int database;
  private final String clientName;
  
  public JedisFactory(String host, int port, int timeout, String password, int database)
  {
    this(host, port, timeout, password, database, null);
  }
  
  public JedisFactory(String host, int port, int timeout, String password, int database, String clientName)
  {
    this.host = host;
    this.port = port;
    this.timeout = timeout;
    this.password = password;
    this.database = database;
    this.clientName = clientName;
  }
  
  public void activateObject(PooledObject<Jedis> pooledJedis)
    throws Exception
  {
    BinaryJedis jedis = (BinaryJedis)pooledJedis.getObject();
    if (jedis.getDB().longValue() != this.database) {
      jedis.select(this.database);
    }
  }
  
  public void destroyObject(PooledObject<Jedis> pooledJedis)
    throws Exception
  {
    BinaryJedis jedis = (BinaryJedis)pooledJedis.getObject();
    if (jedis.isConnected()) {
      try
      {
        try
        {
          jedis.quit();
        }
        catch (Exception e) {}
        jedis.disconnect();
      }
      catch (Exception e) {}
    }
  }
  
  public PooledObject<Jedis> makeObject()
    throws Exception
  {
    Jedis jedis = new Jedis(this.host, this.port, this.timeout);
    
    jedis.connect();
    if (null != this.password) {
      jedis.auth(this.password);
    }
    if (this.database != 0) {
      jedis.select(this.database);
    }
    if (this.clientName != null) {
      jedis.clientSetname(this.clientName);
    }
    return new DefaultPooledObject(jedis);
  }
  
  public void passivateObject(PooledObject<Jedis> pooledJedis)
    throws Exception
  {}
  
  public boolean validateObject(PooledObject<Jedis> pooledJedis)
  {
    BinaryJedis jedis = (BinaryJedis)pooledJedis.getObject();
    try
    {
      return (jedis.isConnected()) && (jedis.ping().equals("PONG"));
    }
    catch (Exception e) {}
    return false;
  }
}
