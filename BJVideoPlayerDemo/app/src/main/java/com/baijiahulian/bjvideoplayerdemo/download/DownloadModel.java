package com.baijiahulian.bjvideoplayerdemo.download;

import java.io.Serializable;

public class DownloadModel implements Serializable {
    private static final long serialVersionUID = 2072893447591548402L;

    private String name;
    private String url;

    public DownloadModel() {
    }

    public DownloadModel(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
