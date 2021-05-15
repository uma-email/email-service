package org.acme.emailservice.model;

public class ResourceFile {

    private String resourceName;
    private String resourceDigest;

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceDigest() {
        return resourceDigest;
    }

    public void addResource(String resourceName, String resourceDigest) {
        this.resourceName = resourceName;
        this.resourceDigest = resourceDigest;
    }
}
