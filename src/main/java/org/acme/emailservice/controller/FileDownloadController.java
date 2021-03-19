package org.acme.emailservice.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "File Download Functionality",description = "APIs to download file")
@Path("/download")
public class FileDownloadController {


	@Operation(summary = "Download a file", description = "Download a file")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Download file successfully"),
			@APIResponse(name = "500", responseCode = "500", description = "Internal service error") })
	@POST
	@Path("/image")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response downloadFileWithPost(@FormParam("file") String file) {
		String path = "/";
		File fileDownload = new File(path + File.separator + file);
		ResponseBuilder response = Response.ok((Object) fileDownload);
		response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file);
		return response.build();
	}

	@Operation(summary = "Download a picture by passing file name as query", description = "Download a picture")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Download file successfully"),
			@APIResponse(name = "500", responseCode = "500", description = "Internal service error") })
	@GET
	@Path("/image/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFileWithGet(@QueryParam("file") String file) {
		String path = "/";
		File fileDownload = new File(path + File.separator + file);
		return Response.ok((Object) fileDownload).header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file)
				.build();
	}

	/* @Operation(summary = "Show a picture by passing file name as query", description = "Show a picture")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Download file successfully"),
			@APIResponse(name = "500", responseCode = "500", description = "Internal service error") })
	@GET
	@Path(value = "showImage")
	@Produces("/image/jpg")
	public Response getImageAsResponseEntity(@QueryParam("fileName") String fileName) {
		String dirPath = "/";
		InputStream in = null;
		byte[] media = new byte[0];
		try {
			in = new FileInputStream(dirPath + fileName);
			media = IOUtils.toByteArray(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Response.ok(new ByteArrayInputStream(media)).build();
	} */
}