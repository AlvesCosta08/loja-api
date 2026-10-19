package com.alvescosta.loja.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== 400 — Bad Request ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Erro de validação");
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", "JSON malformado ou ilegível");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parâmetro '%s' deve ser do tipo %s",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido");
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        String message = String.format("Parâmetro obrigatório '%s' ausente", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    // ==================== 401 — Unauthorized ====================

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    // ==================== 403 — Forbidden ====================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", "Acesso negado. Você não tem permissão para esta operação.");
    }

    // ==================== 404 — Not Found ====================

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", "Recurso não encontrado");
    }

    // ==================== 405 — Method Not Allowed ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "Método HTTP '" + ex.getMethod() + "' não suportado para esta rota");
    }

    // ==================== 409 — Conflict ====================

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMessage());
        // Tenta detectar o tipo de violação pela mensagem (fallback de segurança)
        String message = "Conflito de integridade de dados";
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("email")) {
            message = "E-mail já cadastrado";
        } else if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unique")) {
            message = "Registro duplicado";
        }
        return buildResponse(HttpStatus.CONFLICT, "Conflict", message);
    }

    // ==================== 500 — Internal Server Error ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Erro interno do servidor. Tente novamente mais tarde.");
    }

    // ==================== Builder ====================

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}