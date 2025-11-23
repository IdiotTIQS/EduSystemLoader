# 教育系统后端 API 文档

版本: v1.0  日期: 2025-11-23
Base URL: /api
认证方式: JWT（Authorization: Bearer <token>）
角色: TEACHER, STUDENT
统一响应格式:
{
  "code": 0,          // 0成功, 其它为错误码
  "message": "OK",    // 描述
  "data": { ... }      // 业务数据
}
常见错误码: 401 未认证, 403 无权限, 404 未找到, 409 冲突, 422 参数错误

## 1. 认证与用户
### 1.1 注册
POST /api/auth/register
Body: {"username":"string","password":"string","role":"TEACHER|STUDENT"}
Response: {"id":1,"username":"u","role":"TEACHER"}
约束: username唯一, 密码>=6

### 1.2 登录
POST /api/auth/login
Body: {"username":"string","password":"string"}
Response: {"token":"jwt","user":{"id":1,"role":"TEACHER"}}

### 1.3 自己信息获取
GET /api/users/me
Response: {"id":1,"username":"u","role":"STUDENT","profile":{"realName":"","email":"","phone":""}}

### 1.4 更新个人信息（仅自己）
PUT /api/users/me
Body: {"realName":"","email":"","phone":""}
Response: 个人信息

## 2. 班级管理 (TEACHER)
### 2.1 创建班级
POST /api/classes
Body: {"name":"高一1班","code":"可选自定义或系统生成"}
Response: {"id":10,"name":"高一1班","code":"ABC123"}
说明: code 用于学生加入

### 2.2 更新班级
PUT /api/classes/{classId}
Body: {"name":"新名称"}

### 2.3 删除班级
DELETE /api/classes/{classId}
约束: 若仍有关联课程或学生，返回409

### 2.4 查询自己班级列表
GET /api/classes/my
Response: [{"id":10,"name":"高一1班","studentCount":35}]

### 2.5 班级详情与学生列表
GET /api/classes/{classId}
Response: {"id":10,"name":"高一1班","students":[{"id":21,"username":"stu1"}],"courses":[...]}

## 3. 学生加入班级 (STUDENT)
### 3.1 加入班级
POST /api/classes/join
Body: {"code":"ABC123"}
Response: {"classId":10}
错误: 404 code不存在; 409 已加入

### 3.2 我的班级列表
GET /api/classes/my
Response: 同上 (学生端不含其它老师班级)

## 4. 课程管理 (TEACHER)
### 4.1 创建课程
POST /api/courses
Body: {"title":"数学必修一","description":"","classId":10}
Response: {"id":100,"title":"数学必修一","classId":10}

### 4.2 更新课程
PUT /api/courses/{courseId}
Body: {"title":"新标题","description":""}

### 4.3 删除课程
DELETE /api/courses/{courseId}

### 4.4 班级下课程列表
GET /api/courses?classId=10
Response: [{"id":100,"title":"数学必修一","teacherId":1}]
(学生/教师都可查)

### 4.5 课程详情
GET /api/courses/{courseId}
Response: {"id":100,"title":"数学必修一","materials":[...],"assignments":[...]}

## 5. 课件 (TEACHER 发布, STUDENT 查看)
课件类型示例: PPT, PDF, LINK, TEXT
数据结构: {"id":500,"courseId":100,"type":"PDF","name":"函数概念","url":"/files/xxx.pdf","createdAt":"2025-11-23T15:00:00Z"}
### 5.1 上传/发布课件
POST /api/courses/{courseId}/materials
Multipart/Form 或 JSON:
Fields: type, name, file(可选), url 或 content
Response: 课件对象

### 5.2 删除课件
DELETE /api/materials/{materialId}

### 5.3 课程课件列表
GET /api/courses/{courseId}/materials

## 6. 作业与提交
### 6.1 发布作业 (TEACHER)
POST /api/assignments
Body: {"courseId":100,"title":"第1次作业","description":"第1章习题","deadline":"2025-11-30T23:59:59Z"}
Response: {"id":900,"status":"OPEN"}

### 6.2 更新作业
PUT /api/assignments/{assignmentId}
Body: {"title":"修改标题","description":"","deadline":"..."}
限制: 已过截止时间禁止修改 deadline -> 409

### 6.3 删除作业
DELETE /api/assignments/{assignmentId}
前提: 无提交 或 强制删除标记 ?force=true

### 6.4 查询课程作业列表
GET /api/assignments?courseId=100
Response: [{"id":900,"title":"第1次作业","deadline":"...","submitted":true}]
submitted 对学生表示是否已交

### 6.5 作业详情
GET /api/assignments/{assignmentId}
Response: {"id":900,"title":"","description":"","deadline":"","submissionsCount":30}

### 6.6 学生提交作业 (STUDENT)
POST /api/submissions
Multipart/Form: assignmentId, file 或 answerText
Response: {"id":3000,"assignmentId":900,"studentId":21,"submittedAt":"...","status":"SUBMITTED"}
约束: 每个学生唯一一条; 再次提交可用 PUT 覆盖

### 6.7 更新(二次)提交
PUT /api/submissions/{submissionId}
Body/Multipart: file/answerText
限制: 截止后不允许 -> 403

### 6.8 查询自己提交
GET /api/submissions/my?assignmentId=900
Response: {"id":3000,"score":null,"feedback":null}

### 6.9 教师批改提交列表
GET /api/submissions?assignmentId=900
Response: [{"id":3000,"student":{"id":21,"username":"stu1"},"submittedAt":"...","score":null}]

### 6.10 评分
PUT /api/submissions/{submissionId}/score
Body: {"score":95,"feedback":"很好"}
Response: 更新后对象

## 7. 查询汇总
### 7.1 班级学生成绩汇总 (TEACHER)
GET /api/classes/{classId}/grades
Query: optional courseId
Response: [{"studentId":21,"studentName":"stu1","average":88.5,"details":[{"assignmentId":900,"score":95}]}]

## 8. 数据模型字段参考
User: {id,username,password(存储hash),role,createdAt}
UserProfile: {userId,realName,email,phone}
Class: {id,name,code,teacherId,createdAt}
Enrollment: {id,classId,studentId,joinedAt} (唯一索引 classId+studentId)
Course: {id,title,description,classId,teacherId,createdAt}
CourseMaterial: {id,courseId,type,name,url,content?,createdAt}
Assignment: {id,courseId,title,description,deadline,createdAt}
Submission: {id,assignmentId,studentId,filePath?,answerText?,submittedAt,score?,feedback?,gradedAt}

## 9. 认证 & 安全
- 注册后立即创建 User + (Student 自动建空 Profile)
- 登录鉴权生成 JWT: claims {userId, role}
- 访问受限接口需角色匹配
- 作业提交截止校验: now <= deadline
- 文件上传建议路径: /uploads/{yyyy}/{MM}/{assignmentId}/{userId}/

## 10. 分页与查询约定
参数: page(默认1) size(默认20) sort(字段,如 createdAt,desc)
响应 data 增加: {"list":[...],"page":1,"size":20,"total":132}

## 11. 示例交互流程 (学生)
1. POST /auth/register -> token
2. POST /classes/join (code)
3. GET /courses?classId= -> 课程
4. GET /assignments?courseId= -> 作业
5. POST /submissions -> 提交
6. GET /submissions/my?assignmentId= -> 查看状态

## 12. 示例交互流程 (教师)
1. 注册登录
2. 创建班级 -> 获取 code 分发
3. 创建课程并关联班级
4. 发布课件与作业
5. 查看提交 & 评分
6. 查看成绩汇总

## 13. 状态与枚举
Submission.status: SUBMITTED | GRADED
Material.type: PDF | PPT | LINK | TEXT | OTHER

## 14. 常见错误响应示例
{ "code": 404, "message": "Class not found" }
{ "code": 409, "message": "Already enrolled" }
{ "code": 403, "message": "Deadline passed" }

## 15. 后续扩展建议
- 增加通知表 Notice(id,title,content,scope,createdAt)
- 增加批量导入学生 CSV
- 添加操作审计日志

(完)
