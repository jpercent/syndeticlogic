#!/bin/bash

if [ $# -lt 2 ]
then
  echo "Usage: l2r.sh <task> <test_data_file>"
  exit
fi

task=$1
test_data_file=$2

python l2r.py queryDocTrainData.train queryDocTrainRel.train $test_data_file $task
