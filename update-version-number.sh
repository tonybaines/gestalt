#!/usr/bin/env bash


function currentVersion() {
  echo $(grep "version =" build.gradle | cut -d"'" -f2)
}

function previousVersion() {
  local v=$(grep com.github.tonybaines:gestalt: INSTALLING.md | cut -d":" -f3)
  echo ${v%\'}
}


CURRENT_V=$(currentVersion)
PREV_V=$(previousVersion)
RELEASE_V=${CURRENT_V%-*}

echo "Previous $PREV_V"
echo "Currently $CURRENT_V"
echo "Releasing $RELEASE_V"

# Update files
sed -i  "s/version = '$CURRENT_V'/version = '$RELEASE_V'/" build.gradle
sed -i  "s/$PREV_V/$RELEASE_V/" INSTALLING.md

git commit -a -m "Releasing $RELEASE_V"