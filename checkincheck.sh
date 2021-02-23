#!/usr/bin/env bash

chmod a+x checkincheck.sh

printf "#####################\nRunning test including pact contract verifier tests... \n#####################\n"

sbt "test; testOnly *VerifyContractSpec;"
