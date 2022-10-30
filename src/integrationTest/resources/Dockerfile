FROM eclipse-temurin:11-alpine

# in future we'll run the integration tests in this container
RUN apk --no-cache add curl openssl ca-certificates
RUN wget https://raw.githubusercontent.com/letsencrypt/pebble/main/test/certs/pebble.minica.pem -O /usr/local/share/ca-certificates/pebble.minica.crt
RUN wget https://raw.githubusercontent.com/letsencrypt/pebble/main/test/certs/localhost/cert.pem -O /usr/local/share/ca-certificates/pebble.localhost.crt
RUN update-ca-certificates
COPY run.sh /

CMD ./run.sh
