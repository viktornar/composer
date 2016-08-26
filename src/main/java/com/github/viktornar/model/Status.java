package com.github.viktornar.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Status {
    @Getter
    @Setter
    Boolean status;
    @Getter
    @Setter
    String message;
}
