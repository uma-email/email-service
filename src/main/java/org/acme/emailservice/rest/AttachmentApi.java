package org.acme.emailservice.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/rs")
@RolesAllowed({ "user", "admin" })
public class AttachmentApi {
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

    @GET
	@Path("/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)  
	public Response downloadFileWithGet(@QueryParam("resourceName") String resourceName) {
		File fileDownload = new File(UPLOADED_FILE_PATH + resourceName);
        String fileName = "test.pdf";
		
        return Response.ok((Object) fileDownload).header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName).build();
	}

    @POST
	@Path("/download")
    @PermitAll
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response downloadFileWithPost(@FormParam("resourceName") String resourceName) {
		File fileDownload = new File(UPLOADED_FILE_PATH + resourceName);
        String fileName = "test.pdf";

		ResponseBuilder response = Response.ok((Object) fileDownload);
		response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
		return response.build();
	}

    @POST
    @Path("/upload")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(MultipartFormDataInput input) throws IOException {

        ResourceFile resourceFile = new ResourceFile();

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> fileInputParts = uploadForm.get("uploadedFile");
        List<InputPart> messageTextIdParts = uploadForm.get("messageId");

        for (InputPart messageTextIdPart : messageTextIdParts) {
            System.out.printf(messageTextIdPart.getBodyAsString() + "\n");
        }

        for (InputPart fileInputPart : fileInputParts) {

            try {
                String resourceName = UUID.randomUUID().toString();
                resourceFile.addFilename(resourceName);

                MultivaluedMap<String, String> header = fileInputPart.getHeaders();
                String fileName = getFileName(header);

                // convert the uploaded file to inputstream
                InputStream inputStream = fileInputPart.getBody(InputStream.class, null);

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
