#!/bin/bash

set -x

CHUSER=${CHUSER:-root}

DISTRIBUTION=${DISTRIBUTION:-ubuntu}
RELEASE=${RELEASE:-bionic}
REPOREF=${REPOREF:-origin/stable/queens}
IMAGE_NAME=${IMAGE_NAME:-ipa-ramdisk}

# Build ramdisk
ironic-python-agent-builder -r ${RELEASE} -b ${REPOREF} -o ${IMAGE_NAME} ${DISTRIBUTION}

# Change artifacts ownership
# Since we run diskimage-builder from root inside docker container
# We assume that /artifacts directory is mounted from host machine
# We change ownership of all artifacts to the user id given in CHUSER variable
chown -R ${CHUSER} *
