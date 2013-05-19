import sys
import models
import math

class Corrector(object):
  def __init__(self, model, queries, noise_computer, mu=1):
    self.model = model
    self.word_to_id = {}
    self.id_to_word = {}
    self.queries_loc = queries #'data/queries.txt'
    self.gold_loc = 'data/gold.txt'
    self.google_loc = 'data/google.txt'
    self.alphabet = "abcdefghijklmnopqrstuvwxyz0123546789&$+_'"
    self.queries = []
    self.gold = []
    self.google = []
    self.noise_computer = noise_computer
#    self.edit_cost = edit_cost
    self.mu = mu
    word_id = 0
    for word in self.model.words:
      self.word_to_id[word] = word_id
      self.id_to_word[word_id] = word
      word_id += 1
    
  def read_query_data(self):
    """
    all three files match with corresponding queries on each line
    NOTE: modify the signature of this method to match your program's needs
    """
    with open(self.queries_loc) as f:
      for line in f:
        self.queries.append(line.rstrip())
    with open(self.gold_loc) as f:
      for line in f:
        self.gold.append(line.rstrip())
    with open(self.google_loc) as f:
      for line in f:
        self.google.append(line.rstrip())
    assert( len(self.queries) == len(self.gold) and len(self.gold) == len(self.google) )
    return (self.queries, self.gold, self.google)
  
  def compare(self):
    total = 0
    incorrect = 0
    context = 0
    for i in range(len(self.queries)):
      total += 1
      if self.queries[i] != self.gold[i]:
        words = self.queries[i].split(" ")
        done = False
        incorrect += 1
        for j in words:
          if self.model.words[j] == 0:
            done = True
            break
        if not done:
          context += 1
          print >> sys.stderr, self.queries[i], "::::::", self.gold[i]
          for j in words:
            print >> sys.stderr, j, self.model.words[j]
          words1 = self.gold[i].split(' ')
          for j in words1:
            print >> sys.stderr, j, self.model.words[j]
          print >> sys.stderr, 80*'-'  
    print >> sys.stderr, total, incorrect, context
    print >> sys.stderr, float(incorrect)/float(total), float(context)/float(total), float(context)/float(incorrect)
    
  def max(self, candidates):
    the_max = 0
    ret = None
    original = None
    for query, distance in candidates:
      if original == None:
        original = query
        continue
      
      if distance > 0:
        #assert distance < 3
        prior = self.noise_computer.cost(original, query, distance)
        #print "Cost == ", prior
      else:
        prior = 1
        
      contextual_factor = self.calculate_contextual_factor(query)
      #assert contextual_factor > 0      
      cond = math.pow(contextual_factor, self.mu)
      #cond = contextual_factor
      new_max = cond * prior
      #print "cond ", cond, "prior ", prior, " new max ", new_max, " max ", the_max
      if new_max > the_max or the_max == 0:
        the_max = new_max
        ret = query
    return ret
      
  def calculate_contextual_factor(self, query):
    last_word = None
    factor = 1.0
    for word in query:
      if last_word == None:
        last_word = word
        w1_prob = self.model.calculate_prob(word)
        #print >> sys.stderr, "Probability of ", word, " = ", w1_prob
        factor *= w1_prob
      else:
        w2_given_w1_prob = self.model.calculate_bigram_prob(last_word, word)
        #print >> sys.stderr, "Probability of ", last_word, ":", word, " = ", w2_given_w1_prob
        factor *= w2_given_w1_prob
        last_word = word
    return factor
  
  
  def correct(self, test_fn=None):
    count = 0
    #print >> sys.stderr, " Queries = ", len(self.queries)
    for query in self.queries:
      #print >> sys.stderr, "Generating candidates for [", query, "] count = ", count
      candidates = self.generate_candidates1(query)
      most_likely = self.max(candidates)
      #print >> sys.stderr, 'Most likely', most_likely
      #print >> sys.stderr, 80*'-'
      print " ".join(most_likely)
      if test_fn != None:
        test_fn(most_likely, candidates, count)
      count += 1
          
  def known_splits(self, word):
    #print >> sys.stderr, "know splits RUNNING.... "
    splits = [(word[:i], word[i:]) for i in range(len(word)) if i > 0]
    l = []
    for a, b in splits:
      if self.word_to_id.has_key(a) and self.word_to_id.has_key(b):
        l.append((self.word_to_id[a], self.word_to_id[b]))
    dedups = set(l)
    ret = [[self.id_to_word[w]] + [self.id_to_word[w1]] for w, w1 in dedups]
    #print >> sys.stderr, "know splits COMPLETE "
    return ret
  
  def edits1(self, word):
    splits = [(word[:i], word[i:]) for i in range(len(word) + 1)]
    deletes = [self.word_to_id[a + b[1:]] for a, b in splits if self.word_to_id.has_key(a + b[1:])]
    transposes = [self.word_to_id[a + b[1] + b[0] + b[2:]] for a, b in splits if len(b)>1 and self.word_to_id.has_key(a + b[1] + b[0] + b[2:])]
    replaces = [self.word_to_id[a + c + b[1:]] for a, b in splits for c in self.alphabet if b and self.word_to_id.has_key(a + c + b[1:])]
    inserts = [self.word_to_id[a + c + b] for a, b in splits for c in self.alphabet if self.word_to_id.has_key(a + c + b)]
    ret = set(deletes + transposes + replaces + inserts)
    return self.to_words(ret)

  def to_words(self, ids):
    return [self.id_to_word[wid] for wid in ids]
  
  def known_edits2(self, word):
    #print >> sys.stderr, "known edit 2 RUNNING................... "
    words = [e2 for e1 in self.edits1(word) for e2 in self.edits1(e1)]
    #print >> sys.stderr, "known edit 2 COMPLETE"
    if words:
      return words, 2
      
  def known_edits1(self, word):
    #print >> sys.stderr, "edit 1 RUNNING... "
    words = [w for w in self.edits1(word)]
    #print >> sys.stderr, "known edit 1 COMPLETE"
    if words:
      return words, 1
  
  def known(self, word):
    #print >> sys.stderr, "edit 0 "
    if word in self.model.words.keys():
      return [word], 0
    
    
  def merge(self, queries, new_words, distance):
    #print >> sys.stderr, queries, new_words
    if len(queries) == 0:
      for word in new_words:
        queries.append(([word], distance))
        return queries
    else:
      new_queries = []
      for query, current_distance in queries:
        for word in new_words:
          #assert current_distance+distance < 3
          new_queries.append((query + [word], current_distance+distance))
      return new_queries
#      return [query + [word] for query in queries for word in new_words]

  def merge_splits(self, queries, split_words):
    if len(queries) == 0:
      for split in split_words:
        queries.append((split, 1))
      return queries
    else:
      new_queries = []
      for query, distance in queries:
        for split in split_words:
          #assert distance+1 < 3
          new_queries.append((query + split, distance+1))
      return new_queries

  def generate_candidates1(self, query):
    qwords = query.split(' ')
    new_queries = []
    edits = 0
    for word in qwords:
      if (not self.model.words.has_key(word)) and edits < 1:
        new_words, state = self.known_edits1(word) or self.known_edits2(word) or ([word], 3)
        #print >> sys.stderr, "word, state = ", word, state
        if state == 1:
          edits += 1
        elif state == 2:
          edits += 2
        elif state == 3:
          edits += 1
          new_queries = self.merge_splits(new_queries, self.known_splits(word))
          continue
        new_queries = self.merge(new_queries, new_words, edits)          
      elif (not self.model.words.has_key(word)) and edits < 2:
        new_words, state = self.known(word) or self.known_edits1(word) or ([word], 3)
        #print >> sys.stderr, "word, state = ", word, state
        if state == 1:
          edits += 1
        elif state == 3:
          edits += 1
          new_queries = self.merge_splits(new_queries, self.known_splits(word))
          continue        
        new_queries = self.merge(new_queries, new_words, 1)
      elif self.model.words.has_key(word) and self.model.words[word] < 5 and edits < 2:
        if edits < 2:
          new_words, state = self.known_edits1(word) or self.known_edits2(word) or ([word], 3)
        else:
          new_words, state = self.known(word) or self.known_edits1(word) or ([word], 3)
        if state == 1:
          edits += 1
        elif state == 2:
          edits += 2
        elif state == 3:
          edits += 1
          new_queries = self.merge_splits(new_queries, self.known_splits(word))
          continue
        new_queries = self.merge(new_queries, new_words, state)                    
      else:
        #print >> sys.stderr, "word is accepted ", word
        new_queries = self.merge(new_queries, [word], 0)
    #print >> sys.stderr, "NEW QUERIES == ", new_queries
    return [(query, 0)] + new_queries
        
class QuickTest(object):
  def __init__(self, gold):
    self.gold = gold
    self.total = len(self.gold)
    self.correct = 0
    self.called = 0
  def check(self, most_likely, candidates, index):
    query = self.gold[index]
    words = query.split(' ')
    if words != most_likely:
      pass
      #print >> sys.stderr, "Gold = ", words, " most likely = ", most_likely 
    else:
      self.correct += 1
    self.called += 1
  def accuracy(self):
    print >> sys.stderr, "Total = ",self.total, " correct = ", self.correct, " called ", self.called
    return float(self.correct)/float(self.total)
    
if __name__ == '__main__':
  if sys.argv < 3:
    print >> sys.stderr, "Invalid arguments"
    sys.exit(1)
  command = sys.argv[1]
  directory = sys.argv[2]
  model = models.BigramModel(l = .2, json = True, log=models.compute_log10)
  model.deserialize('./')
  if command == "uniform":
    noise_computer = models.UniformCostTables(.05, log=models.compute_log10)
  else:
    noise_computer = models.ConfusionTables(None, log=models.compute_log10)
    noise_computer.deserialize('./')
    
  c = Corrector(model, directory, noise_computer, mu=2.0)
  c.read_query_data()
  qt = QuickTest(c.gold)
  c.correct(test_fn = qt.check)
  print >> sys.stderr, qt.accuracy()
  #c.compare()
  #print(sys.argv)
