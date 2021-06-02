package com.bolsadeideas.springboot.reactor.app.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Comments {
    List<String> comments = new ArrayList<>();
    public void addComment(String comment){
        this.comments.add(comment);
    }
}
