param(
    [string]$PastaBackup = (Join-Path $HOME "Documents\Backups-Restaurante"),

    [string]$MySqlDump = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe",

    [string]$LoginPath = "restaurante-backup",

    [ValidateRange(1, 365)]
    [int]$DiasRetencao = 7
)

$ErrorActionPreference = "Stop"

New-Item `
    -ItemType Directory `
    -Path $PastaBackup `
    -Force | Out-Null

$arquivoLog = Join-Path $PastaBackup "backup.log"

function Registrar-Log {
    param(
        [string]$Nivel,
        [string]$Mensagem
    )

    $dataHora = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    $linha = "[$dataHora] [$Nivel] $Mensagem"

    Add-Content `
        -LiteralPath $arquivoLog `
        -Value $linha `
        -Encoding UTF8

    Write-Host $linha
}

$dataBackup = Get-Date -Format "yyyyMMdd-HHmmss-fff"

$arquivoBackup = Join-Path `
    $PastaBackup `
    "restaurante-$dataBackup.sql"

$arquivoTemporario = "$arquivoBackup.tmp"

$arquivoErros = "$arquivoBackup.err"

try {

    Registrar-Log "INFO" "Iniciando backup do banco restaurante."

    if (-not (Test-Path -LiteralPath $MySqlDump -PathType Leaf)) {
        throw "Executavel mysqldump nao encontrado."
    }

    $argumentos = @(
        "--login-path=$LoginPath"
        "--single-transaction"
        "--quick"
        "--no-tablespaces"
        "--set-gtid-purged=OFF"
        "--default-character-set=utf8mb4"
        "--result-file=$arquivoTemporario"
        "restaurante"
    )

    $preferenciaAnterior = $ErrorActionPreference

    try {

        $ErrorActionPreference = "Continue"

        & $MySqlDump @argumentos 2> $arquivoErros

        $codigoSaida = $LASTEXITCODE

    } finally {

        $ErrorActionPreference = $preferenciaAnterior

    }

    if ($codigoSaida -ne 0) {

        $detalhes = ""

        if (Test-Path -LiteralPath $arquivoErros) {
            $detalhes = Get-Content `
                -LiteralPath $arquivoErros `
                -Raw
        }

        throw "mysqldump retornou codigo $codigoSaida. $detalhes"
    }

    if (-not (Test-Path -LiteralPath $arquivoTemporario)) {
        throw "Arquivo de backup nao foi criado."
    }

    $tamanho = (Get-Item -LiteralPath $arquivoTemporario).Length

    if ($tamanho -le 0) {
        throw "Arquivo de backup vazio."
    }

    Move-Item `
        -LiteralPath $arquivoTemporario `
        -Destination $arquivoBackup

    Registrar-Log "SUCESSO" "Backup criado: $arquivoBackup"

    Registrar-Log "INFO" "Tamanho: $tamanho bytes."

} catch {

    Registrar-Log "ERRO" $_.Exception.Message

    exit 1

} finally {

    if (Test-Path -LiteralPath $arquivoTemporario) {
        Remove-Item `
            -LiteralPath $arquivoTemporario `
            -Force `
            -ErrorAction SilentlyContinue
    }

    if (Test-Path -LiteralPath $arquivoErros) {
        Remove-Item `
            -LiteralPath $arquivoErros `
            -Force `
            -ErrorAction SilentlyContinue
    }
}

try {

    $dataLimite = (Get-Date).AddDays(-$DiasRetencao)

    $backupsAntigos = @(
        Get-ChildItem `
            -LiteralPath $PastaBackup `
            -Filter "restaurante-*.sql" `
            -File |
        Where-Object {
            $_.Name -match '^restaurante-\d{8}-\d{6}-\d{3}\.sql$' -and
            $_.LastWriteTime -lt $dataLimite -and
            $_.FullName -ne $arquivoBackup
        }
    )

    foreach ($backupAntigo in $backupsAntigos) {

        Remove-Item `
            -LiteralPath $backupAntigo.FullName `
            -Force

        Registrar-Log `
            "INFO" `
            "Backup antigo removido: $($backupAntigo.Name)"
    }

    Registrar-Log "INFO" "Rotina de backup finalizada."

} catch {

    Registrar-Log "ERRO" "Backup criado, mas houve falha na limpeza: $($_.Exception.Message)"

    exit 2
}

exit 0