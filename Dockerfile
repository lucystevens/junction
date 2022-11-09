FROM openjdk:11 AS build

RUN mkdir /src
COPY . /src
WORKDIR /src
RUN ./gradlew shadowJar --no-daemon

FROM adoptopenjdk/openjdk11:alpine

# setup ca certs (move to different dockerfile)
RUN apk --no-cache add curl openssl ca-certificates
RUN wget https://raw.githubusercontent.com/letsencrypt/pebble/main/test/certs/pebble.minica.pem -O /usr/local/share/ca-certificates/pebble.minica.crt
RUN wget https://raw.githubusercontent.com/letsencrypt/pebble/main/test/certs/localhost/cert.pem -O /usr/local/share/ca-certificates/pebble.localhost.crt
RUN update-ca-certificates

RUN mkdir /app
COPY --from=build /src/build/libs/*.jar /app/application.jar

CMD ["java", "-jar", "/app/application.jar"]