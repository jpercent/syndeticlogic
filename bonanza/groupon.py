import tempfile
from urllib import urlencode
import json
import httplib2
from pymongo import MongoClient
  
class JsonAsObject(object):
    def __init__(self, json):
        self.json = json
    def __getattr__(self, attr):
        ret = None
        if attr in self.json:
            ret = self.json[attr]
        return ret
    def dump(self):
        return self.__str__() + ' = '+str(self.json)
    def dump_keys(self):
        return self.json.keys()
class Division(JsonAsObject):
    pass
class Deal(JsonAsObject):
    pass
  
class DealAccessor(object):
    @staticmethod    
    def get_deals(base_uri, client_id, transformer, model):
        headers, divisions = DealAccessor.get_divisions(base_uri, client_id)
        deals_by_division = []
        for division in divisions:
            print division.id, division.name
            uri = DealAccessor.deals(base_uri, division.id, client_id)
            headers, deals = DealAccessor.get_json(uri, DealAccessor.deals_from_json)
            #print len(deals), type(deals)
            #for a in deals:
            #    print a.redemptionLocation
            deals_by_division.append(deals)
        return deals_by_division
    @staticmethod
    def get_divisions(base_uri, client_id):
        uri = DealAccessor.divisions(base_uri, client_id)
        return DealAccessor.get_json(uri, DealAccessor.divisions_from_json)
    @staticmethod
    def get_json(uri, converter = None):
        http = httplib2.Http(tempfile.mkdtemp())
        response_headers, content = http.request(uri, 'GET')
        response = json.loads(content, 'utf-8', object_hook=converter)
        return response_headers, response
    @staticmethod
    def divisions_from_json(dct):
        if 'divisions' in dct:
            ret =  dct['divisions']
            return ret
        elif not 'areas' in dct:
            return dct
        else:
            ret = Division(dct)
            return ret
    @staticmethod
    def deals_from_json(dct):
        if 'deals' in dct:
            return dct['deals']
        elif 'merchant' in dct:
            ret = Deal(dct)
            return ret
        else:
            return dct
    @staticmethod
    def divisions(base_uri, client_id):
        return base_uri + '/divisions.json?client_id='+client_id
    @staticmethod
    def deals(base_uri, division_id, client_id):
        return base_uri + '/deals.json?division_id='+division_id+'&client_id='+client_id
        
class DealPopulator(object):
    @staticmethod
    def populate():
        base_uri = 'http://api.groupon.com/v2'
        client_id = '1621781630ea78f886999474a26d11213c603edb'
        deals_by_division = DealAccessor.get_deals(base_uri, client_id, None, None)
        mongo = MongoWrapper('localhost', 27017, True)
        lucene_doc = LuceneDoc()
        for deals in deals_by_division:
            for adeal in deals:
                try:
                    print adeal.id
                    mongo_deal = mongo.insert(adeal.json)
                    LuceneDoc.dump_doc(mongo_deal)
                    LuceneDoc.generate(mongo_deal)
                except Exception, e:
                    print 'exception received: ', e
        return deals

if __name__ == '__main__':
    DealPopulator.populate()