FROM python:3.6

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y qemu-utils cpio sudo debootstrap

RUN pip install bindep ironic-python-agent-builder==1.1.0

RUN wget https://raw.githubusercontent.com/openstack/diskimage-builder/master/bindep.txt
RUN bindep --list_all newline --file bindep.txt | xargs apt-get install -y

WORKDIR /artifacts

COPY entrypoint.sh /entrypoint.sh
COPY dib /usr/local/lib/python3.6/site-packages/diskimage_builder/elements/

# This is to make proliant-tools element compatible with ironic-python-agent-ramdisk
RUN sed -i 's;IPA_VENV=/usr/share/ironic-python-agent/venv;IPA_VENV=/opt/ironic-python-agent;' /usr/local/lib/python3.6/site-packages/diskimage_builder/elements/proliant-tools/install.d/65-proliant-tools-install

ENTRYPOINT [ "/entrypoint.sh" ]
