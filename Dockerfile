FROM maven:3.6.3-jdk-8

# PREPARE MAVEN REPOSITORY INTEGRATION WITH MAVEN REMOTE REPOSITORY
RUN mkdir ~/.m2

# MAVEN RELEASE REPOSITORY
ARG MVN_RELEASE_USER
ARG MVN_RELEASE_PASSWORD
ARG MVN_LIBS_RELEASE

# MAVEN SNAPSHOT REPOSITORY
ARG MVN_SNAPSHOT_USER
ARG MVN_SNAPSHOT_PASSWORD
ARG MVN_LIBS_SNAPSHOT

# PREPARE MAVEN
RUN mkdir /tmp/scripts
ADD m2.sh /tmp/scripts/m2.sh
RUN chmod 777 /tmp/scripts/m2.sh
RUN /tmp/scripts/m2.sh
RUN rm /tmp/scripts/m2.sh


# PREPARE SOURCES TO COMPILE
RUN mkdir -p /tmp/compile/addons/{captcha,channel,keygen,template,theme}

ADD addons/captcha/api /tmp/compile/addons/captcha/api
ADD addons/captcha/google /tmp/compile/addons/captcha/google

ADD addons/channel/api /tmp/compile/addons/channel/api
ADD addons/channel/console /tmp/compile/addons/channel/console

ADD addons/keygen/api /tmp/compile/addons/keygen/api
ADD addons/keygen/impl /tmp/compile/addons/keygen/impl

ADD addons/template/api /tmp/compile/addons/template/api
ADD addons/template/freemaker /tmp/compile/addons/template/freemaker

ADD addons/theme/v2 /tmp/compile/addons/theme/v2

ADD ./ /tmp/compile/keycloak
RUN rm -r /tmp/compile/keycloak/addons

# COMPILE KEYGEN(S)
WORKDIR /tmp/compile/addons/keygen/api
RUN mvn -Dmaven.test.skip=true clean install package 
WORKDIR /tmp/compile/addons/keygen/impl
RUN mvn -Dmaven.test.skip=true clean install package 

# COMPILE CHANNEL(S)
WORKDIR /tmp/compile/addons/channel/api
RUN mvn -Dmaven.test.skip=true clean install package 
WORKDIR /tmp/compile/addons/channel/console
RUN mvn -Dmaven.test.skip=true clean install package 

# COMPILE TEMPLATE(S)
WORKDIR /tmp/compile/addons/template/api
RUN mvn -Dmaven.test.skip=true clean install package 
WORKDIR /tmp/compile/addons/template/freemaker
RUN mvn -Dmaven.test.skip=true clean install package 

# COMPILE CAPTCHA(S)
WORKDIR /tmp/compile/addons/captcha/api
RUN mvn -Dmaven.test.skip=true clean install package 
WORKDIR /tmp/compile/addons/captcha/google
RUN mvn -Dmaven.test.skip=true clean install package 

# COMPILE THEME
WORKDIR /tmp/compile/addons/theme/v2
RUN mvn -Dmaven.test.skip=true clean install package

# COMPILE CORE
WORKDIR /tmp/compile/keycloak
RUN mvn -Dmaven.test.skip=true clean install package

# SETUP KEYCLOAK
FROM jboss/keycloak

# DEPLOY KEYGEN(S)
COPY --from=0 /tmp/compile/addons/keygen/api/target/com.raitonbl.keycloak.keygen.jar  "${JBOSS_HOME}/providers/keygen.jar"
COPY --from=0 /tmp/compile/addons/keygen/impl/target/com.raitonbl.keycloak.keygen.impl.jar  "${JBOSS_HOME}/providers/keygen.impl.jar"

# DEPLOY TEMPLATE(S)
COPY --from=0 /tmp/compile/addons/template/api/target/com.raitonbl.keycloak.template.jar  "${JBOSS_HOME}/providers/com.raitonbl.keycloak.template.jar"
COPY --from=0 /tmp/compile/addons/template/freemaker/target/com.raitonbl.keycloak.template.freemaker.jar  "${JBOSS_HOME}/providers/com.raitonbl.keycloak.template.freemaker.jar"

# DEPLOY CHANNEL(S)
COPY --from=0 /tmp/compile/addons/channel/api/target/com.raitonbl.keycloak.channel.jar  "${JBOSS_HOME}/providers/com.raitonbl.keycloak.channel.jar"
COPY --from=0 /tmp/compile/addons/channel/console/target/com.raitonbl.keycloak.channel.impl.jar  "${JBOSS_HOME}/providers/com.raitonbl.keycloak.channel.impl.jar"

# DEPLOY CAPTCHA
COPY --from=0 /tmp/compile/addons/captcha/api/target/com.raitonbl.keycloak.captcha.jar  "${JBOSS_HOME}/providers/com.raitonbl.keycloak.captcha.jar"
COPY --from=0 /tmp/compile/addons/captcha/google/target/com.raitonbl.keycloak.captcha.google.jar  "${JBOSS_HOME}/providers/com.raitonbl.keycloak.captcha.google.jar"

# DEPLOY THEME(S)
COPY --from=0 /tmp/compile/addons/theme/v2/target/com.raitonbl.keycloak.theme.v2.jar "${JBOSS_HOME}/standalone/deployments/com.raitonbl.keycloak.theme.v2.jar"


# DEPLOY CORE PROVIDER
COPY --from=0 /tmp/compile/keycloak/target/com.raitonbl.keycloak.jar "${JBOSS_HOME}/providers/com.raitonbl.keycloak.jar"