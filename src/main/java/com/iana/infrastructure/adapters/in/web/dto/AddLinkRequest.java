package com.iana.infrastructure.adapters.in.web.dto;

public class AddLinkRequest {

    private String url;
    private String description;

    public AddLinkRequest() {
    }

    public AddLinkRequest(String url, String description) {
        this.url = url;
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
