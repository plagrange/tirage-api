/**
 * 
 */
package com.lagrange.tirage.tirageapi.services.impl;

/**
 * @author pmekeze
 *
 */

import com.lagrange.tirage.tirageapi.entity.Tirage;
import com.lagrange.tirage.tirageapi.entity.TirageParameter;
import com.lagrange.tirage.tirageapi.model.*;
import com.lagrange.tirage.tirageapi.persistence.ParameterRepository;
import com.lagrange.tirage.tirageapi.persistence.TirageRepository;
import com.lagrange.tirage.tirageapi.services.MailService;
import com.lagrange.tirage.tirageapi.services.interfaces.ITirageCoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class TirageCoreService implements ITirageCoreService {

	private TirageRepository tirageRepository;
	private ParameterRepository parameterRepository;

	public TirageCoreService(TirageRepository tirageRepository,ParameterRepository parameterRepository ){
		this.tirageRepository = tirageRepository;
		this.parameterRepository = parameterRepository;
	}

	public List<UserResponse> initListParticipant(CreateTirageRequest createTirage) throws Exception {
  
		List<UserResource> userResourceList = createTirage.getUsers();

		 List<String> admins = new ArrayList<>();

		for (UserResource userDto : userResourceList) {
			if(userDto.getEmail()!=null && userDto.getSecureCode()!=null){
				Tirage tirage = Tirage.builder()
						.email(userDto.getEmail())
						.secoreCode(userDto.getSecureCode())
						.company(createTirage.getCompany())
						.admin(userDto.isAdmin())
						.createDate(new Date())
						.updateDate(new Date())
						.build();
				if(userDto.isAdmin())
					admins.add(userDto.getEmail());
				try {
					tirageRepository.save(tirage);
					log.info("user '"+userDto.getEmail()+"' and secureCode '"+userDto.getSecureCode() +"' for company '" + createTirage.getCompany() + "' persist with success" );
				} catch (Exception ex) {
					log.error("error caught when tring to persist user '"+userDto.getEmail() + "' and secureCode '"+userDto.getSecureCode() +"' for company '" + createTirage.getCompany(), ex.getCause());
					throw new Exception( ex.getCause());
				}
			}
		}
		List<UserResponse> listUser = initTirage(createTirage.getCompany(), createTirage.isNotificationEnabled(), admins);
		if(createTirage.isNotificationEnabled()){
		    for(UserResponse userResponse : listUser){
				Optional<Tirage> userTirage = tirageRepository.getUserTirage(userResponse.getEmail(), createTirage.getCompany());
				Tirage tirage = userTirage.get();
				tirage.setNotify(userResponse.isNotificationSend());
				tirage.setUpdateDate(new Date());
				tirageRepository.save(tirage);
		    }
		}
		return listUser;
	}
	
	public List<UserResponse> initTirage(String company, boolean notificationEnabled, List<String> admins) throws Exception {
		List<UserResponse> result = new ArrayList<UserResponse>();
		
		try {
			List<UserResource> allUser = tirageRepository.getAllUser(company);
			
			if (allUser != null && allUser.size() != 0) {
				List<String> remainingNumbers;
				remainingNumbers = IntStream.rangeClosed(1, allUser.size()).mapToObj(String::valueOf).collect(Collectors.toList());
				TirageParameter tirageParams = TirageParameter.builder()
						.nbParticipant(allUser.size())
						.remainingNumbers(remainingNumbers.stream().collect(Collectors.joining(";")))
						.company(company)
						.adminList(admins.stream().collect(Collectors.joining(";")))
						.createDate(new Date())
						.updateDate(new Date())
						.build();
				parameterRepository.save(tirageParams);
				
				if( notificationEnabled){
					return sendMailToUsers(allUser, company);
				}else{
					for(UserResource user : allUser){
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
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex.getCause());
			throw new Exception( ex.getCause());
		}
		return result;
	}

	public List<UserResource> getListParticipantFromDB(String company){
		List<UserResource> allUser = tirageRepository.getAllUser(company);
		return allUser;
	}

	public int doTirage(String email, String company) throws Exception {
		Tirage tirageUser = verifyUserAlreadyDoTirage(email, company);
		if (tirageUser != null && tirageUser.getOrderNumber() != 0) {
			return tirageUser.getOrderNumber();
		}
		Integer taken_number = 0;
		TirageParameter parameter = null;
		try {
			Optional<TirageParameter> tirageParameterByCompany = parameterRepository.findTirageParameterByCompany(company);
			if (tirageParameterByCompany.isPresent()) {

				parameter = tirageParameterByCompany.get();
				String remainingNumbers = parameter.getRemainingNumbers();
				List<String> remainingNumberList = Arrays.stream(remainingNumbers.split(";")).collect(Collectors.toList());

				String takenNumbers = parameter.getTakenNumbers();
				List<String> takenNumberList = new ArrayList<>();
				if(takenNumbers!=null)
					takenNumberList = Arrays.stream(takenNumbers.split(";")).collect(Collectors.toList());

				Random rand = new Random();
				int numberTake = rand.nextInt(remainingNumberList.size());
				taken_number = Integer.valueOf(remainingNumberList.get(numberTake));
				remainingNumberList.remove(numberTake);
				takenNumberList.add(String.valueOf(taken_number));

				parameter.setRemainingNumbers(remainingNumberList.stream().collect(Collectors.joining(";")));
				parameter.setTakenNumbers(takenNumberList.stream().collect(Collectors.joining(";")));

				parameter.setUpdateDate(new Date());
				parameterRepository.save(parameter);

				tirageUser.setEmail(email);
				tirageUser.setOrderNumber(taken_number);
				tirageUser.setCompany(company);
				tirageUser.setUpdateDate(new Date());

				tirageRepository.save(tirageUser);
			}else {
				//TODO, namage case tirage parameter not found
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex.getCause());
			throw new Exception( ex.getCause());
		}
		return taken_number;
	}

	public boolean authenticateByDB(String email, String criteria, String company) throws Exception {
		boolean autenticate = false;
		try {
			Optional<Tirage> userTirage = tirageRepository.getUserTirage(email, company);
			if(userTirage.isPresent()){
				Tirage tirage = userTirage.get();
				autenticate = tirage.getSecoreCode().equals(criteria);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex.getCause());
			throw new Exception( ex.getCause());
		}
		return autenticate;
	}

	public boolean authenticateAdminByDB(String email, String criteria, String company) throws Exception {
        boolean autenticate = false;
        TirageParameter tirageParameter = null;
        try {
			Optional<TirageParameter> tirageParameterByCompany = parameterRepository.findTirageParameterByCompany(company);
			if(tirageParameterByCompany.isPresent())
				tirageParameter = tirageParameterByCompany.get();
			else{
				//TODO, namage case tirage parameter not found
			}
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex.getCause());
            throw new Exception( ex.getCause());
        }
        
        if(tirageParameter!=null){
			String admins = tirageParameter.getAdminList();
			List<String> adminList = Arrays.stream(admins.split(";")).collect(Collectors.toList());
            for(String admin : adminList){
				Optional<Tirage> userTirageOptional = tirageRepository.getUserTirage(admin, company);
				if(userTirageOptional.isPresent()) {
					Tirage userTirage = userTirageOptional.get();
					if (userTirage.getEmail().equals(email)) {
						if (userTirage.getSecoreCode().equals(criteria)) {
							autenticate = true;
						} else {
							log.warn("bad admin password provided: provide = " + criteria + " and expected = " + userTirage.getSecoreCode());
						}
					}
				}else{
					//TODO, namage case tirage parameter not found
				}
            }
        }
        
        return autenticate;
    }
	
	public List<UserTirageResponse> getResultList(String company) throws Exception {

		List<UserTirageResponse> result = null;
		try {
			result = tirageRepository.getTirageResult(company);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex.getCause());
			throw new Exception( ex.getCause());
		}

		Collections.sort(result, new Comparator<UserTirageResponse>() {
			public int compare(UserTirageResponse result1, UserTirageResponse result2) {
				Integer integer1 = result1.getOrderNumber();
				Integer integer2 = result2.getOrderNumber();
				return integer1.compareTo(integer2);
			}
		});
		return result;
	}

	public Tirage verifyUserAlreadyDoTirage(String email, String company) throws Exception {
		Tirage tirage = null;
		try {
			Optional<Tirage> userTirage = tirageRepository.getUserTirage(email, company);
			if(userTirage.isPresent()){
				tirage = userTirage.get();
			}else{
				//TODO, namage case tirage parameter not found
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex.getCause());
			throw new Exception( ex.getCause());
		}

		return tirage;
	}
	
	public boolean verifyCompanyAlreadyExist(String company) throws Exception {
		try {
			Optional<TirageParameter> tirageParameterByCompany = parameterRepository.findTirageParameterByCompany(company);
			return tirageParameterByCompany.isPresent();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex.getCause());
			throw new Exception( ex.getCause());
		}
	}
	
	private List<UserResponse> sendMailToUsers(List<UserResource> allUser, String company){
		List<UserResponse> resultList = new ArrayList<UserResponse>();
		MailService instance = MailService.getInstance();
		for(UserResource userResource : allUser){
			if(userResource.getEmail().contains("@")){
				try{
					instance.sendMail(userResource.getEmail(), company, userResource.getSecureCode());
					UserResponse userResponse = UserResponse.builder()
							.email(userResource.getEmail())
							.secureCode(userResource.getSecureCode())
							.isNotificationSend(true)
							.build();
					resultList.add(userResponse);
				}catch(MessagingException e){
					UserResponse userResponse = UserResponse.builder()
							.email(userResource.getEmail())
							.secureCode(userResource.getSecureCode())
							.isNotificationSend(false)
							.build();
					resultList.add(userResponse);
				}
			}else{
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
	
	public NotifyUserResponse notifyUser(NotifyUserResource userResource) throws Exception {
		
		NotifyUserResponse notifyUserResponse = null;
		MailService instance = MailService.getInstance();
		try{
			Optional<Tirage> userTirage = tirageRepository.getUserTirage(userResource.getUserToNotify().getEmail(), userResource.getCompany());
			if(userTirage.isPresent()) {
				Tirage tirage = userTirage.get();
				instance.sendMail(tirage.getEmail(), tirage.getCompany(), tirage.getSecoreCode());
				notifyUserResponse = NotifyUserResponse.builder()
						.email(userResource.getUserToNotify().getEmail())
						.company(userResource.getCompany())
						.isNotificationSend(true)
						.build();
			}else{
				//TODO Manage here case user not found on database
			}
		}catch( Exception e){
			log.error("Unable to notify user '" + userResource.getUserToNotify().getEmail() + "' by email", e);
			throw new Exception(e.getCause());
		}
		
		return notifyUserResponse;
	}
}