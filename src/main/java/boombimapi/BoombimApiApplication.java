package boombimapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BoombimApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoombimApiApplication.class, args);
	}

}
