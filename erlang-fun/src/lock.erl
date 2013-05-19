-module(lock).
-export([acquire/2, release/1]).

acquire(Index, Processes) -> 
    write(enter, Index, true),
    write(number, Index, 1 + max()),  
    write(enter, Index, false),
    acquire_each(0, Processes, Index).

acquire_each(_, _, _) ->
    ok;
acquire_each(Cursor, End, Index) when Cursor < End ->
    read_until(enter, Cursor, false),
    %%multi_read_until(number, Cursor, Index), 
    acquire_each(1+Cursor, End, Index).

write(Symbol, Key, Value) ->
    Pids = read_pids_table(),
    send_pids(Pids, {Symbol, {Key, Value}}).

send([], Msg) ->
    ok;
send([NextPid | RestPids], Msg) ->
    NextPid ! Msg,
    send(RestPids, Msg).

read_until(Symbol, Key, Value) ->
    %% read mnesia field, write field
    %% 
    Value = read_mnesiafield.

multi_read_block(Symbol, Cursor, Index) ->
    ok.

release(Index) ->
    write(number, Index, 0),
    wakeup_waiters(Index).

wakeup_waiters() ->
    foreach(read_waiters_mnesia, send_wakeup).

read_enter(enter, Index) ->
    .
   %% lock(integer i) {
   %%        Entering[i] = true;
   %%        Number[i] = 1 + max(Number[1], ..., Number[NUM_THREADS]);
   %%        Entering[i] = false;
   %%        for (j = 1; j <= NUM_THREADS; j++) {
   %%            // Wait until thread j receives its number:
   %%          while (Entering[j]) { /* nothing */ }
   %%          // Wait until all threads with smaller numbers or with the same
   %%          // number, but with higher priority, finish their work:
   %%         while ((Number[j] != 0) && ((Number[j], j) < (Number[i], i))) { /* nothing */ }
   %%     }
   %% }
   
   %% unlock(integer i) {
   %%     Number[i] = 0;
   %% }
 
   %% Thread(integer i) {
   %%     while (true) {
   %%         lock(i);
   %%         // The critical section goes here...
   %%         unlock(i);
   %%         // non-critical section...
   %%     }
   %% }
