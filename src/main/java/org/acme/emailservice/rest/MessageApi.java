package org.acme.emailservice.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

// Resource server path
@Path("/rs")
@RolesAllowed({ "user", "admin" })
public class MessageApi {

    private final String MESSAGES_FILE_PATH = ""; // a message draft folder

    public class ResourceFile {

        public String resourceName;

        public ResourceFile(String resourceName) {
            this.resourceName = resourceName;
        }
    }

    @POST
    @Path("/message")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(MultipartFormDataInput input) throws IOException {

        String resourceName = UUID.randomUUID().toString();
        ResourceFile resourceFile = new ResourceFile(resourceName);

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> messageTextParts = uploadForm.get("message-text");
        List<InputPart> messageTextIdParts = uploadForm.get("text-message-id");

        String message = messageTextParts.get(0).getBodyAsString();
        String messageTextId = messageTextIdParts.get(0).getBodyAsString();
        long messageId = messageTextId == null ? 0 : Long.parseLong(messageTextId);

        if (message != null && messageId > 0) {
            try {
                InputStream inputStream = messageTextParts.get(0).getBody(InputStream.class, null);
                byte[] bytes = IOUtils.toByteArray(inputStream);

                resourceName = MESSAGES_FILE_PATH + resourceName;
                writeToResourceFile(bytes, resourceName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Response.status(Status.CREATED).entity(resourceFile).build();
    }

    private void writeToResourceFile(byte[] content, String resourceName) throws IOException {

        try {
            File file = new File(resourceName);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream out = new FileOutputStream(file);

            out.write(content);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
