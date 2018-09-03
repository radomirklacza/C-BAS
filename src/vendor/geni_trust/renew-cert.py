#!/usr/bin/env python

import sys
import os.path
import geniutil
import datetime

MA_CERT_FILE = 'ma-cert.pem'
MA_KEY_FILE = 'ma-key.pem'

CRED_EXPIRY = datetime.datetime.utcnow() + datetime.timedelta(days=100)

def update_user(cert, creds, email):
        import pymongo
        client = pymongo.MongoClient('localhost', 27017)
        database = client['ohouse']

        member = database['ma'].find_one({'MEMBER_EMAIL':email})
        member["MEMBER_CERTIFICATE"] = cert
        #member["MEMBER_UID"] = uuid
        #member["MEMBER_FIRSTNAME"] = firstName
        #member["MEMBER_LASTNAME"] = lastName
        #member["MEMBER_USERNAME"] = username
        #member["MEMBER_EMAIL"] = email
        member["MEMBER_CREDENTIALS"] = creds
        #member["MEMBER_URN"] = urn
        #member["type"] = "member"

        database['ma'].save(member)

def get_user(email):
        import pymongo
        client = pymongo.MongoClient('localhost', 27017)
        database = client['ohouse']

        return database['ma'].find_one({'MEMBER_EMAIL':email})

def read_file(dir_path, filename):
    path = os.path.join(dir_path, filename)
    contents = None
    with open(path, 'r') as f:
        contents = f.read()
    return contents

if __name__ == "__main__":
    email = "root@cbas.de"
    cert_serial_number = 99
    member = get_user(email)

    pathname = os.path.dirname(sys.argv[0])        
    print('path =', pathname)
    print('full path =', os.path.abspath(pathname))
    fullpath = os.path.abspath(pathname)
    dir_path = fullpath + "/../../../deploy/trusted/" 
    ma_pr = read_file(dir_path + "cert_keys/", MA_KEY_FILE)
    ma_c =  read_file(dir_path + "certs/", MA_CERT_FILE)

    a_c,a_pu,a_pr = geniutil.create_certificate(member['MEMBER_URN'], issuer_key=ma_pr, issuer_cert=ma_c, email=email, serial_number=cert_serial_number, uuidarg=member['MEMBER_UID'], life_days=10000)
    #write_file(dir_path, ADMIN_CERT_FILE, a_c, opts.silent)
    #write_file(dir_path, ADMIN_KEY_FILE, a_pr, opts.silent)
    p_list = ["GLOBAL_MEMBERS_VIEW", "GLOBAL_MEMBERS_WILDCARDS", "GLOBAL_PROJECTS_MONITOR", "GLOBAL_PROJECTS_VIEW",
              "GLOBAL_PROJECTS_WILDCARDS", "MEMBER_REGISTER", "SERVICE_REMOVE", "SERVICE_VIEW",
              "MEMBER_REMOVE_REGISTRATION", "SERVICE_REGISTER", "info"]
    a_cred = geniutil.create_credential_ex(a_c, a_c, ma_pr, ma_c, p_list, CRED_EXPIRY)
    #write_file(dir_path, ADMIN_CRED_FILE, a_cred, opts.silent)
    update_user(a_c,a_cred,email)

   
