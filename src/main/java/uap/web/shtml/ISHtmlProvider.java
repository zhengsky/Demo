package uap.web.shtml;

import java.util.Map;

public abstract interface ISHtmlProvider
{
  public abstract Map getViewModel(String paramString);
}
