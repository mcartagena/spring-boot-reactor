package com.bolsadeideas.springboot.reactor.app;

import com.bolsadeideas.springboot.reactor.app.models.Comments;
import com.bolsadeideas.springboot.reactor.app.models.CommentsUser;
import com.bolsadeideas.springboot.reactor.app.models.User;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Slf4j
@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        exampleBackPresure();
    }

    public void exampleBackPresure() {
        Flux.range(1, 10)
                .log()
                //.limitRate(5)
                .subscribe(new Subscriber<Integer>() {
                               private Subscription s;
                               private Integer limite = 5;
                               private Integer consumed = 0;

                               @Override
                               public void onSubscribe(Subscription s) {
                                   this.s = s;
                                   s.request(limite);
                               }

                               @Override
                               public void onNext(Integer integer) {
                                   log.info(integer.toString());
                                   consumed++;
                                   if (consumed == limite) {
                                       consumed = 0;
                                       s.request(limite);
                                   }
                               }

                               @Override
                               public void onError(Throwable t) {

                               }

                               @Override
                               public void onComplete() {

                               }
                           }
                );

    }

    public void exampleInfiniteIntervalFromCreate() throws InterruptedException {

        Flux.create(emitter -> {
            Timer timer = new Timer();
            timer.schedule(
                    new TimerTask() {

                        private Integer contador = 0;

                        @Override
                        public void run() {
                            emitter.next(++contador);
                            if (contador == 10) {
                                timer.cancel();
                                emitter.complete();
                            }
                            if (contador == 5) {
                                timer.cancel();
                                emitter.error(
                                        new InterruptedException("Error, the flux has stoped in 5!")
                                );
                            }
                        }
                    }
                    , 1000, 1000);
        })
//                .doOnNext(next -> log.info(next.toString()))
//                .doOnComplete(() -> log.info("Hemos terminado"))
                .subscribe(next -> log.info(next.toString()),
                        error -> log.error(error.getMessage()),
                        () -> log.info("We are finished!"));

    }

    public void exampleInfiniteInterval() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown)
                .flatMap(number -> {
                    if (number > 5) {
                        return Flux.error(new InterruptedException("only 5!"));
                    } else {
                        return Flux.just(number);
                    }
                })
                .map(number -> "Hello " + number)
                .retry(2)
                .subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

        latch.await();

    }

    public void exampleDelayElements() {
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> log.info(i.toString()));

        rango.blockLast();

    }

    public void exampleInterval() {
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
        rango.zipWith(retraso, (origin, target) -> origin)
                .doOnNext(i -> log.info(i.toString()))
                .blockLast();
    }

    public void exampleZipWithRanges() {
        Flux.just(1, 2, 3, 4)
                .map(i -> i * 2)
                .zipWith(Flux.range(0, 4), (origin, target) -> String.format("First Flux: %d, Second Flux: %d", origin, target))
                .subscribe(text -> log.info(text));
    }

    public void userCommentsZipWithFormat2() {
        Mono<User> monoUser = Mono.fromCallable(() -> new User("John", "Doe"));
        Mono<Comments> userComments = Mono.fromCallable(() -> {
                    Comments comments = new Comments();
                    comments.addComment("Hola Marcelo, que tal?");
                    comments.addComment("Estoy estudiando Ingles");
                    comments.addComment("Yo estudio Aleman");
                    comments.addComment("Despues hare deporte");
                    return comments;
                }
        );

        Mono<CommentsUser> commentsUserMono = monoUser.zipWith(userComments)
                .map(tuple -> {
                    User user = tuple.getT1();
                    Comments comments = tuple.getT2();
                    return new CommentsUser(user, comments);
                });

        commentsUserMono.subscribe(commentsUser -> log.info(commentsUser.toString()));
    }

    public void userCommentsZipWith() {
        Mono<User> monoUser = Mono.fromCallable(() -> new User("John", "Doe"));
        Mono<Comments> userComments = Mono.fromCallable(() -> {
                    Comments comments = new Comments();
                    comments.addComment("Hola Marcelo, que tal?");
                    comments.addComment("Estoy estudiando Ingles");
                    comments.addComment("Yo estudio Aleman");
                    comments.addComment("Despues hare deporte");
                    return comments;
                }
        );

        Mono<CommentsUser> commentsUserMono = monoUser.zipWith(userComments, (user, comments) -> new CommentsUser(user, comments));

        commentsUserMono.subscribe(commentsUser -> log.info(commentsUser.toString()));
    }

    public void userCommentsFlapMap() {
        Mono<User> monoUser = Mono.fromCallable(() -> new User("John", "Doe"));
        Mono<Comments> userComments = Mono.fromCallable(() -> {
                    Comments comments = new Comments();
                    comments.addComment("Hola Marcelo, que tal?");
                    comments.addComment("Estoy estudiando Ingles");
                    comments.addComment("Yo estudio Aleman");
                    comments.addComment("Despues hare deporte");
                    return comments;
                }
        );

        monoUser.flatMap(user -> userComments.map(comments -> new CommentsUser(user, comments)))
                .subscribe(commentsUser -> log.info(commentsUser.toString()));
    }

    private void collectList() {
        List<User> usuariosList = new ArrayList<>();
        usuariosList.add(new User("Cielo", "Cartagena"));
        usuariosList.add(new User("Soledad", "Cartagena"));
        usuariosList.add(new User("Leo", "Cartagena"));
        usuariosList.add(new User("Sayen", "Cartagena"));
        usuariosList.add(new User("Mariela", "Orellana"));
        usuariosList.add(new User("Solange", "Troy"));

        Flux.fromIterable(usuariosList)
                .collectList()
                .subscribe(userList -> userList.forEach(
                        item -> log.info(item.toString())
                        )
                );
    }

    private void converToString() {
        List<User> usuariosList = new ArrayList<>();
        usuariosList.add(new User("Cielo", "Cartagena"));
        usuariosList.add(new User("Soledad", "Cartagena"));
        usuariosList.add(new User("Leo", "Cartagena"));
        usuariosList.add(new User("Sayen", "Cartagena"));
        usuariosList.add(new User("Mariela", "Orellana"));
        usuariosList.add(new User("Solange", "Troy"));

        Flux.fromIterable(usuariosList)
                .map(user -> user.getName().toUpperCase().concat(" ").concat(user.getLastname().toUpperCase()))
                .flatMap(name -> {
                            if (name.contains("CARTAGENA")) {
                                return Mono.just(name);
                            } else {
                                return Mono.empty();
                            }
                        }
                )
                .map(name -> {
                    return name.toLowerCase();
                })
                .subscribe(user -> log.info(user.toString()));
    }

    private void iterableFlapMap() {
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Cielo Cartagena");
        usuariosList.add("Soledad Cartagena");
        usuariosList.add("Leo Cartagena");
        usuariosList.add("Sayen Cartagena");
        usuariosList.add("Mariela Orellana");
        usuariosList.add("Solange Troy");

        Flux.fromIterable(usuariosList)
                .map(name -> new User(name.split(" ")[0].toUpperCase(),
                        name.split(" ")[1].toUpperCase()))
                .flatMap(user -> {
                            if (user.getLastname().equalsIgnoreCase("Cartagena")) {
                                return Mono.just(user);
                            } else {
                                return Mono.empty();
                            }
                        }
                )
                .map(user -> {
                    String name = user.getName();
                    user.setName(name.toLowerCase());
                    return user;
                })
                .subscribe(user -> log.info(user.toString()));
    }

    private void iterableExample() {
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Cielo Cartagena");
        usuariosList.add("Soledad Cartagena");
        usuariosList.add("Leo Cartagena");
        usuariosList.add("Sayen Cartagena");
        usuariosList.add("Mariela Orellana");
        usuariosList.add("Solange Troy");

/*        Flux<User> nombres = Flux.just("Cielo Cartagena",
                "Soledad Cartagena",
                "Leo Cartagena",
                "Sayen Cartagena",
                "Mariela Orellana",
                "Solange Troy")*/
        Flux<User> nombres = Flux.fromIterable(usuariosList)
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
