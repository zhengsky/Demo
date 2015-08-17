package org.springside.modules.security.shiro;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.tags.PermissionTag;

public class HasAnyPermissionsTag
  extends PermissionTag
{
  private static final long serialVersionUID = -4786931833148680306L;
  private static final String PERMISSION_NAMES_DELIMETER = ",";
  
  protected boolean showTagBody(String permissionNames)
  {
    boolean hasAnyPermission = false;
    
    Subject subject = getSubject();
    if (subject != null) {
      for (String permission : permissionNames.split(",")) {
        if (subject.isPermitted(permission.trim()))
        {
          hasAnyPermission = true;
          break;
        }
      }
    }
    return hasAnyPermission;
  }
}
