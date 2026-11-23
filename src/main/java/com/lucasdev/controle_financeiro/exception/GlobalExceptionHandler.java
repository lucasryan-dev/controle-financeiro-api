package com.lucasdev.controle_financeiro.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse tratarNaoEncontrado(RecursoNaoEncontradoException ex){
        return new ErroResponse(404, ex.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse tratarValidacao(MethodArgumentNotValidException ex){
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));

        return new ErroResponse(400, "Dados inválidos", campos, LocalDateTime.now());
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroResponse tratarErroGenerico(Exception ex){
        log.error("Erro inesperado", ex);
        return new ErroResponse(500, "Erro interno no servidor");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse tratarJsonInvalido(HttpMessageNotReadableException ex){
        return new ErroResponse(400, "JSON inválido ou com valores não aceitos" );
    }

    @ExceptionHandler(ConflitoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse tratarConflito(ConflitoException ex){
        return new ErroResponse(409, ex.getMessage());
    }

    @ExceptionHandler({DateTimeException.class,
                        MissingServletRequestParameterException.class,
                            MethodArgumentTypeMismatchException.class,
                                InvalidDataAccessApiUsageException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse tratarParametroInvalido(Exception ex){
        return new ErroResponse(400, "Parâmetro inválido ou ausente na requisição");
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErroResponse tratarCredenciaisInvalidas(CredenciaisInvalidasException ex){
        return new ErroResponse(401, ex.getMessage());
    }

}
