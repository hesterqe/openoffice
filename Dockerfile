FROM registry.access.redhat.com/ubi7/ubi

LABEL io.k8s.description="Openoffice Image" \
      io.k8s.display-name="Openoffice Image" \
      io.openshift.expose-services="8100:tcp"

USER root

RUN yum -y install wget java-11-openjdk-devel && yum -y clean all && \
    wget --no-check-certificate https://sourceforge.net/projects/openofficeorg.mirror/files/4.1.7/binaries/en-US/Apache_OpenOffice_4.1.7_Linux_x86-64_install-rpm_en-US.tar.gz && \
    tar xvf Apache_OpenOffice_4.1.7_Linux_x86-64_install-rpm_en-US.tar.gz && \
    rm -f Apache_OpenOffice_4.1.7_Linux_x86-64_install-rpm_en-US.tar.gz && \
    cd en-US/RPMS/ && \
    rpm -Uvh *.rpm && \
    rpm -Uvih desktop-integration/openoffice4.1.7-redhat-menus-4.1.7-9800.noarch.rpm && \
    echo 'export HOME=/tmp' >> /init_wrapper.sh && \
    echo '/bin/soffice -headless -nofirststartwizard -accept="socket,host=0,port=8100;urp;"' >> /init_wrapper.sh && \
    chmod 755 /init_wrapper.sh && \
    chgrp 0 /init_wrapper.sh && \
    chmod g+rwX /init_wrapper.sh
    

# begin temporary, need to remove
RUN mkdir -p /tmp/client
COPY OpenOfficeClient.java /tmp/client/
RUN chgrp 0 /tmp/client && \
    chmod g+rwX /tmp/client
# end temporary    
    
    
# This default user is created in the openshift/base-centos7 image
USER 1001

# Set the default CMD for the image
CMD /init_wrapper.sh
