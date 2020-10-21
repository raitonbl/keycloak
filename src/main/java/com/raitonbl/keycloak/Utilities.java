package com.raitonbl.keycloak;

import com.github.drapostolos.typeparser.TypeParser;
import org.keycloak.models.AuthenticationExecutionModel;

import java.util.Optional;

public final class Utilities {

    private static final TypeParser PARSER = TypeParser.newBuilder().build();
    public static final AuthenticationExecutionModel.Requirement[] REQUIRED_ONLY_REQUIREMENT = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    private Utilities() {
    }

    @SuppressWarnings({"unchecked"})
    public static <R> Optional<R> getConfig(String property, Class<R> returnType) {

        if (returnType == null || property == null) {
            return Optional.empty();
        }

        String value = System.getenv(property);

        if (value == null) {
            value = System.getProperty(property);
        }

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of((R) PARSER.parseType(value, returnType));

    }

}
