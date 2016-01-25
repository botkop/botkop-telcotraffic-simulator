#!/bin/bash

curl -X POST -i -H "Content-type: application/json" -X POST http://localhost:9000/simulator/rest/request -d '
{
    "action": "update",
    "request": {
        "velocity": 1200,
        "slide": 100,
        "topic": "request-topic"
    }
}
'

