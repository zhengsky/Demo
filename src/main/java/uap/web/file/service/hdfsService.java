package uap.web.file.service;

import java.io.IOException;
import org.apache.hadoop.mapred.JobConf;
import org.springframework.stereotype.Service;
import uap.web.file.hadoop.hdfsDao;

@Service
public class hdfsService
{
  public void deleteFile(String file)
  {
    JobConf conf = hdfsDao.config();
    hdfsDao hdfs = new hdfsDao(conf);
    try
    {
      hdfs.deleteFile(file);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public void mkdir(String dir)
  {
    JobConf conf = hdfsDao.config();
    hdfsDao hdfs = new hdfsDao(conf);
    try
    {
      hdfs.mkdir(dir);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public void copyFile(String s, String d)
  {
    JobConf conf = hdfsDao.config();
    hdfsDao hdfs = new hdfsDao(conf);
    try
    {
      hdfs.copyFile(s, d);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public void download(String s, String d)
  {
    JobConf conf = hdfsDao.config();
    hdfsDao hdfs = new hdfsDao(conf);
    try
    {
      hdfs.download(s, d);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
