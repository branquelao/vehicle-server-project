package br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.handler;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.ErroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.ImagemInvalidaException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), null);
    }

    @ExceptionHandler(ImagemInvalidaException.class)
    public ResponseEntity<ErroResponseDTO> handleImagemInvalida(ImagemInvalidaException ex) {
        return construir(HttpStatus.BAD_REQUEST, "Imagem inválida", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Erro de validação", "Um ou mais campos estão inválidos", detalhes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> handleJsonInvalido(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST, "JSON inválido", "O corpo da requisição está malformado", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponseDTO> handleCredenciaisInvalidas(BadCredentialsException ex) {
        return construir(HttpStatus.UNAUTHORIZED, "Falha na autenticação", "Usuário ou senha inválidos", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDTO> handleAcessoNegado(AccessDeniedException ex) {
        return construir(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para executar essa ação", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleGenerico(Exception ex) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado", null);
    }

    private ResponseEntity<ErroResponseDTO> construir(HttpStatus status, String erro, String mensagem, List<String> detalhes) {
        ErroResponseDTO body = new ErroResponseDTO(LocalDateTime.now(), status.value(), erro, mensagem, detalhes);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErroResponseDTO> handleIOException(IOException ex) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Falha no processamento de arquivo",
                "Não foi possível processar o upload da imagem", null);
    }
}