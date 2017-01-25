#!/usr/bin/env bats

function setup {
    CMD="timeout 120 mpjrun.sh -cp target/eps4j-core-0.0.1-SNAPSHOT-with-dependencies.jar:target/eps4j-core-0.0.1-SNAPSHOT-tests.jar org.eps4j.HelloTest"
}

@test "checking jar files" {
    test -f target/eps4j-core-0.0.1-SNAPSHOT-tests.jar
    test -f target/eps4j-core-0.0.1-SNAPSHOT-with-dependencies.jar    
}

@test "hi from 2" {
    run $CMD -np 2
    [ "$status" -eq 0 ]
    result=`echo  "$output" | grep -c -e "Hi from <[0-9][0-9]*>"`
    [ "$result" -eq 2 ]
}

@test "hi from 4" {
    run $CMD -np 4
    [ "$status" -eq 0 ]
    result=`echo  "$output" | grep -c -e "Hi from <[0-9][0-9]*>"`
    [ "$result" -eq 4 ]
}
