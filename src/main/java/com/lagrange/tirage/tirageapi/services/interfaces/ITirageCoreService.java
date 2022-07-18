package com.lagrange.tirage.tirageapi.services.interfaces;

import com.lagrange.tirage.tirageapi.entity.Tirage;
import com.lagrange.tirage.tirageapi.exceptions.UserException;
import com.lagrange.tirage.tirageapi.model.*;

import javax.mail.MessagingException;
import java.util.List;

public interface ITirageCoreService {

    List<UserResponse> initListParticipant(CreateTirageRequest createTirage);
    boolean authenticateByDB(String email, String criteria, String company);
    int doTirage(String email, String company) throws UserException;
    boolean authenticateAdminByDB(String email, String criteria, String company) throws UserException;
    List<UserTirageResponse> getResultList(String company);
    Tirage getUserResult(String email, String company);
    List<UserResource> getListParticipantFromDB(String company);
    NotifyUserResponse notifyUser(NotifyUserResource userResource) throws MessagingException, UserException;
    boolean verifyCompanyAlreadyExist(String company);
    List<String> getListExistedCompany();
    boolean verifyUserAlreadyDoTirage(String email, String company);
}
