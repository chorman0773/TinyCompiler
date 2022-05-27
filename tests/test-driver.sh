#!/bin/bash

LC_ALL="C" # things possibly break off of C Locale

buildCompiler() {
  ./gradlew build > /dev/null 2>&1
  ./gradlew tiny-stdlib:build > /dev/null 2>&1
  compiler="$(pwd)/build/libs/TinyCompiler-1.0.jar"
  stdlib="$(pwd)/tiny-stdlib/build/libs/tiny-stdlib-1.0.jar"
  export compiler
  export stdlib
  return 1
}

runtest(){

  return 0
}

dotest(){
  file=$1
  testline=$(head -n1 "$file")
  dataline=$(echo "$file" | sed "s|tests/test\([0-9]*\)\.tiny|\1|")
  runmode=$(echo "$testline" | sed "s/.*run-\([^[:space:]]*\).*/\1/" | sed "s/.*no-run.*/no/")
  compilemode=$(echo "$testline" | sed "s/.*compile-\([^[:space:]]*\).*/\1/")
  echo "$file: $testline"
  ./gradlew run --args="$file" > /dev/null 2>&1
  compile_result=$?

  case $compilemode in
    pass )
      if [ $compile_result -ne 0 ]
      then
        echo -e "\033[0;31mFAIL\033[0m  $file (compile error)"
        return 1
      fi
      ;;
    fail )
      if [ $compile_result -eq 0 ]
      then
        echo -e "\033[0;31mXPASS\033[0m  $file"
        return 1
      else
        echo -e "\033[1;31mXFAIL\033[0m  $file"
        return 0
      fi
      ;;
    * )
      echo "$file: Invalid Compile mode $compilemode"
      return 2
      ;;
  esac


  compile_output="$(echo "$file" | sed -e "s/\(.*\)\.tiny/\1/")"
  case $runmode in
      no )
          echo -e "\033[0;32mPASS\033[0m $file"
        ;;
      pass | fail )
        input_data="$(cat tests/input-data | sed "${dataline}q;d")"
        if [ "$input_data" == "" ]
        then
          java -cp ".:$stdlib" "$compile_output" > /dev/null 2>&1
          res=$?
          case $runmode in
              pass )
                if [ $res -ne 0 ]
                then
                  echo  -e "\033[0;31mFAIL\033[0m  $file (runtime error)"
                  return 1
                else
                  echo -e "\033[0;32mPASS\033[0m $file"
                  return 0
                fi
                ;;
              fail )
                if [ $res -eq 0 ]
                then
                  echo -e "\033[0;31mXPASS\033[0m $file"
                  return 1
                else
                  echo -e "\033[1;31mXFAIL\033[0m $file"
                  return 0
                fi
                ;;
          esac
        else
          while IFS="$" read -r data
            do
              IFS="!" read -r input xoutput <<< "$data"
              echo "$input" > test-stdin
              java -cp ".:$stdlib" "$compile_output" > /dev/null 2>&1

              res="$?"
              output="$(cat test-stdout)"

              case $runmode in
                pass )
                  if [ $res -ne 0 ]
                  then
                    echo  -e "\033[0;31mFAIL\033[0m  $file (runtime error)"
                    return 1
                  elif [ "$output" != "$xoutput" ]
                  then
                    echo  -e "\033[0;31mFAIL\033[0m  $file (incorrect output)"
                    return 1
                  else
                    echo -e "\033[0;32mPASS\033[0m $file"
                  fi
                  ;;
                fail )
                  if [ $res -eq 0 ]
                  then
                    echo -e "\033[0;31mXPASS\033[0m $file"
                  else
                    echo -e "\033[1;31mXFAIL\033[0m $file"
                    return 1
                  fi
                  ;;
            esac
            done <<< "$input_data"
        fi
        ;;
      * )
            echo "$file: Invalid Run mode $runmode"
            return 2
            ;;
  esac
  return 0
}

buildCompiler

status=0

for file in tests/test*.tiny
do
  dotest "$file"
  if [ $? -ne 0 ]
  then
    status=1
  fi
done

exit $status