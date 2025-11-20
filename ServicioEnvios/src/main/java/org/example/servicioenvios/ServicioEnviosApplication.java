package org.example.servicioenvios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
 
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServicioEnviosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioEnviosApplication.class, args);
    }

}
