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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class})
@ActiveProfiles({"default", "development"})
public class AtlasDaoTest {
    @Autowired
    private ApplicationContext applicationContext;
    private AtlasDao atlasDao;
    private ExtentDao extentDao;
    private List<Atlas> atlases;

    @Before
    public void setUp() {
        atlasDao = (AtlasDao)applicationContext.getBean("atlasDao");
        extentDao = (ExtentDao)applicationContext.getBean("extentDao");

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
        extents.forEach((extent)->{
            extentDao.update(extent);
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
        atlases.forEach((atlas)->{
            atlasDao.update(atlas);
        });
    }

    @Test
    public void getById() throws Exception {
        atlases.forEach((atlas)->{
            String id = atlas.getId();
            atlasDao.update(atlas);
            Atlas _atlas = atlasDao.getById(id);
            assertEquals(id, _atlas.getId());
        });
    }

    @Test
    public void getAll() throws Exception {
        atlases.forEach((atlas)->{
            atlasDao.update(atlas);
        });

        List<Atlas> _atlases = atlasDao.getAll();

        assertNotNull(_atlases);
        assertTrue(_atlases.size() > 0);
    }
};