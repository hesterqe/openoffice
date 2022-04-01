FROM registry.access.redhat.com/ubi7/ubi

LABEL io.k8s.description="Openoffice Image" \
      io.k8s.display-name="Openoffice Image" \
      io.openshift.expose-services="8100:tcp"

USER root

RUN yum -y install wget && yum -y clean all && \
    wget --no-check-certificate https://sourceforge.net/projects/openofficeorg.mirror/files/4.1.7/binaries/en-US/Apache_OpenOffice_4.1.7_Linux_x86-64_install-rpm_en-US.tar.gz && \
    tar xvf Apache_OpenOffice_4.1.7_Linux_x86-64_install-rpm_en-US.tar.gz && \
    rm -f Apache_OpenOffice_4.1.7_Linux_x86-64_install-rpm_en-US.tar.gz && \
    cd en-US/RPMS/ && \
    rpm -Uvh *.rpm && \
    rpm -Uvih desktop-integration/openoffice4.1.7-redhat-menus-4.1.7-9800.noarch.rpm
    
# This default user is created in the openshift/base-centos7 image
USER 1001

# Set the default CMD for the image
CMD /bin/soffice -headless -nofirststartwizard -accept="socket,host=localhost,port=8100;urp;"
