#!/bin/bash

curl -X POST -i -H "Content-type: application/json" -X POST http://localhost:9000/simulator/rest/start -d '
{
"mcc": 206,
"mnc": 10,
"numTrips": 2,
"velocity": 120,
"slide": 500,
"speedFactor": 1.0
}
'

