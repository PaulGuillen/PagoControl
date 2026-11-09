param([switch]$Instrumented)

$ErrorActionPreference = 'Stop'
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Push-Location $projectRoot
try {
    & .\gradlew.bat :core-domain:test :core-data:testDebugUnitTest :features:auth:testDebugUnitTest :features:home:testDebugUnitTest :features:splash:testDebugUnitTest :features:onboarding:testDebugUnitTest :app:lintDevDebug :app:assembleDevDebug :app:compileDevDebugAndroidTestKotlin --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'La validación Android falló.' }
    if ($Instrumented) {
        & .\gradlew.bat :app:connectedDevDebugAndroidTest --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Las pruebas instrumentadas fallaron.' }
    }
    & npm --prefix firebase/functions ci
    if ($LASTEXITCODE -ne 0) { throw 'No se pudieron instalar las dependencias Functions.' }
    & npm --prefix firebase/functions test
    if ($LASTEXITCODE -ne 0) { throw 'Las pruebas Functions fallaron.' }
    & npm --prefix firebase/rules-tests ci
    if ($LASTEXITCODE -ne 0) { throw 'No se pudieron instalar las dependencias de reglas.' }
    & npm --prefix firebase/rules-tests test
    if ($LASTEXITCODE -ne 0) { throw 'Las pruebas de reglas fallaron.' }
} finally {
    Pop-Location
}
