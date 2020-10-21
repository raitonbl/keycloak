package com.raitonbl.keycloak.authentication;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.authenticators.resetcred.ResetCredentialChooseUser;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

public class ResetPasswordChooseUser extends ResetCredentialChooseUser implements Authenticator, AuthenticatorFactory {

    public static final String PROVIDER_ID = "passwd.choose-user";

    @Override
    public void action(AuthenticationFlowContext context) {

        EventBuilder event = context.getEvent();

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String username = formData.getFirst("username");

        if (username == null || username.isEmpty()) {
            event.error(Errors.USERNAME_MISSING);
            Response challenge = context.form().setError(Messages.MISSING_USERNAME).createPasswordReset();
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge);
            return;
        }

        username = username.trim();

        UserModel instance = null;

        if (username.startsWith("+") && !username.contains("@")) {
            List<UserModel> container = context.getSession().users().searchForUserByUserAttribute("phone", username, context.getRealm());
            instance = container.size() == 1 ? container.get(0) : null;
        } else if (!username.startsWith("+") && username.contains("@")) {
            instance = context.getSession().users().getUserByEmail(username, context.getRealm());
        } else if (!(username.startsWith("+") && username.contains("@"))) {
            instance = context.getSession().users().getUserByUsername(username, context.getRealm());
        }

        if (instance == null) {
            event.clone().detail(Details.USERNAME, username).error(Errors.USER_NOT_FOUND);
            context.clearUser();
        }

        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        if (instance != null && !instance.isEnabled()) {
            event.clone().detail(Details.USERNAME, username).user(instance).error(Errors.USER_DISABLED);
            context.clearUser();
        }

        if (instance != null && instance.isEnabled()) {
            context.setUser(instance);
        }

        context.success();
    }

    @Override
    public String getDisplayType() {
        return "Discovery User";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }


}
