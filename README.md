# merchandise-in-baggage

## Who uses the repo/service

merchandise-in-baggage backend service supports two frontends (public facing and internally for administration through
stride).

Both `merchandise-in-baggage-frontend` & `merchandise-in-baggage-admin-frontend`
services are maintained from
the [merchandise-in-baggage-frontend](https://github.com/hmrc/merchandise-in-baggage-frontend) repository
and are deployed as separate instances.

## Start service locally

`sbt run` This will only start the service as standalone but unable to interact with any other services

To complete a journey locally run the services via [service manager 2](https://github.com/hmrc/sm2)
with the following profile:

```bash
sm2 --start MERCHANDISE_IN_BAGGAGE_ALL
```

### Service Local URL

For admin facing you will need to login through stride with the roles found in `application.conf`

```
http://localhost:8281/declare-commercial-goods/start-import 
``` 

## How to run tests

`./run_all_tests.sh` will run all the tests, including unit and contract tests. The contract tests will use
contract files stored in the project root directory folder `pact` of both front-ends.
The tests contract verifier can be executed by running the script:
`checkincheck.sh`. Note - currently contracts test only runs for local build.

The VerifyContractSpec test will pass locally if the pact test in merchandise-in-baggage-frontend is run first.
The pact test in the frontend will populate the pact directory which it will use.

# Endpoints

## GET

### find a declaration by id

```
GET         /declarations/:id   
```

### get all declarations

```
GET         /declarations
```

### validate eori by calling an API

```
GET         /validate/eori/:eoriNumber
```

### fetch available current exchange rate available by calling an API

```
GET         /exchange-rate-url 
```

## POST

### to persist a declaration

```
POST        /declarations
```

### calculates due payments if any

```
POST        /calculations
```

### calculates due payments if any for amendment to an existing declaration

```
POST        /amend-calculations
```

### callback endpoint for payment service

```
POST        /payment-callback
```

## PUT

### to update a decalration

```
PUT         /declarations
```