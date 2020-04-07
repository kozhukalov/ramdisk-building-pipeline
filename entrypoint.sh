#!/bin/bash

set -xe

CHUSER=${CHUSER:-0}

DISTRIBUTION=${DISTRIBUTION:-ubuntu}
RELEASE=${RELEASE:-bionic}
REPOREF=${REPOREF:-origin/stable/queens}
IMAGE_NAME=${IMAGE_NAME:-ipa-ramdisk}

ELEMENTS=${ELEMENTS:-}
ELEMENTS_ARGS=''
for e in ${ELEMENTS}; do
    ELEMENTS_ARGS="${ELEMENTS_ARGS} -e ${e}"
done

ELEMENTS_ARGS="${ELEMENTS_ARGS} -e ironic-python-agent-ramdisk-fix"

# Build ramdisk
ironic-python-agent-builder -r ${RELEASE} -b ${REPOREF} -o ${IMAGE_NAME} ${ELEMENTS_ARGS} ${DISTRIBUTION}

# Change artifacts ownership
# Since we run diskimage-builder from root inside docker container
# We assume that /artifacts directory is mounted from host machine
# We change ownership of all artifacts to the user id given in CHUSER variable
chown -R ${CHUSER} * || true
