import pymongo

import eisoil.core.pluginmanager as pm

from eisoil.core import serviceinterface

import eisoil.core.log
logger=eisoil.core.log.getLogger('mongodb')

class MongoDB(object):
    """
    Lightweight layer between Ohouse and a MongoDB database.

    For more details, please see:
        * http://api.mongodb.org/python/2.6.3/
        * http://docs.mongodb.org/manual/reference/method/

    """

    def __init__(self, ip_hostname, port, database_name):
        """
        Initialise a MongoDB Client and connect to a database named in config file through 'database_name'.
        """

        client = pymongo.MongoClient(ip_hostname, port)
        self.ip_hostname = ip_hostname
        self.port = port
        self.database_name = database_name
        logger.warning("Connected to DB {}:{}".format(ip_hostname,port))
        logger.warning("DB name={}".format(database_name))
        self._database = client[database_name]

    @serviceinterface
    def set_index(self, collection, index, unique=True):
        """
        Set a unique index in a collection.

        `unique=True` ensures unique keys are inforced in the index.
        `sparse=True` enables document insertion without the key present.

        Args:
            collection: name of collection ('sa' or 'ma')
            index: name of index ('SLICE_URN' for example)

        """
        self._database[collection].ensure_index(index, unique=unique, sparse=True)

    @serviceinterface
    def create(self, collection, document):
        """
        Create a new entry within a collection.

        Args:
            collection: name of collection ('sa' or 'ma')
            document: dictionary of key-value pairs to add as a new (single)
                entry

        Raises:
            Exception: a duplicate key was found in database

        """
        try:
            self._database[collection].insert(document)
        except pymongo.errors.DuplicateKeyError as e:
            raise Exception(e)

    @serviceinterface
    def update(self, collection, query, update, upsert=False):
        """
        Update an existing entry within a collection.

        Args:
            collection: name of collection ('ma' or 'sa')
            query: dictionary of key-value pairs to search for
            update: dictionary of key-value pairs to update

        """
        self._database[collection].update(query, {"$set": update}, upsert=upsert)

    @serviceinterface
    def delete(self, collection, query):
        """
        Remove existing entry within a collection.

        Args:
            collection: name of collection ('ma' or 'sa')
            query: dictionary of key-value pairs to search for

        """
        self._database[collection].remove(query)

    @serviceinterface
    def lookup(self, collection, criteria, projection=None):
        """
        Lookup existing entries within a collection.

        Iterate over the Cursor object returned from database to create a
        list of results in dictionary format.

        Args:
            collection: name of collection ('ma' or 'sa')
            criteria: dictionary of key-value pairs to search for
            projection: list of keys to return in result

        Returns:
            list of results in dictionary format

        """

        #projection['_id'] = False
        #projection = None
        logger.warning("database={}, ip={}, port = {}".format(self.database_name,self.ip_hostname,self.port))
        logger.warning("collection={}, criteria={}, projection={}".format(collection, criteria, projection))
        result = self._database[collection].find(criteria, projection)
        objects = []
        for _object in result:
            logger.warning("-"*30)
            logger.warning("MongoDB Plugin")
            logger.warning(_object)
            logger.warning(_object.get("_id"))
            _object["created"] = str(_object["_id"].generation_time)
            del _object["_id"]
            logger.warning(_object.get("created"))
            logger.warning("-"*30)
            #ObjectId.getTimestamp()
            objects.append(_object)

        return objects

    @serviceinterface
    def prune_result(self, result, extra_fields=None):
        """
        Prune the results to remove both predefined and given key-value fields.

        Removing these supplementary keys allows a direct return of the result in
        response to a user's API call.

        Predefined fields to remove are:
            * _id: used as an internal MongoDB key
            * type: used as an internal Ohouse key to define the type of object
                that an entry relates to ('key', 'member', 'slice', etc.)

        Args:
            result: list of results, as returned by 'lookup' method
            extra_fields: additional keys to remove from result

        Returns:
            list of results in dictionary format with unnecessary fields removed

        """
        fields = ['_id','type']
        if extra_fields is not None:
            fields += extra_fields
        for field in fields:
            try:
                del result[field]
            except:
                pass
        return result
