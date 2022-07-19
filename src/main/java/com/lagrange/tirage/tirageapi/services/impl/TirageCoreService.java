/**
 *
 */
package com.lagrange.tirage.tirageapi.services.impl;

/**
 * @author pmekeze
 */

import com.lagrange.tirage.tirageapi.entity.Tirage;
import com.lagrange.tirage.tirageapi.entity.TirageParameter;
import com.lagrange.tirage.tirageapi.exceptions.ErrorCodesEnum;
import com.lagrange.tirage.tirageapi.exceptions.UserException;
import com.lagrange.tirage.tirageapi.model.*;
import com.lagrange.tirage.tirageapi.persistence.ParameterRepository;
import com.lagrange.tirage.tirageapi.persistence.TirageRepository;
import com.lagrange.tirage.tirageapi.services.MailService;
import com.lagrange.tirage.tirageapi.services.interfaces.ITirageCoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class TirageCoreService implements ITirageCoreService {

    private TirageRepository tirageRepository;
    private ParameterRepository parameterRepository;

    private Random rand;

    public TirageCoreService(TirageRepository tirageRepository, ParameterRepository parameterRepository) throws NoSuchAlgorithmException {
        this.tirageRepository = tirageRepository;
        this.parameterRepository = parameterRepository;
        rand = SecureRandom.getInstanceStrong();
    }

    public List<UserResponse> initListParticipant(CreateTirageRequest createTirage) {

        List<UserResource> userResourceList = createTirage.getUsers();

        List<String> admins = new ArrayList<>();

        for (UserResource userDto : userResourceList) {
            if (userDto.getEmail() != null && userDto.getSecureCode() != null) {
                Tirage tirage = Tirage.builder()
                        .email(userDto.getEmail())
                        .secoreCode(userDto.getSecureCode())
                        .company(createTirage.getCompany())
                        .admin(userDto.isAdmin())
                        .createDate(new Date())
                        .updateDate(new Date())
                        .build();
                if (userDto.isAdmin()) {
                    admins.add(userDto.getEmail());
                }
                tirageRepository.save(tirage);
                log.info("user '" + userDto.getEmail() + "' and secureCode '" + userDto.getSecureCode() + "' for company '" + createTirage.getCompany() + "' persist with success");
            }
        }
        List<UserResponse> listUser = initTirage(createTirage.getCompany(), createTirage.isNotificationEnabled(), admins);
        if (createTirage.isNotificationEnabled()) {
            for (UserResponse userResponse : listUser) {
                Optional<Tirage> userTirage = tirageRepository.getUserTirage(userResponse.getEmail(), createTirage.getCompany());
                userTirage.ifPresent(tirage -> {
                            tirage.setNotify(userResponse.isNotificationSend());
                            tirage.setUpdateDate(new Date());
                            tirageRepository.save(tirage);
                        }
                );

            }
        }
        return listUser;
    }

    public List<UserResponse> initTirage(String company, boolean notificationEnabled, List<String> admins) {
        List<UserResponse> result = new ArrayList<>();

        List<UserResource> allUser = tirageRepository.getAllUser(company);

        if (allUser != null && !allUser.isEmpty()) {
            List<String> remainingNumbers;
            remainingNumbers = IntStream.rangeClosed(1, allUser.size()).mapToObj(String::valueOf).toList();
            TirageParameter tirageParams = TirageParameter.builder()
                    .nbParticipant(allUser.size())
                    .remainingNumbers(remainingNumbers.stream().collect(Collectors.joining(";")))
                    .company(company)
                    .adminList(admins.stream().collect(Collectors.joining(";")))
                    .createDate(new Date())
                    .updateDate(new Date())
                    .build();
            parameterRepository.save(tirageParams);

            if (notificationEnabled) {
                return sendMailToUsers(allUser, company);
            } else {
                for (UserResource user : allUser) {
                    UserResponse userResponse = UserResponse.builder()
                            .email(user.getEmail())
                            .secureCode(user.getSecureCode())
                            .isNotificationSend(false)
                            .build();
                    result.add(userResponse);
                }
                return result;
            }
        }
        return result;
    }

    public List<UserResource> getListParticipantFromDB(String company) {
        return tirageRepository.getAllUser(company);
    }

    public int doTirage(String email, String company) throws UserException {

        Optional<Tirage> userTirage = tirageRepository.getUserTirage(email, company);
        if (userTirage.isEmpty()) {
            throw new UserException(ErrorCodesEnum.PARTICIPANT_NOT_FOUND);
        }

        Tirage tirageUser = userTirage.get();
        if (tirageUser.getOrderNumber() != 0) {
            return tirageUser.getOrderNumber();
        }

        Optional<TirageParameter> tirageParameterByCompany = parameterRepository.findTirageParameterByCompany(company);
        if (tirageParameterByCompany.isPresent()) {

            TirageParameter parameter = tirageParameterByCompany.get();
            String remainingNumbers = parameter.getRemainingNumbers();
            List<String> remainingNumberList = Arrays.stream(remainingNumbers.split(";")).collect(Collectors.toList());

            String takenNumbers = parameter.getTakenNumbers();
            List<String> takenNumberList = new ArrayList<>();
            if (takenNumbers != null)
                takenNumberList = Arrays.stream(takenNumbers.split(";")).collect(Collectors.toList());

            int numberTake = rand.nextInt(remainingNumberList.size());
            Integer takenNumber = Integer.valueOf(remainingNumberList.get(numberTake));
            remainingNumberList.remove(numberTake);
            takenNumberList.add(String.valueOf(takenNumber));

            parameter.setRemainingNumbers(remainingNumberList.stream().collect(Collectors.joining(";")));
            parameter.setTakenNumbers(takenNumberList.stream().collect(Collectors.joining(";")));

            parameter.setUpdateDate(new Date());
            parameterRepository.save(parameter);

            tirageUser.setEmail(email);
            tirageUser.setOrderNumber(takenNumber);
            tirageUser.setCompany(company);
            tirageUser.setUpdateDate(new Date());

            tirageRepository.save(tirageUser);

            return takenNumber;
        } else {
            throw new UserException(ErrorCodesEnum.COMPANY_NOT_FOUND);
        }
    }

    public boolean authenticateByDB(String email, String criteria, String company) {
        boolean autenticate = false;
        Optional<Tirage> userTirage = tirageRepository.getUserTirage(email, company);
        if (userTirage.isPresent()) {
            Tirage tirage = userTirage.get();
            autenticate = tirage.getSecoreCode().equals(criteria);
        }
        return autenticate;
    }

    public boolean authenticateAdminByDB(String email, String criteria, String company) throws UserException {
        AtomicBoolean autenticate = new AtomicBoolean(false);
        Optional<TirageParameter> tirageParameterByCompany = parameterRepository.findTirageParameterByCompany(company);
        if (tirageParameterByCompany.isEmpty()) {
            throw new UserException(ErrorCodesEnum.PARTICIPANT_NOT_FOUND);
        }

        TirageParameter tirageParameter = tirageParameterByCompany.get();

        String admins = tirageParameter.getAdminList();
        List<String> adminList = Arrays.stream(admins.split(";")).toList();

        for (String admin : adminList) {
            Optional<Tirage> userTirageOptional = tirageRepository.getUserTirage(admin, company);
            userTirageOptional.ifPresent(userTirage -> {
                if (userTirage.getEmail().equals(email)) {
                    if (userTirage.getSecoreCode().equals(criteria)) {
                        autenticate.set(true);
                    } else {
                        log.warn("bad admin password provided: provide = " + criteria + " and expected = " + userTirage.getSecoreCode());
                    }
                }
            });
        }
        return autenticate.get();
    }

    public List<UserTirageResponse> getResultList(String company) {
        List<UserTirageResponse> result = tirageRepository.getTirageResult(company);
        Collections.sort(result, (result1, result2) -> {
            Integer integer1 = result1.getOrderNumber();
            Integer integer2 = result2.getOrderNumber();
            return integer1.compareTo(integer2);
        });
        return result;
    }

    @Override
    public Tirage getUserResult(String email, String company) throws UserException {
        Optional<Tirage> userTirage = tirageRepository.getUserTirage(email, company);
        if(userTirage.isPresent()){
            return userTirage.get();
        }
        throw new UserException(ErrorCodesEnum.PARTICIPANT_NOT_FOUND);
    }

    public boolean verifyCompanyAlreadyExist(String company) {
        Optional<TirageParameter> tirageParameterByCompany = parameterRepository.findTirageParameterByCompany(company);
        return tirageParameterByCompany.isPresent();
    }

    public List<String> getListExistedCompany() {
        Optional<List<String>> optionalCompanies = parameterRepository.findCompanies();

        return optionalCompanies.orElse(Collections.emptyList());
    }

    private List<UserResponse> sendMailToUsers(List<UserResource> allUser, String company) {
        List<UserResponse> resultList = new ArrayList<>();
        MailService instance = MailService.getInstance();
        for (UserResource userResource : allUser) {
            if (userResource.getEmail().contains("@")) {
                try {
                    instance.sendMail(userResource.getEmail(), company, userResource.getSecureCode());
                    UserResponse userResponse = UserResponse.builder()
                            .email(userResource.getEmail())
                            .secureCode(userResource.getSecureCode())
                            .isNotificationSend(true)
                            .build();
                    resultList.add(userResponse);
                } catch (MessagingException e) {
                    UserResponse userResponse = UserResponse.builder()
                            .email(userResource.getEmail())
                            .secureCode(userResource.getSecureCode())
                            .isNotificationSend(false)
                            .build();
                    resultList.add(userResponse);
                    log.warn("Was not able to notify user : userResource.getEmail()");
                }
            } else {
                UserResponse userResponse = UserResponse.builder()
                        .email(userResource.getEmail())
                        .secureCode(userResource.getSecureCode())
                        .isNotificationSend(false)
                        .build();
                resultList.add(userResponse);
            }
        }
        return resultList;
    }

    public NotifyUserResponse notifyUser(NotifyUserResource userResource) throws MessagingException, UserException {
        MailService instance = MailService.getInstance();
        Optional<Tirage> userTirage = tirageRepository.getUserTirage(userResource.getUserToNotify().getEmail(), userResource.getCompany());
        if (userTirage.isPresent()) {
            Tirage tirage = userTirage.get();
            instance.sendMail(tirage.getEmail(), tirage.getCompany(), tirage.getSecoreCode());
            return NotifyUserResponse.of(userResource.getUserToNotify().getEmail(), userResource.getCompany(), true);
        } else {
            throw new UserException(ErrorCodesEnum.NOTIFY_PARTICIPANT_FAILED);
        }
    }
}