package sg.edu.ntu.taskflow_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class for the Taskflow API Spring Boot application.
 * Initializes and runs the application.
 * 
 * @SpringBootApplication annotation indicates that this is a Spring Boot
 *                        application.
 */
@SpringBootApplication
public class TaskflowApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskflowApiApplication.class, args);
	}

}
