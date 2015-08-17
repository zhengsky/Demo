package uap.web.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil
{
  private static Properties prop = null;
  
  static
  {
    prop = new Properties();
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
    try
    {
      prop.load(in);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public static String getPropertyByKey(String key)
  {
    return prop.getProperty(key).trim();
  }
}
