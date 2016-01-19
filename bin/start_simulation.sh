#!/bin/bash

curl -X POST -i -H "Content-type: application/json" -X POST http://localhost:9000/simulator/rest/request -d '
{
    "action": "start",
    "request": {
        "mcc": 206,
        "mnc": 10,
        "numTrips": 6,
        "velocity": 120,
        "slide": 500,
        "speedFactor": 1.0
    }
}
'

