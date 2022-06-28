package com.lagrange.tirage.tirageapi.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ValidationErrorResponse {

    private List<Violation> violations = new ArrayList<>();

}