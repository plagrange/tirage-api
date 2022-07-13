/**
 * 
 */
package com.lagrange.tirage.tirageapi.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * @author pmekeze
 *
 */
@Entity
@Table(name = "TBL_PARAMETER", uniqueConstraints = @UniqueConstraint(columnNames = { "COMPANY" }))
@NoArgsConstructor
@Builder
@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SequenceGenerator(name = "PARAMETER_SEQUENCE", allocationSize = 1, sequenceName = "PARAMETER_SEQUENCE")
public class TirageParameter {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Basic
    @Column(name = "NB_PARTICIPANT", nullable = true)
    private int nbParticipant;
    @Basic
    @Column(name = "TAKEN_NUMBER", nullable = true)
    private String takenNumbers;
    @Basic
    @Column(name = "REMAINING_NUMBER")
    private String remainingNumbers;
    @Basic
    @Column(name = "COMPANY")
    private String company;
    @Basic
    @Column(name = "ADMINS")
    private String adminList;
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_DATE")
    private Date createDate;
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATE_DATE")
    private Date updateDate;

}
