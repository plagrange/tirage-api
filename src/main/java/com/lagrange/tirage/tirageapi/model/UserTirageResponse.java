package com.lagrange.tirage.tirageapi.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pmekeze
 *
 */
@Data
@NoArgsConstructor
@Builder
public class UserTirageResponse{

	public UserTirageResponse(String email, String company, int orderNumber){
		this.email = email;
		this.company = company;
		this.orderNumber = orderNumber;
	}
    private String email;
	private String company;
	private int orderNumber;

}
