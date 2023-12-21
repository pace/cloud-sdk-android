#!/bin/bash

ASSET_DIRECTORY=configuration/assets
DRAWABLE_DIRECTORY=app/src/main/res/drawable
RES_DIRECTORY=app/src/main/res

# The extension for some files is a wildcard, as several file extensions are allowed
mv configuration/configuration.json configuration.json
mv ${ASSET_DIRECTORY}/firebase_config_android.json app/google-services.json
mv ${ASSET_DIRECTORY}/android_keystore.jks keystore-release.jks

for f in ${ASSET_DIRECTORY}/android_launcher_icon_*
do
    extension="${f##*.}"
    density=${f##*android_launcher_icon_}
    density=${density%%.*}
    path=${RES_DIRECTORY}/mipmap-${density}
    output=${path}/ic_launcher.${extension}
    mkdir -p $path
    mv "$f" "$output"
    echo "Moved launcher icon $f to $output"
done

for f in ${ASSET_DIRECTORY}/*.htm*
do
    filename=$(basename -- "$f")
    filename=${filename%_*}
    extension="${f##*.}"
    language=${f##*_}
    language=${language%-*}
    language=${language%.*}

    if [ $language = "en" ]; then
        path=${RES_DIRECTORY}/raw
    else
        path=${RES_DIRECTORY}/raw-${language}
    fi

    output=${path}/${filename}.${extension}
    mkdir -p $path
    mv "$f" "$output"
    echo "Moved legal document $f to $output"
done

needed_files=("usage_terms" "privacy_statement" "usage_analysis" "imprint")
for item in "${needed_files[@]}";
do
    legal_file=app/src/main/res/raw/${item}.html
    if [ ! -e "$legal_file" ]; then
        echo "Error: File '$legal_file' does not exist in base language."
        exit 1
    fi
done
echo "All legal files exist in base language."

shopt -s extglob
onboarding_header=false
list_header=false
detail_icon=false

for f in ${ASSET_DIRECTORY}/@(*_header_image_android.*|detail_view_brand_icon_android.*)
do
    extension="${f##*.}"

    case "$f" in
        *onboarding_header_image_android*)
            filename=ic_onboarding_header
            onboarding_header=true
            ;;
        *list_header_image_android*)
            filename=ic_list_header
            list_header=true
            ;;
        *fallback_header_image_android*) filename=ic_fallback_header;;
        *detail_view_brand_icon_android.*)
            filename=ic_brand_logo
            detail_icon=true
            ;;
        *android_notification_icon*) filename=ic_notification;;
    esac

    output=${DRAWABLE_DIRECTORY}/${filename}.${extension}
    rm ${DRAWABLE_DIRECTORY}/${filename}.*
    mv "$f" "$output"
    echo "Moved image $f to $output"
done

echo "Onboarding header exists: $onboarding_header"
echo "List header exists: $list_header"
echo "Detail icon exists: $detail_icon"
apt-get update && apt-get install -y jq
jq ". + { \"list_show_custom_header\": $list_header, \"onboarding_show_custom_header\": $onboarding_header, \"detail_screen_show_icon\": $detail_icon }" configuration.json > temp.json && mv temp.json configuration.json

./gradlew clean
./gradlew app:bundleRelease -PbuildNumber=$BUILD_NUMBER -PversionName=$VERSION_NAME
