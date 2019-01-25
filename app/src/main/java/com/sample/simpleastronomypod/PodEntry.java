package com.sample.simpleastronomypod;

import android.support.annotation.Keep;

/**
 * Created by Jon-Luke West on 9/10/2018.
 */
@Keep
class PodEntry {

    private String date;
    private String explanation;
    private String hdurl;
    private String media_type;
    private String service_version;
    private String title;
    private String url;

    @Keep
    public PodEntry() {}

    @Keep
    public String getDate() {
        return date;
    }

    @Keep
    public void setDate(String date) {
        this.date = date;
    }

    @Keep
    public String getExplanation() {
        return explanation;
    }

    @Keep
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @Keep
    public String getHdurl() {
        return hdurl;
    }

    @Keep
    public void setHdurl(String hdurl) {
        this.hdurl = hdurl;
    }

    @Keep
    public String getMedia_type() {
        return media_type;
    }

    @Keep
    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    @Keep
    public String getService_version() {
        return service_version;
    }

    @Keep
    public void setService_version(String service_version) {
        this.service_version = service_version;
    }

    @Keep
    public String getTitle() {
        return title;
    }

    @Keep
    public void setTitle(String title) {
        this.title = title;
    }

    @Keep
    public String getUrl() {
        return url;
    }

    @Keep
    public void setUrl(String url) {
        this.url = url;
    }

    @Keep
    public boolean isImage() {
        return this.media_type.equals("image");
    }

    @Keep
    public boolean isVideo() {
        return this.media_type.equals("video");
    }

    @Keep
    public String getYouTubeKey() {

        if (!this.isVideo()) {
            throw new UnsupportedOperationException("This pod is not a video");
        }

        if (!this.url.contains("youtube")) {
            throw new UnsupportedOperationException("This video is not from Youtube");
        }

        return url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("?"));
    }

    @Keep
    public boolean hasHdUrl() {
        return this.hdurl != null && this.hdurl.length() > 0;
    }
}
