package com.lagrange.tirage.tirageapi.exceptions;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "of")
@Data
@NoArgsConstructor
public class ErrorResponse {
    String errorMessage;
}
