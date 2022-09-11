![Main Status][workflow-badge-main]
![Version][version-badge]

# junction
Programmatically updatable reverse proxy based on Undertow

## Running
Run locally using
```shell
./gradlew run
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

## Deployment
### Release workflow
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
[mit-license]: https://choosealicense.com/licenses/mit/