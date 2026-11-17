package dev.portfolio.incident;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestControllerAdvice public class ErrorHandler {
 @ExceptionHandler(ApiException.class) ResponseEntity<Map<String,String>> expected(ApiException ex){return ResponseEntity.status(ex.status).body(Map.of("error",ex.getMessage()));}
 @ExceptionHandler(OptimisticLockingFailureException.class) ResponseEntity<Map<String,String>> collision(Exception ex){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Concurrent update; reload and retry"));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,String>> invalid(Exception ex){return ResponseEntity.badRequest().body(Map.of("error","Invalid request fields"));}
}
