package com.raitonbl.keycloak.theme;

import org.keycloak.theme.ClasspathThemeResourceProviderFactory;

public class KeycloakThemeResourceProviderFactory extends ClasspathThemeResourceProviderFactory {

    public KeycloakThemeResourceProviderFactory() {
        super("raitonbl.com", KeycloakThemeResourceProviderFactory.class.getClassLoader());
    }

}
