package com.lagrange.tirage.tirageapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test"})
class TirageApiApplicationTests {

	@Value("${spring.jpa.hibernate.ddl-auto}")
	String dll;
	@Test
	void contextLoads() {
		Assertions.assertTrue(true);
		System.out.printf("dll value = " + dll);
	}

}
