#!/usr/bin/env python

### Module imports ###
import sys
import math
import re
import numpy as np
from sklearn import linear_model, svm

class PointwiseLinearRegression(object):
  def __init__(self, training_data, relevance, test_data):
    self.training_data = training_data
    self.relevence = relevence
    self.test_data = test_data
    self.model = linear_model.LinearRegression()
    self.training_features = None
    self.query_doc_vectors = None
    self.class_labels = [0,1,3]
  def train(self):#,train_data_file, train_rel_file):
    X = [[0, 0], [1, 1], [2, 2]]
    y = [0, 1, 2]
    return (X, y)
 
  def extract_testing_features(self):#, test_data_file):
    X = [[0.5, 0.5], [1.5, 1.5]]  
    queries = ['query1', 'query2']
  
  # index_map[query][url] = i means X[i] is the feature vector of query and url
    index_map = {'query1' : {'url1':0}, 'query2': {'url2':1}}
    return (X, queries, index_map)
 
  def learn(self, X, y):
    self.model.fit(X,y)

  def test(X):
    return self.model.predict(X) #[0.5, 1.5]

class PairwiseSupportVectorMachine(object):
  def __init__(self):
    pass
  def pairwise_train_features(self, train_data_file, train_rel_file):
    X = [[0, 0], [1, 1], [2, 2]]
    y = [0, 1, 2]
    return (X, y)

  def pairwise_test_features(self, test_data_file):
    # stub, you need to implement
    X = [[0.5, 0.5], [1.5, 1.5]]  
    queries = ['query1', 'query2']
    # index_map[query][url] = i means X[i] is the feature vector of query and url
    index_map = {'query1' : {'url1':0}, 'query2': {'url2':1}}
    return (X, queries, index_map)

  def pairwise_learning(self, X, y):
    # stub, you need to implement
    model = svm.SVC(kernel='linear', C=1.0)
    return model

  def pairwise_testing(self, X, model):
    # stub, you need to implement
    y = [0.5, 1.5]
    return y
    
    
class RankingLearner(object):
  def __init__(self, classifier, output):
    self.classifier = classifier
    self.output = output
    self.rankings = None
  def rank(self):
    #sys.stderr.write('\n## Training with feature_file = %s, rel_file = %s ... \n' % (train_data_file, train_rel_file))
    self.classifier.train()
    self.rankings = self.classifier.test()
  def write_rankings(self):
    assert self.rankings != None
    self.output_stream.write(self.rankings)
    
  def train(self, train_data_file, train_rel_file, task):
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

def classifier_factory_method(fn, options):
  return fn(options.training_data, options.relevence_data, options.test_data)

if __name__ == '__main__':
  sys.stderr.write('# Input arguments: %s\n' % str(sys.argv))
  args = None
  options = None
  parser = OptionParser()
  training_data_help = 'training data'
  relevence_data_help = 'relevence data'
  test_data_help = 'test data'
  classifier_help = 'indicates which classifier to use; value of 1 = pointwise Linear Regression; value of 2 = pairwise Support Vector Machine (SVM); and value 3 = SVM with addition, experimental features'
  parser.add_option("-t", "--training-data", default='', type=str, dest="training_data", metavar="TRAINING-DATA", help=training_data_help)
  parser.add_option("-r", "--relevence", default='', type=str, dest="relevence_data", metavar="RELEVENCE", help=relevence_help)
  parser.add_option("-e", "--test-data", default='', type=str, dest="test_data", metavar="TEST-DATA", help=test_data_help)
  parser.add_option("-c", "--classifier", default=2, type=int, dest="classifier",  metavar="CLASSIFIER", help=classifier_help)
  (options, args) = parser.parse_args()
  if options.training_data == '' or options.relevence_data == '' or options.test_data == '':
    parser.print_help()
    sys.exit(1)
    
  if options.classifier == 1:
    classifier = classifier_factory_method(PointwiseLinearRegression.__init__, options)
  elif options.classifier == 2:
    classifier = classifier_factory_method(PairwiseSupportVectorMachine.__init__, options)
  elif options.classifier == 3: 
    # Add more features
    print >> sys.stderr, "Task 3\n"
    classifier = classifier_factory_method(PairwiseSupportVectorMachine.__init__, options)
  elif options.classifier == 4: 
    # Extra credit 
    print >> sys.stderr, "Extra credit\n"
    classifier = classifier_factory_method(PairwiseSupportVectorMachine.__init__, options)
  else:
    raise "ERROR"
    queries = ['query1', 'query2']
    index_map = {'query1' : {'url1':0}, 'query2': {'url2':1}}
    X = [[0.5, 0.5], [1.5, 1.5]]  
    y = model.predict(X)
    
  #if len(sys.argv) != 5:
  #  print >> sys.stderr, "Usage:", sys.argv[0], "train_data_file train_rel_file test_data_file task"
  #  sys.exit(1)
  
  learner = RankingLearner(classifier)
  learner.compute_rankings()
  learner.write_rankings()
  
  #train_data_file = sys.argv[1]
  #train_rel_file = sys.argv[2]
  #test_data_file = sys.argv[3]
  #task = int(sys.argv[4])
  print >> sys.stderr, "### Running task", task, "..."
  #model = train(train_data_file, train_rel_file, task)
  
  #test(test_data_file, model, task)
