#!/bin/bash

api_key=$1

if [ -z "$api_key" ]
then
    echo "Usage: $0 API_KEY"
    echo "See: http://opencellid.org/#register"
    exit -1
fi

wget "https://download.unwiredlabs.com/ocid/downloads?token=$api_key&file=cell_towers.csv.gz" -O - | gunzip -c > data/cell_towers.csv
