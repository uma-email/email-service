package org.acme.emailservice.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jose4j.jwk.HttpsJwks;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;

@Path("/rs")
@RolesAllowed({ "user", "admin" })
public class AttachmentApi {

    private static Logger log = Logger.getLogger(AttachmentApi.class);

    @Inject
    JWTParser parser;

    @Inject
    @ConfigProperty(name = "oidc.jwt.verify.publickey.location")
    String oidcPublicKeyLocation;

    @Inject
    @ConfigProperty(name = "oidc.jwt.verify.issuer")
    String oidcIssuer;

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

        return Response.ok((Object) fileDownload)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName).build();
    }

    @POST
    @Path("/download")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFileWithPost(@FormParam("token") String token,
            @FormParam("resourceName") String resourceName) throws Exception {

        Status status = Status.UNAUTHORIZED;

        try {
            HttpsJwks httpsJwks = new HttpsJwks(oidcPublicKeyLocation);

            PublicKey publicKey = httpsJwks.getJsonWebKeys().get(0).getPublicKey();

            JsonWebToken jwt = parser.verify(token, publicKey);

            if (!jwt.getIssuer().equals(oidcIssuer)) {
                throw new Exception("Issuer mishmash.");
            }

            status = Status.FORBIDDEN;

            JsonObject realmAccess = jwt.getClaim("realm_access");
            JsonArray roleArr = realmAccess.getJsonArray("roles");
            Supplier<Stream<String>> streamRolesSupplier = () -> roleArr.stream()
                    .map(json -> ((JsonString) json).getString());

            if (!(containsRoleName(streamRolesSupplier, "user") || containsRoleName(streamRolesSupplier, "admin"))) {
                return Response.status(status).build();
            }

            File fileDownload = new File(UPLOADED_FILE_PATH + resourceName);
            String fileName = "test.pdf";

            ResponseBuilder response = Response.ok((Object) fileDownload);
            response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

            return response.build();

        } catch (Exception ignore) {
            log.error("Error in validate token: ", ignore);
        }

        return Response.status(status).build();
    }

    public static boolean containsRoleName(final Supplier<Stream<String>> roles, final String roleName) {
        return roles.get().filter(o -> o.equals(roleName)).findFirst().isPresent();
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
            log.info("Mesage ID: " + messageTextIdPart.getBodyAsString());
        }

        for (InputPart fileInputPart : fileInputParts) {

            try {
                String resourceName = UUID.randomUUID().toString();
                resourceFile.addFilename(resourceName);

                MultivaluedMap<String, String> header = fileInputPart.getHeaders();
                String fileName = getFileName(header);
                log.info("Uploading: " + fileName);

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
