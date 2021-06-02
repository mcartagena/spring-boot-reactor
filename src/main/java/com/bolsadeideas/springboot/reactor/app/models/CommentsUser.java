package com.bolsadeideas.springboot.reactor.app.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentsUser {
    private User user;
    private Comments comments;
}
