package uap.web.search.solr;

import java.lang.reflect.Field;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrIndexService
{
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private String solrServerUrl;
  
  public String getSolrServerUrl()
  {
    return this.solrServerUrl;
  }
  
  public void setSolrServerUrl(String solrServerUrl)
  {
    this.solrServerUrl = solrServerUrl;
  }
  
  public void updateIndex(JoinPoint jp)
  {
    this.logger.info("被代理方法名字：" + jp.getSignature().getName());
    this.logger.info("被代理方法参数：" + jp.getArgs());
    this.logger.info("被代理对象：" + jp.getTarget());
    
    Object object = null;
    if (jp.getArgs().length > 0) {
      object = jp.getArgs()[0];
    }
    if (object == null) {
      return;
    }
    try
    {
      HttpSolrServer server = new HttpSolrServer(this.solrServerUrl);
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("id", getAttrValueByKey(object, "id"));
      doc.addField("title", getAttrValueByKey(object, "title"));
      doc.addField("url", getAttrValueByKey(object, "imgLarge"));
      doc.addField("rtype", "goods");
      doc.addField("num", getAttrValueByKey(object, "goodsNumber"));
      doc.addField("last_modified", getAttrValueByKey(object, "gmtCreate"));
      doc.addField("catid", getAttrValueByKey(object, "catCode"));
      doc.addField("memo", getAttrValueByKey(object, "goodsDesc"));
      doc.addField("shopid", "10001");
      doc.addField("shopname", "我的测试店铺");
      if ((doc.getField("id") != null) && (doc.getField("title") != null))
      {
        server.add(doc);
        server.commit();
        this.logger.info("实体保存后更新索引，名称为：" + doc.getField("title") + ".");
      }
      else
      {
        this.logger.error("实体未找到构建索引所需要的属性!");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.logger.error("更新索引失败.");
    }
  }
  
  private String getAttrValueByKey(Object object, String attrKey)
  {
    Field[] fields = object.getClass().getDeclaredFields();
    String[] name = new String[fields.length];
    Object[] value = new Object[fields.length];
    String result = null;
    try
    {
      Field.setAccessible(fields, true);
      for (int i = 0; i < name.length; i++)
      {
        name[i] = fields[i].getName();
        value[i] = fields[i].get(object);
        if ((attrKey.equals(name[i])) && (value[i] != null))
        {
          result = String.valueOf(value[i]);
          break;
        }
      }
    }
    catch (Exception e)
    {
      this.logger.error("获取属性值失败，属性名称是：" + attrKey + ".");
    }
    return result;
  }
}
