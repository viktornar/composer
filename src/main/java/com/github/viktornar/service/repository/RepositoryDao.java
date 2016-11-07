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
