package com.lagrange.tirage.tirageapi.services.interfaces;

import com.lagrange.tirage.tirageapi.model.*;

import java.util.List;

public interface ITirageCoreService {

    List<UserResponse> initListParticipant(CreateTirageRequest createTirage) throws Exception;
    boolean authenticateByDB(String email, String criteria, String company) throws Exception;
    int doTirage(String email, String company) throws Exception;
    boolean authenticateAdminByDB(String email, String criteria, String company) throws Exception;
    List<UserTirageResponse> getResultList(String company) throws Exception;
    List<UserResource> getListParticipantFromDB(String company);
    NotifyUserResponse notifyUser(NotifyUserResource userResource) throws Exception;
    boolean verifyCompanyAlreadyExist(String company) throws Exception;
    List<String> getListExistedCompany() throws Exception;
}
