# Mongodb OQM Infra Component

This component runs an instance of MongoDb for the OQM system.

## Admin Hints

### Good commands to remember:

 - Show users:

   `docker exec oqm_infra_mongo mongo admin -u "$(sudo oqm-config -g 'infra.mongodb.adminUser')" -p "$(sudo oqm-config -g 'infra.mongodb.adminPass')" --eval "db.getUsers()"`
 - Show databases:
   
   `docker exec oqm_infra_mongo mongo admin -u "$(sudo oqm-config -g 'infra.mongodb.adminUser')" -p "$(sudo oqm-config -g 'infra.mongodb.adminPass')" --eval "db.getMongo().getDBNames()"`
