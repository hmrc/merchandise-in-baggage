# merchandise-in-baggage

**Who uses the repo/service**

This is the backend service supporting two frontends (public facing and internally for administration through stride).

Both frontend services are maintained from the [merchandise-in-baggage-frontend](https://github.com/hmrc/merchandise-in-baggage-frontend) repository
and are deployed as separate instances `merchandise-in-baggage-frontend` & `merchandise-in-baggage-admin-frontend`.

**How to start the service locally**

`sbt run` This will only start the service as standalone but unable to interact with any other services including DataBase

To complete a journey locally run the services (minus the frontend) via [service manager 2](https://github.com/hmrc/sm2) with the following profile:
```bash
sm2 --start MERCHANDISE_IN_BAGGAGE_ALL
sm2 --stop MERCHANDISE_IN_BAGGAGE
```

`local base url and port` http://localhost:8280/

**How to run tests**

`./run_all_tests.sh` will run all the tests, including unit and contract tests. The contract tests will use
contract files stored in the project root directory folder `pact` of both front-ends.
The tests contract verifier can be executed by running the script:
`checkincheck.sh`. Note - currently contracts test only runs for local build.


The VerifyContractSpec test will pass locally if the pact test in merchandise-in-baggage-frontend is run first.
The pact test in the frontend will populate the pact directory which it will use.

**Endpoints**
POST        /declarations       _to persist a declaration_                

PUT         /declarations       _to update_                

GET         /declarations/:id   _find a declaration by id_                

GET         /declarations       _get all declarations_                

POST        /calculations       _calculates due payments if any_                
POST        /amend-calculations _calculates due payments if any for amendment to an existing declaration_                

POST        /payment-callback   _callback endpoint for payment service_                

GET         /validate/eori/:eoriNumber  _validate eori by calling an API_        

GET         /exchange-rate-url  _fetch available current exchange rate available by calling an API_

