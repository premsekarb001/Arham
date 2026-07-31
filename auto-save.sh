#!/bin/bash

echo "Auto-save script started."
echo "Note: You must have a remote repository configured and credentials set for 'git push' to work."

while true; do
  # Check if there are any changes
  if [[ `git status --porcelain` ]]; then
    echo "Changes detected. Committing..."
    git add .
    git commit -m "Auto-save: $(date)"
    echo "Pushing to remote..."
    git push
  fi
  # Wait for 10 seconds before checking again
  sleep 10
done
