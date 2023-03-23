#!/bin/sh
./gradlew tiny-stdlib:build
java -cp '.;tiny-stdlib/build/tiny-stdlib-1.0.jar' $1