package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginaResponseDTO<T> {
    private List<T> conteudo;
    private int paginaAtual;
    private int totalPaginas;
    private long totalElementos;
    private int tamanhoPagina;
    private boolean primeira;
    private boolean ultima;

    public static <E, D> PaginaResponseDTO<D> fromPage(Page<E> page, Function<E, D> mapper) {
        return new PaginaResponseDTO<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}