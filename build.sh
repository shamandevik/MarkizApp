#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
SDK="${ANDROID_SDK_ROOT:-/home/shaman/Documents/Codex/2026-11-08/ns-x20/work/android-sdk}"
BT="$SDK/build-tools/34.0.0"
JDK="${DEVLENS_JDK:-/usr/lib/jvm/java-17-openjdk}"
export JAVA_HOME="$JDK"
export PATH="$JDK/bin:$PATH"
mkdir -p build/classes build/dex signing
"$BT/aapt" package -f -m -J build -M app/src/main/AndroidManifest.xml -S app/src/main/res -A app/src/main/assets -I "$SDK/platforms/android-34/android.jar" -F build/base.apk
find app/src/main/java build/app -name '*.java' > build/sources.txt
javac -encoding UTF-8 -source 8 -target 8 -cp "$SDK/platforms/android-34/android.jar:app/libs/*" -d build/classes @build/sources.txt
jar cf build/classes.jar -C build/classes .
java -cp "${DEVLENS_R8:-tools/r8.jar}" com.android.tools.r8.D8 --min-api 26 --lib "$SDK/platforms/android-34/android.jar" --output build/dex build/classes.jar app/libs/*.jar
cp build/base.apk build/unsigned.apk
(cd build/dex && jar uf ../unsigned.apk classes*.dex)
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk
if [ ! -f signing/devlens-debug.jks ]; then keytool -genkeypair -keystore signing/devlens-debug.jks -storepass android -keypass android -alias devlens -dname 'CN=DevLens Development' -keyalg RSA -keysize 2048 -validity 3650; fi
"$BT/apksigner" sign --ks signing/devlens-debug.jks --ks-pass pass:android --out ../DevLens.apk build/aligned.apk
"$BT/apksigner" verify ../DevLens.apk
printf 'Built: %s/../DevLens.apk\n' "$PWD"
