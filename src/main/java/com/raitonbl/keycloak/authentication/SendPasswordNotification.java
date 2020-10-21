package com.raitonbl.keycloak.authentication;

import com.raitonbl.keycloak.Utilities;
import com.raitonbl.keycloak.channel.SmsChannelException;
import com.raitonbl.keycloak.keygen.OTPService;
import com.raitonbl.keycloak.keygen.TokenException;
import com.raitonbl.keycloak.keygen.TokenInfo;
import com.raitonbl.keycloak.template.SmsTemplateProvider;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.actiontoken.DefaultActionTokenKey;
import org.keycloak.authentication.authenticators.resetcred.ResetCredentialEmail;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class SendPasswordNotification extends ResetCredentialEmail {

    private static final Integer SENDING_DELAY = 20;
    private static final String PHONE_ATTRIBUTE = "phone";
    public static final String TOKEN_TYPE_NAME = "RESET_PASSWORD";
    public static final String PROVIDER_ID = "send-credential-notification";
    private static final String KEEP_ALIVE_PROPERTY = "COM.RAITONBL.KEYCLOAK.NOTIFICATION.SMSC.OTP.KEEP-ALIVE";


    @Override
    public void authenticate(AuthenticationFlowContext context) {

        UserModel instance = context.getUser();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();

        // we don't want people guessing usernames, so if there was a problem obtaining the user, the user will be null.
        // just reset login for with a success message
        if (instance == null) {
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
            return;
        }

        String actionTokenUserId = authenticationSession.getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);

        if (actionTokenUserId != null && Objects.equals(instance.getId(), actionTokenUserId)) {
            context.success();
            return;
        }

        String phone = instance.getFirstAttribute(PHONE_ATTRIBUTE);

        if ((instance.getEmail() == null || instance.getEmail().trim().length() == 0) && (phone != null && phone.startsWith("+"))) {
            sendSMS(context, instance);
            return;
        }

        super.authenticate(context);
    }

    private void sendSMS(AuthenticationFlowContext context, UserModel instance) {
        try {

            long expirationInMinutes = Duration.of(Utilities.getConfig(KEEP_ALIVE_PROPERTY, Long.class).orElse(120L) + SENDING_DELAY, ChronoUnit.SECONDS).toMinutes();

            TokenInfo tokenInfo = context.getSession().getProvider(OTPService.class).create(instance.getId(), TOKEN_TYPE_NAME, expirationInMinutes);

            context.getSession().getProvider(SmsTemplateProvider.class).setRealm(context.getRealm()).setUser(instance)
                    .setAuthenticationSession(context.getAuthenticationSession()).sendPasswordReset(tokenInfo.getCode(), expirationInMinutes);

            context.getEvent().clone().event(EventType.SEND_RESET_PASSWORD).user(instance).detail(Details.USERNAME, instance.getUsername())
                    .detail(PHONE_ATTRIBUTE, instance.getFirstAttribute(PHONE_ATTRIBUTE)).detail(Details.CODE_ID, context.getAuthenticationSession()
                    .getParentSession().getId()).success();

            context.getAuthenticationSession().setAuthNote(UnlockSmsTokenForm.TOKEN_ID_ATTRIBUTE, tokenInfo.getId());
            context.getAuthenticationSession().setAuthNote(UnlockSmsTokenForm.TOKEN_TYPE_ATTRIBUTE, TOKEN_TYPE_NAME);

            context.success();

        } catch (SmsChannelException | TokenException ex) {
            context.getEvent().clone().event(EventType.SEND_RESET_PASSWORD).detail(Details.USERNAME, instance.getUsername())
                    .user(instance).error(Errors.EMAIL_SEND_FAILED);

            ServicesLogger.LOGGER.debug("Unable to send SMS ", ex);

            Response challenge = context.form().setError(Messages.INTERNAL_SERVER_ERROR).createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
        }

    }

    @Override
    public String getDisplayType() {
        return "Send Reset Notification";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
