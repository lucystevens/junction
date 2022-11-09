FROM eclipse-temurin:11-alpine

ENV INTEGRATION_CONTAINER_CHECK=true


# setup ca certs
RUN apk --no-cache add curl openssl ca-certificates
RUN wget https://raw.githubusercontent.com/letsencrypt/pebble/main/test/certs/pebble.minica.pem -O /usr/local/share/ca-certificates/pebble.minica.crt
RUN wget https://raw.githubusercontent.com/letsencrypt/pebble/main/test/certs/localhost/cert.pem -O /usr/local/share/ca-certificates/pebble.localhost.crt
RUN update-ca-certificates

# Copy project
RUN mkdir /app
COPY src/ /app/src
COPY build.gradle.kts /app/
COPY gradlew /app/
COPY settings.gradle.kts /app/
COPY gradle /app/gradle
WORKDIR /app
RUN ./gradlew integrationTestClasses

CMD src/integrationTest/resources/load-pebble-certs.sh && ./gradlew integrationTestInternal
