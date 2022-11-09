#!/bin/sh

echo "Loading Pebble CA certs"
wget https://pebble:15000/roots/0 -O /usr/local/share/ca-certificates/pebble-root.crt
wget https://pebble:15000/intermediates/0 -O /usr/local/share/ca-certificates/pebble-intermediate.crt
update-ca-certificates
keytool -importcert -file /usr/local/share/ca-certificates/pebble-root.crt -cacerts -keypass changeit -storepass changeit -noprompt -alias root
keytool -importcert -file /usr/local/share/ca-certificates/pebble-intermediate.crt -cacerts -keypass changeit -storepass changeit -noprompt -alias intermediate
echo "Pebble certs loaded"