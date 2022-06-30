package com.lagrange.tirage.tirageapi.persistence;

import com.lagrange.tirage.tirageapi.entity.TirageParameter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author PME
 */
@Repository
public interface ParameterRepository extends CrudRepository<TirageParameter,Long> {

    @Query(value = "SELECT * FROM LAGRANGE.TBL_PARAMETER WHERE COMPANY= ?1 limit 1", nativeQuery = true)
    Optional<TirageParameter> findTirageParameterByCompany(String company);

    @Query(value = "SELECT COMPANY FROM LAGRANGE.TBL_PARAMETER ", nativeQuery = true)
    Optional<List<String>> findCompanies();

    @Query(value = "DELETE FROM LAGRANGE.TBL_PARAMETER WHERE COMPANY= ?1", nativeQuery = true)
    void deleteByCompany(String company);

    @Query(value = "DELETE FROM LAGRANGE.TBL_PARAMETER ", nativeQuery = true)
    public void deleteAll() ;

}
