### Application UP
    docker-compose up --build
### Application DOWN
    docker-compose down


### MULTI_STAGE_DOCKER_FILE
    https://levelup.gitconnected.com/docker-multi-stage-builds-and-copy-from-other-images-3bf1a2b095e0

## Commands
### Build Image from Dockerfile:
    sudo docker build . -t <IMAGE_NAME> // IMAGE_NAME = service_registry_microservice:0.0.1 // tag=0.0.1

### Run Image with port:
    docker run -d -p 81:80 --name <CONTAINER_NAME> <IMAGE_NAME>
    ex: sudo docker run -d -p 8070:8070 --name service_registry_container service_registry_microservice:0.0.1

### Details of Image with jar location:
    docker image inspect <IMAGE_ID>
    default location of jar: /usr/local/lib/*.jar

### Enter inside a container:
    sudo docker exec -it <CONTAINER_ID> bash

### Start a container:
    sudo docker start <CONTAINER_ID>

### Stop a container:
    sudo docker stop <CONTAINER_ID>

### Monitor logs of a container:
    sudo docker logs <CONTAINER_ID>

### Remove a container:
    sudo docker rm <CONTAINER_ID>

### Remove a image:
    sudo docker rmi <IMAGE_ID>

### CONNECTION OF A SERVICE WITH ANOTHER SERVICE USING DOCKER
    http://host.docker.internal

    we should pass this instead of http://localhost

## Our Case:
- [ ] to connect config-server with service-registry(eureka service) we should run the following cmd
```
    sudo docker run -d -p 8091:8091 --add-host=host.docker.internal:172.17.0.1 -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8070/eureka --name config_server_container config_server_microservice:0.0.1
    
    here, 8070 is the port of service registry
    -e means environment_variable, and 
    the variable name is EUREKA_HOST which is mentioned in the application.yml of config server
```
### [doc for add host](https://codeopolis.com/posts/add-a-host-entry-to-a-docker-container/)