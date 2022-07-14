package com.lagrange.tirage.tirageapi.persistence;

import com.lagrange.tirage.tirageapi.entity.Tirage;
import com.lagrange.tirage.tirageapi.model.UserResource;
import com.lagrange.tirage.tirageapi.model.UserTirageResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TirageRepository extends CrudRepository<Tirage,Long> {

    @Query(value = "SELECT * FROM TBL_TIRAGE WHERE EMAIL = ?1 and COMPANY = ?2 limit 1", nativeQuery = true)
    Optional<Tirage> getUserTirage(String email, String company) ;

    @Query(value = "UPDATE TBL_TIRAGE SET NOTIFICATION_SEND = ?1 WHERE EMAIL = ?1 and COMPANY= ?2 ", nativeQuery = true)
    void updateUser(String email, String company, boolean notificationSend);

    @Query(value = "SELECT new com.lagrange.tirage.tirageapi.model.UserResource(t.email, t.secoreCode, t.admin) FROM Tirage t WHERE t.company = ?1")
    List<UserResource> getAllUser(String company) ;

    @Query(value = "SELECT new com.lagrange.tirage.tirageapi.model.UserTirageResponse(t.email, t.company, t.orderNumber) FROM Tirage t WHERE t.company = ?1")
    List<UserTirageResponse> getTirageResult(String company) ;

    @Query(value = "DELETE FROM TBL_TIRAGE WHERE EMAIL = ?1", nativeQuery = true)
    public void delete(String email);

    @Query(value = "DELETE FROM TBL_TIRAGE ", nativeQuery = true)
    public void deleteAll() ;
}