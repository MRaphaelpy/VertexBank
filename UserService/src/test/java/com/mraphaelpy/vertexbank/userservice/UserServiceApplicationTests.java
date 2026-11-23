package com.mraphaelpy.vertexbank.userservice;

import com.mraphaelpy.vertexbank.userservice.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "security.jwt.secret-key=T17h6qB7hY00dzNkb2b0X7gcPAiWfoeuC1KYRQpB51aTSD0ev42rUob41NCWD1rUHKekpRtY5cmviFe8YdQ03w==")
@Import(TestcontainersConfiguration.class)
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
