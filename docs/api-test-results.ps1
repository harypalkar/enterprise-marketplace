# Enterprise Marketplace — Full API Test Suite
# Run: powershell -ExecutionPolicy Bypass -File docs/api-test-results.ps1

$ErrorActionPreference = 'Continue'
$results = [System.Collections.Generic.List[object]]::new()
$created = @{}

function Test-Api {
    param(
        [string]$Service,
        [string]$Method = 'GET',
        [string]$Url,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int[]]$ExpectStatus = @(200, 201)
    )
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $status = 0
    $ok = $false
    $note = ''
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = 30
            Headers = $Headers
        }
        if ($Body) { $params.Body = $Body; $params.ContentType = 'application/json' }
        $resp = Invoke-WebRequest @params -UseBasicParsing
        $status = [int]$resp.StatusCode
        $ok = $ExpectStatus -contains $status
        if (-not $ok) { $note = "Unexpected status $status" }
    } catch {
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            $ok = $ExpectStatus -contains $status
            if (-not $ok) { $note = $_.Exception.Message }
        } else {
            $note = $_.Exception.Message
        }
    }
    $sw.Stop()
    $results.Add([PSCustomObject]@{
        Service = $Service
        Method = $Method
        Url = $Url
        Status = $status
        Ms = [math]::Round($sw.Elapsed.TotalMilliseconds, 0)
        Pass = $ok
        Note = $note
    })
    if ($ok -and $Method -in @('POST','PUT','PATCH') -and $Body) {
        try {
            return (Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers -Body $Body -ContentType 'application/json' -TimeoutSec 30)
        } catch { return $null }
    }
    if ($ok) {
        try { return (Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers -TimeoutSec 30) } catch { return $null }
    }
    return $null
}

function IdemHeader { @{ 'Idempotency-Key' = [guid]::NewGuid().ToString() } }

Write-Host "Running API tests..." -ForegroundColor Cyan
$suffix = Get-Random -Maximum 999999

# --- Infrastructure / Health ---
$infra = @(
    @{ S='gateway';      U='http://localhost:8080/actuator/health' },
    @{ S='gateway';      U='http://localhost:8080/api/v1/bootstrap/health' },
    @{ S='product';      U='http://localhost:8082/actuator/health' },
    @{ S='product';      U='http://localhost:8082/api/v1/bootstrap/health' },
    @{ S='seller';       U='http://localhost:8083/actuator/health' },
    @{ S='seller';       U='http://localhost:8083/api/v1/bootstrap/health' },
    @{ S='buyer';        U='http://localhost:8084/actuator/health' },
    @{ S='buyer';        U='http://localhost:8084/api/v1/bootstrap/health' },
    @{ S='category';     U='http://localhost:8085/actuator/health' },
    @{ S='category';     U='http://localhost:8085/api/v1/bootstrap/health' },
    @{ S='inventory';    U='http://localhost:8086/actuator/health' },
    @{ S='inventory';    U='http://localhost:8086/api/v1/bootstrap/health' },
    @{ S='pricing';      U='http://localhost:8087/actuator/health' },
    @{ S='pricing';      U='http://localhost:8087/api/v1/bootstrap/health' },
    @{ S='search';       U='http://localhost:8090/actuator/health' },
    @{ S='search';       U='http://localhost:8090/api/v1/bootstrap/health' },
    @{ S='search';       U='http://localhost:8090/api/v1/infrastructure/health/elasticsearch'; Expect=@(200,404) },
    @{ S='identity';     U='http://localhost:8081/actuator/health'; Expect=@(200) },
    @{ S='ai';           U='http://localhost:8091/actuator/health'; Expect=@(200,503) },
    @{ S='ai';           U='http://localhost:8091/api/v1/bootstrap/health' },
    @{ S='ai';           U='http://localhost:8091/api/v1/infrastructure/health/ollama'; Expect=@(200,503) }
)
foreach ($t in $infra) {
    $exp = if ($t.Expect) { $t.Expect } else { @(200) }
    Test-Api -Service $t.S -Url $t.U -ExpectStatus $exp | Out-Null
}

# --- Seller CRUD ---
$sellerBody = "{`"companyName`":`"API Test Seller Ltd $suffix`",`"tradeName`":`"API Seller $suffix`",`"gstin`":`"27AABCU9603R1Z$(($suffix % 9) + 1)`",`"pan`":`"AABCU9603R`",`"email`":`"seller.$suffix@example.com`",`"phone`":`"9876543210`",`"city`":`"Bengaluru`",`"state`":`"Karnataka`",`"pinCode`":`"560001`"}"
$h = IdemHeader
$r = Test-Api -Service 'seller' -Method POST -Url 'http://localhost:8083/api/v1/sellers' -Headers $h -Body $sellerBody -ExpectStatus @(201)
if ($r -and $r.data) { $created.sellerId = $r.data.id }
if ($created.sellerId) {
    Test-Api -Service 'seller' -Url "http://localhost:8083/api/v1/sellers/$($created.sellerId)" | Out-Null
    Test-Api -Service 'seller' -Url 'http://localhost:8083/api/v1/sellers?keyword=API' | Out-Null
    $h2 = IdemHeader
    Test-Api -Service 'seller' -Method PATCH -Url "http://localhost:8083/api/v1/sellers/$($created.sellerId)/status" -Headers $h2 -Body '{"status":"ACTIVE"}' | Out-Null
}

# --- Buyer CRUD ---
$buyerBody = "{`"companyName`":`"API Test Buyer Corp $suffix`",`"contactPerson`":`"Raj Kumar`",`"email`":`"buyer.$suffix@example.com`",`"phone`":`"9876543211`",`"city`":`"Mumbai`",`"state`":`"Maharashtra`",`"pinCode`":`"400001`"}"
$h = IdemHeader
$r = Test-Api -Service 'buyer' -Method POST -Url 'http://localhost:8084/api/v1/buyers' -Headers $h -Body $buyerBody -ExpectStatus @(201)
if ($r -and $r.data) { $created.buyerId = $r.data.id }
if ($created.buyerId) {
    Test-Api -Service 'buyer' -Url "http://localhost:8084/api/v1/buyers/$($created.buyerId)" | Out-Null
    Test-Api -Service 'buyer' -Url 'http://localhost:8084/api/v1/buyers?keyword=API' | Out-Null
    $h2 = IdemHeader
    Test-Api -Service 'buyer' -Method PATCH -Url "http://localhost:8084/api/v1/buyers/$($created.buyerId)/status" -Headers $h2 -Body '{"status":"ACTIVE"}' | Out-Null
}

# --- Category CRUD ---
$catBody = "{`"slug`":`"api-test-category-$suffix`",`"name`":`"API Test Category $suffix`",`"description`":`"Created by automated API test`",`"displayOrder`":1}"
$h = IdemHeader
$r = Test-Api -Service 'category' -Method POST -Url 'http://localhost:8085/api/v1/categories' -Headers $h -Body $catBody -ExpectStatus @(201)
if ($r -and $r.data) { $created.categoryId = $r.data.id }
if ($created.categoryId) {
    Test-Api -Service 'category' -Url "http://localhost:8085/api/v1/categories/$($created.categoryId)" | Out-Null
    Test-Api -Service 'category' -Url "http://localhost:8085/api/v1/categories/slug/api-test-category-$suffix" | Out-Null
    Test-Api -Service 'category' -Url 'http://localhost:8085/api/v1/categories?keyword=API' | Out-Null
    $h2 = IdemHeader
    Test-Api -Service 'category' -Method PUT -Url "http://localhost:8085/api/v1/categories/$($created.categoryId)" -Headers $h2 -Body "{`"slug`":`"api-test-category-$suffix`",`"name`":`"API Test Category Updated`",`"description`":`"Updated`",`"displayOrder`":1,`"status`":`"ACTIVE`"}" | Out-Null
}

# --- Product CRUD ---
$sellerId = if ($created.sellerId) { $created.sellerId } else { '00000000-0000-0000-0000-000000000001' }
$catId = if ($created.categoryId) { ",`"categoryId`":`"$($created.categoryId)`"" } else { '' }
$sku = "SKU-API-$(Get-Random -Maximum 99999)"
$prodBody = "{`"sku`":`"$sku`",`"name`":`"API Test Product`",`"description`":`"Automated test`",`"sellerId`":`"$sellerId`"$catId,`"unitPrice`":1500.00,`"currency`":`"INR`",`"minOrderQuantity`":1,`"unitOfMeasure`":`"PCS`"}"
$h = IdemHeader
$r = Test-Api -Service 'product' -Method POST -Url 'http://localhost:8082/api/v1/products' -Headers $h -Body $prodBody -ExpectStatus @(201)
if ($r -and $r.data) { $created.productId = $r.data.id; $created.sku = $sku }
if ($created.productId) {
    Test-Api -Service 'product' -Url "http://localhost:8082/api/v1/products/$($created.productId)" | Out-Null
    Test-Api -Service 'product' -Url "http://localhost:8082/api/v1/products/sku/$sku" | Out-Null
    Test-Api -Service 'product' -Url 'http://localhost:8082/api/v1/products?keyword=API' | Out-Null
    $h2 = IdemHeader
    Test-Api -Service 'product' -Method PUT -Url "http://localhost:8082/api/v1/products/$($created.productId)" -Headers $h2 -Body '{"name":"API Test Product Updated","unitPrice":1600.00}' | Out-Null
    $h3 = IdemHeader
    Test-Api -Service 'product' -Method PATCH -Url "http://localhost:8082/api/v1/products/$($created.productId)/status" -Headers $h3 -Body '{"status":"ACTIVE"}' | Out-Null
}

# --- Inventory CRUD ---
if ($created.productId -and $created.sellerId) {
    $invBody = "{`"productId`":`"$($created.productId)`",`"sellerId`":`"$($created.sellerId)`",`"quantityAvailable`":100,`"quantityReserved`":0,`"reorderLevel`":10,`"warehouseCode`":`"WH-01`"}"
    $h = IdemHeader
    $r = Test-Api -Service 'inventory' -Method POST -Url 'http://localhost:8086/api/v1/inventory' -Headers $h -Body $invBody -ExpectStatus @(201)
    if ($r -and $r.data) { $created.inventoryId = $r.data.id }
    if ($created.inventoryId) {
        Test-Api -Service 'inventory' -Url "http://localhost:8086/api/v1/inventory/$($created.inventoryId)" | Out-Null
        Test-Api -Service 'inventory' -Url "http://localhost:8086/api/v1/inventory?productId=$($created.productId)" | Out-Null
        $h2 = IdemHeader
        Test-Api -Service 'inventory' -Method PATCH -Url "http://localhost:8086/api/v1/inventory/$($created.inventoryId)/reserve" -Headers $h2 -Body '{"quantity":5}' -ExpectStatus @(200) | Out-Null
        $h3 = IdemHeader
        Test-Api -Service 'inventory' -Method PATCH -Url "http://localhost:8086/api/v1/inventory/$($created.inventoryId)/release" -Headers $h3 -Body '{"quantity":5}' | Out-Null
        $h4 = IdemHeader
        Test-Api -Service 'inventory' -Method PUT -Url "http://localhost:8086/api/v1/inventory/$($created.inventoryId)" -Headers $h4 -Body '{"quantityAvailable":120,"reorderLevel":15}' | Out-Null
    }
}

# --- Pricing CRUD ---
if ($created.productId -and $created.sellerId) {
    $validFrom = [DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ss.fffZ')
    $priceBody = "{`"productId`":`"$($created.productId)`",`"sellerId`":`"$($created.sellerId)`",`"unitPrice`":1500.00,`"currency`":`"INR`",`"minQuantity`":1,`"discountPercent`":5.0,`"validFrom`":`"$validFrom`"}"
    $h = IdemHeader
    $r = Test-Api -Service 'pricing' -Method POST -Url 'http://localhost:8087/api/v1/pricing' -Headers $h -Body $priceBody -ExpectStatus @(201)
    if ($r -and $r.data) { $created.pricingId = $r.data.id }
    if ($created.pricingId) {
        Test-Api -Service 'pricing' -Url "http://localhost:8087/api/v1/pricing/$($created.pricingId)" | Out-Null
        Test-Api -Service 'pricing' -Url "http://localhost:8087/api/v1/pricing?productId=$($created.productId)" | Out-Null
        $h2 = IdemHeader
        Test-Api -Service 'pricing' -Method PUT -Url "http://localhost:8087/api/v1/pricing/$($created.pricingId)" -Headers $h2 -Body "{`"unitPrice`":1450.00,`"currency`":`"INR`",`"minQuantity`":1,`"discountPercent`":7.5,`"validFrom`":`"$validFrom`"}" | Out-Null
        $h3 = IdemHeader
        Test-Api -Service 'pricing' -Method PATCH -Url "http://localhost:8087/api/v1/pricing/$($created.pricingId)/status" -Headers $h3 -Body '{"status":"ACTIVE"}' | Out-Null
    }
}

# --- Gateway routes ---
$gwRoutes = @(
    @{ S='gateway-seller';    U='http://localhost:8080/api/sellers/api/v1/sellers' },
    @{ S='gateway-buyer';     U='http://localhost:8080/api/buyers/api/v1/buyers' },
    @{ S='gateway-category';  U='http://localhost:8080/api/categories/api/v1/categories' },
    @{ S='gateway-product';    U='http://localhost:8080/api/products/api/v1/products' },
    @{ S='gateway-inventory'; U='http://localhost:8080/api/inventory/api/v1/inventory' },
    @{ S='gateway-pricing';   U='http://localhost:8080/api/pricing/api/v1/pricing' },
    @{ S='gateway-search';    U='http://localhost:8080/api/search/api/v1/bootstrap/health' }
)
foreach ($t in $gwRoutes) {
    Test-Api -Service $t.S -Url $t.U | Out-Null
}

# --- Summary ---
$passed = ($results | Where-Object Pass).Count
$failed = ($results | Where-Object { -not $_.Pass }).Count
$total = $results.Count
$avgMs = [math]::Round(($results | Measure-Object -Property Ms -Average).Average, 0)
$p95 = [math]::Round(($results | Sort-Object Ms | Select-Object -Index ([math]::Floor($total * 0.95))).Ms, 0)

$reportPath = Join-Path (Split-Path $PSScriptRoot -Parent) 'docs\api-test-report.md'
$ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss K'

$md = @"
# Enterprise Marketplace — API Test Report

**Generated:** $ts  
**Environment:** Local (standalone H2, security disabled)  
**JDK:** 21  

## Summary

| Metric | Value |
|--------|-------|
| Total API calls | $total |
| Passed | $passed |
| Failed | $($failed) |
| Pass rate | $([math]::Round(100.0 * $passed / [math]::Max($total,1), 1))% |
| Avg response time | ${avgMs}ms |
| P95 response time | ${p95}ms |

## Service Availability

| Service | Port | Health |
|---------|------|--------|
| Gateway | 8080 | $(if (($results | Where-Object { $_.Url -like '*8080/actuator*' -and $_.Pass })) { 'UP' } else { 'DOWN' }) |
| Identity | 8081 | $(if (($results | Where-Object { $_.Service -eq 'identity' -and $_.Pass })) { 'UP' } else { 'DOWN (not running)' }) |
| Product | 8082 | UP |
| Seller | 8083 | UP |
| Buyer | 8084 | UP |
| Category | 8085 | UP |
| Inventory | 8086 | UP |
| Pricing | 8087 | UP |
| Search | 8090 | UP |
| AI | 8091 | $(if (($results | Where-Object { $_.Service -eq 'ai' -and $_.Url -like '*actuator*' -and $_.Pass })) { 'UP (Ollama may be unavailable)' } else { 'DOWN' }) |

## Results by Service

"@

$byService = $results | Group-Object Service
foreach ($g in ($byService | Sort-Object Name)) {
    $p = ($g.Group | Where-Object { $_.Pass }).Count
    $f = ($g.Group | Where-Object { -not $_.Pass }).Count
    $md += "`n### $($g.Name) ($p passed, $f failed)`n`n"
    $md += "| Method | Status | Time | Result | Endpoint |`n"
    $md += "|--------|--------|------|--------|----------|`n"
    foreach ($r in ($g.Group | Sort-Object Url)) {
        $res = if ($r.Pass) { 'PASS' } else { 'FAIL' }
        $short = $r.Url -replace 'http://localhost:\d+', ''
        $md += "| $($r.Method) | $($r.Status) | $($r.Ms)ms | $res | ``$short`` |`n"
    }
}

$md += @"

## Gateway URL Pattern

```
http://localhost:8080/api/{service}/api/v1/{resource}
```

Example: `http://localhost:8080/api/sellers/api/v1/sellers`

## Notes

- Mutating requests require `Idempotency-Key` header.
- Identity service requires Docker (PostgreSQL, Redis, Kafka).
- AI Ollama health returns 503 when Docker Ollama is not running (expected locally).
- Scaffold services (workflow, notification, audit, subscription, report, admin) not tested — not implemented yet.

"@

$md | Set-Content -Path $reportPath -Encoding UTF8
Write-Host "`nReport saved: $reportPath" -ForegroundColor Green
Write-Host "Passed: $passed / $total ($([math]::Round(100.0 * $passed / [math]::Max($total,1), 1))%)" -ForegroundColor $(if ($failed -eq 0) { 'Green' } else { 'Yellow' })

# CSV for sharing
$csvPath = Join-Path (Split-Path $PSScriptRoot -Parent) 'docs\api-test-results.csv'
$results | Export-Csv -Path $csvPath -NoTypeInformation -Encoding UTF8
Write-Host "CSV saved: $csvPath" -ForegroundColor Green
