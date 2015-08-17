/*  1:   */ package uap.web.service;
/*  2:   */ 
/*  3:   */ public class ServiceException
/*  4:   */   extends RuntimeException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 3583566093089790852L;
/*  7:   */   
/*  8:   */   public ServiceException() {}
/*  9:   */   
/* 10:   */   public ServiceException(String message)
/* 11:   */   {
/* 12:19 */     super(message);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public ServiceException(Throwable cause)
/* 16:   */   {
/* 17:23 */     super(cause);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public ServiceException(String message, Throwable cause)
/* 21:   */   {
/* 22:27 */     super(message, cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\zhengwsa\Desktop\platform-0.0.1-SNAPSHOT.jar
 * Qualified Name:     uap.web.service.ServiceException
 * JD-Core Version:    0.7.0.1
 */