package com.lagrange.tirage.tirageapi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lagrange.tirage.tirageapi.entity.Tirage;
import com.lagrange.tirage.tirageapi.model.*;
import com.lagrange.tirage.tirageapi.persistence.ParameterRepository;
import com.lagrange.tirage.tirageapi.services.impl.TirageCoreService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TirageController.class)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
    class TirageControllerTest {

    @MockBean
    private TirageCoreService tirageCoreService;

    @MockBean
    ParameterRepository parameterRepository;

    @InjectMocks
    private TirageController tirageController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    UserResponse userResponse1 = UserResponse.builder().email("pmekeze@yahoo.fr").secureCode("PAT").isNotificationSend(false).build();
    UserResponse userResponse2 = UserResponse.builder().email("pmekeze@hotmail.fr").secureCode("PAT").isNotificationSend(false).build();
    List<UserResponse> userResponseList = Arrays.asList(userResponse1, userResponse2);
    CreateTirageResponse createTirageResponse = CreateTirageResponse.of(userResponseList,"TEST", null);

    @Test
    void testCreateTirage() throws Exception {

        UserResource userResource1 = UserResource.builder().email("pmekeze@yahoo.fr").secureCode("PAT").admin(true).build();
        UserResource userResource2 = UserResource.builder().email("pmekeze@hotmail.fr").secureCode("PAT").admin(true).build();
        List<UserResource> userResourceList = Arrays.asList(userResource1, userResource2);

        //Given
        CreateTirageRequest createTirageRequest = CreateTirageRequest.of(userResourceList, "TEST", false);

        //when
        when(tirageCoreService.initListParticipant(createTirageRequest)).thenReturn(userResponseList);

        //then
        mockMvc.perform(post("/create-tirage").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTirageRequest)))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.userResponses").isArray())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.company").value("TEST"))
                        .andDo(document("createTirage", responseFields(
                                fieldWithPath("company").description("The company create by admin to represente this tirage"),
                                fieldWithPath("userResponses").description("The company create by admin to represente this tirage"),
                                fieldWithPath("userResponses[0].email").description("The company create by admin to represente this tirage"),
                                fieldWithPath("userResponses[0].secureCode").description("The company create by admin to represente this tirage"),
                                fieldWithPath("userResponses[0].notificationSend").description("The company create by admin to represente this tirage"),
                                fieldWithPath("message").description("The company create by admin to represente this tirage"))));
    }

    @SneakyThrows
    @Test
    void testDotirage(){

        //given
        UserTirageRequest userTirageRequest = UserTirageRequest.of("pmekeze@yahoo.fr", "TEST", "COMPANY");

        //When
        when(tirageCoreService.authenticateByDB(userTirageRequest.getEmail(), userTirageRequest.getSecureCode(),userTirageRequest.getCompany())).thenReturn(true);
        when(tirageCoreService.doTirage(userTirageRequest.getEmail(), userTirageRequest.getCompany())).thenReturn(1);

        //then
        mockMvc.perform(post("/perform-tirage").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userTirageRequest)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderNumber").value(1))
                .andDo(document("doTirage"));

    }

    @SneakyThrows
    @Test
    void testGetResults(){
        //given
        UserTirageRequest userTirageRequest = UserTirageRequest.of("admin@lagrangien.fr", "securecodeAdmin", "COMPANY");

        UserTirageResponse result1 = UserTirageResponse.builder().company("COMPANY").email("test@lagrangien.fr").orderNumber(2).build();
        UserTirageResponse result2 = UserTirageResponse.builder().company("COMPANY").email("test2@lagrangien.fr").orderNumber(1).build();
        List<UserTirageResponse> responseList = Arrays.asList(result1, result2);
        //when
        when(tirageCoreService.authenticateAdminByDB("admin@lagrangien.fr", "securecodeAdmin", "COMPANY")).thenReturn(true);
        when(tirageCoreService.getResultList( "COMPANY")).thenReturn(responseList);

        //then
        mockMvc.perform(post("/results").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userTirageRequest)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.company").value("COMPANY"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tirageResponseList").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tirageResponseList[0].company").value("COMPANY"))
                .andDo(document("getResults"));
    }

    @SneakyThrows
    @Test
    void testGetResult(){
        //given
        UserTirageRequest userTirageRequest = UserTirageRequest.of("test@lagrangien.fr", "TEST", "COMPANY");
        //when
        when(tirageCoreService.authenticateByDB(userTirageRequest.getEmail(), userTirageRequest.getSecureCode(),userTirageRequest.getCompany())).thenReturn(true);
        when(tirageCoreService.verifyUserAlreadyDoTirage("test@lagrangien.fr", "COMPANY")).thenReturn(true);
        when(tirageCoreService.getUserResult("test@lagrangien.fr", "COMPANY")).thenReturn(Tirage.builder().build());

        //then
        mockMvc.perform(post("/result").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userTirageRequest)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.company").value("COMPANY"))
                .andDo(document("getResultUser"));

    }

    @SneakyThrows
    @Test
    void testGetUsers(){
        //given
        UserTirageRequest userTirageRequest = UserTirageRequest.of("admin@lagrangien.fr", "securecodeAdmin", "COMPANY");

        UserResource userResource1 = UserResource.builder().email("test@lagrangien.fr").secureCode("TEST").admin(true).build();
        UserResource userResource2 = UserResource.builder().email("test2@lagrangien.fr").secureCode("TEST2").admin(false).build();
        List<UserResource> userResourceList = Arrays.asList(userResource1, userResource2);

        //when
        when(tirageCoreService.authenticateAdminByDB("admin@lagrangien.fr", "securecodeAdmin", "COMPANY")).thenReturn(true);
        when(tirageCoreService.getListParticipantFromDB( "COMPANY")).thenReturn(userResourceList);

        //then
        mockMvc.perform(post("/getusers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userTirageRequest)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.company").value("COMPANY"))
                .andDo(document("getUsers"));
    }

    @SneakyThrows
    @Test
    void testNotifyUser(){

        //given
        UserResource userResource = UserResource.builder().email("test@lagrangien.fr").secureCode("TEST").admin(true).build();
        UserResource adminResource = UserResource.builder().email("admin@lagrangien.fr").secureCode("securecodeAdmin").admin(true).build();;

        NotifyUserResource notifyUserResource = NotifyUserResource.of(adminResource, userResource, "COMPANY");

        NotifyUserResponse notifyUserResponse = NotifyUserResponse.of("test@lagrangien.fr", "COMPANY", true);
        //when
        when(tirageCoreService.notifyUser(any())).thenReturn(notifyUserResponse);

        //then
        mockMvc.perform(post("/notifyuser").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notifyUserResource)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.company").value("COMPANY"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test@lagrangien.fr"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.notificationSend").value(true))
                .andDo(document("notifyuser"));
    }

    @Test
    void testVerifyCompany() throws Exception {

        //when
        when(tirageCoreService.verifyCompanyAlreadyExist("company")).thenReturn(true);
        //then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/verifycompany/{company}", "company").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.companyExisted").value(true))
                .andDo(document("verifyCompany",
                RequestDocumentation.pathParameters(RequestDocumentation.parameterWithName("company").description("unique identifier for company"))));
    }

    @Test
    void testGetCompanies() throws Exception {

        //when
        when(tirageCoreService.getListExistedCompany()).thenReturn(Arrays.asList("TIRAGE1", "TIRAGE2"));
        //then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/companies").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andDo(document("getCompanies"));
    }

}
