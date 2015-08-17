package uap.web.file.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.hadoop.mapred.JobConf;
import uap.web.file.hadoop.hdfsDao;

public class hdfsservlet
{
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    request.setCharacterEncoding("UTF-8");
    if (request.getParameter("func").equals("delete"))
    {
      JobConf conf = hdfsDao.config();
      hdfsDao hdfs = new hdfsDao(conf);
      hdfs.deleteFile(request.getParameter("path"));
      request.getRequestDispatcher("/uploadsuccess.jsp").forward(request, response);
    }
    if (request.getParameter("func").equals("download"))
    {
      JobConf conf = hdfsDao.config();
      hdfsDao hdfs = new hdfsDao(conf);
      hdfs.download(request.getParameter("spath"), request.getParameter("dpath"));
      
      request.getRequestDispatcher("/uploadsuccess.jsp").forward(request, response);
    }
  }
  
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    request.setCharacterEncoding("UTF-8");
    if (request.getParameter("func").equals("upload")) {
      upload(request, response);
    }
  }
  
  private void upload(HttpServletRequest request, HttpServletResponse response)
    throws ServletException
  {
    int maxFileSize = 52428800;
    int maxMemSize = 52428800;
    String filePath = "/home/hadoop/tmpdata/";
    System.out.println("source file path:" + filePath + "");
    
    String contentType = request.getContentType();
    if (contentType.indexOf("multipart/form-data") >= 0)
    {
      DiskFileItemFactory factory = new DiskFileItemFactory();
      
      factory.setSizeThreshold(maxMemSize);
      
      factory.setRepository(new File("/home/tmp"));
      

      ServletFileUpload upload = new ServletFileUpload(factory);
      
      upload.setSizeMax(maxFileSize);
      try
      {
        List fileItems = upload.parseRequest(request);
        

        Iterator i = fileItems.iterator();
        
        System.out.println("begin to upload file to tomcat server</p>");
        while (i.hasNext())
        {
          FileItem fi = (FileItem)i.next();
          if (!fi.isFormField())
          {
            String fieldName = fi.getFieldName();
            String fileName = fi.getName();
            
            String fn = fileName.substring(fileName.lastIndexOf("\\") + 1);
            
            System.out.println(fn);
            boolean isInMemory = fi.isInMemory();
            long sizeInBytes = fi.getSize();
            File file;
            File file;
            if (fileName.lastIndexOf("\\") >= 0) {
              file = new File(filePath, fn);
            } else {
              file = new File(filePath, fn);
            }
            fi.write(file);
            System.out.println("upload file to tomcat server success!");
            

            System.out.println("begin to upload file to hadoop hdfs");
            


            JobConf conf = hdfsDao.config();
            hdfsDao hdfs = new hdfsDao(conf);
            hdfs.copyFile(filePath + "/" + fn, "/user1/" + fn);
            System.out.println("upload file to hadoop hdfs success!");
            if (file.exists()) {
              file.delete();
            }
            request.getRequestDispatcher("/uploadsuccess.jsp").forward(request, response);
          }
        }
      }
      catch (Exception ex)
      {
        System.out.println(ex);
      }
    }
    else
    {
      System.out.println("No file uploaded");
    }
  }
}
