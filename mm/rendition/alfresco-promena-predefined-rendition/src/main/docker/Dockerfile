FROM ${docker.acs.image}:${alfresco.platform.version}

ARG TOMCAT_DIR=/usr/local/tomcat

# Copy Dockerfile to avoid an error if no JARs exist
COPY Dockerfile extensions/*.jar $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/

# Copy Dockerfile to avoid an error if no AMPs exist
COPY Dockerfile extensions/*.amp $TOMCAT_DIR/amps/
RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/alfresco-promena-client-0.9.0-SNAPSHOT.amp $TOMCAT_DIR/webapps/alfresco -nobackup -force && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/alfresco-promena-http-client-0.9.0-SNAPSHOT.amp $TOMCAT_DIR/webapps/alfresco -nobackup -force && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/alfresco-promena-rendition-0.9.0-SNAPSHOT.amp $TOMCAT_DIR/webapps/alfresco -nobackup -force && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/alfresco-promena-predefined-rendition-0.9.0-SNAPSHOT.amp $TOMCAT_DIR/webapps/alfresco -nobackup -force

COPY alfresco-global.properties $TOMCAT_DIR/shared/classes/alfresco-global.properties
COPY dev-log4j.properties $TOMCAT_DIR/shared/classes/alfresco/extension
COPY disable-webscript-caching-context.xml $TOMCAT_DIR/shared/classes/alfresco/extension

# Copy Dockerfile to avoid an error if no license file exists
COPY Dockerfile license/*.* $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/alfresco/extension/license/

# It's required by rector-netty
RUN echo $' \n\
grant { \n\
    permission java.security.AllPermission; \n\
}; ' >> /usr/local/tomcat/conf/catalina.policy