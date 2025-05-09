# CodeBricks

![status](https://img.shields.io/badge/status-in--development-yellow)
![build](https://img.shields.io/badge/build-passing-brightgreen)
![license](https://img.shields.io/badge/license-MIT-blue)

> 🇬🇧 This file is in English. [Русская версия здесь](./README.md)

**CodeBricks** is a visual algorithm interpreter based on draggable code blocks.  
It allows you to create simple programs without writing code — using variables, expressions, conditionals, and loops.  
The project is designed to help learn the basics of algorithmic thinking.


## 📜 Content
- [🌐 Technologies](#-technologies)
- [☕ Getting Started](#-getting-started)
- [🛠️ Development](#-development)
- [📦 Deploy и CI/CD](#-deploy--cicd)
- [✅ Completed](#-completed)
- [🔜 In Progress / To Do](#-in-progress--to-do)
- [👥 Project Team](#-project-team)
- [🔍 Sources](#-sources)


## 🌐 Technologies
- **Kotlin** — main development language
- **Jetpack Compose** — UI framework for Android
- **Android SDK** — standard toolkit for Android applications
- **Gradle** — build system and dependency manager


## ☕ Getting Started
### Requirements
- Android Studio Meerkat (2024.3.1) or newer
- Android SDK 33+ (tested with API 35)
- JDK 17+ (tested with Java 21 LTS)
> The app may work on Android 5.0+ (API 21+), but API 29+ is recommended for stable Compose behavior.


## 🛠️ Development
### Installation
1. Clone the repository:
    ```bash
    clone https://github.com/ya-Bell/CodeBricks.git
    cd CodeBricks
    ```
2. Open the project in Android Studio
3. Wait for Gradle dependencies to sync
4. Build the project via `Build > Make Project` or `Ctrl+F9`

### Run
1. Launch an emulator or connect a device
2. Click `Run > Run 'app'` or press `Shift+F10`

### Guidelines
We follow a semantic commit style to keep history clean and understandable:

- `feat`: add a new feature
- `fix`: fix a bug
- `docs`: documentation-only changes
- `refactor`: code refactoring that doesn’t change behavior
- `style`: code formatting, whitespace, etc.
- `test`: add or update tests

**Example:**
```bash
feat(variable): support multiple variable declarations
```
> This helps keep the commit history clean and improves team collaboration.


## 📦 Deploy & CI/CD
Automatic build and deployment are not set up yet.  
In the future, we plan to integrate GitHub Actions or another CI/CD tool for automation.


## ✅ Completed
- [x] Variable declaration (including multiple via comma)
- [x] Variable assignment
- [x] Basic arithmetic operations (`+`, `-`, `*`, `//`, `%`)
- [x] Parentheses in expressions (custom operator precedence)
- [x] Added README (ru/en)


## 🔜 In Progress / To Do
**Basic (minimal):**
- [ ] Implement `If` block with condition and nested commands
- [ ] Visual grouping of nested blocks (Begin-End or similar)
- [ ] Show current values of variables in UI
- 
**Advanced ("Bubble Sort" level):**
- [ ] Implement `While` or `For` loops
- [ ] Implement `If-Else`
- [ ] Add support for static arrays

**HARD (optional):**
- [ ] Function support
- [ ] Boolean logic expressions
- [ ] Floating point types and type conversion
- [ ] Data flow model (Blueprint-style)
- [ ] Custom data structures
- [ ] File save/load for algorithms
- [ ] Debug mode (step-by-step execution)

**💡 General / Technical:**
- [ ] Improve block design (inspired by Scratch)
- [ ] Create UI mockup in Figma


## 👥 Project Team
- Alexey (ya-Bell)
- Slava (Slake1301)


## 🔍 Sources
- Inspired by visual programming languages like [Scratch](https://scratch.mit.edu/)
- Core idea based on Code::Blocks as an algorithm editor
- Official documentation used:
    - [Jetpack Compose](https://developer.android.com/jetpack/compose)
    - [Android Developers](https://developer.android.com/)
- Design and interaction focus on simplicity and learnability
> Thanks to all the open-source resources that helped shape this project.
