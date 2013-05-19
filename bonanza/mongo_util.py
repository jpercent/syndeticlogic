
class MongoWrapper(object):
    def __init__(self, host, port, drop=False):
        self.connection = MongoClient(host, port)
        if drop == True:
            # XXX - destroy the database everytime -- for demo only
            self.connection.drop_database('tsoonami')   
        self.tsoonami = self.connection.tsoonami
        self.deals = self.tsoonami.deals
        self.host = host
        self.port = port
    def insert(self, deal):
        try: 
            id = self.deals.insert(deal)
            deal['mongo_id'] = id
            ret_deal = self.deals.find_one({'_id' : deal['mongo_id']})
            return ret_deal
        except:
            return None
    def find_one(self, ids):
        deals = []
        for id in ids:
            deal = self.deals.find_one({'_id' : id})
            if deal != None:
                deals.append(deal)
            else:
                print 'deal for id = ', id, ' was not found? '                
    def dump_keys(self, mongo_deal):
        print '-'*80
        if mongo_deal == None:
            print 'NO KEYS'
            print '-'*80
            return
        
        for key in mongo_deal.keys():
            print 'KEY == ', key
            print 'VALUE == -->'+str(mongo_deal.get(key))+'<---'
            print ''
            print  ''
            print '-'*80
            
class LuceneDoc:
    @staticmethod
    def generate(mongo_deal):
        div = None
        tags = None
        merchant = None
        try:
            div = str(mongo_deal.get('division')['name'])
            merchant = str(mongo_deal.get('merchant')['name'])
            tags = str(mongo_deal.get('tags').values())
            #LuceneDoc.dump_doc(mongo_deal)
        except:
            print 'Error-div = ', div, 'tags = ', tags, ' merchant = ', merchant
        name = str(div)+'--'+str(mongo_deal.get('id'))
        f = open('groupon-deals/'+name,'w+')
        endofrecord = '\n------------\n'
        f.write(str(mongo_deal.get('_id'))+endofrecord)
        f.write(str(mongo_deal.get('dealUrl'))+endofrecord)
        f.write(str(mongo_deal.get('shortAnnouncementTitle'))+endofrecord)
        f.write(str(mongo_deal.get('redemptionLocation'))+endofrecord)
        f.write(str(merchant)+endofrecord)
        f.write(str(div)+endofrecord)
        f.write(str(mongo_deal.get('announcementTitle'))+endofrecord)
        f.write(str(tags)+endofrecord)
        f.write(str(mongo_deal.get('highlightsHtml'))+endofrecord)
        f.write(str(mongo_deal.get('pitchHtml'))+endofrecord)
        f.write(str(mongo_deal.get('id'))+endofrecord)
        f.flush()
        f.close()
    @staticmethod
    def read_multiline(f):
        ret = f.readline()
        while True:
            line = f.readline()
            if line.find('------------\n') == -1:
                ret = ret + line
            else:
                break
        return ret.rstrip()
    @staticmethod
    def load(doc_name):
        f = open('groupon-deals/'+doc_name,'r')
        doc = {}
        doc['_id'] = f.readline().strip()
        f.readline()
        doc['dealUrl'] = f.readline().strip()
        f.readline()
        doc['shortAnnouncementTitle'] = LuceneDoc.read_multiline(f)
        doc['redemptionLocation'] = f.readline().strip()
        f.readline()
        doc['merchant'] = f.readline().strip()
        f.readline()
        doc['div'] = f.readline().strip()
        f.readline()
        doc['announcementTitle'] = LuceneDoc.read_multiline(f)
        doc['tags'] =  f.readline().strip()
        f.readline()
        doc['highlightsHtml'] = LuceneDoc.read_multiline(f)
        doc['pitchHtml'] = LuceneDoc.read_multiline(f)
        doc['id'] = f.readline().strip()
        f.close()
        return doc
    @staticmethod
    def dump_doc(mongo_deal):
        print '-'*80
        doc = { 'div_name' :str(mongo_deal.get('division')['name']),
                'tags' : str(mongo_deal.get('tags')),
                'merchant' : str(mongo_deal.get('merchant')['name']),
                '_id' : str(mongo_deal.get('_id')),
                'dealUrl' : str(mongo_deal.get('dealUrl')),
                'shorttitile' : str(mongo_deal.get('shortAnnouncementTitle')),
                'location': str(mongo_deal.get('redemptionLocation')),
                'merchant' : str(mongo_deal.get('merchant')),
                'announceTitle' : str(mongo_deal.get('announcementTitle')),
                'highlightsHtml' : str(mongo_deal.get('highlightsHtml')),
                'pitchHtml' : str(mongo_deal.get('pitchHtml')),
                'id' : str(mongo_deal.get('id'))
        }
        print ' Lucene Doc == ', str(doc)
        print '-'*80
        