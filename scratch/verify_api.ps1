# verify_api.ps1
# Script to verify Step 2 (Auth), Step 3 (Workspace), Step 4 (Documents), Step 5 (Pipeline)

$BaseUrl = "http://localhost:8080/api/v1"

Write-Host "--- 1. Registering user 'testuser' ---" -ForegroundColor Cyan
$regBody = @{
    username = "testuser"
    email = "testuser@example.com"
    password = "Password123!"
} | ConvertTo-Json

try {
    $regRes = Invoke-RestMethod -Uri "$BaseUrl/auth/register" -Method Post -ContentType "application/json" -Body $regBody
    Write-Host "Registration response: $($regRes | ConvertTo-Json -Depth 5)" -ForegroundColor Green
} catch {
    Write-Host "Registration failed or user already exists: $_" -ForegroundColor Yellow
}

Write-Host "--- 2. Logging in 'testuser' ---" -ForegroundColor Cyan
$loginBody = @{
    username = "testuser"
    password = "Password123!"
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$token = $loginRes.data.token
Write-Host "JWT Token: $token" -ForegroundColor Green

$headers = @{
    Authorization = "Bearer $token"
}

Write-Host "--- 3. Creating Workspace ---" -ForegroundColor Cyan
$wsBody = @{
    name = "Lumora Workspace"
    description = "Workspace for manual verification"
} | ConvertTo-Json

$wsRes = Invoke-RestMethod -Uri "$BaseUrl/workspaces" -Method Post -ContentType "application/json" -Body $wsBody -Headers $headers
$workspaceId = $wsRes.data.id
Write-Host "Created Workspace ID: $workspaceId" -ForegroundColor Green

Write-Host "--- 4. Uploading PDF, DOCX, TXT ---" -ForegroundColor Cyan

function Upload-File {
    param(
        [string]$FilePath,
        [string]$WorkspaceId,
        [HashTable]$Headers
    )

    $fileName = [System.IO.Path]::GetFileName($FilePath)
    $fileBytes = [System.IO.File]::ReadAllBytes($FilePath)
    
    $LF = "`r`n"
    $boundary = [System.Guid]::NewGuid().ToString()
    
    $bodyLines = (
        "--$boundary",
        "Content-Disposition: form-data; name=`"workspaceId`"",
        "",
        "$WorkspaceId",
        "--$boundary",
        "Content-Disposition: form-data; name=`"file``; filename=`"$fileName`"",
        "Content-Type: application/octet-stream",
        "",
        [System.Text.Encoding]::GetEncoding("ISO-8859-1").GetString($fileBytes),
        "--$boundary--"
    ) -join $LF
    
    $multipartHeaders = $Headers.Clone()
    $multipartHeaders.Add("Content-Type", "multipart/form-data; boundary=$boundary")

    Write-Host "Uploading $fileName..."
    $res = Invoke-RestMethod -Uri "$BaseUrl/documents" -Method Post -Headers $multipartHeaders -Body $bodyLines
    return $res
}

$txtUpload = Upload-File -FilePath "c:\Users\HP\PA-NET-SCOPE\Your-OWN-AI\scratch\test.txt" -WorkspaceId $workspaceId -Headers $headers
Write-Host "TXT Upload Response: $($txtUpload.message)" -ForegroundColor Green

$docxUpload = Upload-File -FilePath "c:\Users\HP\PA-NET-SCOPE\Your-OWN-AI\scratch\test.docx" -WorkspaceId $workspaceId -Headers $headers
Write-Host "DOCX Upload Response: $($docxUpload.message)" -ForegroundColor Green

$pdfUpload = Upload-File -FilePath "c:\Users\HP\PA-NET-SCOPE\Your-OWN-AI\scratch\test.pdf" -WorkspaceId $workspaceId -Headers $headers
Write-Host "PDF Upload Response: $($pdfUpload.message)" -ForegroundColor Green

Write-Host "--- 5. Polling processing status ---" -ForegroundColor Cyan
$allProcessed = $false
$attempts = 0
$maxAttempts = 15

while (-not $allProcessed -and $attempts -lt $maxAttempts) {
    Start-Sleep -Seconds 5
    $attempts++
    
    $docsRes = Invoke-RestMethod -Uri "$BaseUrl/documents?workspaceId=$workspaceId" -Method Get -Headers $headers
    $docs = $docsRes.data
    
    $allProcessed = $true
    Write-Host "Attempt $attempts of $maxAttempts" -ForegroundColor Yellow
    foreach ($doc in $docs) {
        Write-Host "  Document: $($doc.title) | Status: $($doc.processingStatus) | Chunks: $($doc.totalChunks) | Reason: $($doc.failureReason)"
        if ($doc.processingStatus -ne "PROCESSED") {
            $allProcessed = $false
        }
    }
}

if ($allProcessed) {
    Write-Host "SUCCESS: All documents successfully processed and indexed!" -ForegroundColor Green
} else {
    Write-Host "TIMEOUT or FAILURE: One or more documents failed to process." -ForegroundColor Red
}
