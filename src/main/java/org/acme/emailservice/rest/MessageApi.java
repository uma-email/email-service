package org.acme.emailservice.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

// Resource server path
@Path("/rs")
@RequestScoped
@RolesAllowed({ "user", "admin" })
public class MessageApi {

    private final String MESSAGES_FILE_PATH = ""; // a message target folder

    @POST
    @Path("/message")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    public Response uploadFile(MultipartFormDataInput input) throws IOException {

        String fileName = UUID.randomUUID().toString();

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

                fileName = MESSAGES_FILE_PATH + fileName;
                writeFile(bytes, fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Response.status(200).entity("uploadFile is called, Uploaded file name : " + fileName).build();
    }

    private void writeFile(byte[] content, String filename) throws IOException {
        File file = new File(filename);

        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fop = new FileOutputStream(file);

        fop.write(content);
        fop.flush();
        fop.close();
    }

}
