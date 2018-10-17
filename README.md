
[![Build Status](https://travis-ci.org/EICT/C-BAS.svg?branch=master)](https://travis-ci.org/EICT/C-BAS)


#C-BAS: Certificate-based AAA for SDN Experimental Facilities

##Description
C-BAS is a certificate-based AAA architecture for SDN experimental facilities, which is by design both secure and flexible. We are developing C-BAS as a robust AAA infrastructure solution to identify experimenters, police their actions based on the associated roles, facilitate secure resource sharing, and provide for detailed accountability.


##Publications
Interested readers should consider reviewing the following papers, which overview C-BAS architecture and implementation as well as presents a migration path for its introduction in larger scale in SDN experimental facilities.

* Umar Toseef, Adel Zaalouk, Tom Rothe, Matthew Broadbent, and Kostas Pentikousis, ["C-BAS: Certificate-based AAA for SDN Experimental Facilities"](http://www.fp7-alien.eu/files/publications/EWSDN2014-ALIEN-CBAS.pdf), Proc. EWSDN 2014,   doi>[10.1109/EWSDN.2014.41](http://dx.doi.org/10.1109/EWSDN.2014.41), Budapest, Hungary, September 2014.

* Umar Toseef and Kostas Pentikousis, ["Implementation of C-BAS: Certificate-based AAA for SDN Experimental Facilities"](http://www.ict-felix.eu/wp-content/uploads/2015/06/CBAS_NCCA_15.pdf), Proc. IEEE NCCA 2015, Munich, Germany, June 2015. 

##Acknowledgement
This work has been partially funded by the Commission of the European Union within the framework of the FP7 projects ALIEN (www.fp7-alien.eu) and FELIX (www.ict-felix.eu).

##Wiki
Please refer to [wiki section](https://github.com/EICT/C-BAS/wiki) for installation and usage instructions.


## Installation (compliant with MySlice v2.0)

Prerequisites:
- It is recommended to use python virtualenv
- It is required to add a DNS entry for your.domain.com that will point into your public IPv4 (for the development purposes you may just add an entry in the /etc/hosts)
- You need to install git

This software was tested under Ubuntu 16.04LTS / 18.04.1 LTS and Python 2.7. 

1. Clone the repository
```bash
git clone git@github.com:radomirklacza/C-BAS.git
cd C-BAS/
```
2. Install dependencies 
```bash
sudo ./install_dependencies.sh
``` 

3. Install python modules (python 2.7):

```bash
pip install --upgrade pip
pip install -r requirements.txt
```
4. Copy default configurations:
```bash 
./configure.onelab.sh 
```
5. Update src/plugins/geni_trust/gen-certs.py. Replace auth.onelab.eu with your.domain.com
6. Generate private key and certificate: 
```bash 
sh test/creds/gen-certs.sh your.domain.com
```
7. Start C-BAS: 
```bash
sudo sh cbas.sh start
```
## Thirs party tools
### Mongodb GUI client: 
https://robomongo.org/