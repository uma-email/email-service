package org.acme.emailservice.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.acme.emailservice.security.ResourceServerService;
import org.jboss.logging.Logger;

@Path("/rs")
public class IncomingResources {

    private static Logger log = Logger.getLogger(IncomingResources.class);

    @Inject
    ResourceServerService resourceServerService;

    @GET
    @Path("/incoming")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getTicketForIncomingBox() {
        String incomingBoxId = resourceServerService.getIncomingBoxId();
        String ticket = resourceServerService.getTicket(incomingBoxId);
        log.info("GetTicket (Incoming): " + ticket);

        return Response.status(Status.OK).entity(ticket).build();
    }

    @GET
    @Path("/outgoing")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getTicketForOutgoingBox() {
        String outgoingBoxId = resourceServerService.getOutgoingBoxId();
        String ticket = resourceServerService.getTicket(outgoingBoxId);
        log.info("GetTicket (Outgoing): " + ticket);

        return Response.status(Status.OK).entity(ticket).build();
    }   
}