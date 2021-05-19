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
    public void run(String... args) throws Exception {
        Flux<User> nombres = Flux.just("Cielo", "Soledad", "Leo", "Sayen")
                .map(name -> {
                    return new User(name.toUpperCase(),null);
                } )
                .doOnNext(user -> {
                    if (Optional.ofNullable(user).isPresent()) {
                        System.out.println(user);
                    } else {
                        throw new RuntimeException("Nombres no pueden ser vacios");
                    }
                })
                .map(user -> {
                    String nombre = user.getName();
                    user.setName(nombre.toLowerCase());
                    return user;
                });

        nombres.subscribe(user -> log.info(user.toString()), error -> log.error(error.getMessage()), new Runnable() {
            @Override
            public void run() {
                log.info("Ha finalizado la ejecucion del observable con exito!");
            }
        });
    }
}
