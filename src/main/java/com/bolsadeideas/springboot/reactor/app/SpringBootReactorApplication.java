package com.bolsadeideas.springboot.reactor.app;

import com.bolsadeideas.springboot.reactor.app.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Slf4j
@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Flux<User> nombres = Flux.just("Cielo Cartagena",
                "Soledad Cartagena",
                "Leo Cartagena",
                "Sayen Cartagena",
                "Mariela Orellana",
                "Solange Troy")
                .map(name -> new User(name.split(" ")[0].toUpperCase(),
                name.split(" ")[1].toUpperCase()))
                .filter(user -> user.getLastname().equalsIgnoreCase("Cartagena"))
                .doOnNext(user -> {
                    if (Optional.ofNullable(user).isEmpty()) {
                        throw new RuntimeException("Nombres no pueden ser vacios");
                    } else {
                        System.out.println(user);
                    }
                })
                .map(user -> {
                    String name = user.getName();
                    user.setName(name.toLowerCase());
                    return user;
                });

        nombres.subscribe(user -> log.info(user.toString()), error -> log.error(error.getMessage()), () -> log.info("Ha finalizado la ejecucion del observable con exito!"));
    }
}
