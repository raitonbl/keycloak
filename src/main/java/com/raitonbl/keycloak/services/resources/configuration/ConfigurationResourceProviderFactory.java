package com.raitonbl.keycloak.services.resources.configuration;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class ConfigurationResourceProviderFactory implements RealmResourceProviderFactory {

    @Override
    public RealmResourceProvider create(KeycloakSession keycloakSession) {
        return new ConfigurationResourceProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
        // DO NOTHING
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // DO NOTHING
    }

    @Override
    public void close() {
        // DO NOTHING
    }

    @Override
    public String getId() {
        return "configuration";
    }

}
