/*
 *
 * Build ramdisk image with ironic-python-agent using diskimage-builder
 *
 * Expected parameters:
 *
 * DIB_DISTRIBUTION                 = Ramdisk distribution (e.g. ubuntu, fedora, centos)
 * DIB_RELEASE                      = Distribution release (e.g. 8 for Centos 8)
 * DIB_REPOREF_ironic_python_agent  = Ironic-python-agent branch (e.g. origin/stable/queens)
 *
*/

date = new Date()
dateTime = date.format("ddMMyyyy-HHmmss")
jenkinsHomeDir = "/home/jenkins"

distribution = env.DIB_DISTRIBUTION ?: "ubuntu"
release = env.DIB_RELEASE ?: "bionic"
reporef = env.DIB_REPOREF_ironic_python_agent ?: "origin/stable/queens"

ipaBuilderVersion = "1.1.0"
// reporef must be slash separated line
ipaRelease = "-" + reporef.split('/').last()

dockerfileGitRepo = env.DOCKERFILE_GIT_REPO ?: "http://equinix-ems-robot@gerrit.mirantis.com/a/equinix-ems/ramdisk-building-pipeline"
dockerfileGitBranch = env.DOCKERFILE_GIT_BRANCH ?: "master"
dockerfileGitTag = env.DOCKERFILE_GIT_TAG ?: "FETCH_HEAD"
gerritCredentialsID = env.GIT_CREDENTIALS_ID ?: "jenkins"

label = "virtual"
imageName = "ipa${ipaRelease}-${distribution}-${release}-${dateTime}"
// space separated list of additional elements
elements = "pip-and-virtualenv proliant-tools"

setDibDevUser = true
if (setDibDevUser) {
    dibDevUserArgs = "-e DIB_DEV_USER_USERNAME=admin -e DIB_DEV_USER_PASSWORD=admin -e DIB_DEV_USER_PWDLESS_SUDO=yes"
    elements = "${elements} devuser"
} else {
    dibDevUserArgs = ""
}


timeout(time: 6, unit: "HOURS") {
    node(label) {
        def workspace = sh(script: "pwd", returnStdout: true).trim()
        def artifactsRelDir = "artifacts"
        def artifactsDir = "${workspace}/${artifactsRelDir}"
        def tmpDir = "${workspace}/tmp"

        try {
            stage("Cleanup") {
                sh(script: "find . -mindepth 1 -delete > /dev/null || true")
            }
            stage("Prepare env") {
                checkout([
                    $class           : "GitSCM",
                    branches         : [[name: dockerfileGitTag]],
                    userRemoteConfigs: [[url: dockerfileGitRepo, refspec: dockerfileGitBranch, credentialsId: gerritCredentialsID]],
                ])
                sh "mkdir -p \"${artifactsDir}\""
                sh "mkdir -p \"${tmpDir}\""
                sh "docker build -t ipa-builder ."
            }
            stage("Build ramdisk") {
                currentBuild.description = "Building IPA ramdisk: ${distribution} ${release} ${reporef}"
                sh """docker run --rm -v \"${artifactsDir}\":/artifacts -v \"${tmpDir}\":/tmp2 --privileged \
                -e CHUSER=\$(id -u \$(whoami)) \
                -e TMP_DIR=/tmp2 \
                -e DISTRIBUTION=${distribution} \
                -e RELEASE=${release} \
                -e REPOREF=${reporef} \
                -e IMAGE_NAME=${imageName} \
                -e ELEMENTS="${elements}" \
                -e DIB_INSTALLTYPE_pip_and_virtualenv=source \
                ${dibDevUserArgs} \
                ipa-builder"""
                sh """echo > ${artifactsDir}/SHA256SUMS && \
                echo \$(sha256sum ${artifactsDir}/${imageName}.initramfs | cut -d' ' -f1) ${imageName}.initramfs >> ${artifactsDir}/SHA256SUMS && \
                echo \$(sha256sum ${artifactsDir}/${imageName}.kernel | cut -d' ' -f1) ${imageName}.kernel >> ${artifactsDir}/SHA256SUMS
                """
            }
        } catch (Throwable e) {
            currentBuild.result = "FAILURE"
            currentBuild.description = currentBuild.description ? e.message + " " + currentBuild.description : e.message
            throw e
        } finally {
            stage("Archive artifacts") {
                archiveArtifacts artifacts: "${artifactsRelDir}/*.initramfs, ${artifactsRelDir}/*.kernel, ${artifactsRelDir}/${imageName}.d/**/*, ${artifactsRelDir}/SHA256SUMS"
            }
        }
    }
}
