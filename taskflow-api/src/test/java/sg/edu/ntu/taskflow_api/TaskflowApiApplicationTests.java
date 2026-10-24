package sg.edu.ntu.taskflow_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class for the Taskflow API Spring Boot application.
 * Ensures that the application context loads correctly.
 * 
 * The {@code @SpringBootTest} annotation loads the application with a dummy API
 * key. This test does not invoke the model or require real credentials.
 */
@SpringBootTest(properties = "spring.ai.openai.api-key=test-only-not-a-real-key")
class TaskflowApiApplicationTests {

	/**
	 * Test to ensure that the application context loads successfully.
	 */
	@Test
	void contextLoads() {
	}
}
