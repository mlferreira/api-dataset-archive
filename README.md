# Dataset Archive API

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites

#### [MySQL](https://www.mysql.com/)

##### Using [Docker](https://hub.docker.com/_/mysql):

In the terminal/cmd, pull the image and run the container: 
```console
docker run -d -p 3306:3306 --name mysql57 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=ApiSemNome mysql/mysql-server:5.7
```
If image already pulled, start the container using ` docker start mysql57 `
To check if image is running: ` docker ps -a `
<!---
Start the bash shell inside the container:
```console
docker exec -it mysql57 bash
```
Run the MySQL client from bash:
```bash
mysql -uroot -proot
```
Create the user used by the api:
```mysql
CREATE USER 'api'@'%' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON *.* TO 'api'@'%';
FLUSH PRIVILEGES;
``` --->
Run the `configDatabase.sql` script in the container:
```console
docker exec -i mysql57 mysql -uroot -proot mysql < configDatabase.sql
```

#### [Redis](https://redis.io/)

##### Using [Docker](https://hub.docker.com/_/redis):

In the terminal/cmd, pull the image and run the container:  
```console
docker run --name redis -d -p 6379:6379 redis:latest
```
If image already pulled, start the container using ` docker start mysql57 `
To check if image is running: ` docker ps -a `


### Installing


## Running the tests

[Swagger](http://localhost:8080/swagger-ui.html#/)

<!--
### Installing

A step by step series of examples that tell you how to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system


## Built With

* [Docker](https://www.docker.com/) - Container
* [MySQL](https://www.mysql.com/) - Database
* [Redis](https://redis.io/) - Cache
* [Gradle](https://gradle.org/) - Dependency Management
* [Kotlin](https://kotlinlang.org/) - The programming language used
* [Angular](https://angular.io/) - The web framework used


## Authors

* **Melina Ferreira** - *Initial work* - [mlferreira](https://github.com/mlferreira)


## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
 --->