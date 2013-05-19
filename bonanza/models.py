  
import sys
import os.path
import math
import cPickle
import json
import marshal
from glob import iglob
from collections import defaultdict

def compute_log10(cmd, first, second):
  if cmd == 0:
    return math.log10(first) - math.log10(second)
  elif cmd == 1:
    return math.log10(first) + math.log10(second)
  elif cmd == 2:
    return math.log10(first)
  
def log(cmd, first, second):
  if cmd == 0:
    return (first)/(second)
  elif cmd == 1:
    return (first) + (second)
  elif cmd == 2:
    return first
  
class BigramModel(object):
  def __init__(self, l = 0.2, json = False, log = compute_log10):
    self.l = l
    self.json = json
    self.words = defaultdict(lambda: 0)
    self.bigrams = defaultdict(lambda: 0)
    self.N = 0
    self.log = log

  def get_bigram(self, word, word1):
    if self.json:
      return str((word, word1))
    else:
      return str((word, word1))
  
  def build_model(self, training_corpus_loc):
    for block_fname in iglob( os.path.join( training_corpus_loc, '*.txt' ) ):
      #print >> sys.stderr, 'processing dir: ' + block_fname
      with open( block_fname ) as f:
        num_lines = 0
        last = None
        for line in f:
          line = line.rstrip()
          words_in_line = line.split(' ')
          self.N += len(words_in_line)
          for word in words_in_line:
            self.words[word] += 2
            if last != None:
              bigram = (last, word)
              self.bigrams[self.get_bigram(last, word)] += 2
            last = word
          num_lines += 1
        #print >> sys.stderr, 'Number of lines in ' + block_fname + ' = ' + str(num_lines)
      #print >> sys.stderr, 'Number of unique words =', len(self.words), '; the number of bigrams =', len(self.bigrams), '; the total word coun =', self.N
    return self.words, self.bigrams, self.N
  
  def __calculate_bigram_prob__(self, word_freq, bigram_freq):
#    print >> sys.stderr,
    word_prob = self.log(0, (word_freq+1.1), (self.N+1.1))
    bigram_prob = self.log(0, (bigram_freq+1.1), (word_freq+1.1))
    return self.l*word_prob + self.l*bigram_prob

  def calculate_prob(self, word):
    word_freq = self.words[word]
    #print >> sys.stderr, "N = ", self.N, "Word = ", word, ":", word_freq
    return self.log(0, (word_freq+1.1), (self.N+1.1))

  def calculate_bigram_prob(self, word, word1):
    return self.__calculate_bigram_prob__(self.words[word], self.bigrams[self.get_bigram(word, word1)])
  
  def serialize(self, prefix):
    word_dict = os.path.join(prefix, '.word-dict')
    self.words['thewordcountisequalto'] = self.N
    with open(word_dict, 'wb') as f:
      if self.json:
        json.dump(self.words, f)
      else:
        marshal.dump(dict(self.words), f)
        
    bigram_dict = os.path.join(prefix, '.bigram-dict')
    with open(bigram_dict, 'wb') as f:
      if self.json:
        json.dump(self.bigrams, f)
      else:
        marshal.dump(dict(self.bigrams), f)
        
  def deserialize(self, prefix):
    word_dict = os.path.join(prefix, '.word-dict')
    with open(word_dict, 'rb') as f:
      if self.json:
        self.words.update(json.load(f))
      else:
        self.words.update(marshal.load(f))
    self.N = self.words['thewordcountisequalto']
    
    bigram_dict = os.path.join(prefix, '.bigram-dict')
    with open(bigram_dict, 'rb') as f:
      if self.json:
        self.bigrams.update(json.load(f))
      else:
        self.bigrams.update(marshal.load(f))

class UniformCostTables(object):
  def __init__(self, edit_cost, log = compute_log10):
    self.edit_cost = edit_cost
    self.log = log
  def cost(self, typo, correction, distance):
    return self.log(2, self.edit_cost*(distance+1.1), None)
  
class ConfusionTables(object):
  def __init__(self, edit1s_loc, log = compute_log10, json=False):
    self.edit1s_loc = edit1s_loc
    self.log = log
    self.json = json
    self.edit1s = None
    self.chars = defaultdict(lambda: 0)
    self.delete = defaultdict(lambda: 0)
    self.insert = defaultdict(lambda: 0)
    self.replace = defaultdict(lambda: 0)
    self.reverse = defaultdict(lambda: 0)

  def __read_edit1s__(self):
    """
    Returns the edit1s data
    It's a list of tuples, structured as [ .. , (misspelled query, correct query), .. ]
    """
    self.edit1s = []
    with open(self.edit1s_loc) as f:
      # the .rstrip() is needed to remove the \n that is stupidly included in the line
      self.edit1s = [ line.rstrip().split('\t') for line in f if line.rstrip() ]
  
  def generate_tables(self):
    self.__read_edit1s__()
    for bad, good in self.edit1s:      
      self.count_chars(good)
      self.detect_edit1_type(bad, good, self.count_insert, self.count_delete, self.count_reverse, self.count_replace)
  
  def detect_edit2_type(self, bad, good, insert_fn, delete_fn, reverse_fn, replace_fn):
      if len(good) < len(bad):
        #assert (len(good)+1) == len(bad)
        for i in range(len(bad)):
          if i < len(good) and bad[i] == good[i]:
            continue
          else:
            if i == 0:
              return insert_fn("", bad[i])
            else:
              return insert_fn(good[i-1], bad[i])
      elif len(good) > len(bad):
        #assert len(good) == (len(bad)+1)
        for i in range(len(good)):
          if i < len(bad) and bad[i] == good[i]:
            continue
          else:
            if i == 0:
              return delete_fn("", good[i])
            else:
              return delete_fn(good[i-1], good[i])
      else:
        #assert len(good) == len(bad)
        for i in range(len(bad)):
          if bad[i] == good[i]:
            continue
          else:
            if i+1 < len(bad) and good[i+1] == bad[i]:
              return reverse_fn(good[i], good[i+1])
            else:
              return replace_fn(bad[i], good[i])
      #print "no correction", good, ":", bad
      return 1.1
  
  def detect_edit1_type(self, bad, good, insert_fn, delete_fn, reverse_fn, replace_fn):
      if len(good) < len(bad):
        assert (len(good)+1) == len(bad)
        for i in range(len(bad)):
          if i < len(good) and bad[i] == good[i]:
            continue
          else:
            if i == 0:
              return insert_fn("", bad[i])
            else:
              return insert_fn(good[i-1], bad[i])
      elif len(good) > len(bad):
        assert len(good) == (len(bad)+1)
        for i in range(len(good)):
          if i < len(bad) and bad[i] == good[i]:
            continue
          else:
            if i == 0:
              return delete_fn("", good[i])
            else:
              return delete_fn(good[i-1], good[i])
      else:
        assert len(good) == len(bad)
        for i in range(len(bad)):
          if bad[i] == good[i]:
            continue
          else:
            if i+1 < len(bad) and good[i+1] == bad[i]:
              return reverse_fn(good[i], good[i+1])
            else:
              return replace_fn(bad[i], good[i])
      #print "no correction", good, ":", bad
      
  def count_chars(self, query):
    last = None
    for char in query:
      self.chars[char] += 1
      if not last == None:
        self.chars[(last, char)] += 1  
      last = char
      
  def count_delete(self, cbefore, ccurrent):
    self.delete[(cbefore, ccurrent)] += 1
    
  def count_insert(self, cbefore, tcurrent):
    self.insert[(cbefore, tcurrent)] += 1

  def count_reverse(self, ccurrent, cnext):
    self.reverse[(ccurrent, cnext)] += 1
    
  def count_replace(self, typo, correction):
    self.replace[(typo, correction)] += 1
        
  def compute_prob(self, x, N):
    #print >> sys.stderr, "Value of x, N", x, N
    return self.log(0, (x+1.1), (N+1.1))
    
  def cost(self, typo, correction, distance):
    c = " ".join(correction)
    cost = self.detect_edit2_type(typo, c, self.delete_cost, self.insert_cost, self.reverse_cost, self.replace_cost) * 1.1
    return cost
  
  def delete_cost(self, cbefore, ccurrent):
    edit = (cbefore, ccurrent)
    return self.compute_prob(self.delete[edit], self.chars[edit])
      
  def insert_cost(self, cbefore, tcurrent):
    edit = (cbefore, tcurrent)
    return self.compute_prob(self.insert[edit], self.chars[cbefore])

  def reverse_cost(self, ccurrent, nextc):
    edit = (ccurrent, nextc)
    return self.compute_prob(self.reverse[edit], self.chars[edit])
     
  def replace_cost(self, typeo, correction):
    edit = (typeo, correction)
    return self.compute_prob(self.replace[edit], self.chars[correction])
  
  def __serialize__(self, prefix, filename, obj):  
    file_loc = os.path.join(prefix, filename)
    with open(file_loc, 'wb') as f:
      if self.json:
        json.dump(obj, f)
      else:
        try:
          marshal.dump(dict(obj), f)
        except ValueError, e:
          print "DICTs == "
          print obj
          print 80*'-'
          print dict(obj)
          print 80*'-'
          raise e
          
  def serialize(self, prefix):
    self.__serialize__(prefix, '.chars-matrix', self.chars)
    self.__serialize__(prefix, '.delete-matrix', self.delete)
    self.__serialize__(prefix, '.insert-matrix', self.insert)
    self.__serialize__(prefix, '.reverse-matrix', self.reverse)
    self.__serialize__(prefix, '.replace-matrix', self.replace)
        
  def __deserialize__(self, prefix, filename, obj):
    file_loc = os.path.join(prefix, filename)
    with open(file_loc, 'rb') as f:
      if self.json:
        obj.update(json.load(f))
      else:
        obj.update(marshal.load(f))
        
  def deserialize(self, prefix):
    self.__deserialize__(prefix, '.chars-matrix', self.chars)
    self.__deserialize__(prefix, '.delete-matrix', self.delete)
    self.__deserialize__(prefix, '.insert-matrix', self.insert)
    self.__deserialize__(prefix, '.reverse-matrix', self.reverse)
    self.__deserialize__(prefix, '.replace-matrix', self.replace)
          
          
def test_serialization_methods():
    obj = BigramModel(json=True)
    words, bigrams, N = obj.build_model(sys.argv[1])
    obj.serialize('./')
    obj1 = BigramModel(json=True)
    obj1.deserialize('./')
    assert obj != obj1
    assert obj.words == obj1.words
    assert obj.bigrams == obj1.bigrams
    assert obj.N == obj1.N
    
    obj2 = BigramModel(json=False)
    words1, bigrams1, N1 = obj2.build_model(sys.argv[1])
    obj2.serialize('./')
    obj3 = BigramModel(json=False)
    obj3.deserialize('./')
    assert obj2 != obj3
    assert obj2.words == obj3.words
    assert obj2.bigrams == obj3.bigrams
    assert obj2.N == obj3.N
    
    try:
      test_dict(obj.words, obj2.words, "OBJ-words-json", "OBJ2-words-marshall")
      test_dict(obj.bigrams, obj2.bigrams,"OBJ-bigrams-json", "OBJ2-bigrams-marshall")
      assert obj.N == obj2.N
    except AssertionError, e:
      print "YOURE FUCKED", obj.N, ":", obj2.N
      raise e
    try:
      test_dict(obj1.words, obj3.words, "OBJ1-words-json", "OBJ3-words-marshall")
      test_dict(obj1.bigrams, obj3.bigrams,"OBJ1-bigrams-json", "OBJ3-bigrams-marshall")
      assert obj1.N == obj3.N
    except AssertionError, e:
      print "Serialized dicts don't match ", obj1.N, ":", obj3.N
      raise e
      

def test_dict(dict1, dict2, name1, name2):
  try:
    assert dict1 == dict2
  except AssertionError, e:
    if len(dict1.keys()) >= len(dict2.keys()):
      for i in dict1.keys():
        if not dict2.has_key(i):
          print name1, " has key == ", i, " But ", name2, " does not "
          break
        if not dict1[i] == dict2[i]:
          print "Value for key ", i, "differs; ", name1, " = ", dict1[i], ", ", name2 , " = ", dict2[i]
          break
    else:
      print "keys in ", name2, "are not present ", name1
      for i in dict2.keys():
        if not dict1.has_key(i):
          print name1, " has key == ", i, " But ", name2, " does not "
          break
        if not dict1[i] == dict2[i]:
          print "Value for key ", i, "differs; ", name1, " = ", dict1[i], ", ", name2 , " = ", dict2[i]
          break
    raise e
  
def test_bigram_serialization(use_json):
    obj = BigramModel(json = use_json)
    words, bigrams, N = obj.build_model(sys.argv[1])
    obj.serialize('./')
    obj1 = BigramModel(json= use_json)
    obj1.deserialize('./')
    assert obj != obj1
    assert obj.words == obj1.words
    assert obj.bigrams == obj1.bigrams  #code
    assert obj.N == obj1.N
    
def test_confusion_table_serialization(use_json):
    obj2 = ConfusionTables(sys.argv[2], json=use_json)
    obj2.generate_tables()
    obj2.serialize('./')
    obj3 = ConfusionTables(sys.argv[2], json=use_json)
    obj3.deserialize('./')
    assert obj2 != obj3
    assert obj2.chars == obj3.chars
    assert obj2.delete == obj3.delete
    assert obj2.insert == obj3.insert
    assert obj2.reverse == obj3.reverse
    assert obj2.replace == obj3.replace
    #obj.dump(display = True)
  
def generate_models(use_json):
    if len(sys.argv) == 4:
      corpus = sys.argv[2]
      edits = sys.argv[3]
    elif len(sys.argv) == 3:
      corpus = sys.argv[1]
      edits = sys.argv[2]
    obj = BigramModel(json = use_json)
    words, bigrams, N = obj.build_model(corpus)
    obj.serialize('./')
    obj2 = ConfusionTables(edits)
    obj2.generate_tables()
    obj2.serialize('./')
      
if __name__ == '__main__':
  generate_models(True)
  #test_serialization_methods()
  #test_bigram_serialization(True)
  #test_confusion_table_serialization(False)
  