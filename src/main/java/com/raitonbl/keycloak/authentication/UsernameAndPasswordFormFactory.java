package com.raitonbl.keycloak.authentication;

import com.raitonbl.keycloak.Utilities;
import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.DisplayTypeAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

public class UsernameAndPasswordFormFactory implements AuthenticatorFactory, DisplayTypeAuthenticatorFactory {

    public static final String PROVIDER_ID = "auth-mobile-password-form";
    public static final UsernameAndPasswordForm SINGLETON = new UsernameAndPasswordForm();

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public Authenticator createDisplay(KeycloakSession session, String displayType) {

        if (displayType == null) {
            return SINGLETON;
        }

        if (!OAuth2Constants.DISPLAY_CONSOLE.equalsIgnoreCase(displayType)) {
            return null;
        }

        return null;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Username/Mobile Password Form";
    }

    @Override
    public String getHelpText() {
        return "Validates a username/mobile and password from login form.";
    }


    @Override
    public void init(Config.Scope config) {
        // DO NOTHING
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // DO NOTHING
    }

    @Override
    public void close() {
        // DO NOTHING
    }

    @Override
    public String getReferenceCategory() {
        return PasswordCredentialModel.TYPE;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return Utilities.REQUIRED_ONLY_REQUIREMENT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


}
