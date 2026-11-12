param([switch]$Build)
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$projectDir = Split-Path -Parent $PSScriptRoot
$toolsDir = Join-Path $projectDir '.tools'
$sdkDir = Join-Path $toolsDir 'android-sdk'
$gradleDir = Join-Path $toolsDir 'gradle-8.11.1'
New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null

function Get-Archive($Url, $Path) {
    if (!(Test-Path -LiteralPath $Path)) {
        Invoke-WebRequest -Uri $Url -OutFile $Path -MaximumRedirection 8
    }
}

if (!(Test-Path -LiteralPath (Join-Path $gradleDir 'bin\gradle.bat'))) {
    Write-Output 'Downloading Gradle 8.11.1...'
    $gradleZip = Join-Path $toolsDir 'gradle-8.11.1-bin.zip'
    Get-Archive 'https://services.gradle.org/distributions/gradle-8.11.1-bin.zip' $gradleZip
    Expand-Archive -LiteralPath $gradleZip -DestinationPath $toolsDir -Force
}
$sdkManager = Join-Path $sdkDir 'cmdline-tools\latest\bin\sdkmanager.bat'
if (!(Test-Path -LiteralPath $sdkManager)) {
    Write-Output 'Downloading Android command-line tools...'
    $sdkZip = Join-Path $toolsDir 'commandlinetools-win.zip'
    Get-Archive 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' $sdkZip
    $extractDir = Join-Path $toolsDir 'sdk-extract'
    Expand-Archive -LiteralPath $sdkZip -DestinationPath $extractDir -Force
    New-Item -ItemType Directory -Force -Path (Join-Path $sdkDir 'cmdline-tools') | Out-Null
    Move-Item -LiteralPath (Join-Path $extractDir 'cmdline-tools') -Destination (Join-Path $sdkDir 'cmdline-tools\latest')
}
Write-Output 'Installing Android SDK 35 and platform tools...'
1..100 | ForEach-Object { 'y' } | & $sdkManager "--sdk_root=$sdkDir" --licenses | Out-File (Join-Path $toolsDir 'sdk-licenses.log') -Encoding utf8
if ($LASTEXITCODE -ne 0) { throw 'Android SDK license step failed.' }
& $sdkManager "--sdk_root=$sdkDir" 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools' *> (Join-Path $toolsDir 'sdk-install.log')
if ($LASTEXITCODE -ne 0) { throw 'Android SDK installation failed. See .tools/sdk-install.log.' }
$sdkProperty = $sdkDir.Replace('\', '/').Replace(':', '\:')
[System.IO.File]::WriteAllText((Join-Path $projectDir 'local.properties'), "sdk.dir=$sdkProperty`n", [System.Text.UTF8Encoding]::new($false))
Write-Output "SDK ready: $sdkDir"
if ($Build) {
    & (Join-Path $PSScriptRoot 'build.ps1')
}
