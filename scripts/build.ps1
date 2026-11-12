param([switch]$SkipChecks)
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$toolsDir = Join-Path $projectDir '.tools'
$gradleExe = Join-Path $toolsDir 'gradle-8.11.1\bin\gradle.bat'
$sdkDir = Join-Path $toolsDir 'android-sdk'
if (!(Test-Path -LiteralPath $gradleExe) -or !(Test-Path -LiteralPath (Join-Path $sdkDir 'build-tools\35.0.0\apksigner.bat'))) {
    & (Join-Path $PSScriptRoot 'bootstrap.ps1')
    if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) { throw 'Toolchain setup failed.' }
}

# Some Windows environments cannot connect AF_UNIX pipes through the short TEMP path.
# A nonexistent socket directory makes the JDK fall back to TCP; only this build's JVMs are affected.
$originalJavaOptions = $env:JAVA_TOOL_OPTIONS
$env:JAVA_TOOL_OPTIONS = "$originalJavaOptions -Djdk.net.unixdomain.tmpdir=C:/lighttap-no-sockets".Trim()
$originalSigningPassword = $env:LIGHTTAP_KEY_PASSWORD
Push-Location $projectDir
try {
    if ($SkipChecks) { $tasks = @('assembleRelease') }
    else { $tasks = @('testDebugUnitTest', 'lintDebug', 'assembleRelease') }
    & $gradleExe --no-daemon @tasks
    if ($LASTEXITCODE -ne 0) { throw 'Build or checks failed.' }

    $signDir = Join-Path $projectDir '.signing'
    New-Item -ItemType Directory -Force -Path $signDir | Out-Null
    $keyStore = Join-Path $signDir 'lighttap.p12'
    $passwordFile = Join-Path $signDir 'password.txt'
    if (!(Test-Path -LiteralPath $keyStore)) {
        if (!(Test-Path -LiteralPath $passwordFile)) {
            [System.IO.File]::WriteAllText($passwordFile, ([Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')))
        }
        $env:LIGHTTAP_KEY_PASSWORD = [System.IO.File]::ReadAllText($passwordFile).Trim()
        & (Join-Path $env:JAVA_HOME 'bin\keytool.exe') -genkeypair -keystore $keyStore -alias lighttap -storetype PKCS12 -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=LightTap Local, O=LightTap, C=CN' -storepass:env LIGHTTAP_KEY_PASSWORD -keypass:env LIGHTTAP_KEY_PASSWORD
        if ($LASTEXITCODE -ne 0) { throw 'Signing key generation failed.' }
    }
    $env:LIGHTTAP_KEY_PASSWORD = [System.IO.File]::ReadAllText($passwordFile).Trim()
    $deliveryDir = Join-Path $projectDir 'delivery'
    New-Item -ItemType Directory -Force -Path $deliveryDir | Out-Null
    $unsigned = Join-Path $projectDir 'app\build\outputs\apk\release\app-release-unsigned.apk'
    $apkMetadata = Get-Content -LiteralPath (Join-Path $projectDir 'app\build\outputs\apk\release\output-metadata.json') -Raw | ConvertFrom-Json
    $version = $apkMetadata.elements[0].versionName
    if ($version -notmatch '^\d+\.\d+\.\d+$') { throw 'Invalid APK version metadata.' }
    $aligned = Join-Path $toolsDir 'lighttap-aligned.apk'
    $apkName = "LightTap-$version.apk"
    $outputApk = Join-Path $deliveryDir $apkName
    & (Join-Path $sdkDir 'build-tools\35.0.0\zipalign.exe') -f -p 4 $unsigned $aligned
    if ($LASTEXITCODE -ne 0) { throw 'APK alignment failed.' }
    & (Join-Path $sdkDir 'build-tools\35.0.0\apksigner.bat') sign --ks $keyStore --ks-key-alias lighttap --ks-pass env:LIGHTTAP_KEY_PASSWORD --out $outputApk $aligned
    if ($LASTEXITCODE -ne 0) { throw 'APK signing failed.' }
    & (Join-Path $sdkDir 'build-tools\35.0.0\apksigner.bat') verify --verbose $outputApk
    if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed.' }
    $hash = (Get-FileHash -LiteralPath $outputApk -Algorithm SHA256).Hash.ToLowerInvariant()
    [System.IO.File]::WriteAllText((Join-Path $deliveryDir 'SHA256.txt'), "$hash  $apkName`n")
    Write-Output "Installable APK: $outputApk"
    Write-Output ('Size: {0:N0} bytes' -f (Get-Item -LiteralPath $outputApk).Length)
} finally {
    Pop-Location
    $env:JAVA_TOOL_OPTIONS = $originalJavaOptions
    $env:LIGHTTAP_KEY_PASSWORD = $originalSigningPassword
}
