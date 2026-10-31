param(
    [Parameter(Mandatory = $true)][string]$Class,
    [Parameter(Mandatory = $true)][string]$StudentId,
    [Parameter(Mandatory = $true)][string]$Name
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$stringsPath = Join-Path $root 'app\src\main\res\values\strings.xml'

foreach ($value in @($Class, $StudentId, $Name)) {
    if ([string]::IsNullOrWhiteSpace($value)) {
        throw '班级、学号、姓名都不能为空。'
    }
    if ($value -match '[<>&]') {
        throw '内容中不能包含 <、>、& 等 XML 特殊字符。'
    }
}

$content = [IO.File]::ReadAllText($stringsPath, [Text.Encoding]::UTF8)
$full = "班级：$Class　学号：$StudentId　姓名：$Name"

$content = [regex]::Replace($content,
    '<string name="student_class">.*?</string>',
    "<string name=`"student_class`">$Class</string>")
$content = [regex]::Replace($content,
    '<string name="student_id">.*?</string>',
    "<string name=`"student_id`">$StudentId</string>")
$content = [regex]::Replace($content,
    '<string name="student_name">.*?</string>',
    "<string name=`"student_name`">$Name</string>")
$content = [regex]::Replace($content,
    '<string name="student_identity_full">.*?</string>',
    "<string name=`"student_identity_full`">$full</string>")

[IO.File]::WriteAllText($stringsPath, $content, [Text.UTF8Encoding]::new($false))
Write-Host "已更新学生信息：$full"
Write-Host '请重新运行 .\gradlew.bat :app:assembleRelease 生成新的 APK。'
