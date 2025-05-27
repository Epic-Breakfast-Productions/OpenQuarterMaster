# OQM Core API Helm Charts

## Requirements:

### MongoDB Community Operator

`helm install mongo-operator community-operator --repo https://mongodb.github.io/helm-charts -n <desired namespace>`

- This operator is scoped to a namespace, meaning that the manifest needs to go in the same namespace as the operator.

#### Resources and further reading:

- TODO

## TODOS:

- Determine if best to have charts at app level, then this would gather them up as dependencies
- Add infra as dependencies, properly configure
- (optional?) kafka operator include
-
