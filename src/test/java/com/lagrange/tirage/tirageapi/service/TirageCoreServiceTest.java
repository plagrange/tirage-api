package com.lagrange.tirage.tirageapi.service;

import com.lagrange.tirage.tirageapi.entity.Tirage;
import com.lagrange.tirage.tirageapi.exceptions.ErrorCodesEnum;
import com.lagrange.tirage.tirageapi.exceptions.UserException;
import com.lagrange.tirage.tirageapi.model.*;
import com.lagrange.tirage.tirageapi.persistence.ParameterRepository;
import com.lagrange.tirage.tirageapi.persistence.TirageRepository;
import com.lagrange.tirage.tirageapi.services.MailService;
import com.lagrange.tirage.tirageapi.services.interfaces.ITirageCoreService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles({"test"})
public class TirageCoreServiceTest {

    @Autowired
    private ITirageCoreService tirageCoreService;
    @Autowired
    private TirageRepository tirageRepository;

    @Autowired
    private ParameterRepository parameterRepository;

    @MockBean
    private MailService mailService;

    private UserResource userResource1;
    private UserResource userResource2;
    private UserResource userResource3;
    private List<UserResource> userResourceList;
    private static String COMPANY = "COMPANY";
    @BeforeEach
    public void setUp() {
        tirageRepository.deleteAll();
        parameterRepository.deleteAll();

        userResource1 = UserResource.builder().email("test1@lagrangien.fr").secureCode("TEST1").admin(true).build();
        userResource2 = UserResource.builder().email("test2@lagrangien.fr").secureCode("TEST2").admin(true).build();
        userResource3 = UserResource.builder().email("test3@lagrangien.fr").secureCode("TEST3").admin(true).build();
        userResourceList = List.of(userResource1, userResource2);

    }

    @Test
    void testInitParticipants(){


        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        List<UserResponse> userResponses = tirageCoreService.initListParticipant(createTirageRequest);

        System.out.println(userResponses);
        Assertions.assertTrue(userResponses.size()==2);

    }

    @Test
    void testAuthenticateByDB(){

        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);

        boolean company = tirageCoreService.authenticateByDB(userResource1.getEmail(), userResource1.getSecureCode(), "COMPANY");

        Assertions.assertTrue(company, "User authentication test should succeed");
    }

    @Test
    void testAuthenticateByDBFailed(){

        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);

        boolean company = tirageCoreService.authenticateByDB(userResource1.getEmail(), "TEST2", COMPANY);

        Assertions.assertFalse(company, "User authentication test should failed because criteria doesn't match");
    }

    @Test
    void testAuthenticateByDBFailedUserNotExist(){

        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);

        boolean company = tirageCoreService.authenticateByDB("test3@lagrangien.fr", "TEST2", COMPANY);

        Assertions.assertFalse(company, "User authentication test should failed because user not exist");
    }

    @Test
    @SneakyThrows
    void testDotirageSucceed(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);

        int doTirageResult = tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);

        Assertions.assertTrue(doTirageResult!=0, "Tirage result should be succeed");

    }

    @Test
    @SneakyThrows
    void testDotirageFailedCompanyNotFound(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);

        parameterRepository.deleteAll();
        UserException exception = Assertions.assertThrows(UserException.class, () -> {
            tirageCoreService.doTirage(userResource1.getEmail(), "COMPANY");
        });

        Assertions.assertTrue(exception.getErrorCode().equals(ErrorCodesEnum.COMPANY_NOT_FOUND.name()), "Tirage result should throws and user exception with COMPANY_NOT_FOUND_CODE");

    }

    @Test
    @SneakyThrows
    void testDotirageSucceedUserAlreadyDoTirage(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);

        int doTirageResult = tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);
        int doTirageResult2 = tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);

        Assertions.assertTrue(doTirageResult==doTirageResult2, "Tirage already done and should return same result");

    }

    @Test
    @SneakyThrows
    void testDotirageFailedUserNotFound(){

        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);

        UserException exception = Assertions.assertThrows(UserException.class, () -> {
            tirageCoreService.doTirage("test3@lagrangien.fr", COMPANY);
        });

        Assertions.assertTrue(exception.getErrorCode().equals(ErrorCodesEnum.PARTICIPANT_NOT_FOUND.name()), "Tirage result should throws and user exception");

    }

    @Test
    @SneakyThrows
    void testGetResultList(){

        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);
        int doTirageResult1 = tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);
        int doTirageResult2 = tirageCoreService.doTirage(userResource2.getEmail(), COMPANY);

        List<UserTirageResponse> resultList = tirageCoreService.getResultList(COMPANY);

        Assertions.assertTrue(resultList.size()==2);
    }

    @Test
    @SneakyThrows
    void testGetUserResult(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);
        tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);

        Tirage userResult = tirageCoreService.getUserResult(userResource1.getEmail(), COMPANY);
        Assertions.assertTrue(userResult.getOrderNumber()!=0);
    }

    @Test
    @SneakyThrows
    void testGetUserResultParticipantNotFound(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);
        tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);

        UserException exception = Assertions.assertThrows(UserException.class, () -> {
            tirageCoreService.getUserResult("test3@lagrangien.fr", userResource1.getSecureCode());
        });
        Assertions.assertTrue(exception.getErrorCode().equals(ErrorCodesEnum.PARTICIPANT_NOT_FOUND.name()), "Tirage result should throws and user exception");

    }

    @Test
    void testGetListParticipantFromDB(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);

        List<UserResource> listParticipantFromDB = tirageCoreService.getListParticipantFromDB(COMPANY);

        Assertions.assertTrue(listParticipantFromDB.stream().map(u -> u.getEmail()).toList().containsAll(List.of(userResource1.getEmail(), userResource2.getEmail())));
    }

    @Test
    @SneakyThrows
    void testVerifyCompanyAlreadyExist(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);
        tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);

        boolean companyAlreadyExist = tirageCoreService.verifyCompanyAlreadyExist(COMPANY);

        Assertions.assertTrue(companyAlreadyExist);

    }

    @Test
    @SneakyThrows
    void testVerifyCompanyNotAlreadyExist(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);
        tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);

        boolean companyAlreadyExist = tirageCoreService.verifyCompanyAlreadyExist("COMPANY2");

        Assertions.assertFalse(companyAlreadyExist);

    }

    @Test
    @SneakyThrows
    void testGetListExistedCompany(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        tirageCoreService.initListParticipant(createTirageRequest);
        tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);

        List<String> listExistedCompany = tirageCoreService.getListExistedCompany();

        Assertions.assertTrue(listExistedCompany.contains(COMPANY));
        Assertions.assertTrue(listExistedCompany.size()==1);

    }

    @Test
    @SneakyThrows
    void testGetListExistedCompanyShouldBeEmpty(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);

        List<String> listExistedCompany = tirageCoreService.getListExistedCompany();

        Assertions.assertTrue(listExistedCompany.size()==0);

    }


    @Test
    @SneakyThrows
    void testNotifyUser(){
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);
        tirageCoreService.initListParticipant(createTirageRequest);
        tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);

        NotifyUserResource notifyUserResource = NotifyUserResource.of(userResource1, userResource2, COMPANY);

        doNothing().when(mailService).sendMail(notifyUserResource.getUserToNotify().getEmail(), notifyUserResource.getCompany(), notifyUserResource.getUserToNotify().getSecureCode());

        NotifyUserResponse notifyUserResponse = tirageCoreService.notifyUser(notifyUserResource);

        Assertions.assertTrue(notifyUserResponse.isNotificationSend());
    }

    @Test
    @SneakyThrows
    void testNotifyUserFailed() {
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, COMPANY, false);
        tirageCoreService.initListParticipant(createTirageRequest);
        tirageCoreService.doTirage(userResource1.getEmail(), COMPANY);

        NotifyUserResource notifyUserResource = NotifyUserResource.of(userResource1, userResource3, COMPANY);

        doNothing().when(mailService).sendMail("test3@lagrangien.fr", notifyUserResource.getCompany(), notifyUserResource.getUserToNotify().getSecureCode());

        UserException userException = Assertions.assertThrows(UserException.class, () -> {
            tirageCoreService.notifyUser(notifyUserResource);
        });

        Assertions.assertTrue(userException.getErrorCode().equals(ErrorCodesEnum.NOTIFY_PARTICIPANT_FAILED.name()));
    }
}
