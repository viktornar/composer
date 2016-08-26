package com.github.viktornar.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Extent {
    @Getter
    @Setter
    private Double xmin = 0.0;
    @Getter
    @Setter
    private Double ymin = 0.0;
    @Getter
    @Setter
    private Double xmax = 0.0;
    @Getter
    @Setter
    private Double ymax = 0.0;
}
