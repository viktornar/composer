package com.github.viktornar.service.repository;

import com.github.viktornar.model.Atlas;
import java.util.List;

public interface Repository {
    void createAtlas(Atlas atlas);
    void updateAtlas(Atlas atlas);
    Atlas getAtlasById(String id);
    List<Atlas> getAllAtlases();
}
