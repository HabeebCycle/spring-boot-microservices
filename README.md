# Building product components microservices using spring boot and spring cloud.

## The framework we used are Spring Boot, Spring Cloud, Docker, Reactjs, Kubernetes, RabbitMQ, Kafka, MySQL, MongoDB, Redis and other frameworks.

* The microservices are built with a reactive approach using Spring Boot and project reactor (webflux)
* It is resilient and scalable with the help of Spring Cloud resilient4J and Spring Cloud gateway
* It uses OAuth 2.0/OIDC and Spring Security to protect public APIs
* It implements Docker to bridge the gap between development, testing, staging and production.
* It leverages Kubernetes to deploy and manage each microservice container in each environment.
* It applies Istio for improved security, observability and traffic management.
* It uses ReactJs (Hooks), Redux and NodeJs as part of the services.

### 1. Build the basic project
This project is created little by little with each branch for difficulty level. Clone the branch you need to build and run `mvn clean install`

### 2. Docker Setup
* Create a new spring profile in the application.yaml file called 'docker' as ```spring.config.activate:on-profile: - docker 
server.port: 8080``` for each of the microservices. 
  
* Create a Dockerfile at the root folder of each microservice.
* Create a docker-compose.yaml file at the root of the parent.
* Run the following command:
```shell
./mvn clean install
docker-compose build
docker-compose up -d

# OR

mvn clean install && docker-compose build && docker-compose up -d
```

### 3. Swagger API Documentation
Visit the API documentation at
```shell
http://localhost:8080/swagger-ui/index.html
```

### 4. Persistence and Database command in a container

```shell
# MongoDB
docker-compose exec <mongodb-container-name> mongo <db-name> --quiet --eval "db.<collection-name>.find()"

# MySQL
docker-compose exec <mysql-container-name> mysql -u <user-name> -p <db-name> -e "select * from <table-name>"; it will ask for <password>

# Redis
docker-compose exec <redis-container-name> redis-cli auth <password> keys *
# for "string": get <key>
# for "hash": hgetall <key>
# for "list": lrange <key> 0 -1
# for "set": smembers <key>
# for "zset": zrange <key> 0 -1 withscores
```

### 5. Reactive API and Event-Driven Services
product-composite-service:
  GET - POST - DELETE is based on non-blocking synchronous APIs
product-service, recommendation-service and the review-service
  GET is based on non-blocking synchronous APIs
  POST and DELETE is based on event-driven asynchronous services using Apache Kafka and RabbitMQ messaging services.

#### Commands Used Here
mvn clean install && docker-compose build && docker-compose up -d
curl -s localhost:8080/actuator/health | jq -r .status
```shell
For RabbitMQ: http://localhost:15672/#/queues

body='{"productId":1,"name":"product name C","weight":300, 
    "recommendations":[
        {"recommendationId":1,"author":"author 1","rate":1,"content":"content 1"},
        {"recommendationId":2,"author":"author 2","rate":2,"content":"content 2"},
        {"recommendationId":3,"author":"author 3","rate":3,"content":"content 3"}],
    "reviews":[
        {"reviewId":1,"author":"author 1","subject":"subject 1","content":"content 1"},
        {"reviewId":2,"author":"author 2","subject":"subject 2","content":"content 2"},
        {"reviewId":3,"author":"author 3","subject":"subject 3","content":"content 3"}
    ]
}'

curl -X POST localhost:8080/product-composite -H "Content-Type:application/json" --data "$body"
curl localhost:8080/product-composite/1 | jq
curl -X DELETE localhost:8080/product-composite/1

# For RabbitMQ with 2 partitions
export COMPOSE_FILE=docker-compose-rabbitmq-partitions.yaml
docker-compose build && docker-compose up -d
#Try above commands, then,
docker-compose down
unset COMPOSE_FILE

# For Kafka with 2 partitions
export COMPOSE_FILE=docker-compose-kafka.yaml
docker-compose build && docker-compose up -d
#To see a list of topics, run the following command
docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper --list
#To see the partitions in a specific topic, for example, the products topic,
docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --describe --zookeeper zookeeper --topic products
#To see all the messages in a specific topic, for example, the products topic
docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic products --from-beginning --timeout-ms 1000
#To see all the messages in a specific partition, for example, partition 1 in reviews topic
docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic reviews --from-beginning --timeout-ms 1000 --partition 1
#The output will end with a timeout exception since we stop the command by specifying a timeout for the command of 1000 milliseconds
#Try above commands, then,
docker-compose down
unset COMPOSE_FILE
```
For any issue with docker-compose, run `rm ~/.docker/config.json`
