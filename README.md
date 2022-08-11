# rtd-ms-file-register
Micro-service designed to register the state of each and every file handled by CSTAR platform.

## Local deployment
To locally deploy the file register you need to run the following command:
```bash
docker-compose up -d --build
```
> You need to have Docker installed and running.


## Local integration tests
Once the 2 containers are running, you can run the following command to carry out the integration tests.
```bash
newman run postman/local_file_register_integration_test.postman_collection.json
```
> You need to have [newman](https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/) installed.

> The environment is inside the collection.
>
> It is fully customizable both inside the JSON or after importing it in Postman.
