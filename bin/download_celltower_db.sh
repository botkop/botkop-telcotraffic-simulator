#!/bin/bash

api_key=$1

if [ -z "$api_key" ]
then
    echo "Usage: $0 API_KEY"
    echo "See: http://opencellid.org/#action=database.requestForApiKey"
    exit -1
fi

wget "http://opencellid.org/downloads/?apiKey=$api_key&filename=cell_towers.csv.gz" -O - | gunzip -c > dist/cell_towers.csv

