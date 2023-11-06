#!/bin/bash

# Reads the value of a property from a properties file.
#
# $1 - Key name, matched at beginning of line.
function prop {
    grep "^${1}" localise.properties|cut -d'=' -f2
}

echo 'Get project ID and API token from properties file'
PROJECT_ID=$(prop 'projectId')
API_TOKEN=$(prop 'apiToken')

if [ -z "$PROJECT_ID" ] || [ -z "$API_TOKEN" ]; then
  echo "Project ID and/or API token are not set. You can get these values from Localise account."
  exit 1
fi

echo 'Checking if "jq" command is available'
if ! [[ $(command -v jq) ]]; then
  echo "'jq' is not installed. Install 'jq' via 'brew install jq' or your OS's respective dependency manager's command"
  exit 1
fi

echo 'Checking if there are uncommitted changes'
if [[ $(git diff --stat) != '' ]]; then
  echo 'You have uncommited changes. Clean those up first.'
  exit 1
fi


EXPORT_SETTINGS='
    {
      "format": "xml",
      "placeholder_format": "printf",
      "export_empty_as": "empty",
      "indentation": "4sp",
      "original_filenames": false,
      "bundle_structure": "app/src/main/res/values-%LANG_ISO%/strings.%FORMAT%",
      "replace_breaks": true
    }
  '
COMMIT_MSG='Update app strings'


echo 'Trigger Lokalise export...'
BUNDLE_URL=$(curl --request POST \
     --url https://api.lokalise.com/api2/projects/$PROJECT_ID/files/download \
     --header "X-Api-Token:$API_TOKEN" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data "${EXPORT_SETTINGS}" 2>/dev/null | jq -r .bundle_url)

TEMP_ZIP_FILE='tmp_strings.zip'

echo 'Downloading zip file into temporary file'
curl $BUNDLE_URL -o $TEMP_ZIP_FILE 2>/dev/null

echo 'Unzipping file'
unzip -o $TEMP_ZIP_FILE >/dev/null

echo 'Removing temporary zip file'
rm $TEMP_ZIP_FILE

if [[ $(git diff --stat) == '' ]]; then
  echo 'There are no new text changes'
else
  echo 'Committing changes'
  git add -u && git commit -m "$COMMIT_MSG"
fi