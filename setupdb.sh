#!/bin/bash

#FILES="rooms.txt reservations.txt test1.txt"
FILES="rooms2.txt reservations2.txt"
SRC=$(pwd)

rm -fv hotelliketju.*

for i in ${FILES} ; do
    java -jar target/tikape_Varausjarjestelma-1.0-SNAPSHOT.jar < ${SRC}/${i} || exit 1
done

