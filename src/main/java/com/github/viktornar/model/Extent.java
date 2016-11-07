/*
 This file is part of Composer.
 Composer is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.
 Copyright 2016 (C) Viktor Nareiko
 */
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
