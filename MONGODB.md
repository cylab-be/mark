# Setting up MongoDB

The MARK framework can handle large quantities of data fed into it by using MONGODB to store the data and retrieve it when analysis is needed. For that reason certain precautions need to be taken when setting up the database so as not to create a bottleneck when these large quantities of data are written or read from the database.

## Steps for setting up the MongoDB

### Installing the MongoDB

This can be done via the apt-get command in Ubuntu or other available ways such as downloading the zip file from the official website and running the installation.

### Creating the MARK database

Normally the MARK framework will create by itself the MARK database with the DATA and EVIDENCE collections, but for more indepth configuration the database can be created beforehand.

### Setting up Indexes

To speed up the retrieval of data from the database extra Indexes can be defined in the db.DATA and db.EVIDENCE so the fetch query can limit its search to the part of the data that is of relevance to the query. To do that once in the mongo database the command:


```
 db.collection.createIndex(keys, options)
```

The available Index "_id" is already present, but to further speed up the search such indexes as "CLIENT", "SERVER", "TIMESTAMP" or "LABEL" can be added.

### Sharding the Database

Indexes optimise the fetching of data, but to further enchance the writing of data to the database we can set up multiple mongo shards on the server so I/O requests from the framework to the database can happen in parallel.

//TO BE ADDED
