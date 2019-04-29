FROM openjdk:11.0.3-jre-stretch

LABEL maintainer="BeOne"

# TODO maybe Leptonica
RUN apt-get -y update && \
    apt-get -y upgrade && \
    apt-get -y install imagemagick \
        libreoffice \
        tesseract-ocr \
        tesseract-ocr-pol \
        tesseract-ocr-eng \
        tesseract-ocr-deu \
        tesseract-ocr-osd && \
    apt-get -y clean

WORKDIR /
COPY docker-entrypoint.sh /
ENTRYPOINT [ "./docker-entrypoint.sh" ]

EXPOSE 8080

#ENV ENV_OFFICE_HOME /opt/libreoffice6.1
#ENV ENV_OCR_SCRIPTS_HOME /opt
#
#ENV MAGICK_MEMORY_LIMIT 5GB
#ENV MAGICK_THREAD_LIMIT 2
#ENV MAGICK_DISK_LIMIT 5G
#
#ENV TESSDATA_PREFIX /usr/local/share/tessdata

ENV APP_VERSION 3.0.0-alpha
ADD data/executable /opt