package dev.francode.ordersystem.exceptions.advice;

import dev.francode.ordersystem.exceptions.DTOs.ApiResponseDTO;
import dev.francode.ordersystem.exceptions.DTOs.ValidationErrorDTO;
import dev.francode.ordersystem.exceptions.custom.ResourceNotFoundException;
import dev.francode.ordersystem.exceptions.custom.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - URL no existe
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponseDTO> handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
        String requestURL = request.getDescription(false);
        logger.error("URL no encontrada: {}", requestURL);

        ApiResponseDTO response = new ApiResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "La URL solicitada no existe: " + requestURL,
                "ERR_404_URL_NOT_FOUND"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 404 - Recurso no encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Recurso no encontrado: {}", ex.getMessage());

        ApiResponseDTO response = new ApiResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                "ERR_404_RESOURCE_NOT_FOUND"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 400 - Validación general
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseDTO> handleValidationException(ValidationException ex) {
        logger.warn("Error de validación: {}", ex.getMessage());

        ApiResponseDTO response = new ApiResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "ERR_400_VALIDATION"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 - Validación de campos (DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> ((FieldError) error).getDefaultMessage())
                .collect(Collectors.toList());

        logger.warn("Errores de validación de campos: {}", errors);

        ValidationErrorDTO response = new ValidationErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Errores de validación encontrados",
                "ERR_400_FIELD_VALIDATION"
        );
        response.setFieldErrors(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // 400 - Formato inválido en el cuerpo de la solicitud
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.warn("Error de formato en el cuerpo de la solicitud: {}", ex.getMessage());

        ApiResponseDTO response = new ApiResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "El cuerpo de la solicitud no tiene el formato correcto.",
                "ERR_400_INVALID_FORMAT"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 - Manejo de DataIntegrityViolationException
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDTO> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.warn("Error de integridad de base de datos: {}", ex.getMessage());

        String errorMessage = "Se ha producido una violación de integridad de base de datos.";

        Throwable rootCause = ex.getRootCause();
        if (rootCause != null) {
            String message = rootCause.getMessage();

            // Error por registro duplicado
            if (message != null && message.contains("Duplicate entry")) {
                errorMessage = "Ya existe un registro con los mismos datos. Por favor, verifica la información.";
            }

            // Error por restricción de clave foránea
            else if (message != null && message.contains("foreign key constraint fails")) {
                errorMessage = "No se puede eliminar este registro porque está relacionado con otros datos.";
            }
        }

        ApiResponseDTO response = new ApiResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                "ERR_400_INTEGRITY_VIOLATION"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // 403 - Acceso denegado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Acceso denegado: {}", ex.getMessage());

        ApiResponseDTO response = new ApiResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "No tiene permisos para acceder a este recurso.",
                "ERR_403_ACCESS_DENIED"
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 405 - Metodo no permitido
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        logger.error("Método HTTP no soportado: {}", ex.getMethod());

        ApiResponseDTO response = new ApiResponseDTO(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "El método HTTP " + ex.getMethod() + " no está permitido para la URL solicitada.",
                "ERR_405_METHOD_NOT_ALLOWED"
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // 500 - Excepciones no controladas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO> handleUnhandledExceptions(Exception ex, WebRequest request) {
        logger.error("Error inesperado en la solicitud: {}", request.getDescription(false), ex);

        ApiResponseDTO response = new ApiResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error inesperado.",
                "ERR_500_INTERNAL_SERVER_ERROR"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
