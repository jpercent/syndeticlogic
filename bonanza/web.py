from flask import Flask, request, render_template
import search

app = Flask(__name__)

@app.route("/", methods=['POST'])
def search_request():
    print 'headers --->',request.headers,'<----'
    print 'data --->',str(request.data),'<----'
    print 'form --->',request.form,'<----'
    print 'args ', request.args
    print dir(request)
    lucene = search.LuceneSearch()
    search_results = lucene.search(request.form['query'])
    lucene.close()
    print search_results
    return search_results

@app.route("/", methods=['GET'])
def search_display(user=None):
  return render_template('search.html')

if __name__ == '__main__':
  app.run('0.0.0.0', debug=True)