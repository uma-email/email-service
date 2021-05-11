package org.acme.emailservice.rest.client;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.acme.emailservice.model.enums.EResourceType;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

@ApplicationScoped
public class ResourceServerRestClient {

    private static Logger log = Logger.getLogger(ResourceServerRestClient.class);

    public class ResourcesEndpoints {
        public String incoming_resources_endpoint;
        public String outgoing_resources_endpoint;
    }

    public String getEndpoint(String domainName, EResourceType resourceType) throws JsonMappingException, JsonProcessingException {
        String wellKnownUrl = "https://" + domainName + "/.well-known/aems-configuration";

        Builder request = ResteasyClientBuilder.newClient().target(wellKnownUrl).request();
        request.header("Content-Type", MediaType.APPLICATION_JSON);
        Response response = request.get();

        if (response.getStatus() == 200) {
            String resourcesEndpoints = response.readEntity(String.class);
            log.debug("resourcesEndpoints: " + resourcesEndpoints);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(resourcesEndpoints);

            switch (resourceType) {
                case INCOMING:
                    return node.get("incoming_resources_endpoint").asText();
                case OUTGOING:
                    return node.get("outgoing_resources_endpoint").asText();
                default:
                    return null;
            }
        }
        return null;
    }

    public String getTicket(String resourceEndpoint) {
        Builder request = ResteasyClientBuilder.newClient().target(resourceEndpoint).request();
        request.header("Content-Type", MediaType.APPLICATION_JSON);
        Response response = request.get();

        if (response.getStatus() == 200) {
            String ticket = response.readEntity(String.class);
            return ticket;
            // return Jackson.getElement(entity, "introspection_endpoint");
        }
        return null;
    }
}
