#!/bin/bash

data_lines=$(mongo --eval "db = db.getSiblingDB('mark-example'); printjson(db.DATA.count())" | tail -n1);
echo "Found $data_lines data records";

if [ $data_lines = 0 ] 
then 
  exit 1;
fi


evidences=$(mongo --eval "db = db.getSiblingDB('mark-example'); printjson(db.EVIDENCE.count())" | tail -n1);
echo "Found $evidences evidences";

if [ $evidences = 0 ] 
then 
  exit 1;
fi

exit 0;
