## 接口列表 
| 接口 | 说明 |
|------ |----- |
|[/api/assignments](#assignments)| 作业资源 | 
|[/api/classes](#classes)| 班级资源 | 
|[/api/courses](#courses)| 课程资源 | 
|[/api/enrollments](#enrollments)| 班级成员 | 
|[/api/submissions](#submissions)| 作业提交与评分 | 

***
## 错误码列表
| 错误码 | 说明 |
|------ |----- |
| 0 | 正确 |
| 1 | 业务错误 |
| 2 | 参数错误 |

统一返回结构：
| 名称 | 类型 | 说明 |
|----- |------| ---- |
| code | int | 状态码(0成功) |
| message | string | 信息 |
| data | object | 具体数据 |

## 接口详情

* <span id="assignments">作业接口</span>
    * 根路径：/api/assignments
    * 说明：课程下的作业管理
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/assignments | 创建作业 | - | JSON Assignment{courseId,title,description,deadline} | Assignment |
        | GET | /api/assignments/{id} | 作业详情 | id(Path) | - | Assignment |
        | GET | /api/assignments | 按课程列出 | courseId(Query,Long) | - | Assignment[] |
        | PUT | /api/assignments/{id} | 更新作业 | id(Path) | JSON Assignment(可更新字段) | Assignment |
        | DELETE | /api/assignments/{id} | 删除作业 | id(Path) | - | null |

    * Assignment字段：id,courseId,title,description,deadline,createdAt

---
* <span id="classes">班级接口</span>
    * 根路径：/api/classes
    * 说明：班级创建与查询
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/classes | 创建班级 | - | JSON ClassEntity{name,teacherId,code} | ClassEntity |
        | GET | /api/classes/{id} | 班级详情 | id(Path) | - | ClassEntity |
        | GET | /api/classes/teacher/{teacherId} | 教师班级列表 | teacherId(Path) | - | ClassEntity[] |
        | PUT | /api/classes/{id} | 更新名称 | id(Path), name(Query) | - | ClassEntity |
        | DELETE | /api/classes/{id} | 删除班级 | id(Path) | - | null |
        | GET | /api/classes/code/{code} | 邀请码查找 | code(Path) | - | ClassEntity |

    * ClassEntity字段：id,name,code,teacherId,createdAt

---
* <span id="courses">课程接口</span>
    * 根路径：/api/courses
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/courses | 创建课程 | - | JSON Course{classId,title,description,teacherId} | Course |
        | GET | /api/courses/{id} | 课程详情 | id(Path) | - | Course |
        | GET | /api/courses | 班级课程列表 | classId(Query,Long) | - | Course[] |
        | PUT | /api/courses/{id} | 更新课程 | id(Path) | JSON Course{title,description} | Course |
        | DELETE | /api/courses/{id} | 删除课程 | id(Path) | - | null |

    * Course字段：id,title,description,classId,teacherId,createdAt

---
* <span id="enrollments">班级成员接口</span>
    * 根路径：/api/enrollments
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/enrollments | 学生加入 | classId(Query,Long),studentId(Query,Long) | - | Enrollment |
        | GET | /api/enrollments/class/{classId} | 班级成员列表 | classId(Path) | - | Enrollment[] |
        | GET | /api/enrollments/unique | 唯一记录查询 | classId(Query,Long),studentId(Query,Long) | - | Enrollment/null |

    * Enrollment字段：id,classId,studentId,joinedAt

---
* <span id="submissions">作业提交接口</span>
    * 根路径：/api/submissions
    * 方法列表：
        | 方法 | 路径 | 说明 | 请求参数 | 体/Query | 返回data类型 |
        |-----|------|------|---------|----------|-------------|
        | POST | /api/submissions | 提交作业 | - | JSON Submission{assignmentId,studentId,filePath,answerText} | Submission |
        | GET | /api/submissions | 作业提交列表 | assignmentId(Query,Long) | - | Submission[] |
        | GET | /api/submissions/unique | 学生唯一提交 | assignmentId(Query,Long),studentId(Query,Long) | - | Submission/null |
        | PUT | /api/submissions/{id} | 更新内容 | id(Path) | JSON Submission{filePath,answerText} | Submission |
        | PUT | /api/submissions/{id}/grade | 教师评分 | id(Path), score(Query,Double), feedback(Query,可选) | - | Submission |

    * Submission字段：id,assignmentId,studentId,filePath,answerText,submittedAt,score,feedback,gradedAt,status

---
