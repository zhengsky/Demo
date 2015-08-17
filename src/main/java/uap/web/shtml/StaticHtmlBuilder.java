package uap.web.shtml;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springside.modules.nosql.redis.JedisTemplate;

@Component
public class StaticHtmlBuilder
{
  protected final Log logger = LogFactory.getLog(getClass());
  @Autowired
  private JedisTemplate jedisTemplate;
  @Autowired
  FreeMarkerConfigurer fmCfg;
  String encoding = "UTF-8";
  
  public String render(String prefix, String url, String view, ISHtmlProvider provide)
  {
    String error = "template not found!" + view;
    String html = null;
    try
    {
      html = this.jedisTemplate.hget(prefix, url);
    }
    catch (Exception e) {}
    if (html == null)
    {
      StringWriter out = null;
      try
      {
        Locale locale = Locale.getDefault();
        boolean exist = checkResource(locale, view);
        if (!exist)
        {
          this.logger.error(error);
          return error;
        }
        String[] paths = url.split("/");
        paths[1] = null;
        String id = StringUtils.join(paths, "");
        id = id.substring(0, id.length() - 6);
        out = new StringWriter();
        Map viewModel = provide.getViewModel(id);
        getTemplate(view, locale).process(viewModel, out);
        html = out.toString();
        try
        {
          this.jedisTemplate.hset(prefix, url, html);
        }
        catch (Exception e) {}
      }
      catch (IOException e)
      {
        this.logger.error(error, e);
      }
      catch (TemplateException e)
      {
        this.logger.error("render error!" + view, e);
      }
      finally
      {
        IOUtils.closeQuietly(out);
      }
    }
    return html;
  }
  
  public boolean checkResource(Locale locale, String view)
    throws ApplicationContextException
  {
    try
    {
      getTemplate(view, locale);
      return true;
    }
    catch (FileNotFoundException ex)
    {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("No FreeMarker view found for URL: " + view);
      }
      return false;
    }
    catch (ParseException ex)
    {
      throw new ApplicationContextException("Failed to parse FreeMarker template for URL [" + view + "]", ex);
    }
    catch (IOException ex)
    {
      throw new ApplicationContextException("Could not load FreeMarker template for URL [" + view + "]", ex);
    }
  }
  
  protected Template getTemplate(String name, Locale locale)
    throws IOException
  {
    return getEncoding() != null ? getConfiguration().getTemplate(name, locale, getEncoding()) : getConfiguration().getTemplate(name, locale);
  }
  
  protected String getEncoding()
  {
    return this.encoding;
  }
  
  protected Configuration getConfiguration()
  {
    return this.fmCfg.getConfiguration();
  }
}
