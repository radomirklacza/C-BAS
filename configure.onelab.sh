#!/bin/bash
#
# This will configure the deployment

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
cd $SCRIPTPATH/deploy
for f in *.json.example; do 
    cp -- "$f" "${f%.json.example}.json"
done
for f in *.json.onelab; do 
    cp -- "$f" "${f%.json.onelab}.json"
done
