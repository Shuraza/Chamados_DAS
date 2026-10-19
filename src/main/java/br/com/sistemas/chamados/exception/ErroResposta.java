package br.com.sistemas.chamados.exception;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ErroResposta(
        LocalDateTime momento,
        int Status,
        String erro,
        List<String> detalhes
) {
}
