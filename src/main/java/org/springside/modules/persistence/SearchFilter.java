package org.springside.modules.persistence;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

public class SearchFilter
{
  public String fieldName;
  public Object value;
  public Operator operator;
  
  public static enum Operator
  {
    EQ,  LIKE,  GT,  LT,  GTE,  LTE;
    
    private Operator() {}
  }
  
  public SearchFilter(String fieldName, Operator operator, Object value)
  {
    this.fieldName = fieldName;
    this.value = value;
    this.operator = operator;
  }
  
  public static Map<String, SearchFilter> parse(Map<String, Object> searchParams)
  {
    Map<String, SearchFilter> filters = Maps.newHashMap();
    for (Map.Entry<String, Object> entry : searchParams.entrySet())
    {
      String key = (String)entry.getKey();
      Object value = entry.getValue();
      if (!StringUtils.isBlank((String)value))
      {
        String[] names = StringUtils.split(key, "_");
        if (names.length != 2) {
          throw new IllegalArgumentException(key + " is not a valid search filter name");
        }
        String filedName = names[1];
        Operator operator = Operator.valueOf(names[0]);
        

        SearchFilter filter = new SearchFilter(filedName, operator, value);
        filters.put(key, filter);
      }
    }
    return filters;
  }
}
