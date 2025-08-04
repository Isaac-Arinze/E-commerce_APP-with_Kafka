package com.sky_ecommerce.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 400 - Malformed JSON
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String path = getPath(request);
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON Request",
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage(),
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 400 - @Valid body errors
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String path = getPath(request);
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Request validation failed",
                path,
                details
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 400 - @Validated on query params/path variables
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String path = getPath(request);
        List<String> details = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Constraint Violation",
                "One or more constraints failed",
                path,
                details
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 400 - Binding errors on query/form
    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex,
                                                         HttpHeaders headers,
                                                         HttpStatusCode status,
                                                         WebRequest request) {
        String path = getPath(request);
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Binding Failed",
                "Request binding failed",
                path,
                details
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 400 - Missing request parameter
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                         HttpHeaders headers,
                                                                         HttpStatusCode status,
                                                                         WebRequest request) {
        String path = getPath(request);
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Missing Parameter",
                ex.getParameterName() + " parameter is missing",
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 400 - Type mismatch (e.g., path variable cannot be parsed)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String path = getPath(request);
        String msg = "Parameter '" + ex.getName() + "' should be of type " +
                (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Type Mismatch",
                msg,
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 401/403 - Security
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        String path = getPath(request);
        ApiError body = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                ex.getMessage(),
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // 404 - Not found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        String path = getPath(request);
        ApiError body = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Entity Not Found",
                ex.getMessage(),
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Persistence and bad request family
    @ExceptionHandler({DataAccessException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleBadRequestFamily(RuntimeException ex, WebRequest request) {
        String path = getPath(request);
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Mail errors -> 503
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiError> handleMail(MailException ex, WebRequest request) {
        String path = getPath(request);
        ApiError body = new ApiError(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Mail Delivery Error",
                ex.getMessage(),
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // 405 - Method not allowed
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers,
                                                                         HttpStatusCode status,
                                                                         WebRequest request) {
        String path = getPath(request);
        ApiError body = new ApiError(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                ex.getMessage(),
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 415 - Unsupported media type
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpHeaders headers,
                                                                     HttpStatusCode status,
                                                                     WebRequest request) {
        String path = getPath(request);
        ApiError body = new ApiError(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Unsupported Media Type",
                ex.getMessage(),
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // Fallback - 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, WebRequest request) {
        String path = getPath(request);
        ApiError body = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                path,
                null
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            return swr.getRequest().getRequestURI();
        }
        return null;
    }
}
