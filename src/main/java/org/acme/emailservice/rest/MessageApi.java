package org.acme.emailservice.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.acme.emailservice.model.ResourceFile;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.keycloak.common.util.Base64Url;

// Resource server path
@Path("/rs")
@RolesAllowed({ "user", "admin" })
public class MessageApi {

    protected @Context Request request;
    private final String MESSAGES_FILE_PATH = ""; // a message draft folder

    @POST @PUT
    @Path("/message")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(MultipartFormDataInput input) throws IOException, NoSuchAlgorithmException {

        ResourceFile resourceFile = new ResourceFile();

        String resourceName = null;
        
        Status status = Status.BAD_REQUEST;

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> messageParts = uploadForm.get("message-text");
        List<InputPart> messageIdParts = uploadForm.get("text-message-id");
        List<InputPart> messageResourceNameParts = uploadForm.get("text-message-resource-name");

        String message = messageParts.size() == 0 ? null : messageParts.get(0).getBodyAsString();
        String messageTextId = messageIdParts.size() == 0 ? null : messageIdParts.get(0).getBodyAsString();
        String messageResourceName = messageResourceNameParts.size() == 0 ? null : messageResourceNameParts.get(0).getBodyAsString();

        if (request.getMethod().contains("PUT")) {
            resourceName = messageResourceName;
        } else {
            resourceName = UUID.randomUUID().toString();
        }

        if (resourceName != null && resourceName.length() > 0) {
            try {
                InputStream inputStream = messageParts.get(0).getBody(InputStream.class, null);
                byte[] bytes = IOUtils.toByteArray(inputStream);

                // constructs upload file path
                resourceName = MESSAGES_FILE_PATH + resourceName;

                String resourceDigest = writeToResourceFile(bytes, resourceName);

                resourceFile.addResource(resourceName, resourceDigest);

                long messageId = messageTextId == null ? 0 : parseToInt(messageTextId, 0);

                if (message != null && messageId > 0) {
                    // write to database
                }                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (request.getMethod().contains("PUT")) {
            status = Status.OK;
        } else {
            status = Status.CREATED;
        }

        return Response.status(status).entity(resourceFile).build();
    }

    @GET
    @Path("/message")
    // @Produces(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@QueryParam("resourceName") String resourceName) throws IOException {

		File fileDownload = new File(MESSAGES_FILE_PATH + resourceName);
        String fileName = "text.txt";
		return Response.ok((Object) fileDownload).header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName).build();
    }

    private static int parseToInt(String stringToParse, int defaultValue) {
        try {
           return Integer.parseInt(stringToParse);
        } catch(NumberFormatException ex) {
           return defaultValue; //Use default value if parsing failed
        }
    }

    private String writeToResourceFile(byte[] content, String resourceName) throws IOException, NoSuchAlgorithmException {

        try {
            File file = new File(resourceName);

            if (!file.exists()) {
                file.createNewFile();
            }

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            FileOutputStream out = new FileOutputStream(file);

            out.write(content);
            out.flush();
            out.close();

            String resourceDigest = Base64Url.encode(md.digest(content));

            return resourceDigest;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
