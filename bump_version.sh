#!/bin/bash

DATE=$(date +'%Y-%m-%d')
CURRENT_VERSION=$(git describe --tags --abbrev=0)

# https://stackoverflow.com/questions/918886/how-do-i-split-a-string-on-a-delimiter-in-bash#answer-918931
IFS='.' read -ra VERSIONS <<< "$CURRENT_VERSION"

CURRENT_MAJOR_VERSION=${VERSIONS[0]}
CURRENT_MINOR_VERSION=${VERSIONS[1]}
CURRENT_PATCH_VERSION=${VERSIONS[2]}

new_major_version=$CURRENT_MAJOR_VERSION
new_minor_version=$CURRENT_MINOR_VERSION
new_patch_version=$CURRENT_PATCH_VERSION

breaking_changes=()
enhancements=()
fixes=()
internals=()

trimmed_subject=""

trim_subject () {
  subject=$1
  trimmed_subject=$(echo $subject | sed -e s/^.*:" "//)
  trimmed_subject=$(echo $trimmed_subject | awk '{$1=toupper(substr($1,0,1))substr($1,2)}1')
}

# Get latest information
git fetch --all

# Get all commits since latest tag - without merge commits - formatted to only include the commit hash
# Only using the hashes here to avoid cumbersome parsing
COMMITS=$(git log $CURRENT_VERSION..HEAD --no-merges --pretty=format:"%H")

for commit in $COMMITS; do
  # Disable " " as delimiter
  IFS=""

  # Get the subject of the current commit
  subject="$(git log -1 $commit --pretty=format:"%s")"

  # Get the body of the current commit
  body="$(git log -1 $commit --pretty=format:"%B")"

  # Check if commit message body contains 'BREAKING CHANGE'
  # -i: ignore case
  # -q: no output
  # -F: Treat argument as a string rather than a regex
  if echo $body | grep -iqF "BREAKING CHANGE"; then
    trim_subject $subject
    breaking_changes+=( $trimmed_subject )

  # Check if commit is an enhancement
  elif echo $subject | grep -iq "feat\|perf"; then
    trim_subject $subject
    enhancements+=( $trimmed_subject )

  # Check if commit is a fix
  elif echo $subject | grep -iq "fix"; then
    trim_subject $subject
    fixes+=( $trimmed_subject )

  # Check if commit is internal
  elif echo $subject | grep -iq "build\|ci\|docs\|refactor\|test\|chore"; then
    trim_subject $subject
    internals+=( $trimmed_subject )

  # If commit is none if the above -> add to internal
  else
    internals+=( $subject )
  fi
done

# Update versions
# Checks if there are breaking changes available
if (( ${#breaking_changes[@]} )); then
  new_major_version=$(( $CURRENT_MAJOR_VERSION + 1 ))
  new_minor_version=0
  new_patch_version=0

# Checks if there are enhancements or fixes available
elif (( ${#enhancements[@]} )); then
  new_minor_version=$(( $CURRENT_MINOR_VERSION + 1 ))
  new_patch_version=0

# Checks if there are internals available
elif (( ${#fixes[@]} )) || (( ${#internals[@]} )); then
  new_patch_version=$(( $CURRENT_PATCH_VERSION + 1 ))
fi

new_version="$new_major_version.$new_minor_version.$new_patch_version"

# Create Changelog
changelog="$new_version Release notes ($DATE)\n"
changelog+="=============================================================\n"

add_changes_if_needed () {
  title=$1
  shift # Shift all arguments to the left (original $1 gets lost)
  changes=("$@")

  if (( ${#changes[@]} )); then
    changelog+="\n### $title\n\n"
    for change in ${changes[@]}; do
      changelog+="* ${change}\n"
    done
  fi
}

add_changes_if_needed "Breaking Changes" "${breaking_changes[@]}"
add_changes_if_needed "Enhancements" "${enhancements[@]}"
add_changes_if_needed "Fixes" "${fixes[@]}"
add_changes_if_needed "Internal" "${internals[@]}"

# Add new changelog to file
sed -i '' -e "1s/^/$changelog\n/" CHANGELOG.md
echo "Created changelog entry for version $new_version"

# Commit changes
git checkout -b "bump-version-$new_version"
git add -u
git commit -m "Bump version to $new_version"
