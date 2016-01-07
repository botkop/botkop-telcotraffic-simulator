#!/bin/bash

sf=$1

curl -X POST -i -H "Content-type: application/json" -X POST http://localhost:9000/simulator/rest/speed -d "{ \"speedFactor\": $sf }"

