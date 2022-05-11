#/bin/bash

MINOR_VERSION=95
if [ "$1" = "jvm" ]; then
	VERSION=0.0.${MINOR_VERSION}-jvm
	NAMESPACE=quartermaster-jvm
else
	VERSION=0.0.${MINOR_VERSION}-native
	NAMESPACE=quartermaster
fi
echo Building version: ${VERSION}

next_v=$[${MINOR_VERSION}+1]
sed -i "0,/MINOR_VERSION=.*/s//MINOR_VERSION=$next_v/" ${0}

pushd ./software/libs/open-qm-core/
./gradlew clean build publishToMavenLocal -x test -x javadoc
popd

pushd software/plugins/open-qm-plugin-demo/
if [ "$1" = "jvm" ]; then
	./gradlew clean build -Pquarkus.container-image.build=true -x test -x javadoc
else
#	./gradlew clean build -Pquarkus.container-image.build=true -Pquarkus.package.type=native -x test -x javadoc
        ./gradlew clean build -Pquarkus.package.type=native -Pquarkus.native.container-build=true -Pquarkus.jib.docker-executable-name=podman -x test -x javadoc
fi
popd

pushd software/open-qm-base-station/
if [ "$1" = "jvm" ]; then
	./gradlew clean build -Pquarkus.container-image.build=true -x test -x javadoc
else
	./gradlew clean build -Pquarkus.container-image.build=true -Pquarkus.package.type=native -x test -x javadoc
fi
popd

REGISTRY=default-route-openshift-image-registry.apps.sno-demo.nixon.com

docker login ${REGISTRY} -u kubeadmin -p $(oc whoami -t)

oc project ${NAMESPACE}

docker tag root/open-qm-plugin-demo:0.0.1 ${REGISTRY}/${NAMESPACE}/open-qm-plugin-demo:${VERSION}
docker push ${REGISTRY}/${NAMESPACE}/open-qm-plugin-demo:${VERSION}

docker tag root/open-qm-base-station:1.0.0-DEV ${REGISTRY}/${NAMESPACE}/open-qm-base-station:${VERSION}
docker push ${REGISTRY}/${NAMESPACE}/open-qm-base-station:${VERSION}

oc get deployment open-qm-plugin-demo -o yaml | sed -e 's# image: i.*# image: image-registry.openshift-image-registry.svc:5000/'${NAMESPACE}'/open-qm-plugin-demo:'${VERSION}'#g' | oc apply -f -
oc get deployment open-qm-base-station -o yaml | sed -e 's# image: i.*# image: image-registry.openshift-image-registry.svc:5000/'${NAMESPACE}'/open-qm-base-station:'${VERSION}'#g' | oc apply -f -

echo "Deployed version ${VERSION} to cluster"
