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
package com.github.viktornar.service.repository;

import com.github.viktornar.configuration.ApplicationConfig;
import com.github.viktornar.model.Atlas;
import com.github.viktornar.model.Extent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static com.github.viktornar.utils.Helper.getRandomlyNames;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class})
@ActiveProfiles({"default", "development"})
public class RepositoryTest {
    @Autowired
    private ApplicationContext applicationContext;
    Repository repository;
    private List<Extent> extents;

    @Before
    public void setUp() throws Exception {
        repository = (Repository) applicationContext.getBean("repository");
        extents = Arrays.asList(
                new Extent(
                        null,
                        1.0,
                        1.0,
                        1.0,
                        1.0
                ),
                new Extent(
                        null,
                        2.0,
                        2.0,
                        2.0,
                        2.0
                )
        );
    }

    @Test
    public void createAtlas() throws Exception {
        extents.forEach((extent) -> {
            Atlas atlas = new Atlas(
                    getRandomlyNames(8, 1)[0],
                    "atlas",
                    "atlas",
                    1,
                    1,
                    "portrait",
                    "letter",
                    0,
                    0,
                    null
            );

            atlas.setExtent(extent);
            repository.createAtlas(atlas);
        });
    }

    @Test
    public void updateAtlas() throws Exception {
        extents.forEach((extent) -> {
            String id = getRandomlyNames(8, 1)[0];
            Atlas atlas = new Atlas(
                    id,
                    "atlas",
                    "atlas",
                    1,
                    1,
                    "portrait",
                    "letter",
                    0,
                    0,
                    null
            );

            atlas.setExtent(extent);
            repository.createAtlas(atlas);

            atlas = repository.getAtlasById(id);
            atlas.setProgress(5);

            repository.updateAtlas(atlas);
        });
    }
}