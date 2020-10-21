package com.raitonbl.keycloak.services.resources.configuration;

import com.raitonbl.keycloak.Page;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.representations.idm.ErrorRepresentation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ConfigurationResource {

    private KeycloakSession session;

    private static final Logger LOG = Logger.getLogger(ConfigurationResource.class);


    public ConfigurationResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    @Path("/admin/realms/{realm}/attributes")
    public Object findAll(@PathParam("realm") String realm, @QueryParam("s") String s, @DefaultValue("1") @QueryParam("page") int page, @DefaultValue("30") @QueryParam("limit") int size) {

        RealmProvider provider = session.getProvider(RealmProvider.class);

        if (provider == null) {
            return throwNotFound();
        }

        RealmModel model = provider.getRealm(realm);

        if (model == null) {
            return throwNotFound();
        }

        Map<String, String> attributes = model.getAttributes();

        List<PropertyRepresentation> container = attributes.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .skip((page - 1) * size).limit(size).map(this::toRepresentation).collect(Collectors.toList());

        long totalElements = attributes.size();

        return Page.<PropertyRepresentation>builder()
                .setContainer(container).setSize(size).setNumber(page).setTotalElements(totalElements).build();
    }

    @PUT
    @NoCache
    @Path("/admin/realms/{realm}/attributes/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Object set(@PathParam("realm") String realm, @PathParam("name") String name, String value) {

        RealmProvider provider = session.getProvider(RealmProvider.class);

        if (provider == null) {
            return throwNotFound();
        }

        RealmModel model = provider.getRealm(realm);

        if (model == null) {
            return throwNotFound();
        }

        String v = StringUtils.replaceEach(name, new String[]{"-", ".", "_"}, new String[]{"", "", ""});

        if (StringUtils.isNumeric(v) || !StringUtils.isAlphanumeric(v)) {
            return throwError(Response.Status.BAD_REQUEST);
        }

        model.setAttribute(name, value);

        return Response.status(Response.Status.OK).entity(toRepresentation(name, value)).build();
    }

    @DELETE
    @NoCache
    @Path("/admin/realms/{realm}/attributes/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Object remove(@PathParam("realm") String realm, @PathParam("name") String name) {

        RealmProvider provider = session.getProvider(RealmProvider.class);

        if (provider == null) {
            return throwNotFound();
        }

        RealmModel model = provider.getRealm(realm);

        if (model == null) {
            return throwNotFound();
        }

        String v = StringUtils.replaceEach(name, new String[]{"-", ".", "_"}, new String[]{"", "", ""});

        if (StringUtils.isNumeric(v) || !StringUtils.isAlphanumeric(v)) {
            return throwError(Response.Status.NOT_FOUND);
        }

        String value = model.getAttribute(name);

        if (value == null) {
            return throwError(Response.Status.NOT_FOUND);
        }

        model.removeAttribute(name);

        return Response.status(Response.Status.OK).entity(toRepresentation(name, value)).build();
    }

    private ErrorRepresentation throwNotFound() {
        return throwError(Response.Status.NOT_FOUND);
    }

    private ErrorRepresentation throwError(Response.Status status) {
        ErrorRepresentation representation = new ErrorRepresentation();
        representation.setErrorMessage(status.getReasonPhrase());

        LOG.info("ERROR:" + status.getStatusCode() + " => " + status.getReasonPhrase());
        return representation;
    }

    private PropertyRepresentation toRepresentation(Map.Entry<String, String> each) {
        return toRepresentation(each.getKey(), each.getValue());
    }

    private PropertyRepresentation toRepresentation(String key, String value) {
        return new PropertyRepresentation(key, value);
    }

}
