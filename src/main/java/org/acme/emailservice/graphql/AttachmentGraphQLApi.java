package org.acme.emailservice.graphql;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class AttachmentGraphQLApi {

    @Query
    public String helloAttachment() {
        return "Hello Attachment!";
    }

    /* @Query
    public List<Attachment> getAttachments(){
        return attachmentService.getAttachments();
    } */
}
