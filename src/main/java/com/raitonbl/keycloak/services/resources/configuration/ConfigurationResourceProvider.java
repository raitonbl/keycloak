package com.raitonbl.keycloak.services.resources.configuration;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class ConfigurationResourceProvider implements RealmResourceProvider {

    private KeycloakSession keycloakSession;

    public ConfigurationResourceProvider(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    @Override
    public Object getResource() {
        return new ConfigurationResource(keycloakSession);
    }

    @Override
    public void close() {
        //DO NOTHING
    }

}
