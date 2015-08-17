package uap.web.file.hadoop;

import java.io.IOException;
import java.io.PrintStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;

public class hdfsDao
{
  private Configuration conf;
  private String hdfsPath;
  
  public hdfsDao(Configuration conf)
  {
    this.conf = conf;
  }
  
  public hdfsDao(String hdfs, Configuration conf)
  {
    this.hdfsPath = hdfs;
    this.conf = conf;
  }
  
  public static JobConf config()
  {
    JobConf conf = new JobConf(hdfsDao.class);
    conf.setJobName("HdfsDAO");
    conf.addResource(new Path("/usr/local/hadoop/conf/core-site.xml"));
    conf.addResource(new Path("/usr/local/hadoop/conf/mapred-site.xml"));
    conf.addResource(new Path("/usr/local/hadoop/conf/hdfs-site.xml"));
    return conf;
  }
  
  public void mkdir(String dir)
    throws IOException
  {
    FileSystem hdfs = FileSystem.get(this.conf);
    
    hdfs.mkdirs(new Path(dir));
    
    hdfs.close();
  }
  
  public void deleteFile(String folder)
    throws IOException
  {
    Path path = new Path(folder);
    FileSystem fs = FileSystem.get(this.conf);
    fs.deleteOnExit(path);
    System.out.println("Delete: " + folder);
    fs.close();
  }
  
  public void copyFile(String s, String d)
    throws IOException
  {
    FileSystem hdfs = FileSystem.get(this.conf);
    
    Path src = new Path(s);
    Path dst = new Path(d);
    
    hdfs.copyFromLocalFile(src, dst);
    
    hdfs.close();
  }
  
  public void download(String remote, String local)
    throws IOException
  {
    Path path = new Path(remote);
    FileSystem fs = FileSystem.get(this.conf);
    fs.copyToLocalFile(path, new Path(local));
    System.out.println("download: from" + remote + " to " + local);
    fs.close();
  }
}
