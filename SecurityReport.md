# 安全漏洞报告

## 1. 硬编码敏感信息 (Hardcoded Secrets)
**严重程度: 严重 (CRITICAL)**

\src/main/resources/application.properties\ 文件包含硬编码的敏感信息：
- **数据库密码**: \spring.datasource.password=\
- **JWT 密钥**: \jwt.secret=\
- **AI API 密钥**: \i.api.key=\

**建议**:
- 对所有敏感信息使用环境变量。
- 移除包含实际密钥的默认值。
- 立即轮换已泄露的 API 密钥和密码。

## 2. 文件上传导致的存储型跨站脚本攻击 (Stored XSS)
**严重程度: 高 (HIGH)**

\FileUploadController.java\ 和 \CloudStorageServiceImpl.java\ 都使用黑名单方法过滤文件类型，但黑名单不完整。
- **漏洞代码**: \orbiddenExtensions\ 和 \dangerousTypes\ 数组未包含 Web 可执行类型，如 \.html\, \.htm\, \.svg\, \.xml\, \.xhtml\。
- **影响**: 攻击者可以上传包含 JavaScript 的恶意 HTML 文件。由于 \WebMvcConfig.java\ 直接从 \/uploads/**\ 提供文件服务，访问上传的文件将在受害者的浏览器上下文中执行脚本 (XSS)。

**建议**:
- 使用允许扩展名的严格**白名单**（例如 \.jpg\, \.png\, \.pdf\, \.docx\）。
- 如果提供文件服务，请确保设置 \Content-Disposition: attachment\ 标头以强制下载而不是内联显示（\downloadFile\ 端点已这样做，但直接访问静态资源时未做）。
- 如果可能，从 \WebMvcConfig\ 中移除 \egistry.addResourceHandler("/uploads/**")\，并通过强制执行身份验证和标头的控制器提供所有文件。

## 3. 访问控制失效 (Broken Access Control / IDOR)
**严重程度: 高 (HIGH)**

\CloudStorageController\ 和 \CloudStorageServiceImpl\ 缺乏针对班级成员身份的适当授权检查。
- **漏洞**: \downloadFile\ 和 \listFiles\ 不验证请求用户是否属于与文件关联的 \classId\。
- **影响**: 如果猜到文件 ID 或班级 ID，一个班级的学生可以访问另一个班级的“公开”文件。
- **公开访问**: \WebMvcConfig\ 将 \/uploads/**\ 排除在 \AuthInterceptor\ 之外，这意味着任何拥有 URL 的人都可以无需登录即可访问上传的文件。

**建议**:
- 实现检查以确保 \AuthContextHolder.get().userId()\ 属于请求资源的 \classId\。
- 在 \WebMvcConfig\ 的 \xcludePathPatterns\ 中移除 \/uploads/**\，以强制对文件访问进行身份验证。

## 4. 文件管理中的不安全直接对象引用 (IDOR)
**严重程度: 中 (MEDIUM)**

虽然 \deleteFile\ 检查用户是否为上传者，但它似乎没有针对*特定班级*教师的强大基于角色的覆盖。它只是在控制器中检查 \UserRole.TEACHER\，但服务逻辑可能过于简单。

## 5. 缺失 CSRF 保护
**严重程度: 低 (LOW)**

应用程序使用带有 JWT 的自定义 \AuthInterceptor\，似乎未启用 Spring Security 的 CSRF 保护。虽然存储在 localStorage 中的 JWT 对 CSRF 免疫，但如果存储在 cookie 中（此处未见），CSRF 将是一个风险。由于 CORS 中设置了 \llowCredentials(false)\，这可能没问题，但值得注意。

## 总结
应用程序在密钥管理和文件上传安全方面存在严重缺陷。需要立即采取行动保护 API 密钥并修复 XSS 漏洞。
