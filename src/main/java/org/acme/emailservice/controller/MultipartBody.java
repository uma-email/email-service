package org.acme.emailservice.controller;

import java.io.InputStream;
// import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

// it doesn't work with multiple files - List<InputStream> files
// https://stackoverflow.com/questions/67151539/multiple-file-upload-using-quarkus-and-resteasy
public class MultipartBody {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    // private List<InputStream> files;
    public InputStream file;

    @FormParam("fileName")
    @PartType(MediaType.TEXT_PLAIN)
    public String fileName;

    @FormParam("messageId")
    @PartType(MediaType.TEXT_PLAIN)
    public String messageId;
}
