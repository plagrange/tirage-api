package com.lagrange.tirage.tirageapi.web.controller;

import com.lagrange.tirage.tirageapi.entity.Tirage;
import com.lagrange.tirage.tirageapi.model.*;
import com.lagrange.tirage.tirageapi.services.interfaces.ITirageCoreService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
@Slf4j
public class TirageController {

    @Autowired
    private ITirageCoreService tirageCoreService;

    private static final String ANDCOMPANY = " and company : ";
    private static final String RETRIEVE_RESULT = "Retrieving result of tirage for user : ";

    @SneakyThrows
    @PostMapping(value = "/create-tirage", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateTirageResponse> createTirage(@RequestBody @Valid CreateTirageRequest createTirageRequest) {
        log.info("create a new tirage with resource : " + createTirageRequest.toString());

        if (createTirageRequest.getCompany() == null || createTirageRequest.getCompany().length() == 0 || createTirageRequest.getUsers() == null || createTirageRequest.getUsers().size() == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Bad user parameter");
        }
        List<UserResponse> userList = null;
        userList = tirageCoreService.initListParticipant(createTirageRequest);
        CreateTirageResponse createTirageResponse = CreateTirageResponse.builder()
                .userResponses(userList)
                .company(createTirageRequest.getCompany())
                .build();

        log.info("create a new tirage end with success, return response is :  " + createTirageResponse.toString());
        return new ResponseEntity<>(createTirageResponse, HttpStatus.CREATED);
    }

    @PostMapping(value = "/perform-tirage", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserTirageResponse> doTirage(@RequestBody @Valid UserTirageRequest userTirageRequest) throws Exception {
        log.info("Perform tirage for user : " + userTirageRequest.getEmail() + ANDCOMPANY + userTirageRequest.getCompany());
        UserTirageResponse userTirageResponse = UserTirageResponse.builder().build();

        if (tirageCoreService.authenticateByDB(userTirageRequest.getEmail(), userTirageRequest.getSecureCode(), userTirageRequest.getCompany())) {
            int number = tirageCoreService.doTirage(userTirageRequest.getEmail(), userTirageRequest.getCompany());
            userTirageResponse = UserTirageResponse.builder()
                    .email(userTirageRequest.getEmail())
                    .company(userTirageRequest.getCompany())
                    .orderNumber(number)
                    .build();

            log.info("email : " + userTirageResponse.getEmail() + "  and secureCode : " + userTirageRequest.getSecureCode() + "  for company : " + userTirageResponse.getCompany() + "; numero du tour  = " + number);

        } else {
            log.info("email : " + userTirageResponse.getEmail() + "  and secureCode : " + userTirageRequest.getSecureCode() + "  for company : " + userTirageResponse.getCompany() + " not matching");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Bad user parameter, secure code not matching");
        }

        return new ResponseEntity<>(userTirageResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/results", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TirageResponses> getResults(@RequestBody @Valid UserTirageRequest adminresource) throws Exception {
        log.info("retrieving results of tirage for company : " + adminresource.getCompany());

        if (!tirageCoreService.authenticateAdminByDB(adminresource.getEmail(), adminresource.getSecureCode(), adminresource.getCompany())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "secure code not matching");
        }
        List<UserTirageResponse> resultList = tirageCoreService.getResultList(adminresource.getCompany());
        TirageResponses tirageResponses = TirageResponses.of(resultList, adminresource.getCompany());
        log.info("retrieving results of tirage for company : " + adminresource.getCompany() + " end with success.  result list is : " + tirageResponses);
        return new ResponseEntity<>(tirageResponses, HttpStatus.OK);
    }

    @PostMapping(value = "/result", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserTirageResponse> getResultUser(@RequestBody @Valid UserTirageRequest userRequest) throws Exception {
        log.info(RETRIEVE_RESULT + userRequest.getEmail() + ANDCOMPANY + userRequest.getCompany());
        if (tirageCoreService.authenticateByDB(userRequest.getEmail(), userRequest.getSecureCode(), userRequest.getCompany())) {

            Tirage tirage = tirageCoreService.verifyUserAlreadyDoTirage(userRequest.getEmail(), userRequest.getCompany());

            UserTirageResponse userTirageResponse = UserTirageResponse.builder()
                    .email(userRequest.getEmail())
                    .orderNumber(tirage==null?0:tirage.getOrderNumber())
                    .company(userRequest.getCompany())
                    .build();
            log.info(RETRIEVE_RESULT + userRequest.getCompany() + ANDCOMPANY + userRequest.getCompany() + " end with success. result is : " + userTirageResponse);
            return new ResponseEntity<>(userTirageResponse, HttpStatus.OK);

        } else {
            log.info(RETRIEVE_RESULT + userRequest.getEmail() + ANDCOMPANY + userRequest.getCompany() + " is forbiden due to bad criteria provided : " + userRequest.getSecureCode());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "secure code not matching");
        }
    }

    @PostMapping(value = "/getusers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsersResponse> getUsers(@RequestBody @Valid UserTirageRequest adminresource) throws Exception {
        log.info("Getting all users registre for company : " + adminresource.getCompany() + " with adminresource request : " + adminresource);

        if (!tirageCoreService.authenticateAdminByDB(adminresource.getEmail(), adminresource.getSecureCode(), adminresource.getCompany())) {
            UsersResponse usersResponse = new UsersResponse();
            usersResponse.setMessage("Admin authentication failed!");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Admin authentication failed!");
        }
        List<UserResource> usersResource = tirageCoreService.getListParticipantFromDB(adminresource.getCompany());

        UsersResponse usersResponse = UsersResponse.builder().
                userResponseList(usersResource)
                .company(adminresource.getCompany())
                .build();
        log.info("Getting all users register for company : " + adminresource.getCompany() + " end with success ");
        log.debug("Getting all users register for company : " + adminresource.getCompany() + " end with success and result : " + usersResponse);

        return new ResponseEntity<>(usersResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/notifyuser", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotifyUserResponse> notifyUser(@RequestBody @Valid NotifyUserResource notifyUserResource) throws Exception {
        log.info("Notifing user for doing tirage au sort : user resource : " + notifyUserResource);

        NotifyUserResponse notifyUserResponse = tirageCoreService.notifyUser(notifyUserResource);

        log.info("Notifing user for doing tirage au sort end with success ");
        log.debug("Notifing user for doing tirage au sort end with success and result : " + notifyUserResponse);
        return new ResponseEntity<>(notifyUserResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/verifycompany/{company}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompanyExisted> verifyCompany(@PathVariable("company") String company) throws Exception {
        log.info("verifing if the company ' " + company + "' exist");
        boolean companyAlreadyExist = tirageCoreService.verifyCompanyAlreadyExist(company);
        CompanyExisted companyExisted = CompanyExisted.of(companyAlreadyExist);
        log.info("verifing if the company ' " + company + "' exist end with success and result : " + companyExisted);
        return new ResponseEntity<>(companyExisted, HttpStatus.OK);
    }

    @GetMapping(value = "/companies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getCompanies() throws Exception {
        log.info("enterring get list existed companies");
        List<String> listExistedCompany = tirageCoreService.getListExistedCompany();
        log.info("exiting get list existed companies");
        return new ResponseEntity<>(listExistedCompany, HttpStatus.OK);
    }
}
