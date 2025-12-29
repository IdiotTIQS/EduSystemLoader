<h1>EduSystemLoader - 在线教育管理系统</h1>
<p>一个功能完整的在线教育管理系统，支持教师与学生之间的教学互动、作业管理、课程讨论、云存储以及 AI 智能辅导功能。</p>

<h2>项目功能特性</h2>
<ul>
  <li><strong>用户认证</strong>：基于 JWT 的用户登录与注册，支持教师和学生两种角色</li>
  <li><strong>班级管理</strong>：教师可创建班级，学生可加入班级</li>
  <li><strong>课程管理</strong>：教师创建课程，上传课程资料（PDF、PPT、链接等）</li>
  <li><strong>作业系统</strong>：教师发布作业，学生提交作业，教师批改评分</li>
  <li><strong>讨论区</strong>：班级内发起讨论，支持评论与回复</li>
  <li><strong>云存储</strong>：班级文件云盘，支持文件夹层级管理</li>
  <li><strong>AI 聊天</strong>：集成 AI 助手，提供智能答疑服务</li>
  <li><strong>文件上传</strong>：支持大文件上传（最大 100MB）</li>
</ul>

<h2>技术栈</h2>

<h3>后端</h3>
<ul>
  <li>Spring Boot 3.5.8</li>
  <li>MyBatis 3.0.5</li>
  <li>Spring Security Crypto</li>
  <li>JWT (jjwt 0.12.5)</li>
  <li>MySQL Connector</li>
  <li>Unirest Java (HTTP 客户端)</li>
  <li>Lombok</li>
</ul>

<h3>前端（Vue 版本）</h3>
<ul>
  <li>Vue 3.5.24</li>
  <li>Vue Router 4.6.3</li>
  <li>Vite (rolldown-vite 7.2.5)</li>
  <li>Axios 1.13.2</li>
  <li>Font Awesome 7.1.0</li>
</ul>

<h3>前端（HTML 版本）</h3>
<ul>
  <li>原生 HTML/CSS/JavaScript</li>
  <li>Webpack 5.91.0</li>
</ul>

<h2>项目结构</h2>
<pre><code>EduSystemLoader/
├── src/main/java/com/tiqs/        # 后端源代码
│   ├── auth/                       # 认证与授权模块
│   ├── controller/                 # REST API 控制器
│   ├── dto/                        # 数据传输对象
│   ├── entity/                     # 数据库实体
│   ├── mapper/                     # MyBatis Mapper
│   ├── service/                    # 业务逻辑层
│   ├── config/                     # 配置类
│   └── handler/                    # 全局异常处理
├── frontend-Vue/                   # Vue 3 前端项目
│   ├── src/
│   │   ├── components/             # Vue 组件
│   │   ├── views/                  # 页面视图
│   │   ├── router/                 # 路由配置
│   │   └── services/               # API 服务
├── Frontend-HTML/                  # 纯 HTML 前端项目
│   ├── css/                        # 样式文件
│   ├── js/                         # JavaScript 文件
│   └── pages/                      # 页面文件
├── .github/workflows/              # CI/CD 配置
├── .workflow/                      # 工作流配置
└── uploads/                        # 文件上传目录</code></pre>

<p>API调用相关文档详见 <a href="./ApiDoc.md">ApiDoc.md</a></p>
<p>项目已在 <a href="https://gitee.com/IdiotTIQS/EduSystemLoader.git">Gitee</a> 与 <a href="https://github.com/IdiotTIQS/EduSystemLoader.git">GitHub</a> 同步开源，欢迎 Star , Fork 与 Issue。</p>

<h2>数据库设计</h2>
<p>本项目使用MySQL数据库，以下为完整的数据库表结构设计：</p>

<h3>用户表 (users)</h3>
<pre><code>CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('TEACHER', 'STUDENT') NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL
);</code></pre>

<h3>班级表 (classes)</h3>
<pre><code>CREATE TABLE classes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(128) NOT NULL,
    code       VARCHAR(16) NOT NULL UNIQUE,
    teacher_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    INDEX idx_classes_teacher(teacher_id)
);</code></pre>

<h3>云文件夹表 (cloud_folders)</h3>
<pre><code>CREATE TABLE cloud_folders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id         BIGINT NOT NULL,
    name             VARCHAR(255) NOT NULL,
    parent_folder_id BIGINT,
    path             VARCHAR(1000) NOT NULL,
    creator_id       BIGINT NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL ON UPDATE CURRENT_TIMESTAMP(),
    UNIQUE KEY uk_folder_path(class_id, path) USING HASH,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_folder_id) REFERENCES cloud_folders(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_class_id(class_id),
    INDEX idx_creator_id(creator_id),
    INDEX idx_parent_folder_id(parent_folder_id),
    INDEX idx_path(path(768))
);</code></pre>

<h3>云文件表 (cloud_files)</h3>
<pre><code>CREATE TABLE cloud_files (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id           BIGINT NOT NULL,
    file_name          VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_path          VARCHAR(500) NOT NULL,
    file_size          BIGINT NOT NULL,
    file_type          VARCHAR(100) NOT NULL,
    description        TEXT,
    uploader_id        BIGINT NOT NULL,
    download_count     INT DEFAULT 0,
    is_public          TINYINT(1) DEFAULT 1,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL ON UPDATE CURRENT_TIMESTAMP(),
    folder_id          BIGINT,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES cloud_folders(id) ON DELETE SET NULL,
    INDEX idx_class_id(class_id),
    INDEX idx_created_at(created_at),
    INDEX idx_file_type(file_type),
    INDEX idx_uploader_id(uploader_id),
    INDEX idx_folder_id(folder_id)
);</code></pre>

<h3>课程表 (courses)</h3>
<pre><code>CREATE TABLE courses (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(128) NOT NULL,
    description TEXT,
    class_id    BIGINT NOT NULL,
    teacher_id  BIGINT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    INDEX idx_course_class(class_id),
    INDEX idx_course_teacher(teacher_id)
);</code></pre>

<h3>作业表 (assignments)</h3>
<pre><code>CREATE TABLE assignments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT NOT NULL,
    title       VARCHAR(128) NOT NULL,
    description TEXT,
    deadline    DATETIME NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_assignment_course(course_id),
    INDEX idx_assignment_deadline(deadline)
);</code></pre>

<h3>课程资料表 (course_materials)</h3>
<pre><code>CREATE TABLE course_materials (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id  BIGINT NOT NULL,
    type       ENUM('PDF', 'PPT', 'LINK', 'TEXT', 'OTHER') NOT NULL,
    name       VARCHAR(128) NOT NULL,
    url        VARCHAR(255),
    content    TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_material_course(course_id)
);</code></pre>

<h3>讨论表 (discussions)</h3>
<pre><code>CREATE TABLE discussions (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id   BIGINT NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT NOT NULL,
    author_id  BIGINT NOT NULL,
    is_pinned  TINYINT(1) DEFAULT 0,
    view_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL ON UPDATE CURRENT_TIMESTAMP(),
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_class_id(class_id),
    INDEX idx_author_id(author_id),
    INDEX idx_created_at(created_at),
    INDEX idx_is_pinned(is_pinned)
);</code></pre>

<h3>评论表 (comments)</h3>
<pre><code>CREATE TABLE comments (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    discussion_id BIGINT NOT NULL,
    parent_id     BIGINT,
    content       TEXT NOT NULL,
    author_id     BIGINT NOT NULL,
    is_edited     TINYINT(1) DEFAULT 0,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL ON UPDATE CURRENT_TIMESTAMP(),
    FOREIGN KEY (discussion_id) REFERENCES discussions(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_discussion_id(discussion_id),
    INDEX idx_parent_id(parent_id),
    INDEX idx_author_id(author_id),
    INDEX idx_created_at(created_at)
);</code></pre>

<h3>选课表 (enrollments)</h3>
<pre><code>CREATE TABLE enrollments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id   BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    joined_at  DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    UNIQUE KEY uk_enroll(class_id, student_id),
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_enroll_student(student_id)
);</code></pre>

<h3>作业提交表 (submissions)</h3>
<pre><code>CREATE TABLE submissions (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id      BIGINT NOT NULL,
    student_id         BIGINT NOT NULL,
    file_path          VARCHAR(255),
    answer_text        TEXT,
    submitted_at       DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    score              DECIMAL(5, 2),
    feedback           TEXT,
    graded_at          DATETIME,
    status             ENUM('SUBMITTED', 'GRADED') DEFAULT 'SUBMITTED' NOT NULL,
    original_file_name VARCHAR(255) COMMENT '原始文件名',
    UNIQUE KEY uk_submission_unique(assignment_id, student_id),
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_submission_assignment_score(assignment_id, score),
    INDEX idx_submission_student(student_id),
    INDEX idx_submissions_original_file_name(original_file_name)
);</code></pre>

<h3>用户资料表 (user_profile)</h3>
<pre><code>CREATE TABLE user_profile (
    user_id   BIGINT NOT NULL PRIMARY KEY,
    real_name VARCHAR(64),
    email     VARCHAR(128),
    phone     VARCHAR(32),
    UNIQUE KEY uk_user_profile_email(email),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);</code></pre>

<h4>数据库配置</h4>
<p>数据库连接配置支持环境变量，在 <code>src/main/resources/application.properties</code> 中：</p>
<pre><code>spring.datasource.url=${DB_URL:jdbc:mysql://tiqs1337.icu:3306/eduloader?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:eduloader}
spring.datasource.password=${DB_PASSWORD:tiqs1337}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver</code></pre>

<h4>环境变量配置</h4>
<p>项目支持通过环境变量来配置敏感信息，提高安全性：</p>

<h5>方法一：使用 .env 文件</h5>
<ol>
<li>复制环境变量模板文件：</li>
<pre><code>cp .env.example .env</code></pre>
<li>编辑 <code>.env</code> 文件，修改相应的配置：</li>
<pre><code># 数据库配置
DB_URL=jdbc:mysql://localhost:3306/eduloader?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT配置
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=604800000</code></pre>
</ol>

<h5>方法二：系统环境变量</h5>
<p>直接在操作系统中设置环境变量：</p>
<ul>
<li><strong>Windows (CMD)</strong>:
<pre><code>set DB_URL=jdbc:mysql://localhost:3306/eduloader?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
set DB_USERNAME=your_username
set DB_PASSWORD=your_password
set JWT_SECRET=your_jwt_secret_key</code></pre>
</li>
<li><strong>Windows (PowerShell)</strong>:
<pre><code>$env:DB_URL="jdbc:mysql://localhost:3306/eduloader?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
$env:DB_USERNAME="your_username"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your_jwt_secret_key"</code></pre>
</li>
<li><strong>Linux/Mac</strong>:
<pre><code>export DB_URL="jdbc:mysql://localhost:3306/eduloader?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"
export JWT_SECRET="your_jwt_secret_key"</code></pre>
</li>
</ul>

<h5>方法三：启动时传递</h5>
<p>在启动应用时直接传递环境变量：</p>
<ul>
<li><strong>Maven 启动</strong>:
<pre><code>DB_URL=jdbc:mysql://localhost:3306/eduloader DB_USERNAME=your_username DB_PASSWORD=your_password mvnw.cmd spring-boot:run</code></pre>
</li>
<li><strong>JAR 启动</strong>:
<pre><code>java -DB_URL=jdbc:mysql://localhost:3306/eduloader -DB_USERNAME=your_username -DB_PASSWORD=your_password -jar target/EduSystemLoader-0.0.1-SNAPSHOT.jar</code></pre>
</li>
</ul>

<h5>环境变量说明</h5>
<table>
  <thead>
    <tr><th>变量名</th><th>说明</th><th>默认值</th><th>必需</th></tr>
  </thead>
  <tbody>
    <tr><td><code>DB_URL</code></td><td>数据库连接URL</td><td><code>jdbc:mysql://tiqs1337.icu:3306/eduloader?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC</code></td><td>否</td></tr>
    <tr><td><code>DB_USERNAME</code></td><td>数据库用户名</td><td><code>eduloader</code></td><td>否</td></tr>
    <tr><td><code>DB_PASSWORD</code></td><td>数据库密码</td><td><code>tiqs1337</code></td><td>否</td></tr>
    <tr><td><code>JWT_SECRET</code></td><td>JWT签名密钥</td><td><code>eduSystemLoaderSecretKey2024ForJWTTokenGeneration</code></td><td>否</td></tr>
    <tr><td><code>JWT_EXPIRATION</code></td><td>JWT过期时间（毫秒）</td><td><code>604800000</code> (7天)</td><td>否</td></tr>
  </tbody>
</table>

<p><strong>注意</strong>：<code>.env</code> 文件已添加到 <code>.gitignore</code> 中，不会被提交到版本控制系统，确保敏感信息安全。</p>

<h4>AI 配置</h4>
<p>项目集成了 AI 聊天功能，需要配置 AI API 密钥：</p>
<ul>
<li><strong>AI_API_KEY</strong>：AI 服务 API 密钥（默认：sk-f015a829c0e40b482aa169c2dc6ed90a）</li>
<li><strong>AI_API_URL</strong>：AI 服务 API 地址（默认：https://apis.iflow.cn/v1/chat/completions）</li>
</ul>
<p>可在 <code>.env</code> 文件中添加：</p>
<pre><code># AI 配置
AI_API_KEY=your_ai_api_key
AI_API_URL=https://your-ai-service.com/v1/chat/completions</code></pre>

<h2>快速开始</h2>
<p>本项目包含后端 Spring Boot 与前端 Vue(Vite)。以下命令均以 Windows <code>cmd.exe</code> 为例。</p>

<h3>环境要求</h3>
<ul>
  <li>JDK 17+ (根据 <code>pom.xml</code> 要求选择合适版本)</li>
  <li>Node.js 18+ / npm 9+（建议使用长期支持版）</li>
  <li>Git (可选)</li>
</ul>

<h2>后端 (Spring Boot)</h2>
<p>根目录下使用 Maven Wrapper，无需本机安装 Maven。</p>

<h3>启动开发模式</h3>
<pre><code>mvnw.cmd spring-boot:run</code></pre>
<p>默认启动端口（若未修改）通常为 <code>8080</code>，API 文档请看 <code>ApiDoc.md</code>。</p>

<h3>构建可执行 JAR</h3>
<pre><code>mvnw.cmd clean package -DskipTests
</code></pre>
<p>打包后 JAR 位于 <code>target/EduSystemLoader-0.0.2-SNAPSHOT.jar</code>。</p>

<h3>运行打包后的 JAR</h3>
<pre><code>java -jar target/EduSystemLoader-0.0.2-SNAPSHOT.jar</code></pre>

<h3>执行测试</h3>
<pre><code>mvnw.cmd test</code></pre>

<h2>前端 (Vue 3 + Vite)</h2>
<p>Vue 前端源代码位于 <code>frontend-Vue/</code> 目录，是主要推荐使用的现代前端实现。</p>

<h3>安装依赖</h3>
<pre><code>cd frontend-Vue
npm install</code></pre>

<h3>开发启动 (热更新)</h3>
<pre><code>npm run dev</code></pre>
<p>Vite 默认端口为 <code>5173</code>，可在命令行输出中查看。访问：<code>http://localhost:5173/</code></p>

<h3>构建静态资源</h3>
<pre><code>npm run build</code></pre>
<p>生成产物位于 <code>frontend-Vue/dist</code>。可将其交给 Nginx/静态服务器或集成到后端静态目录（例如复制到 <code>src/main/resources/static</code>）。</p>

<h3>生产模式本地静态启动 (使用 serve)</h3>
<pre><code>npm run serve</code></pre>
<p>先执行构建 <code>npm run build</code>，然后使用 <code>serve -s dist</code> 在本地模拟部署。默认端口通常为 <code>3000</code>（若被占用会自动递增，命令行输出会显示）。</p>

<h3>Vite 内置预览 (可选)</h3>
<pre><code>npm run preview</code></pre>
<p>如果仍需要使用 Vite 自带的预览，可继续保留此命令；推荐使用 <code>npm run serve</code> 更贴近真实静态服务器行为。</p>

<h2>前端 (HTML + Webpack)</h2>
<p>纯 HTML 前端位于 <code>Frontend-HTML/</code> 目录，使用 Webpack 构建。</p>

<h3>安装依赖</h3>
<pre><code>cd Frontend-HTML
npm install</code></pre>

<h3>开发启动</h3>
<pre><code>npm start</code></pre>
<p>Webpack Dev Server 默认端口为 <code>8080</code>。</p>

<h3>构建生产版本</h3>
<pre><code>npm run build</code></pre>

<h2>常用脚本汇总</h2>
<table>
  <thead>
    <tr><th>阶段</th><th>命令</th><th>说明</th></tr>
  </thead>
  <tbody>
    <tr><td>后端运行</td><td><code>mvnw.cmd spring-boot:run</code></td><td>启动后端服务</td></tr>
    <tr><td>后端构建</td><td><code>mvnw.cmd clean package -DskipTests</code></td><td>编译打包 JAR</td></tr>
    <tr><td>后端测试</td><td><code>mvnw.cmd test</code></td><td>执行单元测试</td></tr>
    <tr><td>Vue 前端依赖</td><td><code>cd frontend-Vue &amp;&amp; npm install</code></td><td>安装 Vue 依赖</td></tr>
    <tr><td>Vue 前端开发</td><td><code>cd frontend-Vue &amp;&amp; npm run dev</code></td><td>启动 Vue 热更新开发服务器</td></tr>
    <tr><td>Vue 前端构建</td><td><code>cd frontend-Vue &amp;&amp; npm run build</code></td><td>生成 Vue 生产环境静态资源</td></tr>
    <tr><td>Vue 前端静态启动</td><td><code>cd frontend-Vue &amp;&amp; npm run serve</code></td><td>使用 serve 模拟生产静态部署</td></tr>
    <tr><td>HTML 前端依赖</td><td><code>cd Frontend-HTML &amp;&amp; npm install</code></td><td>安装 HTML 依赖</td></tr>
    <tr><td>HTML 前端开发</td><td><code>cd Frontend-HTML &amp;&amp; npm start</code></td><td>启动 HTML Webpack Dev Server</td></tr>
    <tr><td>HTML 前端构建</td><td><code>cd Frontend-HTML &amp;&amp; npm run build</code></td><td>构建 HTML 生产版本</td></tr>
  </tbody>
</table>

<h2>前后端联调建议</h2>
<ol>
  <li>先启动后端：<code>mvnw.cmd spring-boot:run</code></li>
  <li>再启动前端：<code>npm run dev</code></li>
  <li>在前端的 API 服务配置中设置后端地址（例如 <code>http://localhost:8080</code>）</li>
  <li>利用浏览器开发者工具查看网络请求与响应结构（对照 <code>ApiDoc.md</code>）</li>
</ol>

<h2>部署简单流程示例</h2>
<ol>
  <li>执行后端打包：<code>mvnw.cmd clean package -DskipTests</code></li>
  <li>执行前端构建（Vue 版本）：<code>cd frontend-Vue &amp;&amp; npm run build</code></li>
  <li>将 <code>frontend-Vue/dist</code> 内容复制到后端 <code>src/main/resources/static</code> (如需由 Spring Boot 直接托管)</li>
  <li>重新打包后端（若复制了静态资源）：<code>mvnw.cmd clean package -DskipTests</code></li>
  <li>服务器运行：<code>java -jar target/EduSystemLoader-0.0.2-SNAPSHOT.jar</code></li>
</ol>

<h2>故障排查</h2>
<ul>
  <li>端口占用：修改 <code>application.properties</code> 或 Vite 配置 <code>vite.config.js</code>.</li>
  <li>NPM 安装缓慢：使用国内镜像 <code>npm config set registry https://registry.npmmirror.com</code>.</li>
  <li>JAR 启动失败：确认 JDK 版本、查看日志栈信息。</li>
  <li>数据库连接失败：检查数据库服务是否启动，确认连接配置正确。</li>
  <li>AI 聊天无响应：检查 AI_API_KEY 和 AI_API_URL 配置是否正确。</li>
</ul>

<h2>许可证</h2>
<p>本项目采用开源许可证，详见 <a href="./LICENSE">LICENSE</a> 文件。</p>

<h2>贡献指南</h2>
<p>欢迎贡献代码！请遵循以下步骤：</p>
<ol>
  <li>Fork 本仓库</li>
  <li>创建特性分支 (<code>git checkout -b feature/AmazingFeature</code>)</li>
  <li>提交更改 (<code>git commit -m 'Add some AmazingFeature'</code>)</li>
  <li>推送到分支 (<code>git push origin feature/AmazingFeature</code>)</li>
  <li>提交 Pull Request</li>
</ol>

<h2>相关文档</h2>
<ul>
  <li><a href="./ApiDoc.md">API 文档</a></li>
  <li><a href="./SecurityReport.md">安全报告</a></li>
  <li><a href="./项目优化建议.md">项目优化建议</a></li>
</ul>

<p>欢迎补充与改进！</p>
