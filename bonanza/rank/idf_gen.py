#!/bin/env python
from collections import deque
import os, glob, os.path
import math
import sys
import re

def generate_idf_file(root, out_dir, stem):
  word_dict = {}
  words = 0
  for dir in sorted(os.listdir(root)):
    print >> sys.stderr, 'processing dir: ' + dir
    dir_name = os.path.join(root, dir)
    for f in sorted(os.listdir(dir_name)):
      fullpath = os.path.join(dir_name, f)
      #print >> sys.stderr, 'processing file: ' + f
      file = open(fullpath, 'r')
      for line in file.readlines():
        tokens = line.strip().split()
        for t in tokens:
          token = stem(t)
          if token not in word_dict:
            word_dict[token] = 1
            words += 1
          else:
            word_dict[token] += 1 
    #break
    
  output_f = open(out_dir+'/.idf', 'w')
  #print "\n".join(['%s\twords: %d\t v: %d\t 1: %d\t 2: %d' % (k,words, v, math.log10(float(words)/float(v)), (math.log10(float(words)) - math.log10(float(v)))) for (k,v) in word_dict.iteritems()])
#  print "words = ", words
  print >> output_f, str(words)
  print >> output_f, '\n'.join(['%s\t%d' % (k,(math.log10(float(words)) - math.log10(float(v)))) for (k,v) in word_dict.iteritems()])
  output_f.close()

def read_idf_file(root):
  input_f = open(os.path.join(root, '.idf'), 'r')
  first = True
  word_dict = {}
  for line in input_f.readlines():
    tokens = line.strip().split()
#    print tokens
    if len(tokens) != 2 and not first:
      raise "BIG PROBLEM"
    if first:
      first = False
      words = float(tokens[0])
    else:
      word_dict[tokens[0]] = float(tokens[1])
      
#  print '\n'.join(['%s\t%d' % (k,v) for (k,v) in word_dict.iteritems()])
 # print words
  return words, word_dict
if __name__ == '__main__':
  if len(sys.argv) != 3:
    print >> sys.stderr, 'usage: python data_dir output_dir' 
    os._exit(-1)

  root = sys.argv[1]
  out_dir = sys.argv[2]
  if not os.path.exists(out_dir):
    os.makedirs(out_dir)
  import snowball
  stemmer = snowball.EnglishStemmer()
  generate_idf_file(root, out_dir, stemmer.stem)
  read_idf_file('./')