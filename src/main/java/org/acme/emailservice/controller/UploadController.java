package org.acme.emailservice.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/rs")
@RolesAllowed({ "user", "admin" })
public class UploadController {

    // @Inject
    // @RestClient
    // MultipartService service;

    public class ResourceFile {

        public List<String> resourceName;

        public ResourceFile() {
            this.resourceName = new ArrayList<>();
        }

        public void addFilename(String resourceName) {
            this.resourceName.add(resourceName);
        }
    }

    private final String UPLOADED_FILE_PATH = ""; // an message attachment draft folder

    @POST
    @Path("/upload")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(MultipartFormDataInput input) throws IOException {

        ResourceFile resourceFile = new ResourceFile();

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("uploadedFile");
        List<InputPart> inputParts2 = uploadForm.get("messageId");

        for (InputPart inputPart2 : inputParts2) {
            System.out.printf(inputPart2.getBodyAsString() + "\n");
        }

        for (InputPart inputPart : inputParts) {

            try {
                String resourceName = UUID.randomUUID().toString();
                resourceFile.addFilename(resourceName);

                MultivaluedMap<String, String> header = inputPart.getHeaders();
                String fileName = getFileName(header);

                // convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class, null);

                // constructs upload file path
                resourceName = UPLOADED_FILE_PATH + resourceName;

                writeToResourceFile(inputStream, resourceName);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return Response.status(Status.CREATED).entity(resourceFile).build();
    }

    private String getFileName(MultivaluedMap<String, String> header) {

        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

    private void writeToResourceFile(InputStream content, String resourceName) {

        int read = 0;
        byte[] bytes = new byte[1024];

        try {
            File file = new File(resourceName);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream out = new FileOutputStream(file);
            while ((read = content.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}