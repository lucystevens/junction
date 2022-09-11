![Main Status][workflow-badge-main]
![Version][version-badge]

# junction
A template repo for starting kotlin applications.

Remove `.github/workflows/initialise-template.yml` once created.

## Template features
This repo contains boilerplate code for an API, with database access.

## Running
Run locally using
```shell
./gradlew run -Dlogback.configurationFile=src/test/resources/logback-test.xml
```

### Configuration
#### Environment variables
For connecting to the database
- `DATABASE_URL` - String. The JDBC url for the database.
- `DATABASE_USERNAME` - String. The username for the database.
- `DATABASE_PASSWORD` - String. The password for the database.

For running of the application server
- `ADMIN_TOKEN` - String. Secret token uses to authenticate admin requests
- `APP_PORT` - Integer. The port which the http server will listen on. Defaults to 7000.

## Tests
To run unit tests:
```shell
./gradlew test
```

To run integration tests:
```shell
./gradlew integrationTest
```

## Deploying as a service
### First time server setup
Before deploying the application to a server automatically, some initial manual setup needs to be done:

#### Setup deployer user
As root, create the `deployer` user and services directory if they don't exist
```shell
adduser deployer
sudo mkdir /services
sudo chown deployer /services
```

Set up SSH key access for the `deployer` user, you'll need this later.
More detail can be found here: https://www.digitalocean.com/community/tutorials/how-to-set-up-ssh-keys-on-ubuntu-20-04

Setup user as sudoer for systemctl commands.
Add these lines to the `/etc/sudoers.d/deployer` file:
```shell
%deployer ALL= NOPASSWD: /bin/systemctl start junction
%deployer ALL= NOPASSWD: /bin/systemctl stop junction
%deployer ALL= NOPASSWD: /bin/systemctl restart junction
```

#### Setup application as service
First ssh onto the server and create the service directory
```shell
ssh deployer@server
mkdir /services/junction
```

Logout, and copy files over to the server
```shell
scp service/* deployer@server:/services/junction
```

Log back in and install as a service
```shell
ssh deployer@server

touch /services/junction/version
chmod +x /services/junction/run
chmod +x /services/junction/deploy.sh

sudo cp /services/junction/kotlin-app-template.service /etc/systemd/system/
sudo systemctl daemon-reload
```

### Release workflow
Once the initial setup is complete, deployments will be triggered automatically via the release workflow:
Tagging a commit with a semantic version (e.g. vx.x.x) will start the workflow. 
This will build the application, create a GitHub release, and deploy it as a service.

This release workflow requires some secrets to be defined:
 - `HOST` - The IP for the host server to deploy to
 - `SSH_KEY` - The content of the SSH key for the `deployer` user
 - `SSH_PASSPHRASE` - The passphrase to decrypt the ssh key


## Deploying as a Docker container
Services deployed as a Docker container are not quite as seamless as those deployed as a service, and require some additional
manual steps. There is also some initial set up work to be done if the server has not yet been used for Docker applications.

### First time server setup
As root, create the `docker` user
```shell
adduser docker
```

Install Docker, using this guide: https://docs.docker.com/engine/install/ubuntu/
Then add the `docker` user to the `docker` group
```shell
sudo groupadd docker
sudo usermod -aG docker docker
```

### Release workflow
Tagging a commit with a semantic version (e.g. vx.x.x) will start the workflow.
This will build the application, create a GitHub release, and push a tagged Docker image to the central repository.

This release workflow requires some secrets to be defined:
- `DOCKERHUB_TOKEN` - The secret token for the Dockerhub account the image is being pushed to

### Updating the running application
Once the new image has been pushed by the step above, you can update the running container.
You should be logged in as the `docker` user created above for these steps.

If running, stop the existing container and remove it
```shell
docker stop junction
docker rm junction
```

Run the new container, remembering to define any necessary environment variables and the exposed port:
```shell
docker run -d -p PORT:7000 --env-file junction.env lucystevens/kotlin-app-template:latest
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

New features, fixes, and bugs should be branched off of main.

Please make sure to update tests as appropriate.

## License
[MIT][mit-license]

[workflow-badge-main]: https://img.shields.io/github/workflow/status/lucystevens/junction/test/main?label=main
[version-badge]: https://img.shields.io/github/v/release/lucystevens/junction
[mit-license]: https://choosealicense.com/licenses/mit/