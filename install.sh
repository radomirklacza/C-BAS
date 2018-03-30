#!/bin/bash
#
# This will install the dependencies
apt-get install -y swig mongodb python-pip python-dev libffi-dev xmlsec1 git libssl-dev
pip install --upgrade pip
sudo -H pip install -r requirements.txt
