# merchandise-in-baggage

This backend service supports supports two frontends (public facing and internally for administration through
stride).

## Start service locally

`sbt run` will only start the service as standalone.

To complete a journey locally, run the services via [service manager 2](https://github.com/hmrc/sm2)
with the following profile:

```bash
sm2 --start MERCHANDISE_IN_BAGGAGE_ALL
```

## Run tests

`./run_all_tests.sh` will run all the tests, including unit and contract tests.

## License
This code is open source software licensed under the Apache 2.0 License.