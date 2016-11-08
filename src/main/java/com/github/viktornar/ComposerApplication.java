/*
 This file is part of Composer.
 Composer is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 Composer is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Composer.  If not, see <http://www.gnu.org/licenses/>.
 Copyright 2016 (C) Viktor Nareiko
 */
package com.github.viktornar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ComposerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComposerApplication.class, args);
    }
}
