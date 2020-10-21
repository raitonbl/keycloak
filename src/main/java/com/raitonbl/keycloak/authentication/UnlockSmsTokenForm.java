package com.raitonbl.keycloak.authentication;

import com.raitonbl.keycloak.Utilities;
import com.raitonbl.keycloak.captcha.CaptchaService;
import com.raitonbl.keycloak.keygen.Cause;
import com.raitonbl.keycloak.keygen.OTPService;
import com.raitonbl.keycloak.keygen.TokenInfo;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.*;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UnlockSmsTokenForm implements Authenticator, AuthenticatorFactory, DisplayTypeAuthenticatorFactory, ConfigurableAuthenticatorFactory {

    private static final String PROVIDER_ID = "unlock-sms-notification";

    public static final String TOKEN_ID_ATTRIBUTE = "COM.RAITONBL.KEYCLOAK.NOTIFICATION.TOKEN.ID";
    public static final String TOKEN_TYPE_ATTRIBUTE = "COM.RAITONBL.KEYCLOAK.NOTIFICATION.TOKEN.TYPE";

    public static final Logger LOG = Logger.getLogger(UnlockSmsTokenForm.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        if (context.getUser() == null) {
            Response challenge = context.form().setError(Messages.EXPIRED_CODE).createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        String phone = context.getUser().getFirstAttribute("phone");

        if (phone == null || !phone.startsWith("+")) {
            Response challenge = context.form().setError(Messages.EXPIRED_CODE).createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        String tokenID = context.getAuthenticationSession().getAuthNote(TOKEN_ID_ATTRIBUTE);

        if (tokenID == null) {
            Response challenge = context.form().setError(Messages.EXPIRED_CODE).createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        LoginFormsProvider forms = context.form();

        CaptchaService service = context.getSession().getProvider(CaptchaService.class);

        if (service != null) {
            String key = service.getCaptchaSiteKey();

            if (key != null) {
                forms.setAttribute("siteKey", key);
            }

        }

        context.challenge(forms.createForm("sms-otp.ftl"));
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        LocalDateTime now = LocalDateTime.now();

        MultivaluedMap<String, String> params = context.getHttpRequest().getFormParameters();

        if (!isCaptchaValid(context, params)) {
            Response challenge = context.form().setError(Messages.EXPIRED_CODE).createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        OTPService service = context.getSession().getProvider(OTPService.class);

        if (service == null) {
            Response challenge = context.form().setError(Messages.EXPIRED_CODE).createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        TokenInfo key = getToken(context, service, now, params);

        if (key == null) {
            Response challenge = context.form().setError(Messages.EXPIRED_CODE).createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        String code = params.getFirst("code");
        String type = context.getAuthenticationSession().getAuthNote(TOKEN_TYPE_ATTRIBUTE);

        if (!key.getCode().equals(code)) {

            if (key.getAttemptCount() == 3) {
                service.invalidate(key.getId(), type, Cause.MAX_ATTEMPT);
            } else {
                service.registerAttempt(key.getId(), type);
            }

            Response challenge = context.form().setError(Messages.EXPIRED_CODE).createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        service.invalidate(key.getId(), type, Cause.SUBMIT);

        context.success();
    }

    private boolean isCaptchaValid(AuthenticationFlowContext context, MultivaluedMap<String, String> params) {

        CaptchaService service = context.getSession().getProvider(CaptchaService.class);

        if (service == null) {
            return Boolean.TRUE;
        }

        String captcha = params.getFirst("captcha");

        return service.isVerified(context.getSession(), captcha);
    }

    private TokenInfo getToken(AuthenticationFlowContext context, OTPService service, LocalDateTime now, MultivaluedMap<String, String> params) {

        String code = params.getFirst("code");

        if (StringUtils.isBlank(code) || !StringUtils.isNumeric(code) || code.length() < 6) {
            return null;
        }

        String type = context.getAuthenticationSession().getAuthNote(TOKEN_TYPE_ATTRIBUTE);
        String tokenID = context.getAuthenticationSession().getAuthNote(TOKEN_ID_ATTRIBUTE);

        if (StringUtils.isBlank(tokenID) || StringUtils.isBlank(type)) {
            return null;
        }

        TokenInfo key = service.get(tokenID, type).orElse(null);

        if (key == null || key.getType() == null || !Objects.equals(key.getUserID(), context.getUser().getId()) || key.getAttemptCount() >= 3) {
            return null;
        }

        LocalDateTime expiresAt = key.getCreatedAt().plus(key.getExpiresIn(), ChronoUnit.MINUTES);

        if (now.isBefore(key.getCreatedAt()) || now.isAfter(expiresAt)) {
            return null;
        }

        return key;
    }


    @Override
    public boolean requiresUser() {
        return Boolean.FALSE;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        String tokenID = session.getContext().getAuthenticationSession().getAuthNote("COM.RAITONBL.KEYCLOAK.NOTIFICATION.SIZE");
        return tokenID != null && !tokenID.trim().isEmpty() && user != null && user.getFirstAttribute("phone").startsWith("+");
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        //DO NOTHING
    }

    @Override
    public UnlockSmsTokenForm create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
        //DO NOTHING
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        //DO NOTHING
    }

    @Override
    public void close() {
        //DO NOTHING
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator createDisplay(KeycloakSession session, String displayType) {
        return this;
    }

    @Override
    public String getDisplayType() {
        return "SMS OTP Password Form";
    }

    @Override
    public String getHelpText() {
        return "Validates an SMS OTP and triggers the action";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return Boolean.TRUE;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return Utilities.REQUIRED_ONLY_REQUIREMENT;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return Boolean.FALSE;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

}
