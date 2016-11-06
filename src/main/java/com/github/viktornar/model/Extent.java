package com.github.viktornar.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Extent {
    public Extent() {
    }

    public Extent(
            Integer _id,
            Double _xmin,
            Double _ymin,
            Double _xmax,
            Double _ymax
    ) {
        id = _id;
        xmin = _xmin;
        ymin = _ymin;
        xmax = _xmax;
        ymax = _ymax;
    }

    @Getter
    @Setter
    private Integer id;
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
