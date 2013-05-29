#!/usr/bin/env python

### Module imports ###
import sys
import math
import re
import numpy as np
from sklearn import linear_model, svm

###############################
##### Point-wise approach #####
###############################
def pointwise_train_features(train_data_file, train_rel_file):
  # stub, you need to implement
  X = [[0, 0], [1, 1], [2, 2]]
  y = [0, 1, 2]
  return (X, y)
 
def pointwise_test_features(test_data_file):
  # stub, you need to implement
  X = [[0.5, 0.5], [1.5, 1.5]]  
  queries = ['query1', 'query2']
  
  # index_map[query][url] = i means X[i] is the feature vector of query and url
  index_map = {'query1' : {'url1':0}, 'query2': {'url2':1}}

  return (X, queries, index_map)
 
def pointwise_learning(X, y):
  # stub, you need to implement
  model = linear_model.LinearRegression()
  return model

def pointwise_testing(X, model):
  # stub, you need to implement
  y = [0.5, 1.5]
  return y

##############################
##### Pair-wise approach #####
##############################
def pairwise_train_features(train_data_file, train_rel_file):
  X = [[0, 0], [1, 1], [2, 2]]
  y = [0, 1, 2]
  return (X, y)

def pairwise_test_features(test_data_file):
  # stub, you need to implement
  X = [[0.5, 0.5], [1.5, 1.5]]  
  queries = ['query1', 'query2']
  # index_map[query][url] = i means X[i] is the feature vector of query and url
  index_map = {'query1' : {'url1':0}, 'query2': {'url2':1}}

  return (X, queries, index_map)

def pairwise_learning(X, y):
  # stub, you need to implement
  model = svm.SVC(kernel='linear', C=1.0)
  return model

def pairwise_testing(X, model):
  # stub, you need to implement
  y = [0.5, 1.5]
  return y

####################
##### Training #####
####################
def train(train_data_file, train_rel_file, task):
  sys.stderr.write('\n## Training with feature_file = %s, rel_file = %s ... \n' % (train_data_file, train_rel_file))
  
  if task == 1:
    # Step (1): construct your feature and label arrays here
    (X, y) = pointwise_train_features(train_data_file, train_rel_file)
    
    # Step (2): implement your learning algorithm here
    model = pointwise_learning(X, y)
  elif task == 2:
    # Step (1): construct your feature and label arrays here
    (X, y) = pairwise_train_features(train_data_file, train_rel_file)
    
    # Step (2): implement your learning algorithm here
    model = pairwise_learning(X, y)
  elif task == 3: 
    # Add more features
    print >> sys.stderr, "Task 3\n"

  elif task == 4: 
    # Extra credit 
    print >> sys.stderr, "Extra Credit\n"

  else: 
    X = [[0, 0], [1, 1], [2, 2]]
    y = [0, 1, 2]
    model = linear_model.LinearRegression()
    model.fit(X, y)
  
  # some debug output
  weights = model.coef_
  print >> sys.stderr, "Weights:", str(weights)

  return model 

###################
##### Testing #####
###################
def test(test_data_file, model, task):
  sys.stderr.write('\n## Testing with feature_file = %s ... \n' % (test_data_file))

  if task == 1:
    # Step (1): construct your test feature arrays here
    (X, queries, index_map) = pointwise_test_features(test_data_file)
    
    # Step (2): implement your prediction code here
    y = pointwise_testing(X, model)
  elif task == 2:
    # Step (1): construct your test feature arrays here
    (X, queries, index_map) = pairwise_test_features(test_data_file)
    
    # Step (2): implement your prediction code here
    y = pairwise_testing(X, model)
  elif task == 3: 
    # Add more features
    print >> sys.stderr, "Task 3\n"

  elif task == 4: 
    # Extra credit 
    print >> sys.stderr, "Extra credit\n"

  else:
    queries = ['query1', 'query2']
    index_map = {'query1' : {'url1':0}, 'query2': {'url2':1}}
    X = [[0.5, 0.5], [1.5, 1.5]]  
    y = model.predict(X)
  
  # some debug output
  for query in queries:
    for url in index_map[query]:
      print >> sys.stderr, "Query:", query, ", url:", url, ", value:", y[index_map[query][url]]

  # Step (3): output your ranking result to stdout in the format that will be scored by the ndcg.py code

if __name__ == '__main__':
  sys.stderr.write('# Input arguments: %s\n' % str(sys.argv))
  
  if len(sys.argv) != 5:
    print >> sys.stderr, "Usage:", sys.argv[0], "train_data_file train_rel_file test_data_file task"
    sys.exit(1)
  
  train_data_file = sys.argv[1]
  train_rel_file = sys.argv[2]
  test_data_file = sys.argv[3]
  task = int(sys.argv[4])
  print >> sys.stderr, "### Running task", task, "..."
 
  
  model = train(train_data_file, train_rel_file, task)
  
  test(test_data_file, model, task)
