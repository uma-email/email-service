package org.acme.emailservice.graphql;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class FilterGraphQLApi {

    @Query
    public String helloFilter() {
        return "Hello Filter!";
    }

    /* @Query
    public List<Filter> getFilters(){
        return filterService.getFilters();
    } */
}