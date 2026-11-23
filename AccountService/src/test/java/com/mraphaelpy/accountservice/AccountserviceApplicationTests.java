package com.mraphaelpy.accountservice;

import com.mraphaelpy.accountservice.support.JwtTestTokens;
import com.mraphaelpy.accountservice.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = JwtTestTokens.SECRET_PROPERTY)
@Import(TestcontainersConfiguration.class)
class AccountserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
