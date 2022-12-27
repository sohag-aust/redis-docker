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
