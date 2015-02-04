"""A module for Jython emulating (a small part of) CPython's multiprocessing.
   With this, pygrametl can be made to use multiprocessing, but actually use
   threads when used from Jython (where there is no GIL).
"""

# Copyright (c) 2011-2014, Aalborg University (chr@cs.aau.dk)
# All rights reserved.

# Redistribution and use in source anqd binary forms, with or without
# modification, are permitted provided that the following conditions are met:

# - Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.

# - Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.

# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

from threading import Thread
from pygrametl.jythonsupport import Value

# Needed for both pip2 and pip3 to be supported
try:
    from Queue import Queue
except ImportError:
    from queue import Queue

# NOTE: This module is made for Jython.

__author__ = "Christian Thomsen"
__maintainer__ = "Christian Thomsen"
__version__ = '2.3'
__all__ = ['JoinableQueue', 'Process', 'Queue', 'Value']


class Process(Thread):
    pid = '<n/a>'
    daemon = property(Thread.isDaemon, Thread.setDaemon)
    name = property(Thread.getName, Thread.setName)
    
class JoinableQueue(Queue):
    def close(self):
        pass

