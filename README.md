# merchandise-in-baggage

**Who uses the repo/service**

This is the back-end service supporting two front-ends (one is public facing and one used internally through stride)

**How to start the service locally**

`sbt run` This will only start the service as standalone but unable to interact with any other services including DataBase

SM profile : MERCHANDISE_IN_BAGGAGE_ALL
`sm MERCHANDISE_IN_BAGGAGE_ALL` This will start all the required services to complete a journey

`local base url and port` http://localhost:8280/

**How to run tests**

`sbt test` will run all the tests, including unit and contract tests. The contract tests will use
contract files stored in the project root directory folder `pact` of both front-ends.
The tests contract verifier can be executed by running the script:
`checkincheck.sh`. Note - currently contracts test only runs for local build.

**Endpoints**

