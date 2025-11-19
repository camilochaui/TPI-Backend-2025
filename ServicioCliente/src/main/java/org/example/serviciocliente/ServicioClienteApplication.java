package org.example.serviciocliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient //Para registrarse en Eureka (o Consul)
@EnableFeignClients
public class ServicioClienteApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioClienteApplication.class, args);
    }

}
