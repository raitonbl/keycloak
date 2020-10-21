#!/bin/bash

if [[ -n "${MVN_RELEASE_USER}" ]]; then
  if [[ -n "${MVN_RELEASE_PASSWORD}" ]]; then
      if [[ -n "${MVN_LIBS_RELEASE}" ]]; then
        SERVER_XML_RELEASE="<server><username>${MVN_RELEASE_USER}</username> <password>${MVN_RELEASE_PASSWORD}</password> <id>releases</id> </server>";
        REPOSITORY_XML_RELEASE="<repository><snapshots><enabled>false</enabled> </snapshots><id>artifactory</id><name>libs-release</name><url>${MVN_LIBS_RELEASE}</url></repository>";
        PLUGIN_REPOSITORY_XML_RELEASE="<pluginRepository><snapshots><enabled>false</enabled></snapshots><id>releases</id><name>libs-release</name><url>${MVN_LIBS_RELEASE}</url></pluginRepository>";
      fi
  fi
fi

if [[ -n "${MVN_SNAPSHOT_USER}" ]]; then
  if [[ -n "${MVN_SNAPSHOT_PASSWORD}" ]]; then
      if [[ -n "${MVN_LIBS_SNAPSHOT}" ]]; then
        SERVER_XML_SNAPSHOT="<server><username>${MVN_SNAPSHOT_USER}</username><password>${MVN_SNAPSHOT_PASSWORD}</password><id>snapshots</id></server>";
        REPOSITORY_XML_SNAPSHOT="<repository><snapshots/><id>artifactory-snapshots</id><name>libs-snapshot</name><url>${MVN_LIBS_SNAPSHOT}</url></repository>";
        PLUGIN_REPOSITORY_XML_SNAPSHOT="<pluginRepository><snapshots/><id>snapshots</id><name>libs-snapshot</name><url>${MVN_LIBS_SNAPSHOT}</url></pluginRepository>";

      fi
  fi
fi

if [[ -n "${SERVER_XML_RELEASE}" ]] || [[ -n "${SERVER_XML_SNAPSHOT}" ]] ; then
  SERVER_XML="<servers>";
  REPOSITORY_XML="<repositories>";
  PLUGIN_REPOSITORY_XML="<pluginRepositories>";

  if [[ -n "${SERVER_XML_RELEASE}" ]];then
    SERVER_XML="${SERVER_XML} ${SERVER_XML_RELEASE}";
    REPOSITORY_XML="${REPOSITORY_XML} ${REPOSITORY_XML_RELEASE}";
    PLUGIN_REPOSITORY_XML="${PLUGIN_REPOSITORY_XML} ${PLUGIN_REPOSITORY_XML_RELEASE}";
  fi

   if [[ -n "${SERVER_XML_SNAPSHOT}" ]];then
    CENTRAL_REPOSITORY_XML="<repository><id>maven-central</id><url>https://repo1.maven.org/maven2</url><releases><enabled>false</enabled></releases><snapshots><enabled>false</enabled></snapshots></repository>";
    SERVER_XML="${SERVER_XML} ${SERVER_XML_SNAPSHOT}";
    REPOSITORY_XML="${REPOSITORY_XML} ${REPOSITORY_XML_SNAPSHOT}${CENTRAL_REPOSITORY_XML}";
    PLUGIN_REPOSITORY_XML="${PLUGIN_REPOSITORY_XML} ${PLUGIN_REPOSITORY_XML_SNAPSHOT}";
  fi

  SERVER_XML="${SERVER_XML}</servers>";
  REPOSITORY_XML="${REPOSITORY_XML}</repositories>";
  PLUGIN_REPOSITORY_XML="${PLUGIN_REPOSITORY_XML}</pluginRepositories>";

  PROFILES_XML="<profiles><profile>${REPOSITORY_XML} ${PLUGIN_REPOSITORY_XML} <id>default</id></profile></profiles>";
fi

if [[ -n "${SERVER_XML}" ]] && [[ -n "${PROFILES_XML}" ]]  ; then
  XML="<settings>${SERVER_XML}${PROFILES_XML}<activeProfiles><activeProfile>default</activeProfile></activeProfiles></settings>";
else
  XML="<settings><servers></servers><profiles><repositories><repository><id>central</id> <url>https://repo1.maven.org/maven2</url> <releases> <enabled>true</enabled> </releases> <snapshots> <enabled>true</enabled> </snapshots> </repository> <pluginRepositories> <pluginRepository> <id>central</id> <url>https://repo1.maven.org/maven2</url> <releases> <enabled>true</enabled> </releases> <snapshots> <enabled>true</enabled> </snapshots> </pluginRepository></pluginRepositories></repositories><profile><id>default</id></profile></profiles><activeProfiles><activeProfile>default</activeProfile></activeProfiles></settings>";
fi

echo "${XML}" > ~/.m2/settings.xml
