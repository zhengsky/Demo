package uap.web.rest;

import java.util.Map;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springside.modules.beanvalidator.BeanValidators;
import org.springside.modules.mapper.JsonMapper;

@ControllerAdvice
public class RestExceptionHandler
  extends ResponseEntityExceptionHandler
{
  private JsonMapper jsonMapper = new JsonMapper();
  
  @ExceptionHandler({RestException.class})
  public final ResponseEntity<?> handleException(RestException ex, WebRequest request)
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/plain; charset=UTF-8"));
    return handleExceptionInternal(ex, ex.getMessage(), headers, ex.status, request);
  }
  
  @ExceptionHandler({ConstraintViolationException.class})
  public final ResponseEntity<?> handleException(ConstraintViolationException ex, WebRequest request)
  {
    Map<String, String> errors = BeanValidators.extractPropertyAndMessage(ex.getConstraintViolations());
    String body = this.jsonMapper.toJson(errors);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/plain; charset=UTF-8"));
    return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
  }
}
