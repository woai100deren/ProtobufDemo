export VERSION_NAME="${MajorVersion}.${MinorVersion}.${FixVersion}"."${BuildNo}"

echo "${VERSION_NAME}"

export CI=true
export BUILD_MODE=release

echo "当前工作目录$PWD"

export CODE_REVISION=$(git rev-parse --short HEAD)
echo "此次编译代码版本为:${CODE_REVISION}"

chmod +x cibuild.sh
/bin/sh cibuild.sh


# 来看下cibuild.sh长啥样：

#!/usr/bin/env bash

apkPrefix="SOHO_ANDROID"
bakApkPath="app/build/bakApk"

archiveDir=bin

if [[ -z ${BUILD_MODE} ]]; then
    BUILD_MODE=$1
fi

function cleanBinFolder() {
    if [[ -e ${archiveDir} ]]; then
        echo "Clean bin directory"
        rm -r ${archiveDir};
    fi
}

function cleanBakApk() {
    if [[ -e ${bakApkPath} ]]; then
        rm -r ${bakApkPath}
    fi
}

# 构架debug包
function buildDebug() {
    rm -rf app/build/outputs/apk/*

    cleanBakApk

    ${GRADLE} clean assembleDebug -PBuildMode=debug -PBuildNo=$BuildNo
}

# 构建release包
function buildRelease() {
    rm -rf app/build/outputs/apk/*

    cleanBakApk

    ${GRADLE} clean assembleRelease -PBuildMode=release -PBuildNo=$BuildNo
}

######################初始化环境######################
if [ $CI ]; then
	echo "Run in CI environment."

	chmod +x gradle
    GRADLE="gradle -PbatchRun=1 -PapkPrefix=$apkPrefix"

    gradle -v

    cleanBinFolder

    mkdir $archiveDir

	case "${BUILD_MODE}" in
    debug)
        echo "build debug"
        buildDebug
    ;;
    release)
        echo "build release"
        buildRelease
    ;;
    *)
        echo "build all"
        buildDebug
        buildRelease
    ;;
    esac
else
	echo "Run in local environment."

	cleanBinFolder

    mkdir $archiveDir

	chmod +x ./gradlew
    GRADLE="./gradlew -PbatchRun=1 -PapkPrefix=$apkPrefix"

    ./gradlew -v
    buildDebug
fi