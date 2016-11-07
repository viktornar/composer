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
package com.github.viktornar.dao;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.viktornar.utils.Helper.getRandomlyNames;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class})
@ActiveProfiles({"default", "development"})
public class AtlasDaoTest {
    @Autowired
    private ApplicationContext applicationContext;
    private AtlasDao atlasDao;
    private ExtentDao extentDao;
    private List<Atlas> atlases;

    @Before
    public void setUp() {
        atlasDao = (AtlasDao) applicationContext.getBean("atlasDao");
        extentDao = (ExtentDao) applicationContext.getBean("extentDao");

        List<Extent> extents = Arrays.asList(
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

        atlases = new ArrayList<>();
        extents.forEach((extent) -> {
            extentDao.create(extent);
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
                    extent.getId()
            );

            atlas.setExtent(extent);
            atlases.add(atlas);
        });
    }

    @Test
    public void update() throws Exception {
        atlases.forEach((atlas) -> {
            atlasDao.create(atlas);
        });
    }

    @Test
    public void updateById() throws Exception {
        atlases.forEach((atlas) -> {
            atlasDao.create(atlas);
            atlas.setProgress(atlas.getColumns());
            atlasDao.update(atlas);
        });
    }

    @Test
    public void getById() throws Exception {
        atlases.forEach((atlas) -> {
            String id = atlas.getId();
            atlasDao.create(atlas);
            Atlas _atlas = atlasDao.getById(id);
            assertEquals(id, _atlas.getId());
        });
    }

    @Test
    public void getAll() throws Exception {
        atlases.forEach((atlas) -> {
            atlasDao.create(atlas);
        });

        List<Atlas> _atlases = atlasDao.getAll();

        assertNotNull(_atlases);
        assertTrue(_atlases.size() > 0);
    }
};