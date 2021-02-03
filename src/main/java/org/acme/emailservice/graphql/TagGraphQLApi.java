package org.acme.emailservice.graphql;

import java.util.List;

import javax.inject.Inject;

import org.acme.emailservice.model.Tag;
import org.acme.emailservice.service.TagService;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class TagGraphQLApi {

    @Inject
    TagService tagService;

    @Query
    public String helloTag() {
        return "Hello Tag!";
    }

    @Query
    public Tag getTag(Long id){
        return tagService.getTag(id);
    }

    @Query
    public List<Tag> getTags() {
        return tagService.getTags();
    }

    @Mutation
    public Tag updateTag(Tag tag){
        return tagService.updateOrCreate(tag);
    }
    
    @Mutation
    public Tag deleteTag(Long id){
        return tagService.delete(id);
    }
}
