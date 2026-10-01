#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
PROP_FILE="$DIR/version.properties"
README_FILE="$DIR/README.md"

if [ ! -f "$PROP_FILE" ]; then
    echo "VK_CODE=1" > "$PROP_FILE"
    echo "VK_TAG=VK01" >> "$PROP_FILE"
    echo "VERSION_BASE=2.4.0" >> "$PROP_FILE"
fi

CURRENT_CODE=$(grep 'VK_CODE=' "$PROP_FILE" | cut -d'=' -f2)
VERSION_BASE=$(grep 'VERSION_BASE=' "$PROP_FILE" | cut -d'=' -f2)
OLD_TAG=$(grep 'VK_TAG=' "$PROP_FILE" | cut -d'=' -f2)

if [ -z "$CURRENT_CODE" ]; then
    CURRENT_CODE=2
fi
if [ -z "$VERSION_BASE" ]; then
    VERSION_BASE="2.4.0"
fi

NEXT_CODE=$((CURRENT_CODE + 1))
NEXT_TAG=$(printf "VK%02d" "$NEXT_CODE")
FULL_VERSION="v${VERSION_BASE}-${NEXT_TAG}"
OLD_FULL_VERSION="v${VERSION_BASE}-${OLD_TAG}"

cat <<EOF > "$PROP_FILE"
VK_CODE=$NEXT_CODE
VK_TAG=$NEXT_TAG
VERSION_BASE=$VERSION_BASE
EOF

# README.md dosyasını otomatik güncelle
if [ -f "$README_FILE" ]; then
    # Versiyon etiketlerini güncelle
    sed -i "s/${OLD_FULL_VERSION}/${FULL_VERSION}/g" "$README_FILE"
    sed -i "s/${OLD_TAG}/${NEXT_TAG}/g" "$README_FILE"
fi

echo "=========================================="
echo " [StreamFlow] Versiyon Başarıyla Artırıldı! "
echo " Önceki Sürüm : $OLD_TAG ($OLD_FULL_VERSION)"
echo " Yeni Sürüm   : $NEXT_TAG ($FULL_VERSION)"
echo " Versiyon Kodu: $NEXT_CODE"
echo " README.md otomatik senkronize edildi."
echo "=========================================="
