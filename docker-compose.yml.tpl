version: '2'

services:

  missioni:
    image: docker.si.cnr.it/##{CONTAINER_ID}##
    volumes:
    - /tmp
    - /logs
    network_mode: bridge
    command: java
      -Xmx512m
      -Xss512k
      -Dserver.port=8080
      -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8787
      -Dkeycloak.credentials.secret=20ca9914-f235-412c-a80d-43ce30a97798
      -Dkeycloak.auth-server-url=https://traefik.test.si.cnr.it/auth
      -Dmaven.wagon.http.ssl.insecure=true
      -Dmaven.wagon.http.ssl.allowall=true
      -Dace.signing.key='NDh5dm5vcTQ2Nm41b2tud3ZqcDU='
      -Djava.security.egd=file:/dev/./urandom -jar /opt/missioni.war --spring.config.location=classpath:config/application.yml,file:/opt/application-cnr.yml,classpath:config/application-keycloak.yml --spring.profiles.active=dev,cnr,keycloak,swagger
    volumes:
    - ./application-dev.yml:/opt/application-cnr.yml
    labels:
      SERVICE_NAME: "##{SERVICE_NAME}##"