param(
    [ValidateSet("bcp", "steam", "pair", "income", "rejected", "clear")]
    [string]$Scenario = "pair",
    [ValidatePattern('^[0-9]+(\.[0-9]{1,2})?$')]
    [string]$Amount = "12.34",
    [string]$Serial
)
$ErrorActionPreference = "Stop"
$adbPath = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (!(Test-Path -LiteralPath $adbPath)) { $adbPath = (Get-Command adb -ErrorAction Stop).Source }
$deviceArgs = @()
if ($Serial) { $deviceArgs = @("-s", $Serial) }
& $adbPath @deviceArgs shell am broadcast -n com.devpaul.pagocontrol/.notifications.SimulatePaymentReceiver --es scenario $Scenario --es amount $Amount
if ($LASTEXITCODE -ne 0) { throw "No se pudo enviar la simulacion. Instala la version debug y conecta el celular." }