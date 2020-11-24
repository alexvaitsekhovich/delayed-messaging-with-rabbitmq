#!/usr/bin/env bash

echo "Running pre-push hook"
./githooks/execute-tests.bash

if [ $? -ne 0 ]; then
 echo "Tests failed, cannot push!"
 exit 1
fi
