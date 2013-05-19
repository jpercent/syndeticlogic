-module(tut).
-export([double/1]).
-export([fact/1]).
-export([factoid/2, convert_length/1, list_length/1, format_temps/1, reverse/1, 
         list_max/1, find_max_and_min/1, fast_find_max_min/1, convert_list/1, 
         convert_list1/1]).
-export([start/0, say_something/2]).
-export([start1/0, ping/2, pong/0]).
-export([start_ping/1, start_pong/0]).

double(X) -> 2 * X.

fact(1) -> 1;
fact(N) -> N * fact(N - 1).

factoid(M,N) -> M * N.

convert_length({cm, X}) -> 
    {in, X / 2.54};
convert_length({in, Y}) -> 
    {cm, Y*2.54}.

list_length([]) -> 0;
list_length([_| Rest]) -> 1 + list_length(Rest).

format_temps([]) -> ok;
format_temps([City | Rest]) -> 
    print_temp(convert_to_celsius(City)),
    format_temps(Rest).

convert_to_celsius({Name, {c, Temp}}) ->
    {Name, {c, Temp}};
convert_to_celsius({Name, {f, Temp}}) ->
    {Name, {c, (Temp - 32) * 5/9}}.

print_temp({Name, {c, Temp}}) ->
    io:format("~-15w ~w c~n", [Name, Temp]).

list_max([Head|Rest]) -> list_max(Rest, Head).

list_max([],  Result_so_far) -> Result_so_far;
list_max([Head | Rest], Result_so_far) when Head > Result_so_far ->
    list_max(Rest, Head);
list_max([_|Rest], Result_so_far) ->
    list_max(Rest, Result_so_far).

reverse(Listit) -> reverse(Listit, []).

reverse([], Reversed) -> Reversed;
reverse([Head | Rest], Reversed) -> reverse(Rest, [Head | Reversed]).

convert_all([], Converted) ->
    Converted;  
convert_all([City | Rest], Converted) ->
    CityConverted = convert_to_celsius(City),
    convert_all(Rest, [CityConverted | Converted]).

find_max_and_min([City | Rest]) -> 
    [ConvertedCity | ConvertedRest ] = convert_all([City|Rest], []),
    find_max_and_min(ConvertedRest, ConvertedCity, ConvertedCity).

find_max_and_min([], Max, Min) -> {Max, Min};
find_max_and_min([Head | Rest], Max, Min) when element(2, element(2, Head)) > element(2, element(2, Max)) ->
    ConvertedCity = convert_to_celsius(Head),
    find_max_and_min(Rest, ConvertedCity, Min);
find_max_and_min([Head | Rest], Max, Min) when element(2, element(2, Head)) < element(2, element(2, Min)) ->
    ConvertedCity = convert_to_celsius(Head),
    find_max_and_min(Rest, Max, ConvertedCity);
find_max_and_min([_ | Rest], Max, Min) ->
    find_max_and_min(Rest, Max, Min).

fast_find_max_min(Cities) ->
    [FirstCityRaw | CitiesList] = Cities,
    CityPreprocessor = fun(City) -> convert_to_celsius(City) end,
    FirstCity = CityPreprocessor(FirstCityRaw),
    {Max, Min} = fast_find_max_min(CitiesList, FirstCity, FirstCity, CityPreprocessor),
    io:format("Max temperature was ~w:~w~n", [element(1, Max), element(2, element(2, Max))]),
    io:format("Min temperature was ~w:~w~n", [element(1, Min), element(2, element(2, Min))]).

fast_find_max_min([], Max, Min, _) -> {Max, Min};
fast_find_max_min([NextCity | CitiesList], {Max, {c, MaxTemp}}, {Min, {c, MinTemp}}, CityPreprocessor) ->
    {Name, {c, Temp}} = CityPreprocessor(NextCity),
    if 
	Temp > MaxTemp ->
       	    fast_find_max_min(CitiesList, {Name, {c, Temp}}, {Min, {c, MinTemp}}, CityPreprocessor);
	Temp < MinTemp ->
       	    fast_find_max_min(CitiesList, {Max, {c, MaxTemp}}, {Name, {c, Temp}}, CityPreprocessor);
	true ->
       	    fast_find_max_min(CitiesList, {Max, {c, MaxTemp}}, {Min, {c, MinTemp}}, CityPreprocessor)
    end.

convert_list(Cities) ->
    lists:map(fun convert_to_celsius/1, Cities).

convert_list1(Cities) ->
    ConvertedCities = lists:map(fun convert_to_celsius/1, Cities),
    SortedConvertedCities = lists:sort(fun({_, {c, Temp0}}, {_, {c, Temp1}}) -> Temp0 < Temp1 end, 
				       ConvertedCities),
    IOFunction = fun({Name, {c, Temp}}) -> io:format("Temperature in ~w is ~w degrees celsius~n", 
						     [Name, Temp]) end, 
    lists:foreach(IOFunction, SortedConvertedCities).

say_something(_, 0) ->
    done;
say_something(What, Times) ->
    io:format("~p~n", [What]),
    say_something(What, Times - 1).

start() ->
    spawn(tut, say_something, [hello, 3]),
    spawn(tut, say_something, [goodbye, 3]).


ping(0, PongNode) -> 
    {pong, PongNode} ! finished,
    io:format("ping finished ~n", []);
ping(N, PongNode) ->
    {pong, PongNode} ! {ping, self()},
    receive
        pong -> io:format("Ping received pong ~w~n", [pong])
    end,
    ping(N-1, PongNode).

pong() -> 
    receive 
	finished -> 
	    io:format("Pong finished ~n", []);
	{ping, PingPid} -> 
	    io:format("Pong received ping from ~w~n", [PingPid]),
	    PingPid ! pong,
	    pong()
    end. 

start1() ->
    %PongPid =  spawn(tut, pong, []),
    %spawn(tut, ping, [3, PongPid]),
    ok.

start_ping(PongNode) ->
    register(ping, spawn(tut, ping, [5, PongNode])).

start_pong() ->
    register(pong, spawn(tut, pong, [])).
