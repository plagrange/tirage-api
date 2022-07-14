package com.lagrange.tirage.tirageapi.persistence;

import com.lagrange.tirage.tirageapi.entity.Tirage;
import com.lagrange.tirage.tirageapi.model.UserResource;
import com.lagrange.tirage.tirageapi.model.UserTirageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
class TirageRepositoryTest {

    @Autowired
    private TirageRepository tirageRepository;


//    @Test
    void getUserTirageFound() {

        Optional<Tirage> optionalTirage = tirageRepository.getUserTirage("test1@lagrangien.fr", "TEST");
        assertThat(optionalTirage.isPresent(), is(true));
        assertThat(optionalTirage.get().getEmail(), is("test1@lagrangien.fr"));
    }

//    @Test
    void getUserTirageNotFound() {

        Optional<Tirage> optionalTirage = tirageRepository.getUserTirage("test3@lagrangien.fr", "TEST");
        assertThat(optionalTirage.isPresent(), is(false));
    }

//    @Test
    void testGetAllUser(){
        List<UserResource> userResourceList = tirageRepository.getAllUser("TEST");
        assertThat(userResourceList.size(), is(2));
    }

//    @Test
    void testGetTirageResult(){

        Optional<Tirage> tirageOptional = tirageRepository.getUserTirage("test1@lagrangien.fr", "TEST");
        Tirage tirage1 = tirageOptional.get();
        tirage1.setOrderNumber(2);
        tirageRepository.save(tirage1);

        Optional<Tirage> tirageOptional2 = tirageRepository.getUserTirage("test2@lagrangien.fr", "TEST");
        Tirage tirage2 = tirageOptional2.get();
        tirage2.setOrderNumber(1);
        tirageRepository.save(tirage2);

        List<UserTirageResponse> tirageResult = tirageRepository.getTirageResult("TEST");
        assertThat(tirageResult.size(), is(2));
        assertThat(tirageResult.get(0).getOrderNumber(), not(0));
        assertThat(tirageResult.get(1).getOrderNumber(), not(0));

    }
    @BeforeEach
    void setUp() {
        Tirage tirage1 = Tirage.builder()
                .email("test1@lagrangien.fr")
                .secoreCode("TEST1")
                .company("TEST")
                .admin(true)
                .createDate(new Date())
                .updateDate(new Date())
                .build();
        Tirage tirage2 = Tirage.builder()
                .email("test2@lagrangien.fr")
                .secoreCode("TEST2")
                .company("TEST")
                .admin(true)
                .createDate(new Date())
                .updateDate(new Date())
                .build();

        List<Tirage> tirageList = List.of(tirage1, tirage2);
        tirageList.forEach(tirage -> tirageRepository.save(tirage));
    }
}