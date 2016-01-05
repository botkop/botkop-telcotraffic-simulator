#!/bin/bash

curl -X POST -i -H "Content-type: application/json" -X POST http://localhost:9000/simulator/rest/start -d '
{
"mcc": 206,
"mnc": 10,
"numTrips": 2,
"velocity": 12000,
"slide": 1000
}
'

