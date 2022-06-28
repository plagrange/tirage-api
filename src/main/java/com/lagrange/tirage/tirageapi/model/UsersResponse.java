/**
 * 
 */
package com.lagrange.tirage.tirageapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pmekeze
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class UsersResponse{
	
	private List<UserResource> userResponseList = new ArrayList<>();
	private String company;
	private String message;

}
