package com.lagrange.tirage.tirageapi.model;

import lombok.*;

import java.io.Serializable;

/**
 * @author pmekeze
 *
 */
@Data
@NoArgsConstructor
@Builder
public class UserResource implements Serializable{

    private String email;
	private String secureCode;
	private boolean admin = false;

	public UserResource(String email, String secureCode, boolean admin){
		this.email = email;
		this.secureCode = secureCode;
		this.admin = admin;
	}
	public String toString(){
		return "User email = " + this.email + ", secureCode = " + this.secureCode + "isAdmin = "+ this .admin;
	}

}
