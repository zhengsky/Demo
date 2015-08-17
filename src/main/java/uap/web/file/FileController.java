package uap.web.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Controller
@RequestMapping({"/file"})
public class FileController
{
  public static Logger logger = LoggerFactory.getLogger(FileController.class);
  public static String fileDir = "D:\\ecfile\\";
  
  static
  {
    Properties prop = new Properties();
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
    try
    {
      prop.load(in);
      fileDir = prop.getProperty("fileDir").trim();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      logger.error("初始化文件服务器出错!", e);
    }
  }
  
  @RequestMapping(value={"upload/file"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public String uploadFile(HttpServletRequest request)
  {
    String fileName = "" + System.currentTimeMillis();
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    if (multipartResolver.isMultipart(request))
    {
      ShiroHttpServletRequest shiroRequest = (ShiroHttpServletRequest)request;
      MultipartHttpServletRequest multiRequest = multipartResolver.resolveMultipart((HttpServletRequest)shiroRequest.getRequest());
      Iterator<String> iter = multiRequest.getFileNames();
      String basePath = fileDir;
      File fileDir = new File(basePath);
      if (!fileDir.exists()) {
        fileDir.mkdirs();
      }
      while (iter.hasNext())
      {
        MultipartFile file = multiRequest.getFile(((String)iter.next()).toString());
        if (file != null)
        {
          String path = basePath + fileName;
          try
          {
            file.transferTo(new File(path));
          }
          catch (IllegalStateException e)
          {
            logger.error("上传文件错误", e);
            throw new RuntimeException();
          }
          catch (IOException e)
          {
            logger.error("上传文件错误", e);
            throw new RuntimeException();
          }
        }
      }
    }
    return fileName;
  }
  
  @RequestMapping(value={"upload/image"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public String uploadImage(HttpServletRequest request)
  {
    String fileName = "" + System.currentTimeMillis();
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    if (multipartResolver.isMultipart(request))
    {
      ShiroHttpServletRequest shiroRequest = (ShiroHttpServletRequest)request;
      MultipartHttpServletRequest multiRequest = multipartResolver.resolveMultipart((HttpServletRequest)shiroRequest.getRequest());
      Iterator<String> iter = multiRequest.getFileNames();
      while (iter.hasNext())
      {
        MultipartFile file = multiRequest.getFile(((String)iter.next()).toString());
        if (file != null) {
          try
          {
            GridFSUtil.saveFile(file.getBytes(), fileName);
          }
          catch (IllegalStateException e)
          {
            logger.error("上传文件错误", e);
            throw new RuntimeException(e);
          }
          catch (IOException e)
          {
            logger.error("上传文件错误", e);
            throw new RuntimeException(e);
          }
          catch (Exception e)
          {
            logger.error("上传文件到MongoDB错误", e);
            throw new RuntimeException(e);
          }
        }
      }
    }
    return fileName;
  }
  
  @RequestMapping(value={"down/image/{id}"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  public void downLoadImage(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response)
  {
    response.setContentType("image/png;charset=utf-8");
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    try
    {
      byte[] contents = GridFSUtil.readFileContents(id);
      bis = new BufferedInputStream(new ByteArrayInputStream(contents));
      bos = new BufferedOutputStream(response.getOutputStream());
      byte[] buff = new byte[2048];
      int bytesRead;
      while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
        bos.write(buff, 0, bytesRead);
      }
      return;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (bis != null) {
        try
        {
          bis.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
      if (bos != null) {
        try
        {
          bos.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
  }
  
  @RequestMapping(value={"upload/image_fs"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public String uploadImage_fs(HttpServletRequest request)
  {
    String fileName = "" + System.currentTimeMillis();
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    if (multipartResolver.isMultipart(request))
    {
      ShiroHttpServletRequest shiroRequest = (ShiroHttpServletRequest)request;
      MultipartHttpServletRequest multiRequest = multipartResolver.resolveMultipart((HttpServletRequest)shiroRequest.getRequest());
      Iterator<String> iter = multiRequest.getFileNames();
      String basePath = fileDir;
      File fileDir = new File(basePath);
      if (!fileDir.exists()) {
        fileDir.mkdirs();
      }
      while (iter.hasNext())
      {
        MultipartFile file = multiRequest.getFile(((String)iter.next()).toString());
        if (file != null)
        {
          String path = basePath + fileName;
          try
          {
            file.transferTo(new File(path));
          }
          catch (IllegalStateException e)
          {
            logger.error("上传文件错误", e);
            throw new RuntimeException();
          }
          catch (IOException e)
          {
            logger.error("上传文件错误", e);
            throw new RuntimeException();
          }
        }
      }
    }
    return fileName;
  }
  
  @RequestMapping(value={"down/image_fs/{id}"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  public void downLoadImageFs(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response)
  {
    response.setContentType("image/png;charset=utf-8");
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    String downLoadPath = fileDir + id;
    try
    {
      bis = new BufferedInputStream(new FileInputStream(downLoadPath));
      
      bos = new BufferedOutputStream(response.getOutputStream());
      byte[] buff = new byte[2048];
      int bytesRead;
      while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
        bos.write(buff, 0, bytesRead);
      }
      return;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (bis != null) {
        try
        {
          bis.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
      if (bos != null) {
        try
        {
          bos.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
  }
}
