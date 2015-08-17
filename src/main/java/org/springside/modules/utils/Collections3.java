package org.springside.modules.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

public class Collections3
{
  public static Map extractToMap(Collection collection, String keyPropertyName, String valuePropertyName)
  {
    Map map = new HashMap(collection.size());
    try
    {
      for (Object obj : collection) {
        map.put(PropertyUtils.getProperty(obj, keyPropertyName), PropertyUtils.getProperty(obj, valuePropertyName));
      }
    }
    catch (Exception e)
    {
      throw Reflections.convertReflectionExceptionToUnchecked(e);
    }
    return map;
  }
  
  public static List extractToList(Collection collection, String propertyName)
  {
    List list = new ArrayList(collection.size());
    try
    {
      for (Object obj : collection) {
        list.add(PropertyUtils.getProperty(obj, propertyName));
      }
    }
    catch (Exception e)
    {
      throw Reflections.convertReflectionExceptionToUnchecked(e);
    }
    return list;
  }
  
  public static String extractToString(Collection collection, String propertyName, String separator)
  {
    List list = extractToList(collection, propertyName);
    return StringUtils.join(list, separator);
  }
  
  public static String convertToString(Collection collection, String separator)
  {
    return StringUtils.join(collection, separator);
  }
  
  public static String convertToString(Collection collection, String prefix, String postfix)
  {
    StringBuilder builder = new StringBuilder();
    for (Object o : collection) {
      builder.append(prefix).append(o).append(postfix);
    }
    return builder.toString();
  }
  
  public static boolean isEmpty(Collection collection)
  {
    return (collection == null) || (collection.isEmpty());
  }
  
  public static boolean isEmpty(Map map)
  {
    return (map == null) || (map.isEmpty());
  }
  
  public static boolean isNotEmpty(Collection collection)
  {
    return (collection != null) && (!collection.isEmpty());
  }
  
  public static <T> T getFirst(Collection<T> collection)
  {
    if (isEmpty(collection)) {
      return null;
    }
    return collection.iterator().next();
  }
  
  public static <T> T getLast(Collection<T> collection)
  {
    if (isEmpty(collection)) {
      return null;
    }
    if ((collection instanceof List))
    {
      List<T> list = (List)collection;
      return list.get(list.size() - 1);
    }
    Iterator<T> iterator = collection.iterator();
    for (;;)
    {
      T current = iterator.next();
      if (!iterator.hasNext()) {
        return current;
      }
    }
  }
  
  public static <T> List<T> union(Collection<T> a, Collection<T> b)
  {
    List<T> result = new ArrayList(a);
    result.addAll(b);
    return result;
  }
  
  public static <T> List<T> subtract(Collection<T> a, Collection<T> b)
  {
    List<T> list = new ArrayList(a);
    for (T element : b) {
      list.remove(element);
    }
    return list;
  }
  
  public static <T> List<T> intersection(Collection<T> a, Collection<T> b)
  {
    List<T> list = new ArrayList();
    for (T element : a) {
      if (b.contains(element)) {
        list.add(element);
      }
    }
    return list;
  }
}
