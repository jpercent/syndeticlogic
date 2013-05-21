import sys
import re
import idf_gen
import heapq
import snowball
import math

from optparse import OptionParser
from os.path import join, abspath

class RankingBuilder(object):
    def __init__(self):
        self.query = None
        self.url = None
        self.key = None
        self.anchor_text = None
    def add_url(self, query, url):
        self.queries[self.query].append(url)
        self.features[self.query][url] = {}
        self.url = value
    def add_query(self, query):
        self.query = value
        self.queries[self.query] = []
        self.features[self.query] = {}
    def add_title(self, title):
        self.features[self.query][self.url][self.key] = title
    def add_header(self, header):
        curHeader = self.features[self.query][self.url].setdefault(self.key, [])
        curHeader.append(header)
        self.features[self.query][self.url][self.key] = curHeader
    def add_body_hits(self, body_hits):
        if self.key not in self.features[self.query][self.url]:
          self.features[self.query][self.url][self.key] = {}
        temp = body_hits.split(' ', 1)
        self.features[self.query][self.url][self.key][temp[0].strip()] \
                    = [int(i) for i in temp[1].strip().split()]
    def add_page_rank(self, page_rank):
        self.add_body_length(page_rank)
    def add_body_length(self, body_length):
        self.features[self.query][self.url][self.key] = int(body_length)
    def add_anchor_text(self, anchor_text):
        self.anchor_text = anchor_text
        if 'anchors' not in self.features[self.query][self.url]:
            self.features[self.query][self.url]['anchors'] = {}            
    def add_anchor_count(self, anchor_count):
        self.features[self.query][self.url]['anchors'][self.anchor_text] = int(anchor_count)
    def extractFeatures(self, featureFile):
        f = open(featureFile, 'r')
        self.queries = {}
        self.features = {}
        for line in f:
          self.key = line.split(':', 1)[0].strip()
          value = line.split(':', 1)[-1].strip()
          if(self.key == 'query'):
            self.add_query(value)
          elif(self.key == 'url'):
            self.add_url(value) 
          elif(self.key == 'title'):
            self.add_title(value)
          elif(self.key == 'header'):
            self.add_header(value)
          elif(self.key == 'body_hits'):
            self.add_body_hits(value)
          elif(self.key == 'body_length'):
            self.add_body_length(value)
          elif (self.key == 'pagerank'):
            self.add_page_rank(value)
          elif(self.key == 'anchor_text'):
            self.add_anchor_text(value)
          elif(self.key == 'stanford_anchor_count'):
            self.add_anchor_count(value)
          
        f.close()
        return (self.queries, self.features) 
            
            
class CSRankingBuilder(RankingBuilder):
    def __init__(self, params):
        super(CSRankingBuilder, self).__init__()
        self.params = params
    def add_terms(self, terms, vector, offset):
        for term in terms:
            vector.add_term(term, offset)
    def add_sentence(self, sentence, vector):
        self.add_terms(sentence.split(), vector, -1)                
    def add_query(self, query):
#        print "query ", query
        self.query = query
        qm = QueryModel(query, self.params)
        qm.compute_scores = qm.cosine_scores
        self.queries[query] = qm
    def add_url(self, url):
        self.url = UrlModel(url, self.query, self.params)
        current = self.queries[self.query]
        current.add_url(self.url)
        self.add_terms(re.sub(r'\W', ' ', url).split(), self.url.url_vec, -1)
        self.uoffset = 0
        self.toffset = 0
        self.hoffset = 0
        self.buoffset = 0
        self.aoffset = 0
    def add_title(self, title):
        self.add_sentence(title, self.url.title_vec)
    def add_header(self, header):
        self.add_sentence(header, self.url.headers_vec)
    def add_body_hits(self, body_hits):
        temp = body_hits.split(' ', 1)
        term = temp[0].strip()
        postings = temp[1].strip().split()
        for offset in postings:
            self.url.body_hits_vec.add_term(term, int(offset))
    def add_body_length(self, body_length):
        self.url.body_length = int(body_length)
    def add_anchor_text(self, anchor_text):
        self.anchor_text = anchor_text
        self.url.anchors_vec.reset_window()
    def add_anchor_count(self, anchor_count):
        for anchor in self.anchor_text.split():
            for i in range(int(anchor_count)):
                self.add_sentence(anchor, self.url.anchors_vec)
                
                
class BM25FRankingBuilder(CSRankingBuilder):
  def __init__(self, params):
    super(BM25FRankingBuilder, self).__init__(params)
  def add_query(self, query):
    self.query = query
    qm = QueryModel(query, self.params)
    qm.compute_scores = qm.bm25f_scores
    self.queries[query] = qm
  def add_page_rank(self, page_rank):
    self.url.page_rank = float(page_rank)
        
        
class QueryModel(object):
    def __init__(self, q, p):
        self.query = q
        self.params = p
        self.query_vec = QueryVector(q, p.tf_computer, p.N, p.idf_hash, p.stem)
        self.urls = []
        self.url_len = 0
        self.title_len = 0 
        self.headers_len = 0
        self.body_hits_len = 0
        self.anchors_len = 0
    def add_url(self, url):
        self.urls.append(url)
    def cosine_scores(self):
        scores = []
        for url in self.urls:
            heapq.heappush(scores, (-1*url.cosine_score(self.query_vec), url.url))
        return (self.query, scores)
    def bm25f_scores(self):
        scores = []
        for url in self.urls:
            self.add_lens(url)
        self.url_len = self.url_len+self.params.bsmooth/ float(len(self.urls))
        self.title_len = self.title_len+self.params.bsmooth/ float(len(self.urls))
        self.headers_len = self.headers_len+self.params.bsmooth/ float(len(self.urls))
        self.body_hits_len = self.body_hits_len+self.params.bsmooth/ float(len(self.urls))
        self.anchors_len = self.anchors_len+self.params.bsmooth/ float(len(self.urls))
        for url in self.urls:
           heapq.heappush(scores, (-1*url.bm25f_score(self), url.url))
        return (self.query, scores)
    def add_lens(self, url):
        self.url_len += url.url_vec.field_len
        self.title_len += url.title_vec.field_len 
        self.headers_len += url.headers_vec.field_len 
        self.body_hits_len += url.body_hits_vec.field_len 
        self.anchors_len += url.anchors_vec.field_len 
            
            
class UrlModel(object):
    def __init__(self, u, q, p):
        self.url = u
        self.query_vec = q.split()
        self.params = p
        self.url_vec = DocumentVector(p, q)
        self.title_vec = DocumentVector(p, q)
        self.headers_vec = DocumentVector(p, q)
        self.body_hits_vec = DocumentVector(p, q)
        self.anchors_vec = DocumentVector(p, q)
    def compute_boost(self):
        smallest_list = [self.url_vec.smallest_window(), self.title_vec.smallest_window(),
                         self.headers_vec.smallest_window(), self.body_hits_vec.smallest_window(),
                         self.anchors_vec.smallest_window()]
        heapq.heapify(smallest_list)
        smallest = -1
        while len(smallest_list) > 0 and smallest == -1:
            smallest = heapq.heappop(smallest_list)
        if smallest != -1:
            assert smallest >= len(self.query_vec)
            if smallest == len(self.query_vec):
                boost = self.params.boost
            else:
                boost = 1/float(smallest) * self.params.boost
        else:
            boost = 1
        return boost
    def cosine_score(self, query_vec):
        assert query_vec == query_vec
        u = [self.params.up * e for e in self.url_vec.tfnorm_value(self.body_length)]
        t = [self.params.tp * e for e in self.title_vec.tfnorm_value(self.body_length)]
        h = [self.params.hp * e for e in self.headers_vec.tfnorm_value(self.body_length)]
        bh = [self.params.bp * e for e in self.body_hits_vec.tfnorm_value(self.body_length)]
        a = [self.params.ap * e for e in self.anchors_vec.tfnorm_value(self.body_length)]
        lu = len(u)
        assert lu == len(t) and len(h) == lu and len(bh) == lu and len(a) == lu
        doc_score = [u[i]+t[i]+h[i]+bh[i]+a[i] for i in range(len(u))]
        return self.dot(doc_score, query_vec.value())
    def dot(self, v, v1):
        assert len(v) == len(v1)
        score = 0
        for i in range(len(v)):
            score += v[i]*v1[i]
        return score + self.compute_boost()
    def bm25f_score(self, average_lens):
        self.url_vec.avg_len = average_lens.url_len
        self.title_vec.avg_len = average_lens.title_len
        self.headers_vec.avg_len = average_lens.headers_len
        self.body_hits_vec.avg_len = average_lens.body_hits_len
        self.anchors_vec.avg_len = average_lens.anchors_len
        url_ftf_vec = self.url_vec.bm25f_value()
        title_ftf_vec = self.title_vec.bm25f_value()
        headers_ftf_vec = self.headers_vec.bm25f_value()
        body_hits_ftf_vec = self.body_hits_vec.bm25f_value()
        anchors_ftf_vec = self.anchors_vec.bm25f_value()
        term_scores = []
        for i in range(len(url_ftf_vec)):
            term_scores.append(self.params.Wu * url_ftf_vec[i] + self.params.Wt * title_ftf_vec[i] + self.params.Wh * headers_ftf_vec[i] + self.params.Wb * body_hits_ftf_vec[i] + self.params.Wa * anchors_ftf_vec[i])           
        score = 0
        for i in range(len(term_scores)):
            w_dt = term_scores[i]
            score += ((w_dt * self.params.idf_hash[self.query_vec[i]]) / float((self.params.K + w_dt) + self.params.V(self.page_rank)))
        return score + self.compute_boost()
            
            
class SmallestWindowComputer(object):
    def __init__(self, query):
        self.window = []
        self.query = query.split()
        self.reset()        
    def reset(self):
        self.window = {term : [] for term in self.query}
    def add_term(self, term, current_offset):
        if not term in self.query:
            return
        else:
            self.window[term].append(current_offset)
    def is_hash_filled(self, hash_values):
        filled = True
        for value in hash_values:
            if value == None:
                filled = False
                break
        return filled
    def compute_min(self, term_values, current_min):
        minv = sys.maxint
        maxv = -1
        for v in term_values:
            if minv > v:
                minv = v
            if maxv < v:
                maxv = v
        
        new_v = (maxv+1) - minv
        if new_v < current_min:
            current_min = new_v
        assert current_min >= len(self.query)
        return current_min
    def get_next(self, offset_lists):
        ret_term = None
        offset = None
        for i in range(len(offset_lists)):
            term, offsets, index = offset_lists[i]
            if index < len(offsets):
                if offset == None or offsets[index] < offset:
                    ret_term = term
                    offset = offsets[index]
                    ret_offsets = offsets
                    reset_index = index + 1
                    list_index = i
        if ret_term != None:
            offset_lists[list_index] = ret_term, ret_offsets, reset_index
        return ret_term, offset
    def create_min_search_lists(self):
        offset_lists = []
        for term, offsets in self.window.iteritems():
            if len(offsets) == 0:
                return None, None, True
            else:
                offsets.sort()
                offset_lists.append((term, offsets, 0))
        terms_map = {term : None for term in self.query }
        return terms_map, offset_lists, False
    def compute_smallest_window(self):
        current_min = sys.maxint
        terms_map, offset_lists, done = self.create_min_search_lists()
        if done:
            return -1
        filled = False
        while True:
            term, offset = self.get_next(offset_lists)
            if term == None:
                break
            if terms_map[term] == None:
                terms_map[term] = offset
                filled = self.is_hash_filled(terms_map.values())
                if filled:
                    current_min = self.compute_min(terms_map.values(), current_min)
            elif terms_map[term] != None and not filled:
                terms_map[term] = offset
            else:
                assert terms_map[term] != None and filled
                terms_map[term] = offset
                new_min = self.compute_min(terms_map.values(), current_min)
                if new_min < current_min:
                    current_min = new_min
        assert current_min >= len(self.query)
        return current_min
    def suspend(self):
        self.min = lambda : -1
        self.add_term = lambda term, offset: term
            
            
class DocumentVector(object):
    def __init__(self, params, query):
        self.tf = {}
        self.stem = params.stem
        self.B = params.B
        self.field_len = 0
        for term in query.split():
            self.tf[self.stem(unicode(term, 'utf-8'))] = 0
        self.tf_computer = params.tf_computer
        self.bsmooth = params.bsmooth
        self.query = query
        self.sw_computer = SmallestWindowComputer(query)
        self.smallest = -1
        self.offset = 0
        if not params.compute_smallest_window:
            self.sw_computer.suspend()
    def smallest_window(self):
        self.reset_window()
        return self.smallest
    def reset_window(self):        
        candidate = self.sw_computer.compute_smallest_window()
        if self.smallest == -1 or self.smallest > candidate:
            self.smallest = candidate
        self.sw_computer.reset()
        self.offset = 0
    def add_term(self, term, offset):
        term = self.stem(unicode(term, 'utf-8'))
        if term in self.tf:
            self.tf[term] += 1
        self.field_len += 1
        self.offset += 1
        if offset == -1:
            offset = self.offset
        self.sw_computer.add_term(term, offset)
    def tfnorm_value(self, body_length):
        return [self.tf_computer(self.tf[term])/float(body_length + self.bsmooth) for term in self.tf.keys()]
    def bm25f_value(self):
        return [self.tf[term] / (1 + self.B + (self.field_len/float(self.avg_len))) for term in self.tf.keys()]
            
            
class QueryVector(object):
    def __init__(self, query, tf_computer, N, idf_hash, stem):
        self.query = query
        self.tf_computer = tf_computer
        self.N = N
        self.idf_hash = idf_hash
        self.raw = {}
        self.stem = stem
        for term in query.split():
            st = self.stem(unicode(term, 'utf-8'))
            if st in self.raw:
                self.raw[st] += 1
            else:
                self.raw[st] = 1
                if not st in self.idf_hash.keys():
                    self.idf_hash[st] = math.log10(self.N) - math.log10(1.1)
    def value(self):
        return [self.tf_computer(self.raw[term])*self.idf_hash[term] for term in self.raw.keys()]
            
            
class VectorParameters(object):
    def __init__(self, up, tp, bp, hp, ap, bsmooth, compute_smallest_window, boost, B, K, lam):
        self.up = up
        self.tp = tp
        self.bp = bp
        self.hp = hp
        self.ap = ap
        self.bsmooth = float(bsmooth)
        self.Wu = up
        self.Wt = tp
        self.Wb = bp
        self.Wh = hp
        self.Wa = ap
        self.B = B
        self.K = K
        self.lam = lam
        self.N, self.idf_hash = idf_gen.read_idf_file('./')
        self.stemmer = snowball.EnglishStemmer()
        self.boost = boost
        self.compute_smallest_window = compute_smallest_window

    def logLambdaV(self, page_rank):
        return math.log10(params.lam+.1) + math.log10(page_rank+self.bsmooth)
    def logV(self, page_rank):
        return math.log10(float(page_rank+self.bsmooth)) * self.lam
    def fracV(self, page_rank):
        return page_rank / (self.lam + page_rank)
    def snowball_stemmer(self):
        return self.stemmer.stem
    def no_stemmer(self):
        return lambda x:x
    def linear_tf(self, count):
        return count
    def log10_tf(self, count):
        if count == 0:
            return 1
        else:
            return 1 + math.log10(count)

class Ranker:
    def __init__(self, help_fn, ranking, output, params):
        if ranking == 'cosine':
            self.create_builder_fn = self.create_cosine_builder
        elif ranking == 'bm25f':
            self.create_builder_fn = self.create_bm25f_builder
        else:
            print "ERROR ", ranking, " is an unsupported algorithm"
            help_fn()
            sys.exit(1)
        if output == '':
            self.output = sys.stdout
        else:
            self.output_file = output
            self.output = open(output, 'w')
        self.params = params
        self.params.tf_computer = self.params.linear_tf
        self.params.stem = self.params.no_stemmer()

    def create_bm25f_builder(self):
        self.params.V = self.params.logV
        builder = BM25FRankingBuilder(self.params)
        return builder
    def create_cosine_builder(self):
        builder = CSRankingBuilder(self.params)
        return builder
    def run(self, feature_file):
        builder = self.create_builder_fn()
        (queries, features) = builder.extractFeatures(feature_file)
        rankings = [queries[query].compute_scores() for query in queries.keys()]
        for scores in rankings:
            print >> self.output, "query: "+scores[0]
            while len(scores[1]) > 0:
                print >> self.output, "  url: " + heapq.heappop(scores[1])[1]
            
if __name__=='__main__':
    args = None
    options = None
    parser = OptionParser(version="%prog 1.3.37")
    scoring_help = 'sets the scoring algorithm that will be used; valid values are bm25f and cosine'
    results_help = 'path to the file containing the search results'
    output_help = 'path to the output file containing the rankings'
    window_help = 'turns on smallest window calculation'
    parser.add_option("-s", "--scoring", default='', type=str, dest="scoring", metavar="ALGORITM", help=scoring_help)
    parser.add_option("-r", "--results", default='', type=str, dest="results", metavar="RESULTS", help=results_help)
    parser.add_option("-o", "--output", default='', type=str, dest="output", metavar="OUTPUT", help=output_help)
    parser.add_option("-w", "--smallest-window", action="store_true", dest="window", default=False, metavar="WINDOW", help=window_help)
    (options, args) = parser.parse_args()
    if options.scoring == '' or options.results == '':
        parser.print_help()
        sys.exit(1)
        
    boost = 1
    bsmooth = 1.1
    window = False
    if options.scoring == 'cosine':
        up = 1
        tp = 511
        bp = .05
        hp = 20000
        ap = 1111111
        B = 1
        K = 1
        lam = 1
    elif options.scoring == 'bm25f':
        up = 1000
        tp = 1110
        bp = 0.05
        hp = 2000
        ap = 1111111
        B  = 1
        K = 200
        lam = 1
        
    if options.window:
        boost = 11111        
    params = VectorParameters(up, tp, bp, hp, ap, bsmooth,  options.window, boost, B, K, lam)
    ranker = Ranker(parser.print_help, options.scoring, options.output, params)        
    ranker.run(options.results)
