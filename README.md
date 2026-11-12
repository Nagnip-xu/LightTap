# 轻点官网

中文产品介绍页，面向了解功能、体验操作并下载 APK 的普通用户。线上地址：<https://nagnip-xu.github.io/LightTap/>。

## 本地预览

在项目根目录执行（需要 Node.js 18 或以上）：

```powershell
node website/serve.mjs
```

打开 <http://localhost:4173>。停止预览时按 `Ctrl+C`。也可以用任意静态文件服务器以 `website/` 为根目录提供页面。

## 内容与交互

- 首屏下载入口与网页连点演示。
- 演示支持单点／多点、拖动位置和工具栏、调整间隔、添加／删除目标、手动停止及重置；每次最多完成 5 轮。
- 演示只更新网页内的计数，不会操作桌面或手机；离开可见区域或切换标签页会停止。
- 真实应用截图、浅深色预览、上手步骤、下载区域和可展开问答。
- 无前端框架、打包工具或运行时第三方依赖。字体和截图均为本地资源。

## 维护

页面为静态 HTML、CSS 和 JavaScript；`assets/` 内包含截图、应用图标及字体。字体许可证与字体文件一并保留。

修改版本时，同步更新 `index.html` 中的版本、文件体积和 GitHub 下载地址。当前 APK 对应仓库发行版 1.0.4。

所有本地资源使用相对路径，适配 GitHub Pages 的项目子路径。官网发布分支为仓库的 `gh-pages`，其根目录内容由本目录同步而来。

## 更新线上官网

先在 `main` 分支修改并检查 `website/`，再在项目根目录执行：

```powershell
git add README.md website
git commit -m "更新官网"
git push origin main
git subtree split --prefix website -b gh-pages
git push origin gh-pages
git branch -D gh-pages
```

GitHub Pages 会从 `gh-pages` 分支根目录提供静态文件。发布后等待几分钟，再打开线上地址确认资源和下载链接。

首次配置时，在仓库 **Settings → Pages → Build and deployment** 选择 **Deploy from a branch**，分支设为 `gh-pages`、目录设为 `/ (root)`。本项目已完成该配置；`.nojekyll` 让 GitHub 直接提供静态页面。

## 已完成的检查

- Chrome 中验证 5 轮结束、停止后不再计数、单点／多点切换、最多 10 个目标、目标与工具栏拖动、目标方向键移动。
- 验证浅深色截图切换、问答触控与键盘展开、手机停止操作、减少动态效果设置，以及关闭 JavaScript 后的说明。
- 320、390、768、1440 像素宽度无横向溢出；桌面和手机截图均已复核。
- 页面本地链接、图片、字体许可证和设计约定已核对。Impeccable 独立完成度审阅结论为 `ship`，无待修复项。
