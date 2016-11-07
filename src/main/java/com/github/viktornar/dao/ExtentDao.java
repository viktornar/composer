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

import com.github.viktornar.migration.dao.DaoHelper;
import com.github.viktornar.model.Extent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.String.format;

@Component
public class ExtentDao extends BaseDao {
    private static final String EXTENT_COLUMNS = "xmin, ymin, xmax, ymax";
    private ExtentRowMapper extentRowMapper = new ExtentRowMapper();

    @Autowired
    ExtentDao(DaoHelper _daoHelper) {
        super(_daoHelper);
    }

    public void create(Extent extent) {
        String sql = format("INSERT INTO EXTENT (%s) VALUES (%s)", EXTENT_COLUMNS, questionMarks(EXTENT_COLUMNS));

        KeyHolder keyHolder = new GeneratedKeyHolder();

        getJdbcTemplate().update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setDouble(1, extent.getXmin());
            ps.setDouble(2, extent.getYmin());
            ps.setDouble(3, extent.getXmax());
            ps.setDouble(4, extent.getYmax());
            return ps;
        }, keyHolder);

        extent.setId((Integer)keyHolder.getKey());
    }

    public Extent getById(Integer id) {
        String sql = String.format("SELECT %s FROM extent WHERE id=?", EXTENT_COLUMNS);
        Extent extent = queryOne(sql, extentRowMapper, id);
        extent.setId(id);
        return extent;
    }

    /**
     * Inner class for mapping results to <code>Extent.class</code>.
     */
    private class ExtentRowMapper implements RowMapper<Extent> {
        public Extent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Extent(
                    null,
                    rs.getDouble(1),
                    rs.getDouble(2),
                    rs.getDouble(3),
                    rs.getDouble(4)
            );
        }
    }
}
