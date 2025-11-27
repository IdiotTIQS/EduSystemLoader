## 接口列表 
| 接口 | 说明 |
|------ |----- |
|[/api/assignments](#assignments)| 作业资源 | 
|[/api/classes](#classes)| 班级资源 | 
|[/api/courses](#courses)| 课程资源 | 
|[/api/enrollments](#enrollments)| 班级成员 | 
|[/api/submissions](#submissions)| 作业提交与评分 |
|[/api/upload](#upload)| 文件上传 |
|[/api/cloud](#cloud)| 班级云盘文件管理 |
|[/api/cloud/folders](#cloud-folders)| 班级云盘文件夹管理 |
|[/api/discussions](#discussions)| 班级讨论 | 

***
## 错误码列表
| 错误码 | 说明 | HTTP状态码 |
|------ |----- |-----------|
| 0 | 成功 | 200 |
| 1 | 业务错误 | 200 |
| 2 | 参数错误 | 400 |
| 400 | 参数验证失败 | 400 |
| 401 | 认证失败/未登录 | 401 |
| 403 | 权限不足 | 403 |
| 404 | 资源不存在 | 404 |
| 405 | HTTP方法不支持 | 405 |
| 409 | 数据冲突/重复 | 409 |
| 413 | 文件大小超出限制 | 413 |
| 500 | 服务器内部错误 | 500 |

### 常见错误场景
- **400**: 参数格式错误、缺少必需参数、验证失败
- **401**: JWT令牌无效、过期、未携带
- **403**: 角色权限不足访问该接口
- **409**: 用户名已存在、邮箱重复注册
- **413**: 上传文件超过100MB限制
- **500**: 数据库连接失败、系统异常 |

统一返回结构：
| 名称 | 类型 | 说明 |
|----- |------| ---- |
| code | int | 状态码(0成功) |
| message | string | 信息 |
| data | object | 具体数据 |

### 数据类型说明
#### UserProfile
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| userId | Long | 用户ID |
| realName | String | 真实姓名 |
| email | String | 邮箱地址 |
| phone | String | 手机号码 |

#### Assignment
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| id | Long | 作业ID |
| courseId | Long | 课程ID |
| title | String | 作业标题 |
| description | String | 作业描述 |
| deadline | LocalDateTime | 截止时间 |
| createdAt | LocalDateTime | 创建时间 |

#### ClassEntity
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| id | Long | 班级ID |
| name | String | 班级名称 |
| code | String | 邀请码 |
| teacherId | Long | 教师ID |
| createdAt | LocalDateTime | 创建时间 |

#### Course
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| id | Long | 课程ID |
| title | String | 课程标题 |
| description | String | 课程描述 |
| classId | Long | 班级ID |
| teacherId | Long | 教师ID |
| createdAt | LocalDateTime | 创建时间 |

#### Enrollment
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| id | Long | 记录ID |
| classId | Long | 班级ID |
| studentId | Long | 学生ID |
| joinedAt | LocalDateTime | 加入时间 |

#### Submission
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| id | Long | 提交ID |
| assignmentId | Long | 作业ID |
| studentId | Long | 学生ID |
| filePath | String | 文件路径 |
| answerText | String | 文本答案 |
| submittedAt | LocalDateTime | 提交时间 |
| score | Double | 得分 |
| feedback | String | 反馈 |
| gradedAt | LocalDateTime | 评分时间 |
| status | String | 状态 |

#### CloudFile
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| id | Long | 文件ID |
| classId | Long | 班级ID |
| fileName | String | 存储文件名 |
| originalFileName | String | 原始文件名 |
| filePath | String | 文件路径 |
| fileSize | Long | 文件大小(字节) |
| fileType | String | 文件类型 |
| description | String | 文件描述 |
| uploaderId | Long | 上传者ID |
| downloadCount | Integer | 下载次数 |
| isPublic | Boolean | 是否公开 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |
| uploaderName | String | 上传者姓名 |
| uploaderRole | String | 上传者角色 |

#### CloudFileStatistics
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| totalSize | Long | 总大小(字节) |
| fileCount | Integer | 文件数量 |

#### Discussion
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| id | Long | 讨论ID |
| classId | Long | 班级ID |
| title | String | 讨论标题 |
| content | String | 讨论内容 |
| authorId | Long | 作者ID |
| isPinned | Boolean | 是否置顶 |
| viewCount | Integer | 查看次数 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |
| authorName | String | 作者姓名 |
| commentCount | Integer | 评论数量 |

#### Comment
| 字段 | 类型 | 说明 |
|----- |------| ---- |
| id | Long | 评论ID |
| discussionId | Long | 讨论ID |
| parentId | Long | 父评论ID |
| content | String | 评论内容 |
| authorId | Long | 作者ID |
| isEdited | Boolean | 是否已编辑 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |
| authorName | String | 作者姓名 |
| authorRole | String | 作者角色 |

## 鉴权说明

### JWT令牌认证
所有 `/api/**` 接口必须携带以下请求头：
- `Authorization`：`Bearer {JWT_TOKEN}` 格式的认证令牌

### JWT令牌说明
- **获取方式**: 通过登录或注册接口获取
- **有效期**: 7天 (604800000毫秒)
- **格式**: `Bearer eyJhbGciOiJIUzI1NiJ9...`
- **存储位置**: 前端localStorage
- **自动续期**: 令牌过期后需要重新登录

### 令牌payload结构
```json
{
  "sub": "1",           // 用户ID
  "role": "TEACHER",    // 用户角色
  "username": "teacher001", // 用户名
  "iat": 1700800000,    // 签发时间
  "exp": 1701404800     // 过期时间
}
```

### 令牌使用注意事项
1. **安全存储**: 建议使用httpOnly cookie或安全的localStorage
2. **传输安全**: 生产环境必须使用HTTPS
3. **过期处理**: 前端需处理401错误并跳转登录
4. **令牌撤销**: 目前不支持主动撤销，依赖过期机制

权限概览：
- **教师角色**：允许访问/操作 `POST|PUT|DELETE /api/assignments/**`、`POST|PUT|DELETE /api/classes/**`、`GET /api/classes/teacher/{teacherId}`、`POST|PUT|DELETE /api/courses/**`、`GET /api/enrollments/class/{classId}`、`GET /api/submissions`、`PUT /api/submissions/{id}/grade`、`POST /api/cloud/upload`、`PUT|DELETE /api/cloud/files/**`、`PUT /api/discussions/{id}/pin`。
- **学生角色**：允许访问 `POST /api/enrollments`、`POST /api/submissions`、`PUT /api/submissions/{id}`、`GET /api/cloud/files/{id}/download`、`GET /api/cloud/files`、`GET /api/cloud/statistics`、`POST /api/discussions`、`POST /api/discussions/{discussionId}/comments`、`PUT /api/discussions/comments/{id}`、`DELETE /api/discussions/comments/{id}`。
- **通用接口**：未列出的其余 GET/查询接口支持教师与学生共同访问 (如 `/unique`、资源详情查询等)。

鉴权失败会返回 `401` 状态码以及 `{code:401,message:"无权限"}` 结构。

### 认证接口
| 方法 | 路径 | 说明 | 请求体 | 返回 data |
|------|------|------|--------|-----------|
| POST | /api/auth/register | 用户注册（教师/学生） | `{username,password,role,realName?,email?,phone?}` | `{userId,username,role,realName,email,phone,token}` |
| POST | /api/auth/login | 用户登录 | `{username,password}` | `{userId,username,role,realName,email,phone,token}` |
| GET | /api/users/profile | 获取当前用户资料 | - | `UserProfile{userId,realName,email,phone}` |
| GET | /api/users/{userId}/profile | 获取指定用户资料 | userId(Path,Long) | - | `UserProfile{userId,realName,email,phone}` |
| PUT | /api/users/profile | 更新用户资料 | `{realName?,email?,phone?}` | `UserProfile` |

> `/api/auth/**` 接口无需携带鉴权头，其余接口保持原有策略。

## 请求/响应示例

### 成功响应示例
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "teacher001",
    "role": "TEACHER",
    "realName": "张老师",
    "email": "teacher@example.com",
    "phone": "13800138000",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 错误响应示例
```json
{
  "code": 401,
  "message": "认证失败：无效的认证令牌",
  "data": null
}
```

### 认证接口示例

#### 用户登录
**请求:**
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "teacher001",
  "password": "password123"
}
```

**响应:**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "teacher001",
    "role": "TEACHER",
    "realName": "张老师",
    "email": "teacher@example.com",
    "phone": "13800138000",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlRFQUNIRVIiLCJ1c2VybmFtZSI6InRlYWNoZXIwMDEiLCJpYXQiOjE3MDA4MDAwMDAsImV4cCI6MTcwMTQwNDgwMH0.signature"
  }
}
```

#### 创建班级
**请求:**
```bash
POST /api/classes
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

{
  "name": "计算机科学2024级",
  "teacherId": 1,
  "code": "CS2024"
}
```

**响应:**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "计算机科学2024级",
    "code": "CS2024",
    "teacherId": 1,
    "createdAt": "2024-11-25T12:00:00"
  }
}
```

### 文件上传示例
**请求:**
```bash
POST /api/upload/file
Content-Type: multipart/form-data
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

file: [binary data]
```

**响应:**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": "/uploads/2024/11/25/abc123-def456-ghi789.pdf"
}
```

---

### 开发环境配置
- **后端地址**: http://localhost:8080
- **前端地址**: http://localhost:5173
- **数据库**: MySQL (tiqs1337.icu:3306)
- **文件上传**: 本地存储在 `/uploads/` 目录

### 测试账号
- **教师账号**: teacher001 / password123
- **学生账号**: student001 / password123

---

## 接口详情

* <span id="assignments">作业接口</span>
    * 根路径：/api/assignments
    * 说明：课程下的作业管理
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/assignments | 创建作业 | - | JSON Assignment{courseId,title,description,deadline} | Assignment |
        | GET | /api/assignments/{id} | 作业详情 | id(Path,Long) | - | Assignment |
        | GET | /api/assignments | 按课程列出 | courseId(Query,Long) | - | Assignment[] |
        | PUT | /api/assignments/{id} | 更新作业 | id(Path,Long) | JSON Assignment{title,description,deadline} | Assignment |
        | DELETE | /api/assignments/{id} | 删除作业 | id(Path,Long) | - | null |
        | GET | /api/assignments/{id} | 作业详情 | id(Path) | - | Assignment |
        | GET | /api/assignments | 按课程列出 | courseId(Query,Long) | - | Assignment[] |
        | PUT | /api/assignments/{id} | 更新作业 | id(Path) | JSON Assignment(可更新字段) | Assignment |
        | DELETE | /api/assignments/{id} | 删除作业 | id(Path) | - | null |

    * 请求参数类型：Assignment{courseId(Long),title(String),description(String),deadline(LocalDateTime)}

---
* <span id="classes">班级接口</span>
    * 根路径：/api/classes
    * 说明：班级创建与查询
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/classes | 创建班级 | - | JSON ClassEntity{name,teacherId,code} | ClassEntity |
        | GET | /api/classes/{id} | 班级详情 | id(Path,Long) | - | ClassEntity |
        | GET | /api/classes/teacher/{teacherId} | 教师班级列表 | teacherId(Path,Long) | - | ClassEntity[] |
        | PUT | /api/classes/{id} | 更新名称 | id(Path,Long), name(Query,String) | - | ClassEntity |
        | DELETE | /api/classes/{id} | 删除班级 | id(Path,Long) | - | null |
        | GET | /api/classes/code/{code} | 邀请码查找 | code(Path,String) | - | ClassEntity |

    * 请求参数类型：ClassEntity{name(String),teacherId(Long),code(String)}

---
* <span id="courses">课程接口</span>
    * 根路径：/api/courses
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/courses | 创建课程 | - | JSON Course{classId,title,description,teacherId} | Course |
        | GET | /api/courses/{id} | 课程详情 | id(Path,Long) | - | Course |
        | GET | /api/courses | 班级课程列表 | classId(Query,Long) | - | Course[] |
        | PUT | /api/courses/{id} | 更新课程 | id(Path,Long) | JSON Course{title,description} | Course |
        | DELETE | /api/courses/{id} | 删除课程 | id(Path,Long) | - | null |

    * 请求参数类型：Course{classId(Long),title(String),description(String),teacherId(Long)}

---
* <span id="enrollments">班级成员接口</span>
    * 根路径：/api/enrollments
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/enrollments | 学生加入 | classId(Query,Long),studentId(Query,Long) | - | Enrollment |
        | GET | /api/enrollments/class/{classId} | 班级成员列表 | classId(Path,Long) | - | Enrollment[] |
        | GET | /api/enrollments/unique | 唯一记录查询 | classId(Query,Long),studentId(Query,Long) | - | Enrollment/null |

    * 请求参数类型：Enrollment{classId(Long),studentId(Long)}

---
* <span id="submissions">作业提交接口</span>
    * 根路径：/api/submissions
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/submissions | 提交作业 | - | JSON Submission{assignmentId,studentId,filePath,answerText} | Submission |
        | GET | /api/submissions | 作业提交列表 | assignmentId(Query,Long) | - | Submission[] |
        | GET | /api/submissions/unique | 学生唯一提交 | assignmentId(Query,Long),studentId(Query,Long) | - | Submission/null |
        | PUT | /api/submissions/{id} | 更新内容 | id(Path,Long) | JSON Submission{filePath,answerText} | Submission |
        | PUT | /api/submissions/{id}/grade | 教师评分 | id(Path,Long), score(Query,Double), feedback(Query,String,可选) | - | Submission |

    * 请求参数类型：Submission{assignmentId(Long),studentId(Long),filePath(String),answerText(String)}

---
* <span id="upload">文件上传接口</span>
    * 根路径：/api/upload
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/upload/file | 上传文件 | file(FormData,MultipartFile) | - | String(文件URL) |

    * 文件上传说明：
        - 支持所有文件类型（除可执行文件和脚本文件）
        - 文件大小限制：100MB
        - 禁止上传的文件类型：.exe, .bat, .sh, .cmd, .com, .scr, .msi, .sql, .ddl, .dml, .pl, .php, .asp, .jsp, .js, .vbs, .py, .rb, .ps1, .bash, .zsh
        - 文件按日期存储：/uploads/yyyy/MM/dd/
        - 返回格式：/uploads/yyyy/MM/dd/UUID.扩展名
        - 需要鉴权：所有用户角色均可上传文件

---
* <span id="cloud">班级云盘接口</span>
    * 根路径：/api/cloud
    * 说明：班级云盘文件管理，支持教师上传文件，学生下载文件
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/cloud/upload | 上传云盘文件 | classId(FormData,Long), file(FormData,MultipartFile), description(FormData,String,可选), isPublic(FormData,Boolean) | - | CloudFile |
        | GET | /api/cloud/files | 查询云盘文件列表 | classId(Query,Long), fileType(Query,String,可选), uploaderId(Query,Long,可选) | - | CloudFile[] |
        | GET | /api/cloud/files/{id} | 获取文件详情 | id(Path,Long) | - | CloudFile |
        | GET | /api/cloud/files/{id}/download | 下载文件 | id(Path,Long) | - | 二进制文件流 |
        | PUT | /api/cloud/files/{id} | 更新文件信息 | id(Path,Long) | JSON CloudFile{description,isPublic} | CloudFile |
        | DELETE | /api/cloud/files/{id} | 删除文件 | id(Path,Long) | - | null |
        | GET | /api/cloud/statistics | 获取云盘统计信息 | classId(Query,Long) | - | CloudFileStatistics |

    * 上传文件请求参数说明：
        - classId: 班级ID（必需）
        - file: 上传的文件（必需）
        - description: 文件描述（可选，最大500字符）
        - isPublic: 是否公开文件（可选，默认true）

    * 查询文件列表参数说明：
        - classId: 班级ID（必需）
        - fileType: 文件类型过滤（可选，如image、document、spreadsheet等）
        - uploaderId: 上传者ID过滤（可选，仅管理员可用）

    * 文件分类说明：
        - image: 图片文件 (jpg, jpeg, png, gif, bmp, webp, svg)
        - document: 文档文件 (pdf, doc, docx, txt, rtf, odt)
        - spreadsheet: 表格文件 (xls, xlsx, csv, ods)
        - presentation: 演示文稿 (ppt, pptx, odp)
        - archive: 压缩包 (zip, rar, 7z, tar, gz)

    * 权限说明：
        - 上传文件：仅教师角色
        - 删除文件：仅文件上传者或教师角色
        - 更新文件信息：仅文件上传者
        - 下载公开文件：所有角色
        - 下载私有文件：仅文件上传者
        - 查看文件列表：所有角色
        - 获取统计信息：所有角色

    * 文件大小限制：
        - 单个文件最大100MB
        - 支持的文件类型：除可执行文件和脚本外的所有类型
        - 危险文件类型：.exe, .bat, .cmd, .com, .scr, .msi, .dll, .so, .dylib, .sh, .bash, .zsh, .fish, .ps1, .py, .pl, .rb, .php, .asp, .jsp, .js, .vbs, .wsf, .jar, .app, .deb, .rpm, .dmg, .pkg, .iso, .img, .vmdk, .ova, .ovf

    * 上传文件示例：
        **请求:**
        ```bash
        POST /api/cloud/upload
        Content-Type: multipart/form-data
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

        ------WebKitFormBoundary7MA4YWxkTrZu0gW
        Content-Disposition: form-data; name="classId"
        
        1
        ------WebKitFormBoundary7MA4YWxkTrZu0gW
        Content-Disposition: form-data; name="file"; filename="课件.pdf"
        Content-Type: application/pdf

        [binary file data]
        ------WebKitFormBoundary7MA4YWxkTrZu0gW
        Content-Disposition: form-data; name="description"
        
        第三章课程课件
        ------WebKitFormBoundary7MA4YWxkTrZu0gW--
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": {
            "id": 1,
            "classId": 1,
            "fileName": "abc123-def456.pdf",
            "originalFileName": "第三章课程课件.pdf",
            "filePath": "/cloud/2024/11/26/abc123-def456.pdf",
            "fileSize": 2048576,
            "fileType": "pdf",
            "description": "第三章课程课件",
            "uploaderId": 1,
            "downloadCount": 0,
            "isPublic": true,
            "createdAt": "2024-11-26T10:30:00",
            "updatedAt": "2024-11-26T10:30:00",
            "uploaderName": "张老师",
            "uploaderRole": "TEACHER"
          }
        }
        ```

    * 查询文件列表示例：
        **请求:**
        ```bash
        GET /api/cloud/files?classId=1&fileType=document
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": [
            {
              "id": 1,
              "classId": 1,
              "fileName": "course_notes.pdf",
              "originalFileName": "课程笔记.pdf",
              "filePath": "/cloud/2024/11/26/course_notes.pdf",
              "fileSize": 1024000,
              "fileType": "pdf",
              "description": "第一章课程笔记",
              "uploaderId": 1,
              "downloadCount": 15,
              "isPublic": true,
              "createdAt": "2024-11-26T09:15:00",
              "updatedAt": "2024-11-26T09:15:00",
              "uploaderName": "张老师",
              "uploaderRole": "TEACHER"
            },
            {
              "id": 2,
              "classId": 1,
              "fileName": "homework_template.docx",
              "originalFileName": "作业模板.docx",
              "filePath": "/cloud/2024/11/25/homework_template.docx",
              "fileSize": 512000,
              "fileType": "docx",
              "description": "作业模板",
              "uploaderId": 1,
              "downloadCount": 8,
              "isPublic": false,
              "createdAt": "2024-11-25T14:20:00",
              "updatedAt": "2024-11-25T14:20:00",
              "uploaderName": "张老师",
              "uploaderRole": "TEACHER"
            }
          ]
        }
        ```

    * 获取统计信息示例：
        **请求:**
        ```bash
        GET /api/cloud/statistics?classId=1
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": {
            "totalSize": 2560576,
            "fileCount": 2
          }
        }
        ```

---
* <span id="discussions">班级讨论接口</span>
    * 根路径：/api/discussions
    * 说明：班级讨论功能，支持创建讨论主题、发表评论、回复等
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/discussions | 创建讨论 | - | JSON Discussion{classId,title,content} | Discussion |
        | GET | /api/discussions/{id} | 获取讨论详情 | id(Path,Long) | - | Discussion |
        | GET | /api/discussions/class/{classId} | 班级讨论列表 | classId(Path,Long) | - | Discussion[] |
        | PUT | /api/discussions/{id} | 更新讨论 | id(Path,Long) | JSON Discussion{title,content} | Discussion |
        | DELETE | /api/discussions/{id} | 删除讨论 | id(Path,Long) | - | null |
        | PUT | /api/discussions/{id}/pin | 置顶/取消置顶讨论 | id(Path,Long), isPinned(Query,Boolean) | - | Discussion |
        | POST | /api/discussions/{discussionId}/comments | 发表评论 | discussionId(Path,Long) | JSON Comment{content,parentId} | Comment |
        | GET | /api/discussions/{discussionId}/comments | 获取讨论评论列表 | discussionId(Path,Long) | - | Comment[] |
        | PUT | /api/discussions/comments/{id} | 更新评论 | id(Path,Long) | JSON Comment{content} | Comment |
        | DELETE | /api/discussions/comments/{id} | 删除评论 | id(Path,Long) | - | null |

    * 请求参数类型：
        - Discussion{classId(Long),title(String),content(String)}
        - Comment{content(String),parentId(Long,可选)}

    * 评论层级说明：
        - parentId为null表示顶级评论
        - parentId为其他评论ID表示回复评论
        - 支持无限层级的回复结构

    * 权限说明：
        - 创建讨论：所有角色
        | 更新讨论：仅讨论创建者
        | 删除讨论：仅讨论创建者
        | 置顶/取消置顶：仅教师角色
        | 发表评论：所有角色
        | 更新评论：仅评论作者
        | 删除评论：仅评论作者
        - 查看讨论和评论：所有角色

    * 创建讨论示例：
        **请求:**
        ```bash
        POST /api/discussions
        Content-Type: application/json
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

        {
          "classId": 1,
          "title": "第一章学习讨论",
          "content": "请大家讨论第一章的重点内容和难点"
        }
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": {
            "id": 1,
            "classId": 1,
            "title": "第一章学习讨论",
            "content": "请大家讨论第一章的重点内容和难点",
            "authorId": 1,
            "isPinned": false,
            "viewCount": 0,
            "createdAt": "2024-11-26T10:00:00",
            "updatedAt": "2024-11-26T10:00:00",
            "authorName": "张老师",
            "commentCount": 0
          }
        }
        ```

    * 发表评论示例：
        **请求:**
        ```bash
        POST /api/discussions/1/comments
        Content-Type: application/json
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

        {
          "content": "我觉得第一章的重点是..."
        }
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": {
            "id": 1,
            "discussionId": 1,
            "parentId": null,
            "content": "我觉得第一章的重点是...",
            "authorId": 2,
            "isEdited": false,
            "createdAt": "2024-11-26T10:05:00",
            "updatedAt": "2024-11-26T10:05:00",
            "authorName": "学生A",
            "authorRole": "STUDENT"
          }
        }
        ```

    * 回复评论示例：
        **请求:**
        ```bash
        POST /api/discussions/1/comments
        Content-Type: application/json
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

        {
          "content": "@学生A 我也有同感",
          "parentId": 1
        }
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": {
            "id": 2,
            "discussionId": 1,
            "parentId": 1,
            "content": "@学生A 我也有同感",
            "authorId": 1,
            "isEdited": false,
            "createdAt": "2024-11-26T10:10:00",
            "updatedAt": "2024-11-26T10:10:00",
            "authorName": "张老师",
            "authorRole": "TEACHER"
          }
        }
        ```

    * 置顶讨论示例：
        **请求:**
        ```bash
        PUT /api/discussions/1/pin?isPinned=true
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": {
            "id": 1,
            "classId": 1,
            "title": "第一章学习讨论",
            "content": "请大家讨论第一章的重点内容和难点",
            "authorId": 1,
            "isPinned": true,
            "viewCount": 25,
            "createdAt": "2024-11-26T10:00:00",
            "updatedAt": "2024-11-26T10:30:00",
            "authorName": "张老师",
            "commentCount": 3
          }
        }
        ```

---
* <span id="cloud-folders">班级云盘文件夹接口</span>
    * 根路径：/api/cloud/folders
    * 说明：班级云盘文件夹管理，支持创建文件夹、文件夹层级结构、移动重命名等
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/cloud/folders | 创建文件夹 | - | JSON CloudFolder{classId,name,parentFolderId} | CloudFolder |
        | GET | /api/cloud/folders/root | 获取根文件夹列表 | classId(Query,Long) | - | CloudFolder[] |
        | GET | /api/cloud/folders/{id} | 获取文件夹详情 | id(Path,Long) | - | CloudFolder |
        | GET | /api/cloud/folders/{id}/subfolders | 获取子文件夹列表 | id(Path,Long) | - | CloudFolder[] |
        | GET | /api/cloud/folders/{id}/tree | 获取文件夹树形结构 | id(Path,Long) | - | CloudFolder |
        | PUT | /api/cloud/folders/{id}/rename | 重命名文件夹 | id(Path,Long), name(Query,String) | - | CloudFolder |
        | PUT | /api/cloud/folders/{id}/move | 移动文件夹 | id(Path,Long), parentFolderId(Query,Long) | - | CloudFolder |
        | DELETE | /api/cloud/folders/{id} | 删除文件夹 | id(Path,Long) | - | null |
        | GET | /api/cloud/folders/search | 搜索文件夹 | classId(Query,Long), keyword(Query,String) | - | CloudFolder[] |
        | GET | /api/cloud/folders/statistics | 获取文件夹统计信息 | classId(Query,Long) | - | CloudFolderStatistics |

    * 请求参数类型：CloudFolder{classId(Long),name(String),parentFolderId(Long,可选)}

    * 返回数据类型补充：
        #### CloudFolder
        | 字段 | 类型 | 说明 |
        |----- |------| ---- |
        | id | Long | 文件夹ID |
        | classId | Long | 班级ID |
        | name | String | 文件夹名称 |
        | parentFolderId | Long | 父文件夹ID |
        | path | String | 文件夹路径 |
        | creatorId | Long | 创建者ID |
        | createdAt | LocalDateTime | 创建时间 |
        | updatedAt | LocalDateTime | 更新时间 |
        | parentFolder | CloudFolder | 父文件夹对象 |
        | subFolders | CloudFolder[] | 子文件夹列表 |
        | files | CloudFile[] | 文件列表 |
        | creatorName | String | 创建者姓名 |
        | fileCount | Integer | 文件数量 |
        | folderCount | Integer | 子文件夹数量 |

        #### CloudFolderStatistics
        | 字段 | 类型 | 说明 |
        |----- |------| ---- |
        | totalFolders | Integer | 总文件夹数量 |
        | totalFiles | Integer | 总文件数量 |
        | totalSize | Long | 总大小(字节) |
        | maxDepth | Integer | 最大层级深度 |

    * 文件夹层级说明：
        - parentFolderId为null表示根级文件夹
        - parentFolderId为其他文件夹ID表示子文件夹
        - 支持无限层级的文件夹结构
        - path字段记录完整路径，如"/根文件夹/子文件夹"

    * 权限说明：
        - 创建文件夹：仅教师角色
        - 删除文件夹：仅文件夹创建者或教师角色
        - 重命名文件夹：仅文件夹创建者
        - 移动文件夹：仅文件夹创建者
        - 查看文件夹：所有角色
        - 搜索文件夹：所有角色

    * 文件夹操作限制：
        - 不能删除包含文件的文件夹（需先删除或移动所有文件）
        - 不能移动文件夹到其子文件夹中（避免循环引用）
        - 不能重命名为已存在的同名文件夹（同级内）
        - 文件夹名称最大长度：100字符
        - 最大层级深度：10层

    * 创建文件夹示例：
        **请求:**
        ```bash
        POST /api/cloud/folders
        Content-Type: application/json
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

        {
          "classId": 1,
          "name": "第一章课件",
          "parentFolderId": null
        }
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": {
            "id": 1,
            "classId": 1,
            "name": "第一章课件",
            "parentFolderId": null,
            "path": "/",
            "creatorId": 1,
            "createdAt": "2024-11-26T10:00:00",
            "updatedAt": "2024-11-26T10:00:00",
            "parentFolder": null,
            "subFolders": [],
            "files": [],
            "creatorName": "张老师",
            "fileCount": 0,
            "folderCount": 0
          }
        }
        ```

    * 获取文件夹树形结构示例：
        **请求:**
        ```bash
        GET /api/cloud/folders/1/tree
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": {
            "id": 1,
            "name": "第一章课件",
            "path": "/",
            "subFolders": [
              {
                "id": 2,
                "name": "基础概念",
                "path": "/第一章课件",
                "subFolders": [
                  {
                    "id": 3,
                    "name": "重点知识",
                    "path": "/第一章课件/基础概念",
                    "subFolders": [],
                    "files": []
                  }
                ],
                "files": []
              }
            ],
            "files": [
              {
                "id": 1,
                "fileName": "intro.pdf",
                "originalFileName": "课程介绍.pdf",
                "fileSize": 1024000
              }
            ]
          }
        }
        ```

    * 移动文件夹示例：
        **请求:**
        ```bash
        PUT /api/cloud/folders/3/move?parentFolderId=1
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        ```

        **响应:**
        ```json
        {
          "code": 0,
          "message": "操作成功",
          "data": {
            "id": 3,
            "name": "重点知识",
            "parentFolderId": 1,
            "path": "/第一章课件",
            "updatedAt": "2024-11-26T10:30:00"
          }
        }
        ```

---

---
