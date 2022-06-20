/**
 * 
 */
package com.lagrange.tirage.tirageapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class CreateTirageResponse {
	
	private List<UserResponse> userResponses = new ArrayList<UserResponse>();
	private String company;
	private String message;

}
