# 教育系统前端

基于 Vue 3 + Vite 构建的教育系统前端应用，包含用户认证、学生和教师仪表板等功能。

## 技术栈

- **Vue 3** - 使用 Composition API 和 `<script setup>` 语法
- **Vite** - 快速的构建工具
- **Vue Router** - 路由管理
- **Axios** - HTTP 请求库
- **FontAwesome** - 图标库

## 依赖说明

### 核心依赖

#### Vue 3 生态系统
- `vue` (^3.5.24) 
  - **安装方式**: `npm install vue@latest`
  - **作用**: Vue 3 渐进式 JavaScript 框架，提供响应式数据绑定和组件化开发
  - **特性**: Composition API、更好的 TypeScript 支持、性能优化

- `vue-router` (^4.6.3)
  - **安装方式**: `npm install vue-router@4`
  - **作用**: Vue 官方路由管理器，用于单页面应用的页面导航
  - **特性**: 嵌套路由、路由守卫、动态路由、导航编程

#### 网络请求
- `axios` (^1.13.2)
  - **安装方式**: `npm install axios`
  - **作用**: 基于 Promise 的 HTTP 客户端，用于与后端 API 通信
  - **特性**: 请求/响应拦截器、自动 JSON 转换、错误处理、取消请求

### UI 和图标依赖

#### FontAwesome 图标系统
- `@fortawesome/fontawesome-svg-core` 
  - **安装方式**: `npm install @fortawesome/fontawesome-svg-core`
  - **作用**: FontAwesome 核心库，提供图标渲染和管理功能
  - **特性**: SVG 图标渲染、图标库管理、树摇优化

- `@fortawesome/free-solid-svg-icons`
  - **安装方式**: `npm install @fortawesome/free-solid-svg-icons`
  - **作用**: FontAwesome 免费实心图标集合
  - **包含**: 用户、锁、信封、毕业帽、身份证、电话等常用图标

- `@fortawesome/free-brands-svg-icons`
  - **安装方式**: `npm install @fortawesome/free-brands-svg-icons`
  - **作用**: FontAwesome 免费品牌图标集合
  - **包含**: QQ、微信、微博、支付宝等社交媒体图标

- `@fortawesome/vue-fontawesome` 
  - **安装方式**: `npm install @fortawesome/vue-fontawesome@latest-3`
  - **作用**: Vue 3 FontAwesome 官方组件
  - **特性**: 响应式图标、自动优化、Vue 3 Composition API 支持

### 开发依赖

#### 构建工具
- `@vitejs/plugin-vue` (^6.0.1)
  - **安装方式**: `npm install @vitejs/plugin-vue --save-dev`
  - **作用**: Vite 的 Vue 单文件组件插件
  - **特性**: 热重载、模板编译、样式预处理

- `rolldown-vite` (^7.2.5)
  - **安装方式**: `npm install rolldown-vite --save-dev`
  - **作用**: 基于 Rollup 的 Vite 构建，提供更快的构建速度
  - **特性**: 快速打包、Tree Shaking、代码分割

#### 开发服务器
- `serve` (^14.2.3)
  - **安装方式**: `npm install serve --save-dev`
  - **作用**: 静态文件服务器，用于预览构建结果
  - **特性**: 简单易用、支持 SPA、自动刷新

## 项目结构

```
frontend/
├── src/
│   ├── assets/          # 静态资源
│   │   └── images/      # 图片资源（登录页面SVG插图）
│   ├── components/      # Vue 组件
│   │   ├── CloudDrive.vue
│   │   ├── CloudFileUpload.vue
│   │   ├── CreateDiscussion.vue
│   │   ├── DiscussionDetail.vue
│   │   ├── DiscussionList.vue
│   │   ├── FileUpload.vue
│   │   ├── HelloWorld.vue
│   │   └── Toaster.vue
│   ├── router/          # 路由配置
│   │   └── index.js
│   ├── services/        # API 服务
│   │   ├── api.js       # API 接口封装
│   │   ├── auth.js      # 认证相关服务
│   │   └── toast.js     # 通知服务
│   ├── views/           # 页面组件
│   │   ├── LoginView.vue      # 登录页面（包含注册功能）
│   │   ├── RegisterView.vue   # 独立注册页面
│   │   ├── StudentDashboard.vue
│   │   └── TeacherDashboard.vue
│   ├── App.vue          # 根组件
│   ├── main.js          # 应用入口
│   └── style.css        # 全局样式
├── public/              # 公共静态文件
├── index.html           # HTML 模板
├── package.json         # 项目配置
├── vite.config.js       # Vite 配置
└── README.md            # 项目说明
```

## 功能特性

### 用户认证
- 现代化的登录/注册界面设计
- 滑动切换式登录注册页面
- 支持用户名/密码登录
- 支持邮箱注册和角色选择（学生/教师）
- 集成第三方登录图标（QQ、微信、微博、支付宝）

### 用户界面
- 响应式设计，支持移动端和桌面端
- 精美的动画效果和过渡
- 统一的通知系统
- 现代化的卡片式布局

## 开发指南

### 安装依赖
```bash
npm install
```

### 开发服务器
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 预览生产构建
```bash
npm run preview
```

### 静态文件服务
```bash
npm run serve
```

## FontAwesome 使用

项目中已配置 FontAwesome 图标库，可以在组件中直接使用：

```vue
<template>
  <font-awesome-icon icon="fa-solid fa-user" />
  <font-awesome-icon icon="fa-brands fa-weixin" />
</template>
```

### 已配置的图标
- **实心图标**: fa-user, fa-lock, fa-envelope, fa-graduation-cap, fa-id-card, fa-phone
- **品牌图标**: fa-qq, fa-weixin, fa-weibo, fa-alipay

## API 集成

前端通过 `src/services/api.js` 与后端进行通信：

- 认证相关接口：登录、注册
- 用户管理：获取用户信息、更新资料
- 课程管理：课程列表、详情、创建
- 文件管理：上传、下载、云存储
- 讨论功能：创建讨论、回复评论

## 路由配置

- `/` - 重定向到登录页
- `/login` - 登录页面（包含注册功能）
- `/register` - 独立注册页面
- `/student` - 学生仪表板
- `/teacher` - 教师仪表板

## 样式系统

项目使用现代 CSS 设计：
- 响应式布局
- CSS 变量主题系统
- 动画和过渡效果
- 移动端优先设计

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).
