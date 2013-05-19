#!/bin/env python
from collections import deque
import os, glob, os.path
import math
import sys
import snowball
import re

from optparse import OptionParser
from os.path import join, abspath


def generate_idf_file(root, out_dir, stem):
  word_dict = {}
  words = 0
  for dir in sorted(os.listdir(root)):
    print >> sys.stderr, 'processing dir: ' + dir
    dir_name = os.path.join(root, dir)
    for f in sorted(os.listdir(dir_name)):
      fullpath = os.path.join(dir_name, f)
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
    
  output_f = open(out_dir+'/.idf', 'w')
  print >> output_f, str(words)
  print >> output_f, '\n'.join(['%s\t%d' % (k,(math.log10(float(words)) - math.log10(float(v)))) for (k,v) in word_dict.iteritems()])
  output_f.close()

def no_stemmer(word):
  return word

def read_idf_file(root):
  input_f = open(os.path.join(root, '.idf'), 'r')
  first = True
  word_dict = {}
  for line in input_f.readlines():
    tokens = line.strip().split()
    if len(tokens) != 2 and not first:
      raise "Invalid token length"
    if first:
      first = False
      words = float(tokens[0])
    else:
      word_dict[tokens[0]] = float(tokens[1])
      
  return words, word_dict

if __name__ == '__main__':
  
  args = None
  options = None
  parser = OptionParser(version="%prog 1.3.37")
  corpus_help = 'path to the corpus'
  output_help = 'path to the output file'
  snowball_stemmer_help = 'generates the IDF values using the Snowball (porter2) stemmer'
  parser.add_option("-c", "--corpus", default='', type=str, dest="corpus", metavar="CORPUS", help=corpus_help)
  parser.add_option("-o", "--output", default='', type=str, dest="output", metavar="OUTPUT", help=output_help)
  parser.add_option("-s", "--snowball", action="store_true", dest="snowball", default=False, metavar="SNOWBALL", help=snowball_help)
  
  (options, args) = parser.parse_args()
  if options.corpus == '' or options.output == '':
    parser.print_help()
    sys.exit(1)

  if not os.path.exits(options.corpus):
    print "ERROR ", options.corpus, " does not exist "
    sys.exit(1)

  root = options.corpus
  out_dir = options.output
  if not os.path.exists(out_dir):
    os.makedirs(out_dir)
  if options.snowball:
    stemmer = snowball.EnglishStemmer().stem
  else:
    stemmer = no_stemmer
  generate_idf_file(root, out_dir, stemmer)
  #read_idf_file('./')