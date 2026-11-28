package br.com.restaurante.dto;

public record ResumoEstoqueResponse(

    long totalIngredientes,

    long ingredientesAtivos,

    long ingredientesInativos,

    long ingredientesEstoqueBaixo,

    long ingredientesIndisponiveis,

    long ingredientesComReserva

) {
}