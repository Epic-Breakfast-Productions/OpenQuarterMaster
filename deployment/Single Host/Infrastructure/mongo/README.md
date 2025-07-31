# Mongodb OQM Infra Component

This component runs an instance of MongoDb for the OQM system.

## Admin Hints

### References

 - Mongodb roles docs https://www.mongodb.com/docs/manual/reference/built-in-roles/#mongodb-authrole-dbOwner
 - Mongodb user management https://www.mongodb.com/docs/v7.0/tutorial/manage-users-and-roles/#modify-access-for-an-existing-user
 - https://stackoverflow.com/questions/15272521/command-line-authentication-of-mongo-fails
 - https://www.mongodb.com/docs/manual/tutorial/enforce-keyfile-access-control-in-existing-replica-set/
 - https://www.prisma.io/dataguide/mongodb/configuring-mongodb-user-accounts-and-authentication

### Good commands to remember:

 - Show users:

   `sudo docker exec oqm-infra-mongo mongosh -u "$(sudo oqm-config -g 'infra.mongodb.adminUser')" -p "$(sudo oqm-config -g 'infra.mongodb.adminPass')" --eval "db.getUsers()"`

 - Show databases:
   
   `sudo docker exec oqm-infra-mongo mongosh -u "$(sudo oqm-config -g 'infra.mongodb.adminUser')" -p "$(sudo oqm-config -g 'infra.mongodb.adminPass')" --eval "db.getMongo().getDBNames()"`

 - Show collections in database:
   
   `sudo docker exec oqm-infra-mongo mongosh -u "$(sudo oqm-config -g 'infra.mongodb.adminUser')" -p "$(sudo oqm-config -g 'infra.mongodb.adminPass')" --eval "use oqmCoreApiMain" --eval "db.getCollectionNames()"`

 - Dump collection data to json:

   `sudo docker exec oqm-infra-mongo mongoexport --jsonArray --username=$(sudo oqm-config -g 'infra.mongodb.adminUser') --password=$(sudo oqm-config -g 'infra.mongodb.adminPass') --authenticationDatabase admin --db=oqmCoreApiMain-default --collection=ItemCheckout 2> /dev/null`
