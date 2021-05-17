package org.acme.emailservice.rest.client;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.acme.emailservice.model.enums.EResourceType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

@ApplicationScoped
public class ResourceServerRestClient {

    private static Logger log = Logger.getLogger(ResourceServerRestClient.class);

    @ConfigProperty(name = "uma.wide-ecosystem.well-known.configuration")
    String wellKnownConfiguration;

    public class ResourcesEndpoints {
        public String incoming_resources_endpoint;
        public String outgoing_resources_endpoint;
    }

    public String getEndpoint(String domainName, EResourceType resourceType) {
        String wellKnownUrl = "https://" + domainName + wellKnownConfiguration;

        Builder request = ResteasyClientBuilder.newClient().target(wellKnownUrl).request();

        Response response;

        try {
            response = request.get();
        } catch(Exception e) {
            return null;
        }

        if (response.getStatus() == 200) {
            String resourcesEndpoints = response.readEntity(String.class);
            log.debug("resourcesEndpoints: " + resourcesEndpoints);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = null;
            
            try {
                node = mapper.readTree(resourcesEndpoints);
            } catch (JsonProcessingException e) {
                return null;
            }

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
        Response response;
        
        try {
            response= request.get();
        } catch(Exception e) {
            return null;
        }

        if (response.getStatus() == 200) {
            return response.readEntity(String.class);
        }

        return null;
    }
}
