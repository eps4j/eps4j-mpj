#!/usr/bin/env bats

function setup {
    CMD="timeout 120 mpjrun.sh -cp target/eps4j-core-0.0.1-SNAPSHOT-with-dependencies.jar:target/eps4j-core-0.0.1-SNAPSHOT-tests.jar org.eps4j.EPSCmd org.eps4j.mockup.MockupFactory"
}

@test "CLI error" {
    run $CMD -np 3 -c
    [ "$status" -eq 0 ]
    echo  "$output" | grep -q  "Exception"
}

@test "no job, no better, 1 worker" {
    run $CMD -np 3 -n 0 -b 0 
    [ "$status" -eq 0 ]
    !(echo  "$output" | grep -q  "Exception")
}

@test "1 job, no better, 1 worker" {
    run $CMD -np 3 -n 1 -b 0 
    [ "$status" -eq 0 ]
    !(echo  "$output" | grep -q  "Exception")
}

@test "10 jobs, no better, 2 workers" {
    run $CMD -np 4 -n 10 -b 0 -ts 1000 -td 1000 -tb 500
    [ "$status" -eq 0 ]
    !(echo  "$output" | grep -q  "Exception")
}

@test "50 jobs, 10 betters, 4 workers" {
    run $CMD -np 6 -n 50 -b 10 -ts 1000 -td 100 -tb 500
    [ "$status" -eq 0 ]
    !(echo  "$output" | grep -q  "Exception")
}

@test "50 jobs, 10 betters, 6 workers" {
    run $CMD -np 8 -n 50 -b 10 -ts 100 -td 1000 -tb 500
    [ "$status" -eq 0 ]
    !(echo  "$output" | grep -q  "Exception")
}

@test "50 jobs, 10 betters, 8 workers" {
    run $CMD -np 10 -n 50 -b 10 -ts 1000 -td 100 -tb 100
    [ "$status" -eq 0 ]
    !(echo  "$output" | grep -q  "Exception")
}

@test "10 jobs, 10 better, 2 workers, bounds = [-1,1]" {
    run $CMD -np 4 -n 10 -b 10 -ts 1000 -td 1000 -tb 500 -lb -1 -ub 1
    [ "$status" -eq 0 ]
    !(echo  "$output" | grep -q  "Exception")
}

@test "5 jobs, 100 better, 2 workers" {
    run $CMD -np 4 -n 5 -b 100 -ts 1000 -td 1000 -tb 2000 -lb -10 -ub 10
    [ "$status" -eq 0 ]
    !(echo  "$output" | grep -q  "Exception")
}
