#!/usr/bin/env bash
set -e

PROP_FILE="$(dirname "$0")/version.properties"

if [ ! -f "$PROP_FILE" ]; then
    echo "VK_CODE=1" > "$PROP_FILE"
    echo "VK_TAG=VK01" >> "$PROP_FILE"
    echo "VERSION_BASE=2.4.0" >> "$PROP_FILE"
fi

CURRENT_CODE=$(grep 'VK_CODE=' "$PROP_FILE" | cut -d'=' -f2)
VERSION_BASE=$(grep 'VERSION_BASE=' "$PROP_FILE" | cut -d'=' -f2)

if [ -z "$CURRENT_CODE" ]; then
    CURRENT_CODE=1
fi
if [ -z "$VERSION_BASE" ]; then
    VERSION_BASE="2.4.0"
fi

NEXT_CODE=$((CURRENT_CODE + 1))
NEXT_TAG=$(printf "VK%02d" "$NEXT_CODE")

cat <<EOF > "$PROP_FILE"
VK_CODE=$NEXT_CODE
VK_TAG=$NEXT_TAG
VERSION_BASE=$VERSION_BASE
EOF

echo "=========================================="
echo " [StreamFlow] Versiyon Başarıyla Artırıldı! "
echo " Önceki: VK$(printf "%02d" "$CURRENT_CODE")"
echo " Yeni:   $NEXT_TAG  ($VERSION_BASE-$NEXT_TAG)"
echo " Code:   $NEXT_CODE"
echo "=========================================="
