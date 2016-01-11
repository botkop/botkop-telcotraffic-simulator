#!/bin/bash

curl -X POST -i -H "Content-type: application/json" -X POST http://localhost:9000/simulator/rest/request -d '
{
    "action": "stop"
}
'
