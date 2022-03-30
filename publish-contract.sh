#!/bin/bash
docker run --rm -w ${PWD} -v ${PWD}:${PWD} pactfoundation/pact-cli:latest publish ${PWD}/Contracts/  --broker-base-url http://23.dfder.tw:10141  --consumer-app-version "v0.1"

