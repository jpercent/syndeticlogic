import os
import urllib
import urllib2
import hashlib
import random
import email
import email.message
import email.encoders
import StringIO
import sys
import json
import subprocess
import getpass
from tempfile import NamedTemporaryFile
from time import time
from subprocess import Popen
from subprocess import PIPE
import datetime

class NullDevice:
  def write(self, s):
    pass

def submit(partId):
    
  print '==\n== [inforetrieval] Submitting Solutions | Programming Exercise %s\n=='% homework_id()
  if(not partId):
    partId = promptPart()

  partNames = validParts()
  if not isValidPartId(partId):
    print '!! Invalid homework part selected.'
    print '!! Expected an integer from 0 to %d.' % (len(partNames) + 1)
    print '!! Submission Cancelled'
    return

  (login, password) = loginPrompt()
  if not login:
    print '!! Submission Cancelled'
    return

  print '\n== Connecting to coursera ... '

  # Setup submit list
  if partId == len(partNames):
    submitParts = range(0, len(partNames))
  else:
    submitParts = [partId]
  print submitParts
  for partId in submitParts:
    # Get Challenge
    (login, ch, state, ch_aux) = getChallenge(login, partId)
    if((not login) or (not ch) or (not state)):
      # Some error occured, error string in first return element.
      print '\n!! Error: %s\n' % login
      return

    # Get source files
    src = source(partId)
    

    # Attempt Submission with Challenge
    ch_resp = challengeResponse(login, password, ch)
    (result, string) = submitSolution(login, ch_resp, partId, output(partId, ch_aux), \
                                    src, state, ch_aux)
    print '== [inforetrieval] Submitted Homework %s - Part %d - %s' % \
          (homework_id(), partId, partNames[partId])
    print '== %s' % string.strip()
    if (string.strip() == 'Exception: We could not verify your username / password, please try again. (Note that your password is case-sensitive.)'):
      print '== The password is not your login, but a 10 character alphanumeric string displayed on the top of the Assignments page'

## This collects the source code (just for logging purposes) 
def source(partId):
  if partId == 0:
    if not os.path.exists("report.pdf"):
      print "No report.pdf file found in the directory. Please make sure it exists (and make sure it's all lowercase letters)."
      sys.exit(1);

    p = open("report.pdf","rb").read().encode("base64")
    return p

  return ""

def promptPart():
  """Prompt the user for which part to submit."""
  print('== Select which part(s) to submit for assignment ' + homework_id())
  partNames = validParts()
  for i in range(0, len(partNames)):
    print '==   %d) %s' % (i, partNames[i])
  print '==   %d) All of the above \n==\n ' % (len(partNames))
  selPart = raw_input('Enter your choice [0-{0}]: '.format(len(partNames)))
  partId = int(selPart)
  if not isValidPartId(partId):
    partId = -1
  return partId

def isValidPartId(partId):
  """Returns true if partId references a valid part."""
  partNames = validParts()
  return ((partId >= 0) and (partId <= len(partNames) + 1))


# =========================== LOGIN HELPERS ===========================

def loginPrompt():
  """Prompt the user for login credentials. Returns a tuple (login, password)."""
  (login, password) = basicPrompt()
  return login, password


def basicPrompt():
  """Prompt the user for login credentials. Returns a tuple (login, password)."""
  login = raw_input('Login (Email address): ')
  password = raw_input('Password: ')
  return login, password


def homework_id():
  """Returns the string homework id."""
  return '4'


def getChallenge(email, partId):
  """Gets the challenge salt from the server. Returns (email,ch,state,ch_aux)."""
  url = challenge_url()
  values = {'email_address' : email, 'assignment_part_sid' : "%s-%s" % (homework_id(), partId), 'response_encoding' : 'delim'}
  data = urllib.urlencode(values)
  req = urllib2.Request(url, data)
  response = urllib2.urlopen(req)
  text = response.read().strip()

  # text is of the form email|ch|signature
  splits = text.split('|')
  if(len(splits) != 9):
    print 'Badly formatted challenge response: %s' % text
    return None
  return (splits[2], splits[4], splits[6], splits[8])



def challengeResponse(email, passwd, challenge):
  sha1 = hashlib.sha1()
  sha1.update("".join([challenge, passwd])) # hash the first elements
  digest = sha1.hexdigest()
  strAnswer = ''
  for i in range(0, len(digest)):
    strAnswer = strAnswer + digest[i]
  return strAnswer


def challenge_url():
  """Returns the challenge url."""
  return "https://stanford.coursera.org/" + URL + "/assignment/challenge"
  #return "https://stanford.coursera.org/inforetrieval/assignment/challenge"
  #return "https://stanford.coursera.org/inforetrieval-staging/assignment/challenge"
  
def submit_url():
  """Returns the submission url."""
  return "https://stanford.coursera.org/" + URL + "/assignment/submit"
  return "https://stanford.coursera.org/inforetrieval/assignment/submit"
  #return "https://stanford.coursera.org/inforetrieval-staging/assignment/submit"
  
def submitSolution(email_address, ch_resp, part, output, source, state, ch_aux):
  """Submits a solution to the server. Returns (result, string)."""
  #source_64_msg = email.message.Message()
  #source_64_msg.set_payload(source)
  #email.encoders.encode_base64(source_64_msg)

  output_64_msg = email.message.Message()
  output_64_msg.set_payload(output)
  email.encoders.encode_base64(output_64_msg)
  values = { 'assignment_part_sid' : ("%s-%s" % (homework_id(), part)), \
             'email_address' : email_address, \
             'submission' : output_64_msg.get_payload(), \
             'submission_aux' : source, \
             'challenge_response' : ch_resp, \
             'state' : state \
           }
  url = submit_url()
  inp = raw_input('Do you want to actually submit this (yes|y)?: ') #CHANGELIVE
  inp = inp.strip().lower()
  if inp != 'y' and inp != 'yes':
    print '== Fine, aborting'
    sys.exit(0) #CHANGELIVE
  data = urllib.urlencode(values)
  req = urllib2.Request(url, data)
  response = urllib2.urlopen(req)
  string = response.read().strip()
  # TODO parse string for success / failure
  result = 0
  return result, string

############ BEGIN ASSIGNMENT SPECIFIC CODE ##############
# For example, if your URL is https://class.coursera.org/pgm-2012-001-staging, set it to "pgm-2012-001-staging".
URL = 'cs276-001'

def validParts():
  """Returns a list of valid part names."""
  partNames = ['Report', \
               'Task 1 (Linear Regression)', \
               'Task 2 (Ranking SVM)', \
               'Task 3 (More Features)', \
               'Task 4 (Extra Credit)']
  return partNames

def output(partId, ch_aux):
  """Uses the student code to compute the output for test cases."""
  res = []
  print '== Make sure queryDocTrainData.(train|dev) and queryDocTrainRel.(train|dev) are in the current working directory'
  print '== Running your code ...'
  print '== Your code should output results (and nothing else) to stdout'
  test_file = NamedTemporaryFile(delete=False)
  test_file.write(ch_aux)
  test_file.close()
  linesOutput = 1320

  if not os.path.exists("people.txt"):
      print "There is no people.txt file in this directory. Please make people.txt file in this directory with your and your partner's SUNet ID in separate lines (do NOT include @stanford.edu)"
      sys.exit(1)

  people = open("people.txt").read().splitlines();
  if len(people) == 0:
      print "people.txt is empty! Write the SUNet ids of you and your partner, if any, in separate lines.."
      sys.exit(1);
  for x in people:
      if len(x) < 3 or len(x) > 8 or ' ' in x.strip() == True:
        print "The SUNet IDs don't seem to be correct. Make sure to remove empty lines. They are supposed to be between 3 and 8 characters. Also, make sure to not include @stanford.edu."
        sys.exit(1);
  peopleStr = "_".join(str(x.strip()) for x in people  if x)
 
  elapsed= 0.0 
  if partId == 0:
    print 'Submitting the report'
  elif partId == 1:
    print 'Calling ./l2r.sh for Task 1 (this might take a while)'
    start = time()
    child = Popen(['./l2r.sh', '1', test_file.name], stdout=PIPE, stderr=PIPE, shell=False);
    (res, err) = child.communicate("")
    elapsed = time() - start
    guesses = res.splitlines()
    if (len(guesses) != linesOutput):
        print 'Warning. The number of url-document pairs ' + str(len(guesses)) + ' is not correct. Please ensure that the output is formatted properly.'
  elif partId == 2:
      print 'Calling ./l2r.sh for Task 2 (this might take a while)'
      start = time()
      child = Popen(['./l2r.sh', '2', test_file.name], stdout=PIPE, stderr=PIPE, shell=False)
      (res, err) = child.communicate("")
      elapsed = time() - start
      guesses = res.splitlines()
      print err
      if (len(guesses) != linesOutput):
          print 'Warning. The number of url-document pairs is not correct. Please ensure that the output is formatted properly.'
  elif partId == 3:
      print 'Calling ./l2r.sh for Task 3 (this might take a while)'
      start = time()
      child = Popen(['./l2r.sh', '3', test_file.name], stdout=PIPE, stderr=PIPE, shell=False)
      (res, err) = child.communicate("")
      elapsed = time() - start
      guesses = res.splitlines()
      print err
      if (len(guesses) != linesOutput):
          print 'Warning. The number of url-document pairs is not correct. Please ensure that the output is formatted properly.'
  elif partId == 4:
      print 'Calling ./l2r.sh for Task 4 (this might take a while)'
      start = time()
      child = Popen(['./l2r.sh', '4', test_file.name], stdout=PIPE, stderr=PIPE, shell=False)
      (res, err) = child.communicate("")
      elapsed = time() - start
      guesses = res.splitlines()
      print err
      if (len(guesses) != linesOutput):
        print 'Warning. The number of url-document pairs is no://piazza.com/class#spring2013/cs276/501t correct. Please ensure that the output is formatted properly.'
  else:
    print '[WARNING]\t[output]\tunknown partId: %s' % partId
    sys.exit(1)
  print '== Finished running your code'
  
  return json.dumps( { 'result': res, 'time': elapsed, 'timesubmitted': str(datetime.datetime.now()), 'USERIDS': peopleStr } )

def test_python_version():
  """docstring for test_python_version"""
  if sys.version_info < (2,6):
    print >> sys.stderr, "Your python version is too old, please use >= 2.6"
    sys.exit(1)

if __name__ == '__main__':
  test_python_version()
  submit(0)
