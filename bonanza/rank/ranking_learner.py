#!/usr/bin/env python

import sys
import math
import re
import numpy as np
from sklearn import linear_model, svm
from ranking import RankingBuilder
from optparse import OptionParser
from sklearn import preprocessing
from os.path import join, abspath
from ranking import *
from itertools import combinations

class LearnToRank(object):
  def __init__(self, training_data, relevance, test_data):
    self.training_data = training_data
    self.relevance = relevance
    self.test_data = test_data
    self.model = linear_model.LinearRegression()
    self.params = VectorParameters(1, 1, 1, 1, 1, 1.1,  False, 1, 1, 1, 1) #create_default_params() #create_default_params(scoring='cosine') #
    self.params.tf_computer = self.params.linear_tf #self.params.log10_tf# self.params.linear_tf #self.params.log10_tf ##self.params.linear_tf #
    self.params.norm = True
    self.params.stem = self.params.no_stemmer()  
  def training_prep(self):
    self.builder = BM25FRankingBuilder(self.params)
    self.builder.extractFeatures(self.training_data)
    f = open(self.relevance, 'r')
    for line in f:
      key = line.split(':', 1)[0].strip()
      value = line.split(':', 1)[-1].strip()
      if key == 'query':
        query_str = value
        continue
      else:
        assert key == 'url'
        url_str = value.split(' ', 1)[0].strip()
        rel = float(value.split(' ', 1)[-1].strip())
          
      query = self.builder.queries[query_str]
      found = False
      for url in query.urls:
        if url_str == url.url:
          url.rel = rel
          found = True
      if not found:
        s = "Query, URL "+query_str+":"+url_str+" not found "
        raise s
  def make_vectors(self):
    self.scores = []
    self.classes = []
    for query, model in self.builder.queries.items():
      self.scores = model.tf_idf_scores(self.scores)
      self.classes.extend(model.rels())      
    self.scores = self.preprocess(self.scores)
      
  def test(self):
    self.builder = BM25FRankingBuilder(self.params)
    self.builder.extractFeatures(self.test_data)
    for query, query_model in self.builder.queries.items():
      scores = query_model.tf_idf_scores([])
      scores = self.preprocess(scores)
      labels = self.predict(scores)
      assert len(query_model.urls) == len(labels)
      query_model.ranking = []
      for i in range(len(labels)):
        heapq.heappush(query_model.ranking, (-1*labels[i], query_model.urls[i].url))
    self.query_models = self.builder.queries
  def predict(self, scores):
    return self.model.predict(scores)
  def preprocess(self, scores):
    return preprocessing.scale(scores)  
      
class PairwiseSupportVectorMachine(LearnToRank):
  def __init__(self, training_data, relevance, test_data):
    super(PairwiseSupportVectorMachine, self).__init__(training_data, relevance, test_data)
    self.model = svm.SVC(kernel='linear', C=1.0)
  def train(self):
    self.training_prep()
    self.make_vectors()
    print self.scores[0], self.scores[1]
    assert len(self.scores) == len(self.classes)
    scaled = self.preprocess(self.scores)
    print scaled[0], scaled[1]
    indexes = tuple(combinations([i for i in range(len(scaled))], 2))
    print "indexes ", len(indexes), indexes[0:100]
    classes1 = []
    print 'after combinations'
    diffed_scaled_scores = []
    for first, second in indexes:
      if self.classes[first] > self.classes[second]:
        classes1.append(1)
      else:
        classes1.append(-1)
      assert len(scaled[first]) == len(scaled[second])
      diffed_scaled_scores.append(scaled[first] - scaled[second])
    self.scores = diffed_scaled_scores
    self.classes = classes1
    assert len(diffed_scaled_scores) == len(classes1)
    print 'before fitting... '
    self.model.fit(self.scores, self.classes)
    print 'after fitting... '    
  def difference(self, v, v1):
    assert len(v) == len(v1)
    ret = []
    for i in range(len(v)):
      ret.append((v[i] - v1[i]))
    return ret
  def test(self):
    print 'calling test'
    super(PairwiseSupportVectorMachine, self).test()
    print 'test complete...'
  def predict(self, scores):
    import numpy as np
#        weights = model.coef_  
    weights = self.model.coef_ / np.linalg.norm(self.model.coef_)
    print "about to test... weights.. ", weights
    ret = [np.dot(score, weights) for score in scores]
    print ' test complete ...'
    return ret
    
class PointwiseLinearRegression(LearnToRank):
  def __init__(self, training_data, relevance, test_data):
    super(PointwiseLinearRegression, self).__init__(training_data, relevance, test_data)
  def train(self):
    self.training_prep()
    self.make_vectors()
    self.model.fit(self.scores, self.classes)
    print " coefficients ", self.model.coef_
    #self.model.coef_[4] += .05
    
    
class RankingLearner(object):
  def __init__(self, classifier, output):
    self.classifier = classifier
    self.output = output
    self.rankings = None
  def rank(self):
    self.classifier.train()
    self.classifier.test()
    for query, query_model in self.classifier.query_models.items():
      print >> self.output, "query: "+query
      while len(query_model.ranking) > 0:
        print >> self.output, "  url: " + heapq.heappop(query_model.ranking)[1]

if __name__ == '__main__':
  sys.stderr.write('# Input arguments: %s\n' % str(sys.argv))
  args = None
  options = None
  parser = OptionParser()
  training_data_help = 'training data'
  relevance_help = 'relevance data'
  test_data_help = 'test data'
  output_help = 'path to the output file containing the rankings'
  classifier_help = 'indicates which classifier to use; value of 1 = pointwise Linear Regression; value of 2 = pairwise Support Vector Machine (SVM); and value 3 = SVM with addition, experimental features'
  parser.add_option("-t", "--training-data", default='', type=str, dest="training_data", metavar="TRAINING-DATA", help=training_data_help)
  parser.add_option("-r", "--relevance", default='', type=str, dest="relevance_data", metavar="RELEVENCE", help=relevance_help)
  parser.add_option("-e", "--test-data", default='', type=str, dest="test_data", metavar="TEST-DATA", help=test_data_help)
  parser.add_option("-c", "--classifier", default=2, type=int, dest="classifier",  metavar="CLASSIFIER", help=classifier_help)
  parser.add_option("-o", "--output", default='', type=str, dest="output", metavar="OUTPUT", help=output_help)
  (options, args) = parser.parse_args()
  if options.training_data == '' or options.relevance_data == '' or options.test_data == '':
    parser.print_help()
    sys.exit(1)
    
  if options.classifier == 1:
    classifier = PointwiseLinearRegression(options.training_data, options.relevance_data, options.test_data)
  elif options.classifier == 2:
    classifier = PairwiseSupportVectorMachine(options.training_data, options.relevance_data, options.test_data)
  elif options.classifier == 3: 
    print >> sys.stderr, "Task 3\n"
    classifier = classifier_factory_method(PairwiseSupportVectorMachine.__init__, options)
  elif options.classifier == 4: 
    print >> sys.stderr, "Extra credit\n"
    classifier = classifier_factory_method(PairwiseSupportVectorMachine.__init__, options)
  else:
    raise "ERROR"

  if options.output == '':
    output = sys.stdout
  else:
    output = open(options.output, 'w')
    
  learner = RankingLearner(classifier, output)
  learner.rank()
