# Docs


## DB tips n tricks

## Getting schema

Get table names:

```
$ psql -U quarkus

# \dt+
```

Dump table schema creation script:

`pg_dump -U quarkus -t public.modulerecord --schema-only`
