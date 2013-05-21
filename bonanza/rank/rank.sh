#!/bin/bash
case $1 in
    "1" )
        python ranking.py -s cosine -r $2 ;;
    "2" )
        python ranking.py -s bm25f -r $2 ;;
    "3" )
        python ranking.py -s cosine -r $2 -w;;
    "4" )
        python ranking.py -s bm25f -r $2 ;;
esac
