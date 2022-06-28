package com.lagrange.tirage.tirageapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pmekeze
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class UserTirageResponse{

    private String email;
	private String company;
	private int orderNumber;

}
