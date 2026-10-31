# GitHub 公开发布说明

> 仓库已创建：<https://github.com/lonely-dan/Calendar-Notes>，当前为 **Public**、默认分支 `main`。
> 本说明用于把本地源码推送上去，并在提交前做最后检查。仓库为空时直接按“方式 A”执行即可。

## 一、确认仓库信息

- 仓库地址：<https://github.com/lonely-dan/Calendar-Notes>
- 可见性：Public
- 默认分支：`main`
- 当前状态：已创建，尚未推送源码

## 二、上传源码

### 方式 A：命令行 Git（推荐）

```powershell
cd "01-源码\ShiguangCalendar"

git init
git add .
git commit -m "feat: 拾光日历 Android 日历备忘录课程设计"
git branch -M main
git remote add origin https://github.com/lonely-dan/Calendar-Notes.git
git push -u origin main
```

如果提示仓库已有内容：

```powershell
git pull --rebase origin main
git push -u origin main
```

### 方式 B：GitHub Desktop / Android Studio

1. 用 GitHub Desktop 添加本地仓库 `01-源码\ShiguangCalendar`。
2. Commit 后点击 **Publish repository**，仓库选择 `Calendar-Notes`，不要勾选 “Keep this code private”。
3. 或用 Android Studio 打开工程，`Git → GitHub → Share Project on GitHub`，选择 Public。

## 三、检查公开状态

在未登录的浏览器（或隐私窗口）打开：

```text
https://github.com/lonely-dan/Calendar-Notes
```

能直接看到 README 和源码，说明公开成功。

## 四、文档与 README 中的链接

文档 **9.5 GitHub 公开仓库**、文档最后一页、根目录 `README.txt` 和源码 `README.md` 已经写入：

```text
https://github.com/lonely-dan/Calendar-Notes
```

如果以后更换仓库，需要同步修改以上位置并重新生成 Word 文档。

## 五、APK / 视频 / 文档放哪里

推荐使用 GitHub **Releases**：

1. 仓库右侧 **Releases → Create a new release**。
2. Tag 填 `v1.0`，标题填 `拾光日历 v1.0`。
3. 上传：
   - `拾光日历-v1.0.apk`
   - `拾光日历-Android日历备忘录-设计开发文档.docx`
   - `拾光日历-课程设计演示.mp4`
4. 点击 **Publish release**。

这样源码仓库保持轻量，APK、Word、视频仍可从公开 Release 下载，链接也更稳定。

## 六、提交前检查

- [ ] 仓库为 Public，未登录可访问。
- [ ] `main` 分支已出现 README、`app/`、`gradle/` 与 `gradlew.bat`。
- [ ] 文档 9.5 节、最后一页、根目录 `README.txt` 均为实际链接。
- [ ] 源码中的班级、学号、姓名已写入（`计算机241 / 2404020123 / 张凌昊`）。
- [ ] 重新构建的 APK 与视频中的身份信息一致。
- [ ] 压缩包按“班级-学号-姓名”命名。
