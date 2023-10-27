git remote set-url origin "git@$CI_SERVER_HOST:$CI_PROJECT_PATH.git" # Set correct git url
git fetch --all
git checkout main && git pull --rebase # Update main if needed
git checkout release && git pull --rebase # Update release branch if needed
git merge main && git push # Merge main into release