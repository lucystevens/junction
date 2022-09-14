![Main Status][workflow-badge-main]
![Version][version-badge]

# junction
Programmatically updatable reverse proxy based on Undertow, featuring automatic
SSL certificate ordering and renewal using LetsEncrypt

_Currently in development_

## Overview
Junction is designed to be a simple, updatable reverse proxy with SSL support.

### Quickstart
I recommend running the server with docker, using a volume for the application data
and a few required configs. 

_Note: this requires Docker v20.10 and above on Ubuntu to use 
`host.docker.internal` bind address, but this is only necessary if 
you'll be proxying to services on the host._

```shell
docker pull lucystevens/junction:latest
docker run -d -P \
  -v junction:/data \
  -e "EMAIL_ADDRESS=me@email.com" \
  -e "SECRET_KEY=secret-key" \
  -e "ACME_URL=acme://letsencrypt.org" \
  -e "DATASTORE=/data" \
  --restart=always \
  --add-host=host.docker.internal:host-gateway \
  lucystevens/junction:latest
```

### Configuration
To further configure the server beyond the above, you can use the following environment variables

| Variable      | Required     | Default   | Description                                                                    |
|---------------|--------------|-----------|--------------------------------------------------------------------------------|
| SECRET_KEY    | Yes          |           | Secret key for securing the SSL keystore and encrypting data                   |
| ACME_URL      | Yes          |           | Url for the acme renewal server.                                               |
| DATASTORE     | Yes          |           | The *directory* to persist application data to.                                |
| ADMIN_TOKEN   | No           | None      | An admin token to authenticate api routes. Recommended if these are exposed.   |
| EMAIL_ADDRESS | On first run |           | Email address for registering LetsEncrypt account. Only required on first run. |
| BIND_ADDRESS  | No           | localhost | Address for the server to listen on                                            |
| HTTP_PORT     | No           | 80        | Port for http proxy connections                                                |
| HTTPS_PORT    | No           | 443       | Port for https proxy connections                                               |
| API_PORT      | No           | 8000      | Port for apis to update routing and domains                                    |
| RSA_KEY_SIZE  | No           | 2048      | Key size to use when generating RSA keys                                       |

### API
The server exposes an internal API on the `API_PORT` (defaulting to 8000) for adding domains and routes.
If the `ADMIN_TOKEN` environment variable has been supplied, all requests will be authenticated against this
in the `token` header.

The proxy configuration consists of 2 main entities: routes and domains.
*Routes* represent a path to proxy to a target. These work fine on their own over http. 
*Domains* are used to set up SSL certificates and allow route paths to be
exposed via https.

#### Adding a route
Routes can be added via a `PUT` or `POST` request to `/api/routes`. 
Below is an example request that would expose the internal api on the
`routes.mydomain.com` host (over http).
```json
{
  "route" : {
    "host": "routes.mydomain.com",
    "path": "/api"
  },
  "targets": [
    {
      "scheme": "http",
      "host": "localhost",
      "port": 8000
    }
  ]
}
```

##### Schema:
`route`: A single route to _proxy from_
 - `host`: the host of the route (required)
 - `path`: the path _prefix_ to match, this is preserved (default: /)

`targets`: A list of targets to _proxy to_
 - `scheme`: the scheme for the proxy URL (default: http)
 - `host`: the host address to proxy to (required)
 - `port`: the port on that address to proxy to (required)

#### Adding a domain
Domains can be added via a `PUT` or `POST` request to `/api/domains`.
Below is an example request that would set up ssl for the 
`routes.mydomain.com` domain, and allow the internal api (see above) to
be accessed over https.
```json
{
  "domain": "routes.mydomain.com",
  "redirectToHttps": true,
  "enableSsl": true
}
```
_Domains must be registered this way to enable SSL for them_

Once domains are added, and have SSL set up, the certificates will be renewed automatically.

##### Schema
 - `domain`: the domain to update (required)
 - `redirectToHttps`: whether requests to this domain over http should be redirected to https (required)
 - `enableSsl`: whether to enable SSL for this domain (required)

## Roadmap
### v0.1.0
Below are the requirements before the first beta version of junction will be released:
 - [ ] Integration test suite using pebble, and guides on how to run locally
 - [ ] Automatic scheduled renewal of certificates
 - [ ] Encryption of stored keys and certificates
 - [ ] Improved validation of domains when setting up SSL
 - [ ] Swagger API documentation

### v1.0.0
Below are future improvements being considered for post-beta
 - UI for managing routes
 - Support for data stores
 - More options for routing e.g. cookie-based routes
 - DNS-01 challenge support for wildcard certificates
 - Automatic updating of DNS records when adding a domain

## Development
### Running
To run locally
```shell
./gradlew run
```

### Tests
To run unit tests:
```shell
./gradlew test
```

To run integration tests:
```shell
./gradlew integrationTest
```

### Deployment
#### Release workflow
Once the initial setup is complete, deployments will be triggered automatically via the release workflow:
Tagging a commit with a semantic version (e.g. vx.x.x) will start the workflow. 
This will build the application, create a GitHub release, and deploy it using docker.

This release workflow requires some secrets to be defined:
 - `DOCKERHUB_TOKEN` - The secret token for the Dockerhub account the image is being pushed to
 - `HOST` - The IP for the host server to deploy to
 - `SSH_KEY` - The content of the SSH key for the `deployer` user
 - `SSH_PASSPHRASE` - The passphrase to decrypt the ssh key
 

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

New features, fixes, and bugs should be branched off of main.

Please make sure to update tests as appropriate.

## License
[MIT][mit-license]

[workflow-badge-main]: https://img.shields.io/github/workflow/status/lucystevens/junction/test/main?label=main
[version-badge]: https://img.shields.io/github/v/release/lucystevens/junction
[docker-version-badge]: https://img.shields.io/docker/v/lucystevens/junction?sort=date
[mit-license]: https://choosealicense.com/licenses/mit/