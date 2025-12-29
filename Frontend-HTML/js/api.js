/**
 * API接口封装
 * 包含所有后端API的调用方法
 */

// API接口定义
const API = {
  // 认证相关接口
  auth: {
    // 登录
    login: (credentials) => {
      return EduSystem.Http.post('/auth/login', credentials);
    },
    
    // 注册
    register: (userData) => {
      return EduSystem.Http.post('/auth/register', userData);
    },
    
    // 刷新token
    refreshToken: () => {
      return EduSystem.Http.post('/auth/refresh');
    },
    
    // 登出
    logout: () => {
      return EduSystem.Http.post('/auth/logout');
    },
    
    // 获取当前用户信息
    getCurrentUser: () => {
      return EduSystem.Http.get('/auth/me');
    }
  },

  // 用户相关接口
  user: {
    // 获取用户信息
    getProfile: () => {
      return EduSystem.Http.get('/user/profile');
    },
    
    // 更新用户信息
    updateProfile: (profileData) => {
      return EduSystem.Http.put('/user/profile', profileData);
    },
    
    // 修改密码
    changePassword: (passwordData) => {
      return EduSystem.Http.post('/user/change-password', passwordData);
    },
    
    // 上传头像
    uploadAvatar: (formData) => {
      return EduSystem.Http.upload('/user/avatar', formData);
    }
  },

  // 班级相关接口
  class: {
    // 获取班级列表
    getList: (params = {}) => {
      return EduSystem.Http.get('/class', params);
    },
    
    // 获取班级详情
    getDetail: (classId) => {
      return EduSystem.Http.get(`/class/${classId}`);
    },
    
    // 创建班级
    create: (classData) => {
      return EduSystem.Http.post('/class', classData);
    },
    
    // 更新班级
    update: (classId, classData) => {
      return EduSystem.Http.put(`/class/${classId}`, classData);
    },
    
    // 删除班级
    delete: (classId) => {
      return EduSystem.Http.delete(`/class/${classId}`);
    },
    
    // 加入班级
    join: (inviteCode) => {
      return EduSystem.Http.post('/class/join', { inviteCode });
    },
    
    // 获取班级成员
    getMembers: (classId, params = {}) => {
      return EduSystem.Http.get(`/class/${classId}/members`, params);
    },
    
    // 移除班级成员
    removeMember: (classId, userId) => {
      return EduSystem.Http.delete(`/class/${classId}/members/${userId}`);
    }
  },

  // 课程相关接口
  course: {
    // 获取课程列表
    getList: (params = {}) => {
      return EduSystem.Http.get('/course', params);
    },
    
    // 获取课程详情
    getDetail: (courseId) => {
      return EduSystem.Http.get(`/course/${courseId}`);
    },
    
    // 创建课程
    create: (courseData) => {
      return EduSystem.Http.post('/course', courseData);
    },
    
    // 更新课程
    update: (courseId, courseData) => {
      return EduSystem.Http.put(`/course/${courseId}`, courseData);
    },
    
    // 删除课程
    delete: (courseId) => {
      return EduSystem.Http.delete(`/course/${courseId}`);
    },
    
    // 获取班级课程
    getByClass: (classId, params = {}) => {
      return EduSystem.Http.get(`/class/${classId}/courses`, params);
    }
  },

  // 作业相关接口
  assignment: {
    // 获取作业列表
    getList: (params = {}) => {
      return EduSystem.Http.get('/assignment', params);
    },
    
    // 获取作业详情
    getDetail: (assignmentId) => {
      return EduSystem.Http.get(`/assignment/${assignmentId}`);
    },
    
    // 创建作业
    create: (assignmentData) => {
      return EduSystem.Http.post('/assignment', assignmentData);
    },
    
    // 更新作业
    update: (assignmentId, assignmentData) => {
      return EduSystem.Http.put(`/assignment/${assignmentId}`, assignmentData);
    },
    
    // 删除作业
    delete: (assignmentId) => {
      return EduSystem.Http.delete(`/assignment/${assignmentId}`);
    },
    
    // 获取课程作业
    getByCourse: (courseId, params = {}) => {
      return EduSystem.Http.get(`/course/${courseId}/assignments`, params);
    },
    
    // 获取学生作业列表
    getStudentAssignments: (params = {}) => {
      return EduSystem.Http.get('/assignment/student', params);
    }
  },

  // 提交相关接口
  submission: {
    // 获取提交列表
    getList: (params = {}) => {
      return EduSystem.Http.get('/submission', params);
    },
    
    // 获取提交详情
    getDetail: (submissionId) => {
      return EduSystem.Http.get(`/submission/${submissionId}`);
    },
    
    // 创建提交
    create: (submissionData) => {
      return EduSystem.Http.post('/submission', submissionData);
    },
    
    // 更新提交
    update: (submissionId, submissionData) => {
      return EduSystem.Http.put(`/submission/${submissionId}`, submissionData);
    },
    
    // 删除提交
    delete: (submissionId) => {
      return EduSystem.Http.delete(`/submission/${submissionId}`);
    },
    
    // 评分提交
    grade: (submissionId, gradeData) => {
      return EduSystem.Http.post(`/submission/${submissionId}/grade`, gradeData);
    },
    
    // 获取作业提交
    getByAssignment: (assignmentId, params = {}) => {
      return EduSystem.Http.get(`/assignment/${assignmentId}/submissions`, params);
    },
    
    // 获取学生提交
    getByStudent: (studentId, params = {}) => {
      return EduSystem.Http.get(`/student/${studentId}/submissions`, params);
    }
  },

  // 讨论相关接口
  discussion: {
    // 获取讨论列表
    getList: (params = {}) => {
      return EduSystem.Http.get('/discussion', params);
    },
    
    // 获取讨论详情
    getDetail: (discussionId) => {
      return EduSystem.Http.get(`/discussion/${discussionId}`);
    },
    
    // 创建讨论
    create: (discussionData) => {
      return EduSystem.Http.post('/discussion', discussionData);
    },
    
    // 更新讨论
    update: (discussionId, discussionData) => {
      return EduSystem.Http.put(`/discussion/${discussionId}`, discussionData);
    },
    
    // 删除讨论
    delete: (discussionId) => {
      return EduSystem.Http.delete(`/discussion/${discussionId}`);
    },
    
    // 获取班级讨论
    getByClass: (classId, params = {}) => {
      return EduSystem.Http.get(`/class/${classId}/discussions`, params);
    },
    
    // 点赞讨论
    like: (discussionId) => {
      return EduSystem.Http.post(`/discussion/${discussionId}/like`);
    },
    
    // 取消点赞
    unlike: (discussionId) => {
      return EduSystem.Http.delete(`/discussion/${discussionId}/like`);
    }
  },

  // 评论相关接口
  comment: {
    // 获取评论列表
    getList: (discussionId, params = {}) => {
      return EduSystem.Http.get(`/discussion/${discussionId}/comments`, params);
    },
    
    // 创建评论
    create: (discussionId, commentData) => {
      return EduSystem.Http.post(`/discussion/${discussionId}/comments`, commentData);
    },
    
    // 更新评论
    update: (commentId, commentData) => {
      return EduSystem.Http.put(`/comment/${commentId}`, commentData);
    },
    
    // 删除评论
    delete: (commentId) => {
      return EduSystem.Http.delete(`/comment/${commentId}`);
    }
  },

  // 云存储相关接口
  cloudStorage: {
    // 获取文件列表
    getFiles: (params = {}) => {
      return EduSystem.Http.get('/cloud/files', params);
    },
    
    // 获取文件夹列表
    getFolders: (params = {}) => {
      return EduSystem.Http.get('/cloud/folders', params);
    },
    
    // 创建文件夹
    createFolder: (folderData) => {
      return EduSystem.Http.post('/cloud/folders', folderData);
    },
    
    // 上传文件
    uploadFile: (formData, onProgress) => {
      return EduSystem.Http.upload('/cloud/files', formData, onProgress);
    },
    
    // 下载文件
    downloadFile: (fileId) => {
      return EduSystem.Http.get(`/cloud/files/${fileId}/download`);
    },
    
    // 删除文件
    deleteFile: (fileId) => {
      return EduSystem.Http.delete(`/cloud/files/${fileId}`);
    },
    
    // 删除文件夹
    deleteFolder: (folderId) => {
      return EduSystem.Http.delete(`/cloud/folders/${folderId}`);
    },
    
    // 获取班级文件
    getClassFiles: (classId, params = {}) => {
      return EduSystem.Http.get(`/class/${classId}/files`, params);
    }
  },

  // 统计相关接口
  statistics: {
    // 获取教师统计
    getTeacherStats: () => {
      return EduSystem.Http.get('/statistics/teacher');
    },
    
    // 获取学生统计
    getStudentStats: () => {
      return EduSystem.Http.get('/statistics/student');
    },
    
    // 获取班级统计
    getClassStats: (classId) => {
      return EduSystem.Http.get(`/statistics/class/${classId}`);
    }
  },

  // 通知相关接口
  notification: {
    // 获取通知列表
    getList: (params = {}) => {
      return EduSystem.Http.get('/notification', params);
    },
    
    // 标记为已读
    markAsRead: (notificationId) => {
      return EduSystem.Http.put(`/notification/${notificationId}/read`);
    },
    
    // 标记全部为已读
    markAllAsRead: () => {
      return EduSystem.Http.put('/notification/read-all');
    },
    
    // 删除通知
    delete: (notificationId) => {
      return EduSystem.Http.delete(`/notification/${notificationId}`);
    }
  }
};

// 数据转换工具
const DataTransform = {
  /**
   * 转换班级数据
   */
  transformClass: (data) => {
    return {
      id: data.id,
      name: data.name,
      inviteCode: data.inviteCode,
      description: data.description,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
      teacher: data.teacher,
      memberCount: data.memberCount || 0
    };
  },

  /**
   * 转换课程数据
   */
  transformCourse: (data) => {
    return {
      id: data.id,
      title: data.title,
      description: data.description,
      classId: data.classId,
      className: data.className,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
      teacher: data.teacher
    };
  },

  /**
   * 转换作业数据
   */
  transformAssignment: (data) => {
    return {
      id: data.id,
      title: data.title,
      description: data.description,
      courseId: data.courseId,
      courseName: data.courseName,
      deadline: data.deadline,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
      teacher: data.teacher,
      submissionCount: data.submissionCount || 0
    };
  },

  /**
   * 转换提交数据
   */
  transformSubmission: (data) => {
    return {
      id: data.id,
      assignmentId: data.assignmentId,
      assignmentTitle: data.assignmentTitle,
      studentId: data.studentId,
      studentName: data.studentName,
      studentEmail: data.studentEmail,
      content: data.content,
      attachments: data.attachments || [],
      score: data.score,
      feedback: data.feedback,
      status: data.status, // PENDING, SUBMITTED, GRADED
      submittedAt: data.submittedAt,
      gradedAt: data.gradedAt,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt
    };
  },

  /**
   * 转换讨论数据
   */
  transformDiscussion: (data) => {
    return {
      id: data.id,
      title: data.title,
      content: data.content,
      classId: data.classId,
      className: data.className,
      authorId: data.authorId,
      authorName: data.authorName,
      authorAvatar: data.authorAvatar,
      likeCount: data.likeCount || 0,
      commentCount: data.commentCount || 0,
      isLiked: data.isLiked || false,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt
    };
  },

  /**
   * 转换评论数据
   */
  transformComment: (data) => {
    return {
      id: data.id,
      content: data.content,
      discussionId: data.discussionId,
      authorId: data.authorId,
      authorName: data.authorName,
      authorAvatar: data.authorAvatar,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt
    };
  },

  /**
   * 转换文件数据
   */
  transformFile: (data) => {
    return {
      id: data.id,
      name: data.name,
      originalName: data.originalName,
      size: data.size,
      type: data.type,
      mimeType: data.mimeType,
      path: data.path,
      folderId: data.folderId,
      folderName: data.folderName,
      classId: data.classId,
      className: data.className,
      uploaderId: data.uploaderId,
      uploaderName: data.uploaderName,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt
    };
  },

  /**
   * 转换用户数据
   */
  transformUser: (data) => {
    return {
      id: data.id,
      username: data.username,
      email: data.email,
      role: data.role,
      avatar: data.avatar,
      phone: data.phone,
      realName: data.realName,
      studentId: data.studentId,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt
    };
  }
};

// 扩展全局对象
window.EduSystem.API = API;
window.EduSystem.DataTransform = DataTransform;