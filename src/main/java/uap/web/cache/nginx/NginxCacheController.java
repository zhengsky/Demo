package uap.web.cache.nginx;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NginxCacheController
{
  private static final String TRUE = "true";
  private static final String SECRET_HEADER = "secret-header";
  private String[] servers;
  private static final int TIMEOUT_SECONDS = 20;
  private static final int POOL_SIZE = 20;
  private static Logger logger = LoggerFactory.getLogger(NginxCacheController.class);
  private CloseableHttpClient httpClient;
  
  public NginxCacheController(String servers)
  {
    this.servers = servers.split(",");
  }
  
  public void remove(String url)
  {
    for (String server : this.servers) {
      remove(server, url);
    }
  }
  
  private void remove(String server, String url)
  {
    String contentUrl = server + url;
    HttpGet httpGet = new HttpGet(contentUrl);
    httpGet.addHeader("secret-header", "true");
    
    httpGet.addHeader("Accept", "text/html");
    CloseableHttpResponse remoteResponse = null;
    try
    {
      remoteResponse = this.httpClient.execute(httpGet);
      int statusCode = remoteResponse.getStatusLine().getStatusCode();
      if (statusCode >= 400) {
        logger.error("error statusCode :" + statusCode + ";clear nginx cache error from " + contentUrl);
      }
    }
    catch (Exception e)
    {
      logger.error("clear nginx cache error from " + contentUrl, e);
    }
    finally
    {
      IOUtils.closeQuietly(remoteResponse);
    }
  }
  
  public void init()
  {
    initApacheHttpClient();
  }
  
  public void destroy()
  {
    destroyApacheHttpClient();
  }
  
  private void initApacheHttpClient()
  {
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(20000).setConnectTimeout(20000).build();
    


    this.httpClient = HttpClientBuilder.create().setMaxConnTotal(20).setMaxConnPerRoute(20).setDefaultRequestConfig(requestConfig).build();
  }
  
  private void destroyApacheHttpClient()
  {
    try
    {
      this.httpClient.close();
    }
    catch (IOException e)
    {
      logger.error("httpclient close fail", e);
    }
  }
}
