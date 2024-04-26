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

`./run_all_tests.sh` will run all the tests, including unit and contract tests. The contract tests will use
contract files stored in the project root directory folder `pact` of both front-ends.
The tests contract verifier can be executed by running the script:
`checkincheck.sh`. Note - currently contracts test only runs for local build.

The VerifyContractSpec test will pass locally if the pact test in merchandise-in-baggage-frontend is run first.
The pact test in the frontend will populate the pact directory which it will use.

## License
This code is open source software licensed under the Apache 2.0 License.