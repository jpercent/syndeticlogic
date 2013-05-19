import sys
import re
from math import log

def printUsage():
    print("Please specify two files in correct format: (i) the ranked input file" 
          + " and (ii) the input file containing the relevance scores.")

#inparams 
#  myResults: list of ranked urls for a query
#  groundTruth: map from urls to their relevance values for a query
#return value
#  ndcg score for the query
def getNDCG(myResults, groundTruth):
    relValues = []
    ndcgScore = 0.0
    for (index, res) in enumerate(myResults):
      rel = groundTruth[res.strip()]
      rel = rel if rel > 0.0 else 0.0
      relValues.append(rel)
      ndcgScore += (2**rel - 1)/log(2 + index, 2)
    
    z = 0.0
    relValues.sort(reverse=True)
    for (index, val) in enumerate(relValues):
      z += (2**val - 1)/log(2 + index, 2)

    return ndcgScore/z if z>0.0 else 1.0 

#extract queries from the file
def getQueries(rankingFile):
    pat = re.compile('((^|\n)query.*?($|\n))')
    rankings = open(rankingFile,'r')
    res = filter(lambda x: not(x is '' or x=='\n'), pat.split(rankings.read()))

    for item in res:
      if (item.strip().startswith('query:')):
        query = item.strip()
      else:
        results = filter(lambda x: not(x=='' or x=='\n'), 
                         re.findall('url: .*', item.strip()))
        yield(query, results)
    rankings.close()

def main(myRankFile, groundTruthFile):
    groundTruth = {}
    ndcgScore = 0.0
    numQueries = 0

    #populate map with ground truth for each query
    for (query, results) in getQueries(groundTruthFile):
      groundTruth[query] = {}
      for res in results:
        temp = res.rsplit(' ', 1)
        url = temp[0].strip()
        rel = float(temp[1].strip())
        groundTruth[query][url] = rel
            
    #go through each query of rank file and calculate ndcg
    for (query, results) in getQueries(myRankFile):
      ndcgScore += getNDCG(results, groundTruth[query])
      numQueries += 1

    print ndcgScore/numQueries
       
if __name__=='__main__':
    if (len(sys.argv) < 3):
      printUsage()   
      os.exit(1) 
    main(sys.argv[1], sys.argv[2])
