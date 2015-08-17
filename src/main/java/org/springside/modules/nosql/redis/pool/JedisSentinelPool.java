package org.springside.modules.nosql.redis.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.nosql.redis.JedisTemplate;
import org.springside.modules.nosql.redis.JedisTemplate.JedisAction;
import org.springside.modules.nosql.redis.JedisUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

public final class JedisSentinelPool
  extends JedisPool
{
  private static final String NO_ADDRESS_YET = "I dont know because no sentinel up";
  private static Logger logger = LoggerFactory.getLogger(JedisSentinelPool.class);
  private List<JedisPool> sentinelPools = new ArrayList();
  private MasterSwitchListener masterSwitchListener;
  private String masterName;
  private JedisPoolConfig masterPoolConfig;
  private ConnectionInfo masterConnectionInfo;
  private CountDownLatch poolInit = new CountDownLatch(1);
  
  public JedisSentinelPool(String poolName, HostAndPort[] sentinelAddresses, String masterName, ConnectionInfo masterConnectionInfo, JedisPoolConfig masterPoolConfig)
  {
    this.poolName = poolName;
    
    assertArgument((sentinelAddresses == null) || (sentinelAddresses.length == 0), "seintinelInfos is not set");
    for (HostAndPort sentinelAddress : sentinelAddresses)
    {
      JedisPool sentinelPool = new JedisDirectPool(poolName + "-sentinel", sentinelAddress, new JedisPoolConfig());
      this.sentinelPools.add(sentinelPool);
    }
    assertArgument(masterConnectionInfo == null, "masterConnectionInfo is not set");
    this.masterConnectionInfo = masterConnectionInfo;
    

    assertArgument((masterName == null) || (masterName.isEmpty()), "masterName is not set");
    this.masterName = masterName;
    

    assertArgument(masterPoolConfig == null, "masterPoolConfig is not set");
    this.masterPoolConfig = masterPoolConfig;
    

    this.masterSwitchListener = new MasterSwitchListener();
    this.masterSwitchListener.start();
    try
    {
      if (!this.poolInit.await(5L, TimeUnit.SECONDS)) {
        logger.warn("the sentiel pool can't not init in 5 seconds");
      }
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
    }
  }
  
  public JedisSentinelPool(String poolName, HostAndPort[] sentinelAddresses, String masterName, JedisPoolConfig masterPoolConfig)
  {
    this(poolName, sentinelAddresses, masterName, new ConnectionInfo(), masterPoolConfig);
  }
  
  public void destroy()
  {
    this.masterSwitchListener.shutdown();
    for (JedisPool sentinel : this.sentinelPools) {
      sentinel.destroy();
    }
    destroyInternelPool();
    try
    {
      logger.info("Waiting for MasterSwitchListener thread finish");
      this.masterSwitchListener.join();
      logger.info("MasterSwitchListener thread finished");
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
    }
  }
  
  protected void destroyInternelPool()
  {
    closeInternalPool();
    
    this.address = null;
    this.connectionInfo = null;
    this.internalPool = null;
  }
  
  private static void assertArgument(boolean expression, String message)
  {
    if (expression) {
      throw new IllegalArgumentException(message);
    }
  }
  
  public MasterSwitchListener getMasterSwitchListener()
  {
    return this.masterSwitchListener;
  }
  
  public class MasterSwitchListener
    extends Thread
  {
    public static final String THREAD_NAME_PREFIX = "MasterSwitchListener-";
    private JedisPubSub subscriber;
    private JedisPool sentinelPool;
    private Jedis sentinelJedis;
    private AtomicBoolean running = new AtomicBoolean(true);
    private HostAndPort previousMasterAddress;
    
    public MasterSwitchListener()
    {
      super();
    }
    
    public void shutdown()
    {
      this.running.getAndSet(false);
      interrupt();
      try
      {
        if (this.subscriber != null) {
          this.subscriber.unsubscribe();
        }
      }
      finally
      {
        JedisUtils.destroyJedis(this.sentinelJedis);
      }
    }
    
    public void run()
    {
      while (this.running.get()) {
        try
        {
          this.sentinelPool = pickupSentinel();
          if (this.sentinelPool != null)
          {
            HostAndPort masterAddress = queryMasterAddress();
            if ((JedisSentinelPool.this.internalPool != null) && (isAddressChange(masterAddress)))
            {
              JedisSentinelPool.logger.info("The internalPool {} had changed, destroy it now.", this.previousMasterAddress);
              JedisSentinelPool.this.destroyInternelPool();
            }
            if (JedisSentinelPool.this.internalPool == null)
            {
              JedisSentinelPool.logger.info("The internalPool {} is not init or the address had changed, init it now.", masterAddress);
              
              JedisSentinelPool.this.initInternalPool(masterAddress, JedisSentinelPool.this.masterConnectionInfo, JedisSentinelPool.this.masterPoolConfig);
              JedisSentinelPool.this.poolInit.countDown();
            }
            this.previousMasterAddress = masterAddress;
            
            this.sentinelJedis = ((Jedis)this.sentinelPool.getResource());
            this.subscriber = new MasterSwitchSubscriber(null);
            this.sentinelJedis.subscribe(this.subscriber, new String[] { "+switch-master", "+redirect-to-master" });
          }
          else
          {
            JedisSentinelPool.logger.info("All sentinels down, sleep 2 seconds and try to connect again.");
            if (JedisSentinelPool.this.internalPool == null)
            {
              HostAndPort masterAddress = new HostAndPort("I dont know because no sentinel up", 6379);
              JedisSentinelPool.this.initInternalPool(masterAddress, JedisSentinelPool.this.masterConnectionInfo, JedisSentinelPool.this.masterPoolConfig);
              this.previousMasterAddress = masterAddress;
            }
            sleep(2000);
          }
        }
        catch (JedisConnectionException e)
        {
          if (this.sentinelJedis != null) {
            this.sentinelPool.returnBrokenResource(this.sentinelJedis);
          }
          if (this.running.get())
          {
            JedisSentinelPool.logger.error("Lost connection with Sentinel " + this.sentinelPool.getAddress() + ", sleep 1 seconds and try to connect other one. ");
            
            sleep(1000);
          }
        }
        catch (Exception e)
        {
          JedisSentinelPool.logger.error(e.getMessage(), e);
          sleep(1000);
        }
      }
    }
    
    public HostAndPort getCurrentMasterAddress()
    {
      return this.previousMasterAddress;
    }
    
    private JedisPool pickupSentinel()
    {
      for (JedisPool pool : JedisSentinelPool.this.sentinelPools) {
        if (JedisUtils.ping(pool)) {
          return pool;
        }
      }
      return null;
    }
    
    private boolean isAddressChange(HostAndPort currentMasterAddress)
    {
      if (this.previousMasterAddress == null) {
        return true;
      }
      return !this.previousMasterAddress.equals(currentMasterAddress);
    }
    
    private HostAndPort queryMasterAddress()
    {
      JedisTemplate sentinelTemplate = new JedisTemplate(this.sentinelPool);
      List<String> address = (List)sentinelTemplate.execute(new JedisTemplate.JedisAction()
      {
        public List<String> action(Jedis jedis)
        {
          return jedis.sentinelGetMasterAddrByName(JedisSentinelPool.this.masterName);
        }
      });
      if ((address == null) || (address.isEmpty())) {
        throw new IllegalArgumentException("Master name " + JedisSentinelPool.this.masterName + " is not in sentinel.conf");
      }
      return new HostAndPort((String)address.get(0), Integer.valueOf((String)address.get(1)).intValue());
    }
    
    private void sleep(int millseconds)
    {
      try
      {
        Thread.sleep(millseconds);
      }
      catch (InterruptedException e1)
      {
        Thread.currentThread().interrupt();
      }
    }
    
    private class MasterSwitchSubscriber
      extends JedisPubSub
    {
      private MasterSwitchSubscriber() {}
      
      public void onMessage(String channel, String message)
      {
        JedisSentinelPool.logger.info("Sentinel " + JedisSentinelPool.MasterSwitchListener.this.sentinelPool.getAddress() + " published: " + message);
        String[] switchMasterMsg = message.split(" ");
        if (JedisSentinelPool.this.masterName.equals(switchMasterMsg[0]))
        {
          HostAndPort masterAddress = new HostAndPort(switchMasterMsg[3], Integer.parseInt(switchMasterMsg[4]));
          
          JedisSentinelPool.logger.info("Switch master to " + masterAddress);
          JedisSentinelPool.this.destroyInternelPool();
          JedisSentinelPool.this.initInternalPool(masterAddress, JedisSentinelPool.this.masterConnectionInfo, JedisSentinelPool.this.masterPoolConfig);
          JedisSentinelPool.MasterSwitchListener.this.previousMasterAddress = masterAddress;
        }
      }
      
      public void onPMessage(String pattern, String channel, String message) {}
      
      public void onSubscribe(String channel, int subscribedChannels) {}
      
      public void onUnsubscribe(String channel, int subscribedChannels) {}
      
      public void onPUnsubscribe(String pattern, int subscribedChannels) {}
      
      public void onPSubscribe(String pattern, int subscribedChannels) {}
    }
  }
}
