package com.github.viktornar.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Atlas {
    public Atlas() {
    }

    public Atlas(
            String _id,
            String _atlasName,
            String _atlasFolder,
            Integer _columns,
            Integer _rows,
            String _orientation,
            String _size,
            Integer _zoom,
            Integer _progress,
            Integer _extentId
    ) {

        id = _id;
        atlasName = _atlasName;
        atlasFolder = _atlasFolder;
        columns = _columns;
        rows = _rows;
        orientation = _orientation;
        size = _size;
        zoom = _zoom;
        progress = _progress;
        extentId = _extentId;
    }

    @Setter
    @Getter
    private String id;
    @Setter
    @Getter
    private String atlasName = "atlas";
    @Getter
    @Setter
    private String atlasFolder = "atlas";
    @Getter
    @Setter
    private Integer columns = 2;
    @Getter
    @Setter
    private Integer rows = 1;

    @Getter
    private Extent extent;
    public void setExtent(Extent _extent) {
        extent = _extent;
        extentId = _extent.getId();
    }

    @Getter
    @Setter
    private String orientation = "landscape";
    @Getter
    @Setter
    private String size = "letter";
    @Getter
    @Setter
    private Integer zoom = 4;
    @Getter
    @Setter
    private Integer progress = 0;
    @Getter
    @Setter
    private Integer extentId = 0;

    public void copyBean(Atlas fromBean) {
        setAtlasFolder(fromBean.getAtlasFolder());
        setAtlasName(fromBean.getAtlasName());
        setColumns(fromBean.getColumns());
        setExtent(fromBean.getExtent());
        setOrientation(fromBean.getOrientation());
        setRows(fromBean.getRows());
        setSize(fromBean.getSize());
        setZoom(fromBean.getZoom());
        setProgress(fromBean.getProgress());
        setExtentId(fromBean.getExtentId());
    }
}
