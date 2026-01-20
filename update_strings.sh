#!/bin/bash

# Reads the value of a property from a properties file.
#
# $1 - Key name, matched at beginning of line.
function prop {
    grep "^${1}" lokalise.properties|cut -d'=' -f2
}

echo 'Get project ID and API token from properties file'
PROJECT_ID=$(prop 'projectId')
API_TOKEN=$(prop 'apiToken')

if [ -z "$PROJECT_ID" ] || [ -z "$API_TOKEN" ]; then
  echo "Project ID and/or API token are not set. You can get these values from Lokalise account."
  exit 1
fi

echo 'Checking if "jq" command is available'
if ! [[ $(command -v jq) ]]; then
  echo "'jq' is not installed. Install 'jq' via 'brew install jq' or your OS's respective dependency manager's command"
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

echo 'Trigger Lokalise export...'
EXPORT_RESPONSE=$(curl --request POST \
     --url "https://api.lokalise.com/api2/projects/$PROJECT_ID/files/async-download" \
     --header "X-Api-Token:$API_TOKEN" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data "${EXPORT_SETTINGS}")

# Extract the process ID from the response
PROCESS_ID=$(echo "$EXPORT_RESPONSE" | jq -r .process_id)

if [ -z "$PROCESS_ID" ] || [ "$PROCESS_ID" = "null" ]; then
  echo "Failed to start async export. API response:"
  echo "$EXPORT_RESPONSE"
  exit 1
fi

echo "Waiting for export process with ID $PROCESS_ID to complete..."

# Poll the process endpoint until the export is ready
MAX_ATTEMPTS=60  # Wait up to 5 minutes (60 * 5 seconds)
ATTEMPT=0
STATUS="queued"

while [ "$STATUS" != "finished" ] && [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
  sleep 5
  ATTEMPT=$((ATTEMPT + 1))

  PROCESS_RESPONSE=$(curl --request GET \
       --url "https://api.lokalise.com/api2/projects/$PROJECT_ID/processes/$PROCESS_ID" \
       --header "X-Api-Token:$API_TOKEN" \
       --header 'accept: application/json')

  STATUS=$(echo "$PROCESS_RESPONSE" | jq -r .process.status)
  echo "Export process status: $STATUS (attempt $ATTEMPT/$MAX_ATTEMPTS)"

  if [ "$STATUS" = "failed" ]; then
    echo "Failed retrieving the export process. API response:"
    echo "$PROCESS_RESPONSE"
    exit 1
  fi
done

if [ "$STATUS" != "finished" ]; then
  echo "Export process timed out after $MAX_ATTEMPTS attempts"
  exit 1
fi

# Get the download URL from the completed export process
DOWNLOAD_URL=$(echo "$PROCESS_RESPONSE" | jq -r .process.details.download_url)

if [ -z "$DOWNLOAD_URL" ] || [ "$DOWNLOAD_URL" = "null" ]; then
  echo "Download URL is null or empty. API response:"
  echo "$PROCESS_RESPONSE"
  exit 1
fi

echo "Download URL: $DOWNLOAD_URL"

TEMP_ZIP_FILE='tmp_strings.zip'

echo 'Downloading zip file into temporary file'
curl $DOWNLOAD_URL -o $TEMP_ZIP_FILE 2>/dev/null

echo 'Unzipping file'
unzip -o $TEMP_ZIP_FILE >/dev/null

echo 'Removing temporary zip file'
rm $TEMP_ZIP_FILE
