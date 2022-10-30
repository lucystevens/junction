#!/bin/sh

echo "Loading Pebble CA certs"
wget https://pebble:15000/roots/0 -O /usr/local/share/ca-certificates/pebble-root.crt
wget https://pebble:15000/intermediates/0 -O /usr/local/share/ca-certificates/pebble-intermediate.crt
update-ca-certificates
echo "Pebble certs loaded"
echo "Debugging server1 with openssl"
openssl s_client -connect server1:443 -servername server1 -prexit -showcerts
echo "Sending cUrl request"
curl https://server1/api/routes