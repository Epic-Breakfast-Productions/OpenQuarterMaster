# Mongodb OQM Infra Component


## Admin Hints

### Good commands to remember:

 - `docker exec oqm_infra_mongo mongo admin -u "$(sudo oqm-config -g 'infra.mongodb.adminUser')" -p "$(sudo oqm-config -g 'infra.mongodb.adminPass')" --eval "db.getUsers()"`
