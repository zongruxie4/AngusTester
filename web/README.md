AngusTesterWeb
===

- [English Version](README.md)
- [中文版](README_zh.md)

## Project Introduction

**[AngusTester](https://www.xcan.cloud)** application is developed using a modern architecture with a separation of frontend and backend
technologies. This separation design significantly enhances the application's flexibility, scalability, and
maintainability.

The current project is the web frontend of AngusTester, aiming to provide a user-friendly interface that seamlessly
interacts with the backend APIs.

For detailed information about the backend project and its technology stack, please refer to the *
*[AngusTester](https://github.com/xcancloud/AngusTester.git)** documentation.

Below is the main technology stack for the AngusTester frontend web project:

| **Technology**         | **Version**        | **Description**                                                                                                                       |
|------------------------|--------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| Runtime Engine         | Node 14+           | Provides a JavaScript runtime environment.                                                                                            |
| Package Manager        | NPM 6+             | A package management tool for managing project dependencies and build tools.                                                          |
| Enterprise Framework   | Vue 3.3.2          | A modern JavaScript framework that supports component-based development, suitable for building user interfaces.                       |
| Open-source UI Library | Ant Design 3.2.20  | An enterprise-level UI design language based on open-source projects, providing a rich component library.                             |
| Custom UI Library      | Angus Design 1.0.0 | A UI component library customized for project requirements, offering consistent styles and functionality.                             |
| Component Styles       | Tailwind           | A utility-first CSS framework that provides flexible styling customization options.                                                   |
| Project Build Tool     | Vite               | The next-generation front-end build tool that supports fast development and hot module replacement, enhancing development efficiency. |
| Code Analysis          | ESLint             | A static code analysis tool used to identify and report issues in JavaScript code, improving code quality.                            |

## Contribute Code

GitHub Contributor's Guide for Clone, Configure, and Run AngusTesterWeb Project This guide will walk you
through the process of contributing to AngusTesterWeb project hosted on GitHub.

### 1. Clone the Project

1. Go to the [GitHub](https://github.com/xcancloud/AngusTesterWeb.git) project page.
2. Click the "Code" button and copy the HTTPS or SSH URL.
3. Open your terminal and navigate to your desired directory.
4. Run:
   ```bash
   git clone https://github.com/xcancloud/AngusTesterWeb.git
   ```
5. Enter the project directory:
   ```bash
   cd AngusTesterWeb
   ```

### 2. Run Project

1. Use the following command to configure NPM to use XCan registry:
   ```bash
   npm set registry http://nexus.xcan.work/repository/xcan-npm-public/
   ```
2. In the project directory, use the following command to install all the dependencies required by the project:
   ```bash
   npm install
   ```
3. After the installation is complete, you can start the development server. Use the following command:
   ```bash
   npm run dev
   ```

   Once the development server is running, it will be accessible at http://localhost:80. Please open this
   address in your browser to view and verify the project.

### 3. Make Changes and Contribute

1. Create a new branch:
   ```
   git checkout -b dev/your-feature-name
   ```
2. Make your code changes.
3. Commit your changes:
   ```bash
   git add .
   git commit -m "Describe your changes"
   ```
4. Push to GitHub:
   ```bash
   git push origin dev/your-feature-name
   ```
5. Go to the GitHub project page and create a Pull Request.

## Feedback

Please send your feedback email to: `feedback@xcan.cloud`.

Online ticket feedback address: [https://www.xcan.cloud/support](https://www.xcan.cloud/support)

Thank you again for your support! We especially appreciate your sharing and forwarding.

Sincerely grateful!

The AngusTester Team!
