package org.springside.modules.persistence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.SQLServer2008Dialect;

public class Hibernates
{
  public static void initLazyProperty(Object proxyedPropertyValue)
  {
    Hibernate.initialize(proxyedPropertyValue);
  }
  
  public static String getDialect(DataSource dataSource)
  {
    String jdbcUrl = getJdbcUrlFromDataSource(dataSource);
    if (StringUtils.contains(jdbcUrl, ":h2:")) {
      return H2Dialect.class.getName();
    }
    if (StringUtils.contains(jdbcUrl, ":mysql:")) {
      return MySQL5InnoDBDialect.class.getName();
    }
    if (StringUtils.contains(jdbcUrl, ":oracle:")) {
      return Oracle10gDialect.class.getName();
    }
    if (StringUtils.contains(jdbcUrl, ":postgresql:")) {
      return PostgreSQL82Dialect.class.getName();
    }
    if (StringUtils.contains(jdbcUrl, ":sqlserver:")) {
      return SQLServer2008Dialect.class.getName();
    }
    throw new IllegalArgumentException("Unknown Database of " + jdbcUrl);
  }
  
  private static String getJdbcUrlFromDataSource(DataSource dataSource)
  {
    Connection connection = null;
    try
    {
      connection = dataSource.getConnection();
      if (connection == null) {
        throw new IllegalStateException("Connection returned by DataSource [" + dataSource + "] was null");
      }
      return connection.getMetaData().getURL();
    }
    catch (SQLException e)
    {
      throw new RuntimeException("Could not get database url", e);
    }
    finally
    {
      if (connection != null) {
        try
        {
          connection.close();
        }
        catch (SQLException e) {}
      }
    }
  }
}
