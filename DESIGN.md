---
name: 轻点 LightTap Web
description: 清楚、轻巧、可操作的原生工具语汇。
colors:
  blue: "#2458bd"
  blue-hover: "#1b469c"
  ink: "#182438"
  muted: "#536279"
  page: "#f6f8fc"
  white: "#fff"
  tonal: "#eaf0fa"
  line: "#d9e1ef"
typography:
  display:
    fontFamily: '"Manrope", "Noto Sans SC", sans-serif'
    fontSize: "clamp(48px, 5.2vw, 70px)"
    fontWeight: 800
    lineHeight: 1.22
    letterSpacing: "-0.04em"
  headline:
    fontFamily: '"Manrope", "Noto Sans SC", sans-serif'
    fontSize: "clamp(30px, 3.3vw, 44px)"
    fontWeight: 700
    lineHeight: 1.45
    letterSpacing: "-0.035em"
  title:
    fontFamily: '"Manrope", "Noto Sans SC", sans-serif'
    fontSize: "21px"
    fontWeight: 600
    lineHeight: 1.5
    letterSpacing: "-0.025em"
  body:
    fontFamily: '"Manrope", "Noto Sans SC", sans-serif'
    fontSize: "16px"
    lineHeight: 1.7
  copy:
    fontFamily: '"Manrope", "Noto Sans SC", sans-serif'
    fontSize: "15px"
    lineHeight: 1.9
  label:
    fontFamily: '"Manrope", "Noto Sans SC", sans-serif'
    fontSize: "12px"
    lineHeight: 1.7
  action:
    fontFamily: '"Manrope", "Noto Sans SC", sans-serif'
    fontSize: "16px"
    fontWeight: 600
    lineHeight: 1.5
rounded:
  compact: "8px"
  control: "10px"
  action: "12px"
  surface: "16px"
  circle: "50%"
spacing:
  xs: "4px"
  sm: "8px"
  md: "12px"
  lg: "16px"
  xl: "24px"
  xxl: "32px"
  section-gap: "48px"
  wide-gap: "64px"
components:
  button-primary:
    backgroundColor: "{colors.blue}"
    textColor: "{colors.white}"
    typography: "{typography.action}"
    rounded: "{rounded.action}"
    padding: "14px 24px"
  button-primary-hover:
    backgroundColor: "{colors.blue-hover}"
  button-inverse:
    backgroundColor: "{colors.white}"
    textColor: "{colors.blue}"
    typography: "{typography.action}"
    rounded: "{rounded.action}"
    padding: "14px 28px"
  button-inverse-hover:
    backgroundColor: "{colors.tonal}"
  button-small:
    backgroundColor: "{colors.blue}"
    textColor: "{colors.white}"
    rounded: "{rounded.action}"
    padding: "11px 16px"
  tonal-surface:
    backgroundColor: "{colors.tonal}"
    rounded: "{rounded.surface}"
  floating-toolbar:
    backgroundColor: "{colors.white}"
    rounded: "{rounded.surface}"
    padding: "8px"
  tool-button:
    backgroundColor: "transparent"
    textColor: "{colors.ink}"
    rounded: "{rounded.control}"
    size: "48px"
  tap-target:
    backgroundColor: "{colors.blue}"
    textColor: "{colors.white}"
    rounded: "{rounded.circle}"
    size: "56px"
  mode-option-selected:
    backgroundColor: "{colors.white}"
    textColor: "{colors.blue}"
    typography: "{typography.label}"
    padding: "6px 16px"
---

# Design System: 轻点 LightTap Web

## Overview

**Creative North Star: "掌心里的点击控制台"**

轻点的网页延续 Android 应用的原生工具感：冷白底、钴蓝操作、浅蓝分组面和柔和圆角。它应像一个可以立即上手的小工具，让普通用户看清内容、辨认控制、理解操作反馈。品牌语气清楚、平静，信息密度适中。

清晰的文字层级与真实应用截图承担说明任务；有意义的动作才使用位移、脉冲或状态变化。此文件描述 website/ 内已经实现的视觉系统，Android 工程的设计依据仍由上层文件维护。具体页面顺序与首屏构图记录在表面简报中，不作为所有后续页面的固定模板。

**Key Characteristics:**
- 钴蓝标记可执行动作、选中状态与重点文字。
- 冷白背景与浅蓝分组面建立安静的层次。
- 中文清楚舒展，拉丁字母与数字采用 Manrope。
- 圆角控制与圆形编号具有可触摸的工具感。
- 交互同时支持指针、触摸、键盘与减少动态效果偏好。

## Colors

配色由一个钴蓝主色和一组略带蓝调的中性色构成；不另设第二品牌强调色。

### Primary
- **操作钴蓝**（`blue`）：主按钮、重点标题、选中状态、编号、计数及大面积品牌背景。
- **按下前的深钴蓝**（`blue-hover`）：蓝色按钮悬停与当前点击目标的反馈。

### Neutral
- **冷白纸面**（`page`）：页面基础背景。
- **纯白控制面**（`white`）：悬浮工具栏、选中分段、说明区和反白下载按钮。
- **浅蓝分组面**（`tonal`）：演示容器、截图衬底、步骤编号底色及轻量悬停。
- **蓝黑正文**（`ink`）：标题、正文和当前数值。
- **灰蓝说明**（`muted`）：辅助文字、默认导航和未选中的控制。
- **轻蓝边界**（`line`）：导航底线、内容分隔和工具栏分隔。

**The Action Blue Rule.** 蓝色必须承担动作、状态、阅读重点或品牌面的明确角色；同一控制中的选中与未选中状态保持可辨。

## Typography

**Display Font:** Manrope、Noto Sans SC，最后回退至 sans-serif。
**Body Font:** 同一字体栈；Manrope 优先覆盖拉丁文字和数字，Noto Sans SC 覆盖中文。字体随站点本地加载，并使用 `font-display: swap`。

**Character:** 字形清楚、圆润而紧凑。标题通过字重与字号建立力量，说明文字以较松行距保持中文阅读舒适；不依赖全大写或额外的小标题前缀。

### Hierarchy
- **Display**：粗字重的大标题；基准值见 frontmatter。窄屏使用 `clamp(42px, 8.5vw, 62px)`、行高（1.32）；极窄屏为（39px），超宽屏为（76px）。
- **Headline**：章节标题，粗字重、平衡换行；移动端按上下文收至（28–34px）。
- **Title**：步骤与组件标题，半粗字重；步骤标题窄屏为（19px）。
- **Body / Copy**：页面根正文与叙述文本分层；说明段落常在 Copy 基础上使用（1.9–1.95）行高，窄屏常收至（13–14px）。
- **Label**：模式、参数、版本与辅助状态。按钮使用独立的 Action 字号与行高，不把小标签字号套到主操作。
- **Numbers**：计数与间隔值使用 `tabular-nums`，避免数值变化时横向跳动；圆形编号使用 Manrope 的半粗／粗字重。

**The Clear Roles Rule.** 标题负责定意，正文负责解释，标签只辅助已明确的控件；每一级都通过字号、字重和间距建立层级。

## Layout

主容器居中，最大宽度（1160px），默认两侧合计留白（96px）。在（1050px）以下改为合计（64px），在（760px）以下为（40px），在（360px）以下为（32px）。布局使用正常文档流；导航并非固定悬浮。

大屏的说明与截图、下载内容、问答采用双栏；使用步骤为三栏。到（760px）断点均变成单栏，步骤编号进入文字左侧的独立窄列。桌面章节留白以（120px）为基准；窄屏通常为（76px），特殊章节保留各自的呼吸空间。frontmatter 中的间距是已复用的刻度，并不意味着所有局部间距必须强行归并到等差网格。

导航在窄屏隐藏中间的章节链接，保留品牌、带可访问名称的源码图标和下载动作。演示场桌面高度（300px），窄屏增加至（350px），超宽屏为（330px），为拖动和底部控制留出空间。模式与速度设置在窄屏换行，速度滑杆独占一行。

文字不依靠固定高度容器截断；组件允许换行。截图保持实际纵横比，手机框按容器宽度约束；浅深色切换只改变应用截图，不切换整个网站主题。

## Elevation & Depth

基础页面通过冷白、纯白与浅蓝的色面以及细分隔线建立层级。阴影只用于确实浮起的工具栏和手机截图框，不给每一段说明都加卡片。

### Shadow Vocabulary
- **悬浮工具栏**（`0 8px 28px #18243814`）：短距离、柔和的工具层。
- **设备展示**（`0 20px 45px #1824381a`）：支撑真实截图的环境阴影；设备边框是展示载体，不是普通内容容器。

**The Quiet Surface Rule.** 普通内容以色面和细线分组；只有浮起的工具或实体展示对象使用环境阴影。

## Shapes

主要操作使用中等圆角（`action`），大分组面与工具栏使用更舒展的圆角（`surface`）；小型图标控制使用（`control`），紧凑辅助面使用（`compact`）。点击目标与步骤编号为完整圆形。控件轮廓以原生、温和、可触摸为目标。

分隔线细而低对比，不把普通说明段落封装成强边框卡片。图标采用真正的线性 SVG，通常为（24px）、描边（1.8px），随控件尺寸缩放。手机外框的圆角与厚边框只用于设备展示，不提升为通用容器规则。

## Components

### Buttons

清楚的主操作，足够的触摸面积。
- **Primary:** 蓝底白字，内边距与圆角见 frontmatter，最小高度（56px）；小版最小高度（44px）。
- **Inverse:** 品牌蓝背景上使用白底蓝字；悬停改为浅蓝。
- **Hover / Active:** 悬停上移（2px），按下回到原位；背景变化（0.2s），位移（0.25s）。
- **Focus:** 键盘焦点为（3px）蓝色描边、外偏移（5px）；蓝色品牌区中的链接改用白色焦点线。
- **Disabled:** 原生 `disabled` 语义，普通控制透明度（0.35）并显示不可用光标；运行中的编号保留完整透明度，以免抹掉当前目标。
- **Text action:** 文字配真实 SVG 箭头，悬停箭头右移（4px）。

### Navigation

横向、克制，主次通过位置与字重区分。桌面章节链接使用灰蓝文字，悬停变蓝；品牌与下载始终清楚可见。窄屏源码入口保留（44px）点击高度和可访问名称。

### Segmented Controls / Range

单选式模式以一块浅蓝底承托，选中项为纯白、蓝字与半粗字重。状态通过 `aria-pressed` 表达；截图选择器采用相同的内嵌分段语汇。间隔使用原生 range 与蓝色 `accent-color`，有显式标签和文本 output，运行时锁定相关设置。

### Containers / Accordion

演示是浅蓝圆角场地，工具栏是白色浮层。常规能力说明与问答以分隔线组织；问答使用原生 `details` / `summary`，展开时标题变蓝，加号旋转为关闭方向，正文保持正常流和舒展行距。

### Signature: 点击控制台

悬浮栏包含拖动柄、播放／停止、添加、删除与重置。桌面图标按钮为（48px）方形；窄屏保持（44px）宽、（46px）高，极窄屏宽（42px）。编号圆点桌面直径（56px）、窄屏（48px），以虚线路径说明顺序；当前点变深蓝并产生一次向外淡出的圆环。

工具栏与编号支持拖动，方向键每次移动（12px），并被约束在可操作区域内。演示最多显示十个点，每次运行五轮；播放变停止、计数与实时状态文字同步更新。运行时不能编辑点位／模式／间隔；可随时停止或重置。场地离开视口或页面进入后台时停止，不自动续跑。网页模拟的边界以明文保留。

默认缓动为 `cubic-bezier(.16, 1, .3, 1)`。标题仅入场一次（0.8s），点击脉冲仅响应当前点（0.55s），模拟指针位置变化（0.24s）。`prefers-reduced-motion: reduce` 关闭所有 CSS 动画、过渡与平滑滚动，仍保留真实状态和计数。

**The Operable Motion Rule.** 动态只辅助理解操作；键盘、文本状态和减少动态效果模式都能完成同一任务。

## Do's and Don'ts

### Do:
- **Do** 让钴蓝承担明确的动作、状态或阅读重点。
- **Do** 复用冷白、纯白和浅蓝的层次及细分隔线。
- **Do** 保持中文正文的舒展行距，并让数值使用等宽数字。
- **Do** 为图标控制提供名称、可见键盘焦点和足够的操作面积。
- **Do** 让动作有可停止的边界，并尊重减少动态效果偏好。
- **Do** 用真实应用截图和明确的网页模拟说明支撑产品理解。

### Don't:
- **Don't** 给普通说明内容普遍添加悬浮阴影或设备外框。
- **Don't** 用小标签字号替代主要操作和正文的阅读层级。
- **Don't** 只靠动画或颜色传达运行状态。
- **Don't** 把截图的深色预览误写成网站已经支持的深色主题。
- **Don't** 把当前页面的内容顺序和首屏构图变成所有页面必须重复的模板。
