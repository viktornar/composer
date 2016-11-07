package com.github.viktornar.dao;

import com.github.viktornar.migration.dao.DaoHelper;
import com.github.viktornar.model.Atlas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.lang.String.format;

@Component
public class AtlasDao extends BaseDao {
    private static final String ATLAS_COLUMNS =
            "id, " +
                    "atlas_name, " +
                    "atlas_folder, " +
                    "columns, " +
                    "rows, " +
                    "orientation, " +
                    "size, " +
                    "zoom, " +
                    "progress, " +
                    "extent_id";

    private AtlasRowMapper atlasRowMapper = new AtlasRowMapper();

    @Autowired
    public AtlasDao(DaoHelper _daoHelper) {
        super(_daoHelper);
    }

    public void create(Atlas atlas) {
        assert atlas.getExtent() != null;
        assert atlas.getId() != null;

        String sql = format("INSERT INTO ATLAS (%s) VALUES (%s)", ATLAS_COLUMNS, questionMarks(ATLAS_COLUMNS));
        update(
                sql,
                atlas.getId(),
                atlas.getAtlasName(),
                atlas.getAtlasFolder(),
                atlas.getColumns(),
                atlas.getRows(),
                atlas.getOrientation(),
                atlas.getSize(),
                atlas.getZoom(),
                atlas.getProgress(),
                atlas.getExtent().getId()
        );
    }

    public void update(Atlas atlas) {
        assert atlas.getExtent() != null;
        assert atlas.getId() != null;

        String sql = format("UPDATE ATLAS SET %s WHERE id=?", suffix(ATLAS_COLUMNS.replace("id, ", ""), "=?"));

        getJdbcTemplate().update(
                sql,
                atlas.getAtlasName(),
                atlas.getAtlasFolder(),
                atlas.getColumns(),
                atlas.getRows(),
                atlas.getOrientation(),
                atlas.getSize(),
                atlas.getZoom(),
                atlas.getProgress(),
                atlas.getExtent().getId(),
                atlas.getId()
        );
    }

    public Atlas getById(String id) {
        String sql = format("SELECT %s FROM ATLAS WHERE id=?", ATLAS_COLUMNS);
        return queryOne(sql, atlasRowMapper, id);
    }

    public List<Atlas> getAll() {
        String sql = format("SELECT %s FROM ATLAS", ATLAS_COLUMNS);
        return query(sql, atlasRowMapper);
    }

    /**
     * Inner class for mapping results to <code>Atlas.class</code>.
     */
    private class AtlasRowMapper implements RowMapper<Atlas> {
        public Atlas mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Atlas(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getInt(4),
                    rs.getInt(5),
                    rs.getString(6),
                    rs.getString(7),
                    rs.getInt(8),
                    rs.getInt(9),
                    rs.getInt(10)
            );
        }
    }
}
