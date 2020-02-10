FROM python:3.6

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y qemu-utils cpio sudo debootstrap

RUN pip install bindep ironic-python-agent-builder==1.1.0

RUN wget https://raw.githubusercontent.com/openstack/diskimage-builder/master/bindep.txt
RUN bindep --list_all newline --file bindep.txt | xargs apt-get install -y

WORKDIR /artifacts

COPY entrypoint.sh /entrypoint.sh

ENTRYPOINT [ "/entrypoint.sh" ]
