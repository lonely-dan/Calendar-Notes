# 拾光日历 · Android 日历备忘录

一个以“可点击月历”为入口的本地备忘录 App：选择日期，查看当天的记录；新建记录时可以修改日期，保存后自动回到所选日期并高亮当天备忘录。

本项目为《移动应用开发》课程设计，使用 **Java + Android SDK + SQLite** 完成，不依赖后端、不申请网络权限，所有数据保存在本机。

## 一、功能一览

- 简洁首页：只保留一个“记录事件”主按钮，底部固定展示班级、学号、姓名。
- 可点击月历：GridView 自定义 7 × 6 日期格，支持上/下月、回到今天、相邻月份日期点击。
- 当日记录：ListView 显示选中日期的备忘录，日期下方小圆点提示当天有记录。
- 新建 / 编辑 / 删除：标题、内容、日期、分类、重要程度、是否显示圆点。
- 日期选择：DatePickerDialog 修改日期，保存后自动定位到新日期。
- 分类筛选：Spinner 选择“全部/学习/生活/工作/其他”，CheckBox“只看重要”。
- 节日说明：国庆、春节、中秋、重阳等日期显示金色；选中后显示节日名称与一句介绍。
- 本地持久化：SQLiteOpenHelper + DAO，重启 App 后数据保留。
- 细节体验：空状态、删除确认、未保存修改提醒、页面切换动画、刘海屏/系统栏适配。

## 二、页面流程

```text
HomeActivity（首页）
    │  Intent: EXTRA_SELECTED_DATE = 今天
    ▼
CalendarActivity（日历记事本）
    │  Intent: EXTRA_SELECTED_DATE + EXTRA_MEMO_ID
    ▼
MemoEditActivity（记录本）
    │  setResult(RESULT_OK): EXTRA_SELECTED_DATE = 最终日期
    ▼
CalendarActivity 定位并高亮该日期，刷新当日列表
```

## 三、课程要求对照

| 要求 | 实现 |
| --- | --- |
| 至少 2 个 Activity、页面跳转 | 3 个 Activity：Home / Calendar / MemoEdit |
| 至少 1 次 Intent 传值 | 首页传日期、日历传日期和备忘录 id、编辑页 result 回传日期 |
| TextView / EditText / Button 交互 | 日期、标题、内容、保存按钮等完整交互 |
| 所有页面静态写死姓名、学号 | `view_identity_footer.xml` + `strings.xml`，三个页面均 include，无编辑入口 |
| 至少 4 种备选控件 | ImageButton、RadioGroup、CheckBox、Spinner、ListView、GridView，共 6 种 |

## 四、开发环境

- Android Studio（支持 AGP 9.3 的版本）或命令行 Gradle
- JDK 17
- Gradle 9.5.0（已包含 Wrapper）
- Android Gradle Plugin 9.3.0
- compileSdk / targetSdk：37；minSdk：24
- 验证设备：Android 15（API 35）x86_64 模拟器，1080 × 2340

## 五、构建与运行

```powershell
# 在项目根目录
.\gradlew.bat :app:assembleDebug
# 或构建已签名的 release APK
.\gradlew.bat :app:assembleRelease
```

> 命令行构建前请确认已安装 JDK 17 与 Android SDK（compileSdk 37）。
> 如未配置 `ANDROID_HOME`，在项目根目录新建 `local.properties`：
> `sdk.dir=C:/Users/你的用户名/AppData/Local/Android/Sdk`。
> 用 Android Studio 打开工程时会自动生成该文件。
>
> 工程已开启 `android.overridePathCheck=true`，可直接放在“班级-学号-姓名”这类含中文的提交目录下构建。

APK 输出：

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

安装：

```powershell
adb install -r app\build\outputs\apk\release\app-release.apk
```

> `keystore.properties` 与 `shiguang-release.jks` 仅用于课程演示，请勿用于生产环境。

## 六、替换本人信息

所有页面的身份信息都来自 `app/src/main/res/values/strings.xml`，没有运行时编辑入口。当前使用本人信息：

```text
班级：计算机241　学号：2404020123　姓名：张凌昊
```

在项目根目录执行：

```powershell
.\tools\set_identity.ps1 -Class "你的班级" -StudentId "你的学号" -Name "你的姓名"
.\gradlew.bat :app:assembleRelease
```

脚本只会修改字符串资源，不会引入任何可编辑控件。

## 七、目录结构

```text
app/src/main/java/com/example/calendarmemo/
├── HomeActivity.java              首页
├── CalendarActivity.java          日历记事本
├── MemoEditActivity.java          新建/编辑记录
├── adapter/
│   ├── CalendarDayAdapter.java    月历 GridView 适配器
│   └── MemoListAdapter.java       当日记录 ListView 适配器
├── db/
│   ├── MemoDbHelper.java          SQLiteOpenHelper
│   └── MemoDao.java               DAO
├── model/Memo.java                数据模型
└── util/
    ├── DateUtils.java             日期计算与 42 格月历
    ├── CategoryUtils.java         分类颜色
    └── UiUtils.java               沉浸式/刘海屏/键盘工具
```

## 八、设计说明

视觉上使用奶油色背景、深青绿主色与珊瑚橙强调色，首页用一枚倾斜的“撕页日历”作为视觉锚点，避免工具型 App 的冰冷感。月历使用真实的 7 × 6 网格，日期格同时表达“今天 / 选中 / 非本月 / 有记录”四种状态。

更完整的需求分析、数据库设计、关键代码、测试用例与问题解决记录见 Word 文档《拾光日历-Android日历备忘录-设计开发文档.docx》。

## 九、GitHub

仓库地址：

```text
https://github.com/lonely-dan/Calendar-Notes
```

发布步骤见 `docs/GitHub发布说明.md`。
