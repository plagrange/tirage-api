/**
 * 
 */
package com.lagrange.tirage.tirageapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.util.Date;


/**
 * @author pmekeze
 *
 */
@Entity
@Table(name = "TBL_TIRAGE", schema = "LAGRANGE")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SequenceGenerator(name = "TIRAGE_SEQUENCE", allocationSize = 1, sequenceName = "TIRAGE_SEQUENCE")
public class Tirage{

	@Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Basic
    @Column(name = "EMAIL", nullable = false)
    private String email;
    @Column(name = "SECURE_CODE", nullable = false)
    @Basic
    private String secoreCode;
    @Column(name = "COMPANY", nullable = false)
    private String company;
    @Basic
    @Column(name = "ORDER_NUMBER")
    private int orderNumber;
	@Basic
    @Column(name = "ADMIN")
    private boolean admin;
	@Basic
    @Column(name = "NOTIFY")
    private boolean notify;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_DATE")
    private Date createDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UODATE_DATE")
    private Date updateDate;

}
