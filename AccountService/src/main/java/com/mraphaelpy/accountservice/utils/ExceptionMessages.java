package com.mraphaelpy.accountservice.utils;

public final class ExceptionMessages {

    private ExceptionMessages() {
    }

    public static final String ACCOUNT_NOT_FOUND = "Conta não encontrada";
    public static final String INSUFFICIENT_BALANCE = "Saldo insuficiente";
    public static final String INVALID_AMOUNT = "O valor da operação deve ser maior que zero";
    public static final String ACCOUNT_INACTIVE = "A conta não está ativa";
    public static final String INVALID_OPERATION = "Tipo de operação inválido";
    public static final String USER_NOT_FOUND = "Usuário não encontrado no UserService";
}
