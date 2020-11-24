#!/usr/bin/env bash

GIT_DIR=$(git rev-parse --git-dir)

echo "Installing hooks..."
ln -s ../../githooks/pre-push.bash $GIT_DIR/hooks/pre-push
chmod +x ./githooks/execute-tests.bash ./githooks/pre-push.bash
echo "Done!"