package com.lagrange.tirage.tirageapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class CreateTirageRequest {
    @Builder.Default
    private List<UserResource> users = new ArrayList<>();
    private String company;
    @Builder.Default
    private boolean notificationEnabled = false;

}
