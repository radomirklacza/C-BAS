#!/bin/bash
#
# This will configure the deployment

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
rename 's/.json.example$/.json/' $SCRIPTPATH/deploy/*.json.example
$SCRIPTPATH/test/creds/gen-certs.sh auth.noc.onelab.eu
