#!/bin/bash

LC_ALL="C" # things possibly break off of C Locale

buildCompiler() {
  pushd ..
  ./gradlew build
  compiler="$(pwd)/build/TinyCompiler-1.0.jar"
  export compiler
  popd || true
  return 1
}

runTest(){
  file=$1
  testline=$(head -n1 "$file")
  runmode=$(echo "$testline" | sed "s/.*run-([^\s]*).*/\1/")$(echo "$testline" | sed "s/$.*no-run.*/no/")
  compilemode=$(echo "$testline" | sed "s/.*compile-([^\s]*).*/\1/")
}