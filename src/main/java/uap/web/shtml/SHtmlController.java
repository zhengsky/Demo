package uap.web.shtml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Controller
@RequestMapping({"/*/*.shtml"})
public class SHtmlController
{
  Map<String, Properties> props = new HashMap();
  @Autowired
  StaticHtmlBuilder hb;
  
  public SHtmlController()
  {
    Logger logger = LoggerFactory.getLogger(getClass());
    ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
    try
    {
      Resource[] ress = patternResolver.getResources("classpath*:/shtml/*.properties");
      for (Resource res : ress) {
        if (res.exists())
        {
          Properties prop = new Properties();
          InputStream inStream = null;
          try
          {
            inStream = res.getInputStream();
            prop.load(inStream);
            String name = res.getFile().getName();
            name = name.replace(".properties", "");
            this.props.put(name, prop);
          }
          catch (Exception e) {}finally
          {
            IOUtils.closeQuietly(inStream);
          }
        }
      }
    }
    catch (IOException e)
    {
      logger.error("scan html propitres error", e);
    }
  }
  
  @RequestMapping(method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public String index(HttpServletRequest request)
  {
    String url = request.getServletPath();
    String[] paths = url.split("/");
    String prefix = paths[1];
    paths[1] = null;
    String id = StringUtils.join(paths, "");
    id = id.substring(0, id.length() - 6);
    Properties prop = (Properties)this.props.get(prefix);
    String provideName = (String)prop.get("provide");
    String view = (String)prop.get("view");
    WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());
    ISHtmlProvider provide = (ISHtmlProvider)wac.getBean(provideName, ISHtmlProvider.class);
    if (provide != null)
    {
      String html = this.hb.render(prefix, url, view, provide);
      return html;
    }
    return "<title>PageNoteFound</title>PageNoteFound";
  }
}
