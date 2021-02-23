#!/usr/bin/env bash

chmod a+x checkincheck.sh

PACT="../merchandise-in-baggage-frontend/pact/"

contractsList() {
  for file in "$PACT"*; do
    printf "%s\n" "$file" | cut -d"/" -f4
  done
}


if [ -d $PACT ] && [ "$(ls -A $PACT)" ]; then
  printf "#####################\nContracts found in $PACT \n"
  contractsList
  printf "Running tests including pact contract verifier... \n#####################\n"
else
  printf "#####################\nThere are not contract tests in $PACT. Running any other tests... \n#####################\n"
fi

sbt "test;"