AngusTesterWeb
===

- [English Version](README.md)
- [中文版](README_zh.md)

## 项目介绍

**[AngusTester](https://www.xcan.cloud)** 应用采用现代架构，前后端分离的技术方案进行开发。这种分离设计使得应用在灵活性、可扩展性和可维护性方面得到了显著提升。

当前项目是AngusTester的Web前端，前端旨在提供用户友好的界面，与后端 API 无缝交互。

关于后端项目及其技术栈的详细信息，请参考 **[AngusTester](https://github.com/xcancloud/AngusTester.git)** 后端项目文档。

以下是AngusTester前端项目主要技术栈：

| **技术**   | **版本**             | **说明**                                     |
|----------|--------------------|--------------------------------------------|
| 运行引擎     | Node 14+           | 提供JavaScript 运行时环境。                        |
| 包管理      | NPM 6+             | 包管理工具，用于管理项目依赖和构建工具。                       |
| 企业级框架    | Vue 3.3.2          | 现代化 JavaScript 框架，支持组件化开发，适合构建用户界面。        |
| 开源 UI 库  | Ant Design 3.2.20  | 基于开源项目的企业级 UI 设计语言，提供丰富的组件库。               |
| 自定义 UI 库 | Angus Design 1.0.0 | 针对项目需求定制的 UI 组件库，提供统一的样式和功能。               |
| 组件样式     | Tailwind           | 功能类优先的 CSS 框架，提供灵活的样式定制方式。                 |
| 项目打包构建   | Vite               | 下一代前端构建工具，支持快速开发和热更新，提升开发效率。               |
| 代码分析     | ESLint             | 静态代码分析工具，用于识别和报告 JavaScript 代码中的问题，提高代码质量。 |

## 贡献代码

GitHub贡献者指南：克隆、配置和运行AngusTesterWeb项目。本指南将引导完成为GitHub上托管的AngusTesterWeb项目做出贡献的过程。

### 1. 克隆项目

1. 前往[GitHub](https://github.com/xcancloud/AngusTester.git)项目页面。
2. 点击"Code"按钮并复制HTTPS或SSH URL。
3. 打开终端并导航到您想要的目录。
4. 运行：
   ```bash
   git clone https://github.com/xcancloud/AngusTesterWeb.git
   ```
5. 进入项目目录：
   ```bash
   cd AngusTesterWeb
   ```

### 2. 运行项目

1. 使用以下命令配置 NPM 使用XCan注册表：
   ```bash
   npm set registry https://nexus.xcan.cloud/repository/xcan-npm-public/
   ```
2. 在项目目录中，使用以下命令安装项目所需的所有依赖：
   ```bash
   npm install
   ```
3. 安装完成后，可以启动开发服务器。使用以下命令：
   ```bash
   npm run dev
   ```

   开发服务器启动后，会在 http://localhost:80 运行。请在浏览器中打开该地址以查看并验证项目。

### 3. 提交代码

1. 创建新分支：
   ```
   git checkout -b dev/your-feature-name
   ```
2. 进行代码更改。
3. 提交更改：
   ```bash
   git add .
   git commit -m "描述您的更改"
   ```
4. 推送到GitHub：
   ```bash
   git push origin dev/your-feature-name
   ```
5. 前往GitHub项目页面并创建`Pull Request`。

## 反馈

请将您的反馈邮件发送至：feedback@xcan.cloud

在线工单反馈地址：[https://www.xcan.cloud/support](https://www.xcan.cloud/support)

再次衷心感谢您的支持！特别感激您的分享和转发。

诚挚致谢！

AngusTester团队！
