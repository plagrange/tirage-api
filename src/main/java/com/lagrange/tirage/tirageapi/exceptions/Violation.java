package com.lagrange.tirage.tirageapi.exceptions;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
class Violation{

    private final String fieldName;

    private final String message;
}
