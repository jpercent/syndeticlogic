-module(lock_tests).
-include_lib("eunit/include/eunit.hrl").

acquire_test() ->
    1 = lock:acquire(1).

acquire1_test() ->
    2 = lock:acquire(1).



