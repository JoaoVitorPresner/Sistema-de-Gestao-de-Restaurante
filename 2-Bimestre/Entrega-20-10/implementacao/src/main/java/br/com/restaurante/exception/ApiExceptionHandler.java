
package br.com.restaurante.exception;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {


    /* =========================================
       ERROS DAS REGRAS DE NEGOCIO
       ========================================= */

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>>
            tratarRegraNegocio(

                ResponseStatusException erro) {


        Map<String, Object> resposta =
                new LinkedHashMap<>();


        resposta.put(

            "erro",

            erro.getReason() != null

                ? erro.getReason()

                : "Operacao nao permitida"
        );


        resposta.put(

            "status",

            erro.getStatusCode().value()
        );


        return ResponseEntity

            .status(erro.getStatusCode())

            .body(resposta);
    }


    /* =========================================
       ERROS DE VALIDACAO
       ========================================= */

    @ExceptionHandler(
        MethodArgumentNotValidException.class
    )
    public ResponseEntity<Map<String, Object>>
            tratarValidacao(

                MethodArgumentNotValidException erro) {


        Map<String, String> campos =
                new LinkedHashMap<>();


        erro.getBindingResult()

            .getFieldErrors()

            .forEach(campo ->

                campos.putIfAbsent(

                    campo.getField(),

                    campo.getDefaultMessage()
                )
            );


        Map<String, Object> resposta =
                new LinkedHashMap<>();


        resposta.put(

            "erro",

            "Dados informados invalidos"
        );

        resposta.put("status", 400);

        resposta.put("campos", campos);


        return ResponseEntity

            .badRequest()

            .body(resposta);
    }


    /* =========================================
       JSON INVALIDO
       ========================================= */

    @ExceptionHandler(
        HttpMessageNotReadableException.class
    )
    public ResponseEntity<Map<String, Object>>
            tratarJsonInvalido(

                HttpMessageNotReadableException erro) {


        return ResponseEntity

            .badRequest()

            .body(

                Map.of(

                    "erro",
                    "Formato dos dados invalido",

                    "status",
                    400
                )
            );
    }


    /* =========================================
       CONFLITOS DE CONCORRENCIA
       ========================================= */

    @ExceptionHandler({

        OptimisticLockingFailureException.class,

        PessimisticLockingFailureException.class

    })
    public ResponseEntity<Map<String, Object>>
            tratarConcorrencia(Exception erro) {


        return ResponseEntity

            .status(HttpStatus.CONFLICT)

            .body(

                Map.of(

                    "erro",

                    "Registro alterado simultaneamente. Atualize e tente novamente.",

                    "status",

                    409
                )
            );
    }
}
