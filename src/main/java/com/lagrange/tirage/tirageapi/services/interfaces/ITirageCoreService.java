package com.lagrange.tirage.tirageapi.services.interfaces;

import com.lagrange.tirage.tirageapi.entity.Tirage;
import com.lagrange.tirage.tirageapi.exceptions.SendMessageException;
import com.lagrange.tirage.tirageapi.model.*;

import javax.mail.MessagingException;
import java.util.List;

public interface ITirageCoreService {

    List<UserResponse> initListParticipant(CreateTirageRequest createTirage);
    boolean authenticateByDB(String email, String criteria, String company);
    int doTirage(String email, String company);
    boolean authenticateAdminByDB(String email, String criteria, String company);
    List<UserTirageResponse> getResultList(String company);
    List<UserResource> getListParticipantFromDB(String company);
    NotifyUserResponse notifyUser(NotifyUserResource userResource) throws MessagingException;
    boolean verifyCompanyAlreadyExist(String company);
    List<String> getListExistedCompany();
    Tirage verifyUserAlreadyDoTirage(String email, String company);
}
