# Avro + Schema Registry for Order Service

## Infrastructure

This project uses a shared Docker network so both repositories can resolve `kafka` and `schema-registry` from their own Compose files.

Create the shared network once:

```bash
docker network create shared-network
```

Start the shared Kafka + Schema Registry + MongoDB stack from Payment Service:

```bash
docker compose -f ../Innowise_Vitali_Payment_Service/docker-compose.yml up -d zookeeper kafka schema-registry mongodb
```

Then start the Order Service stack:

```bash
docker compose -f ./docker-compose.yml up -d postgres order-service
```

Then start Order Service locally if needed:

```bash
./mvnw spring-boot:run
```

## Schema Registry checks

```bash
curl http://localhost:8081/subjects
curl http://localhost:8081/subjects/com.example.events-value/versions
curl http://localhost:8081/config/com.example.events-value
```

## Compatibility

```bash
curl -X PUT http://localhost:8081/config/com.example.events-value \
  -H 'Content-Type: application/json' \
  -d '{"compatibility":"BACKWARD"}'
```
