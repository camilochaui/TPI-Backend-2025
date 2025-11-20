package org.example.serviciocliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
 
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServicioClienteApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioClienteApplication.class, args);
    }

}
