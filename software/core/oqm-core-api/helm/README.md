# OQM Depot Helm Charts

## Requirements:

 - MongoDB Community Operator:
   1. `helm repo add mongodb https://mongodb.github.io/helm-charts`
   2. `helm repo update`
   3. `helm install mongodb-community-operator mongodb/community-operator --namespace mongodb-operator --set operator.watchNamespace="*"`
 - Keycloak Operator

## TODOS:

 - configuration
