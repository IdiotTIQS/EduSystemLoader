<h1>Education System SpringBoot</h1>
<p> This is a simple education system.Code base by GPT5-copilot</p>
<p>大神AI</p>
<p>API调用相关文档详见Apidoc.md</p>

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
<p>打包后 JAR 位于 <code>target/EduSystemLoader-0.0.1-SNAPSHOT.jar</code>。</p>

<h3>运行打包后的 JAR</h3>
<pre><code>java -jar target/EduSystemLoader-0.0.1-SNAPSHOT.jar</code></pre>

<h3>执行测试</h3>
<pre><code>mvnw.cmd test</code></pre>

<h2>前端 (Vue 3 + Vite)</h2>
<p>前端源代码位于 <code>frontend/</code> 目录。</p>

<h3>安装依赖</h3>
<pre><code>cd frontend
npm install</code></pre>

<h3>开发启动 (热更新)</h3>
<pre><code>npm run dev</code></pre>
<p>Vite 默认端口为 <code>5173</code>，可在命令行输出中查看。访问：<code>http://localhost:5173/</code></p>

<h3>构建静态资源</h3>
<pre><code>npm run build</code></pre>
<p>生成产物位于 <code>frontend/dist</code>。可将其交给 Nginx/静态服务器或集成到后端静态目录（例如复制到 <code>src/main/resources/static</code>）。</p>

<h3>生产模式本地静态启动 (使用 serve)</h3>
<pre><code>npm run serve</code></pre>
<p>先执行构建 <code>npm run build</code>，然后使用 <code>serve -s dist</code> 在本地模拟部署。默认端口通常为 <code>3000</code>（若被占用会自动递增，命令行输出会显示）。</p>

<h3>Vite 内置预览 (可选)</h3>
<pre><code>npm run preview</code></pre>
<p>如果仍需要使用 Vite 自带的预览，可继续保留此命令；推荐使用 <code>npm run serve</code> 更贴近真实静态服务器行为。</p>

<h3>常用脚本汇总</h3>
<table>
  <thead>
    <tr><th>阶段</th><th>命令</th><th>说明</th></tr>
  </thead>
  <tbody>
    <tr><td>后端运行</td><td><code>mvnw.cmd spring-boot:run</code></td><td>启动后端服务</td></tr>
    <tr><td>后端构建</td><td><code>mvnw.cmd clean package -DskipTests</code></td><td>编译打包 JAR</td></tr>
    <tr><td>后端测试</td><td><code>mvnw.cmd test</code></td><td>执行单元测试</td></tr>
    <tr><td>前端依赖</td><td><code>npm install</code></td><td>安装依赖</td></tr>
    <tr><td>前端开发</td><td><code>npm run dev</code></td><td>启动热更新开发服务器</td></tr>
    <tr><td>前端构建</td><td><code>npm run build</code></td><td>生成生产环境静态资源</td></tr>
    <tr><td>前端静态启动</td><td><code>npm run serve</code></td><td>使用 serve 模拟生产静态部署</td></tr>
    <tr><td>前端预览(可选)</td><td><code>npm run preview</code></td><td>Vite 内置预览模式</td></tr>
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
  <li>执行前端构建：<code>npm run build</code></li>
  <li>将 <code>frontend/dist</code> 内容复制到后端 <code>src/main/resources/static</code> (如需由 Spring Boot 直接托管)</li>
  <li>重新打包后端（若复制了静态资源）：<code>mvnw.cmd clean package -DskipTests</code></li>
  <li>服务器运行：<code>java -jar EduSystemLoader-0.0.1-SNAPSHOT.jar</code></li>
</ol>

<h2>故障排查</h2>
<ul>
  <li>端口占用：修改 <code>application.properties</code> 或 Vite 配置 <code>vite.config.js</code>.</li>
  <li>NPM 安装缓慢：使用国内镜像 <code>npm config set registry https://registry.npmmirror.com</code>.</li>
  <li>JAR 启动失败：确认 JDK 版本、查看日志栈信息。</li>
</ul>

<p>欢迎补充与改进！</p>
