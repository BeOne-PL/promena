FROM alfresco/alfresco-share:6.1.0-RC3

COPY ./data/amps /usr/local/tomcat/amps
RUN echo 'java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt-6.0.jar install /usr/local/tomcat/amps/$1 /usr/local/tomcat/webapps/share -force -nobackup' > install_amp.sh && chmod +x install_amp.sh && \
    ./install_amp.sh javascript-console-share-0.6.0.amp && \
    rm install_amp.sh