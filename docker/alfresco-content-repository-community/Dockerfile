FROM alfresco/alfresco-content-repository-community:6.1.2-ga

COPY ./data/amps /usr/local/tomcat/amps
RUN echo 'java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt-6.0.jar install /usr/local/tomcat/amps/$1 /usr/local/tomcat/webapps/alfresco -force -nobackup' > install_amp.sh && chmod +x install_amp.sh && \
    ./install_amp.sh javascript-console-repo-0.6.amp && \
    ./install_amp.sh java-code-executor-2.0.0.amp && \
    ./install_amp.sh alfresco-promena-client-1.0.0.amp && \
    ./install_amp.sh alfresco-promena-http-client-1.0.0.amp && \
    ./install_amp.sh alfresco-promena-message-broker-client-1.0.0.amp && \
    rm install_amp.sh

RUN echo $' \n\
grant { \n\
    permission java.security.AllPermission; \n\
}; ' >> /usr/local/tomcat/conf/catalina.policy