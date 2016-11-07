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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class})
@ActiveProfiles({"default", "development"})
public class ExtentDaoTest {
    @Autowired
    private ApplicationContext applicationContext;
    private ExtentDao extentDao;

    @Before
    public void setUp() {
        extentDao = (ExtentDao)applicationContext.getBean("extentDao");
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