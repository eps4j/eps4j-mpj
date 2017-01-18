#!/usr/bin/env bats

function setup {
    #CMD="timeout 120 mpjrun.sh -jar ../../../target/eps4j-0.0.1-SNAPSHOT-test-jar-with-dependencies.jar org.eps4j.mockup.MockupFactory"
    CMD="timeout 120 java -cp target/eps4j-core-0.0.1-SNAPSHOT-with-dependencies.jar:target/eps4j-core-0.0.1-SNAPSHOT-tests.jar org.eps4j.HelloTest"
}


@test "addition using bc" {
  result="$(echo 2+2 | bc)"
  [ "$result" -eq 4 ]
}

@test "checking jar files" {
    test -f target/eps4j-core-0.0.1-SNAPSHOT-tests.jar
    test -f target/eps4j-core-0.0.1-SNAPSHOT-with-dependencies.jar    
}

@test "testing command" {
    run $CMD
    [ "$status" -eq 0 ]
    echo  "$output" | grep "Hello World!"
}
