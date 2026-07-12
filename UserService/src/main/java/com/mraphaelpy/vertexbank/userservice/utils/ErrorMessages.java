package com.mraphaelpy.vertexbank.userservice.utils;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static final String USER_PROFILE_ALREADY_EXISTS = "Perfil de usuário já cadastrado para este ID";
    public static final String CPF_ALREADY_EXISTS = "CPF já cadastrado no sistema";
    public static final String USER_PROFILE_NOT_FOUND = "Perfil de usuário não encontrado";
    public static final String VALIDATION_FAILED = "Erro de validação nos campos fornecidos";
    public static final String INTERNAL_SERVER_ERROR = "Ocorreu um erro interno no servidor";
}
