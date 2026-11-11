package br.com.sistemas.chamados.exception;

import java.util.Set;

public class OrdenacaoInvalidaException extends RuntimeException {
    public OrdenacaoInvalidaException(String campo, Set<String> permitidos) {
        super("Campo de ordenação inválido: " + campo + ". Permitidos: " + String.join(", ", permitidos));
    }
}
