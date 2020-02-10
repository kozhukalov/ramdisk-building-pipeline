# IPA Ramdisk Building Pipeline

## Pipeline Input Parameters

The pipeline uses diskimage-builder to build ramdisk with ironic-python-agent inside. We use diskimage-builder not directly but using ironic-python-agent-builder package that provides wrapper script and ironic-python-agent element for diskimage-builder.

* DIB_DISTRIBUTION                 -- Diskimage-builder operating system element (e.g. ubuntu)
* DIB_RELEASE                      -- Operating system release (e.g. bionic)
* DIB_REPOREF_ironic_python_agent  -- Ironic-python-agent branch to checkout (e.g. origin/stable/queens)

## Pipeline workflow

1. Cleanup -- remove all contents of the working directory
2. Prepare env -- build Docker image with ironic-python-agent-builder
3. Build ramdisk -- build ramdisk using ironic-python-agent-builder
4. Archive artifacts -- archive ramdisk and dib manifests

## Tools

Base Docker image

- python:3.6

Python packages:

- ironic-python-agent-builder==1.1.0

Binary packages:

- all that listed in https://raw.githubusercontent.com/openstack/diskimage-builder/master/bindep.txt
