# OQM Core Applications Chart

## Requirements:

### MongoDB Community Operator

`helm install mongo-operator community-operator --repo https://mongodb.github.io/helm-charts -n <desired namespace>`

 - This operator is scoped to a namespace, meaning that  that manifest needs to go in the same namespace as the operator.

#### Resources and further reading:

 - [MongoDB Kubernetes Operator](https://github.com/mongodb/mongodb-kubernetes-operator)
 - [Helm Chart on Artifact Hub](https://artifacthub.io/packages/helm/community-operator/mongodb)

## TODOS:

 - Determine if best to have charts at app level, then this would gather them up as dependencies
 - Add infra as dependencies, properly configure
 - Figure out how to add entries for depot's service files, or otherwise possibly do that functionality
