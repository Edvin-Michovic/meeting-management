package dev.edvinmichovic.meetingmanagement.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleInvalidArgument (MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(exception -> errorMap.put(exception.getField(), exception.getDefaultMessage()));
        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingArgument (MissingServletRequestParameterException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResponseStatusException.class)
    public String handleNotFoundArgument (ResponseStatusException ex) {
        return ex.getReason();
    }

   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ExceptionHandler(ConstraintViolationException.class)
   public String handleConstraintViolation (ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("\n"));
   }

   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ExceptionHandler(MethodArgumentTypeMismatchException.class)
   public String handleMismatchException (MethodArgumentTypeMismatchException ex) {
        MethodParameter parameter = ex.getParameter();
        String parameterName = parameter.getParameterName();
        Class<?> requiredType = parameter.getParameterType();
        Object incorrectValue = ex.getValue();
        return "Invalid value for parameter '" + parameterName + "'. Expected type: " + requiredType.getSimpleName() + ". Actual value: " + incorrectValue;
   }

}
