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
package com.github.viktornar.dao;

import com.github.viktornar.configuration.ApplicationConfig;
import com.github.viktornar.model.Extent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class})
@ActiveProfiles({"default", "development"})
public class ExtentDaoTest {
    @Autowired
    private ApplicationContext applicationContext;
    private ExtentDao extentDao;

    @Before
    public void setUp() {
        extentDao = (ExtentDao) applicationContext.getBean("extentDao");
    }

    @Test
    public void getById() throws Exception {
        Extent extent = new Extent(
                null,
                1.0,
                1.0,
                1.0,
                1.0
        );

        extentDao.create(extent);
        assertNotNull(extent.getId());

        Extent extentById = extentDao.getById(extent.getId());
        assertNotNull(extentById);
        assertEquals(extent.getId(), extentById.getId());
    }
}