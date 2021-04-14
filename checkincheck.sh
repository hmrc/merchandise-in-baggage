#!/usr/bin/env bash

chmod a+x checkincheck.sh


pactlist=("../merchandise-in-baggage-frontend" "../merchandise-in-baggage-internal-frontend")


contractsList() {
  for file in "$PACT"*; do
    printf "%s\n" "$file" | cut -d"/" -f4
  done
}

# clean build everything
sbt clean test

for PACT in "${pactlist[@]}"; do
    cd $PACT
    sbt "testOnly *VerifyContractSpec;"
done

# contract test everything
for PACT in "${pactlist[@]}"; do
  export PACTTEST=$PACT/pact/
  if [ -d $PACTTEST ] && [ "$(ls -A $PACTTEST)" ]; then
    printf "#####################\nContracts found in $PACTTEST \n"
    contractsList
    printf "Running tests including pact contract verifier... \n#####################\n"
    cd ../merchandise-in-baggage
    sbt "contractVerifier;"
  else
    printf "#####################\nThere are not contract tests in $PACTTEST. Running any other tests... \n#####################\n"
  fi
done