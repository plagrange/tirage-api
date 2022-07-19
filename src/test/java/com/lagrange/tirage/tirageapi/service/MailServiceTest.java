package com.lagrange.tirage.tirageapi.service;

import com.lagrange.tirage.tirageapi.exceptions.ErrorCodesEnum;
import com.lagrange.tirage.tirageapi.exceptions.UserException;
import com.lagrange.tirage.tirageapi.services.MailService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.Transport;

@SpringBootTest
@ActiveProfiles("test")
class MailServiceTest {

    @Autowired
    private MailService mailService;

    @MockBean
    private Transport transport;

    @BeforeEach
    public void setUp() {

    }

    @Test
    @SneakyThrows
    void testSendMail(){

        UserException userException = Assertions.assertThrows(UserException.class, () -> {

            mailService.sendMail("test1@lagrangien.fr", "COMPANY", "TEST1");
        });

        Assertions.assertEquals(userException.getErrorCode(), ErrorCodesEnum.NOTIFY_PARTICIPANT_FAILED.name());

    }

}
