#!/usr/bin/env bats

@test "addition using bc" {
  result="$(echo 2+2 | bc)"
  [ "$result" -eq 4 ]
}

@test "addition using dc" {
  result="$(echo 2 2+p | dc)"
  [ "$result" -eq 4 ]
}

@test "checking jar files" {
    echo $BATS_TEST_DIRNAME
    test -f target/eps4j-core-0.0.1-SNAPSHOT-tests.jar
    test -f target/eps4j-core-0.0.1-SNAPSHOT-with-dependencies.jar
    
}
