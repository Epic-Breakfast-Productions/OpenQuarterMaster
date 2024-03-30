oc create sa quartermaster-base

oc apply -f AuthorizationPolicy.yml
oc apply -f gateway.yml
oc apply -f networkpolicy.yml
oc apply -f requestauthentication.yml
oc apply -f serviceentry.yml
oc apply -f VirtualService.yml
oc apply -f vs-external.yml

