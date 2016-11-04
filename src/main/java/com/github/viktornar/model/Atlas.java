package com.github.viktornar.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Atlas {
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
    @Setter
    private Extent extent;
    @Getter
    @Setter
    private String orientation = "landscape";
    @Getter
    @Setter
    private String size = "letter";
    @Getter
    @Setter
    private Integer zoom = 4;

    public void copyBean(Atlas fromBean) {
        this.setAtlasFolder(fromBean.getAtlasFolder());
        this.setAtlasName(fromBean.getAtlasName());
        this.setColumns(fromBean.getColumns());
        this.setExtent(fromBean.getExtent());
        this.setOrientation(fromBean.getOrientation());
        this.setRows(fromBean.getRows());
        this.setSize(fromBean.getSize());
        this.setZoom(fromBean.getZoom());
    }
}
