oc create sa mongodb
oc create sa quartermaster-base

oc apply -f secrets.yml
oc apply -f services.yml
oc apply -f deployments.yml
oc apply -f routes.yml

