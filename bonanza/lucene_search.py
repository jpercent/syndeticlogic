#!/usr/bin/env python
from lucene import \
    QueryParser, IndexSearcher, StandardAnalyzer, SimpleFSDirectory, File, \
    VERSION, initVM, Version
from groupon import MongoWrapper, LuceneDoc

class LuceneSearch(object):
    def __init__(self):
        STORE_DIR = "index"
        initVM()
        print 'lucene', VERSION
        self.directory = SimpleFSDirectory(File(STORE_DIR))
        print self.directory
        self.searcher = IndexSearcher(self.directory, True)
        self.analyzer = StandardAnalyzer(Version.LUCENE_CURRENT)

    def close(self):
        self.searcher.close()
    
    def raw_search(self, query_string):
        query = QueryParser(Version.LUCENE_CURRENT, "contents",
                            self.analyzer).parse(query_string)
        scoreDocs = self.searcher.search(query, 50).scoreDocs
        print "%s total matching documents." % len(scoreDocs)
        matches = []
        for scoreDoc in scoreDocs:
            doc = self.searcher.doc(scoreDoc.doc)
            #print 'doc matched = ', dir(doc)
            contents = LuceneDoc.load(doc.get('name'))
            matches.append({'contents' : contents, 'doc' : doc})
        return matches
           
    def search(self, query):
        matches = self.raw_search(query)
        results = ''
        if len(matches) > 0:
            results += str(len(matches))+" results <br/>"
            for match in matches:
                results += '<a href='+str(match['contents']['dealUrl'])+'>'+str(match['contents']['merchant'])+'</a><br />'
                results += '<p>'+str(match['contents']['shortAnnouncementTitle'])+','+str(match['contents']['redemptionLocation'])+'</p><br/>'
        else:
            results = "0 results <br/>"
        return results
        
    def cli_search(self):
        while True:
            print
            print "Hit enter with no input to quit."
            command = raw_input("Query:")
            if command == '':
                return
            matches = self.raw_search(command)
            print
            print "Searching for:", command
            
            for match in matches:
                print match['contents']['dealUrl']
                print match['contents']['merchant'], ',', match['contents']['redemptionLocation'], ', ', match['contents']['div']
                print match['contents']['shortAnnouncementTitle']
                print '-'*80
    
if __name__ == '__main__':
    lucene = LuceneSearch()
    lucene.cli_search()
    lucene.close()
