package uap.web.file;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uap.web.utils.PropertyUtil;

public class GridFSUtil
{
  public static Logger logger = LoggerFactory.getLogger(GridFSUtil.class);
  public static String mongoDbName = "ec";
  public static String FILE_ATTR_NAME = "filename";
  
  public static void saveFile(byte[] contents, String fileName)
    throws Exception
  {
    String mongoHost = PropertyUtil.getPropertyByKey("mongodbhost");
    

    Mongo mongo = new MongoClient(mongoHost);
    
    DB db = mongo.getDB(mongoDbName);
    

    GridFS gridFS = null;
    gridFS = new GridFS(db);
    
    GridFSInputFile mongofile = gridFS.createFile(new ByteArrayInputStream(contents), fileName);
    

    mongofile.save();
    
    mongo.close();
  }
  
  public static void saveFile(File saveFile)
    throws Exception
  {
    String mongoHost = PropertyUtil.getPropertyByKey("mongodbhost");
    

    Mongo mongo = new MongoClient(mongoHost);
    
    DB db = mongo.getDB(mongoDbName);
    

    GridFS gridFS = null;
    gridFS = new GridFS(db);
    
    GridFSInputFile mongofile = gridFS.createFile(saveFile);
    

    mongofile.save();
    
    mongo.close();
  }
  
  public static byte[] readFileContents(String readFileName)
    throws Exception
  {
    String mongoHost = PropertyUtil.getPropertyByKey("mongodbhost");
    
    Mongo mongo = new MongoClient(mongoHost);
    
    DB db = mongo.getDB(mongoDbName);
    GridFS gridFs = null;
    gridFs = new GridFS(db);
    

    DBObject query = new BasicDBObject();
    query.put(FILE_ATTR_NAME, readFileName);
    
    GridFSDBFile gridDBFile = gridFs.findOne(query);
    if (gridDBFile == null) {
      return null;
    }
    String fileName = (String)gridDBFile.get(FILE_ATTR_NAME);
    logger.debug("从Mongodb获得文件名为：" + fileName);
    
    ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
    
    byte[] buff = new byte[100];
    int rc = 0;
    InputStream in = gridDBFile.getInputStream();
    while ((rc = in.read(buff, 0, 100)) > 0) {
      swapStream.write(buff, 0, rc);
    }
    byte[] contents = swapStream.toByteArray();
    in.close();
    swapStream.close();
    mongo.close();
    
    return contents;
  }
  
  public static File readFile(String readFileName)
    throws Exception
  {
    File writeFile = new File(readFileName);
    if (!writeFile.exists()) {
      writeFile.createNewFile();
    }
    String mongoHost = PropertyUtil.getPropertyByKey("mongodbhost");
    
    Mongo mongo = new MongoClient(mongoHost);
    
    DB db = mongo.getDB(mongoDbName);
    GridFS gridFs = null;
    gridFs = new GridFS(db);
    

    DBObject query = new BasicDBObject();
    query.put(FILE_ATTR_NAME, readFileName);
    
    GridFSDBFile gridDBFile = gridFs.findOne(query);
    if (gridDBFile == null) {
      return null;
    }
    String fileName = (String)gridDBFile.get(FILE_ATTR_NAME);
    logger.debug("从Mongodb获得文件名为：" + fileName);
    gridDBFile.writeTo(writeFile);
    mongo.close();
    
    return writeFile;
  }
  
  public static void main(String[] args)
  {
    try
    {
      File dir = new File("d:\\ecfile");
      if ((dir.exists()) && (dir.isDirectory()))
      {
        File[] files = dir.listFiles();
        
        Mongo mongo = new MongoClient("20.12.6.69");
        
        DB db = mongo.getDB(mongoDbName);
        
        GridFS gridFS = null;
        gridFS = new GridFS(db);
        for (File file : files)
        {
          GridFSInputFile mongofile = gridFS.createFile(file);
          
          mongofile.save();
        }
        mongo.close();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
