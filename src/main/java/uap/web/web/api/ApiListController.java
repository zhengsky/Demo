package uap.web.web.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/api"})
public class ApiListController
{
  @RequestMapping(method={org.springframework.web.bind.annotation.RequestMethod.GET})
  public String list()
  {
    return "api/list";
  }
}
