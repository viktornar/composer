package com.github.viktornar.service.repository;

import com.github.viktornar.dao.AtlasDao;
import com.github.viktornar.dao.ExtentDao;
import com.github.viktornar.model.Atlas;
import com.github.viktornar.model.Extent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class RepositoryDao implements Repository {
    private AtlasDao atlasDao;
    private ExtentDao extentDao;

    @Autowired
    public RepositoryDao(AtlasDao _atlasDao, ExtentDao _extentDao) {
        atlasDao = _atlasDao;
        extentDao = _extentDao;
    }

    @Override
    public void createAtlas(Atlas atlas) {
        Extent extent = atlas.getExtent();
        extentDao.create(extent);
        atlas.setExtentId(extent.getId());
        atlasDao.create(atlas);
    }

    @Override
    public void updateAtlas(Atlas atlas) {
        atlasDao.update(atlas);
    }

    @Override
    public Atlas getAtlasById(String id) {
        Atlas atlas = atlasDao.getById(id);
        Extent extent = extentDao.getById(atlas.getExtentId());
        atlas.setExtent(extent);
        return atlas;
    }

    @Override
    public List<Atlas> getAllAtlases() {
        List<Atlas> atlases = atlasDao.getAll();

        atlases.forEach((atlas) -> {
            Extent extent = extentDao.getById(atlas.getExtentId());
            atlas.setExtent(extent);
        });

        return atlases;
    }
}
