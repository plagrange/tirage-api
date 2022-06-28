/**
 * 
 */
package com.lagrange.tirage.tirageapi.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author pmekeze
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class UserResource implements Serializable{

    private String email;
	private String secureCode;
	private boolean admin = false;

	public String toString(){
		return "User email = " + this.email + ", secureCode = " + this.secureCode + "isAdmin = "+ this .admin;
	}

}
