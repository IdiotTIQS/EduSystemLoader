/**
 * 教师端专用JavaScript功能
 * 包含教师控制台的所有交互逻辑
 */

// 教师端应用对象
const TeacherApp = {
  // 当前状态
  state: {
    activeTab: 'dashboard',
    currentClass: null,
    currentCourse: null,
    currentAssignment: null,
    classes: [],
    courses: [],
    assignments: [],
    submissions: []
  },

  // 初始化应用
  init() {
    // 检查权限
    EduSystem.Page.init({
      requireAuth: true,
      requiredRole: 'TEACHER',
      title: '教师控制台'
    });

    // 绑定事件
    this.bindEvents();
    
    // 加载初始数据
    this.loadInitialData();
    
    // 显示默认标签页
    this.switchTab('dashboard');
  },

  // 绑定事件
  bindEvents() {
    // 标签页切换
    document.querySelectorAll('.sidebar-nav-item').forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const tabName = item.dataset.tab || this.getTabFromText(item.textContent);
        this.switchTab(tabName);
      });
    });

    // 班级创建表单
    const classForm = document.getElementById('create-class-form');
    if (classForm) {
      classForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.createClass();
      });
    }

    // 课程创建表单
    const courseForm = document.getElementById('create-course-form');
    if (courseForm) {
      courseForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.createCourse();
      });
    }

    // 作业创建表单
    const assignmentForm = document.getElementById('create-assignment-form');
    if (assignmentForm) {
      assignmentForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.createAssignment();
      });
    }

    // 评分表单
    const gradeForm = document.getElementById('grade-form');
    if (gradeForm) {
      gradeForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.gradeSubmission();
      });
    }

    // 提交筛选
    const submissionClassSelect = document.getElementById('submission-class-id');
    if (submissionClassSelect) {
      submissionClassSelect.addEventListener('change', () => {
        this.loadCoursesByClass();
      });
    }

    const submissionCourseSelect = document.getElementById('submission-course-id');
    if (submissionCourseSelect) {
      submissionCourseSelect.addEventListener('change', () => {
        this.loadAssignmentsByCourse();
      });
    }

    const submissionAssignmentSelect = document.getElementById('submission-assignment-id');
    if (submissionAssignmentSelect) {
      submissionAssignmentSelect.addEventListener('change', () => {
        this.loadSubmissions();
      });
    }

    // 讨论区班级选择
    const discussionClassSelect = document.getElementById('discussion-class-select');
    if (discussionClassSelect) {
      discussionClassSelect.addEventListener('change', () => {
        this.loadDiscussions();
      });
    }

    // 云盘班级选择
    const cloudClassSelect = document.getElementById('cloud-class-select');
    if (cloudClassSelect) {
      cloudClassSelect.addEventListener('change', () => {
        this.loadCloudFiles();
      });
    }
  },

  // 从文本获取标签页名称
  getTabFromText(text) {
    const tabMap = {
      '仪表盘': 'dashboard',
      '班级管理': 'classes',
      '课程管理': 'courses',
      '作业发布': 'assignments',
      '提交管理': 'submissions',
      '班级成员': 'members',
      '班级讨论': 'discussions',
      '班级云盘': 'cloud'
    };
    return tabMap[text] || 'dashboard';
  },

  // 切换标签页
  switchTab(tabName) {
    // 更新状态
    this.state.activeTab = tabName;
    
    // 更新导航样式
    document.querySelectorAll('.sidebar-nav-item').forEach(item => {
      item.classList.remove('active');
    });
    
    const activeNavItem = document.querySelector(`[data-tab="${tabName}"]`) ||
                         document.querySelector(`.sidebar-nav-item:contains("${this.getTabText(tabName)}")`);
    if (activeNavItem) {
      activeNavItem.classList.add('active');
    }
    
    // 隐藏所有内容区域
    document.querySelectorAll('[id$="-content"]').forEach(el => {
      el.classList.add('hidden');
    });
    
    // 显示当前内容区域
    const currentContent = document.getElementById(`${tabName}-content`);
    if (currentContent) {
      currentContent.classList.remove('hidden');
    }
    
    // 加载对应数据
    this.loadTabData(tabName);
  },

  // 获取标签页文本
  getTabText(tabName) {
    const textMap = {
      'dashboard': '仪表盘',
      'classes': '班级管理',
      'courses': '课程管理',
      'assignments': '作业发布',
      'submissions': '提交管理',
      'members': '班级成员',
      'discussions': '班级讨论',
      'cloud': '班级云盘'
    };
    return textMap[tabName] || '';
  },

  // 加载标签页数据
  async loadTabData(tabName) {
    switch (tabName) {
      case 'dashboard':
        await this.loadDashboardData();
        break;
      case 'classes':
        await this.loadClasses();
        break;
      case 'courses':
        await this.loadCourses();
        break;
      case 'assignments':
        await this.loadAssignments();
        break;
      case 'submissions':
        await this.loadSubmissionsData();
        break;
      case 'members':
        await this.loadMembers();
        break;
      case 'discussions':
        await this.initDiscussions();
        break;
      case 'cloud':
        await this.initCloud();
        break;
    }
  },

  // 加载初始数据
  async loadInitialData() {
    try {
      // 加载班级列表
      await this.loadClasses();
      
      // 加载统计信息
      await this.loadDashboardData();
    } catch (error) {
      console.error('加载初始数据失败:', error);
    }
  },

  // 加载仪表盘数据
  async loadDashboardData() {
    try {
      const stats = await EduSystem.API.statistics.getTeacherStats();
      this.renderDashboardStats(stats);
      
      // 加载最近活动
      const activities = await this.getRecentActivities();
      this.renderRecentActivities(activities);
    } catch (error) {
      console.error('加载仪表盘数据失败:', error);
    }
  },

  // 渲染仪表盘统计
  renderDashboardStats(stats) {
    const classCount = document.querySelector('.stats-card.class-count .stats-card-value');
    const courseCount = document.querySelector('.stats-card.course-count .stats-card-value');
    const pendingCount = document.querySelector('.stats-card.pending-count .stats-card-value');
    
    if (classCount) classCount.textContent = stats.classCount || 0;
    if (courseCount) courseCount.textContent = stats.courseCount || 0;
    if (pendingCount) pendingCount.textContent = stats.pendingSubmissions || 0;
  },

  // 渲染最近活动
  renderRecentActivities(activities) {
    const container = document.querySelector('.activity-list');
    if (!container) return;
    
    container.innerHTML = '';
    
    activities.forEach(activity => {
      const item = this.createActivityItem(activity);
      container.appendChild(item);
    });
  },

  // 创建活动项
  createActivityItem(activity) {
    const item = document.createElement('li');
    item.className = 'activity-item';
    
    const iconClass = this.getActivityIconClass(activity.type);
    const timeAgo = this.getTimeAgo(activity.createdAt);
    
    item.innerHTML = `
      <div class="activity-icon ${iconClass}">
        <i class="${activity.icon}"></i>
      </div>
      <div class="activity-content">
        <div class="activity-title">${activity.title}</div>
        <div class="activity-description">${activity.description}</div>
      </div>
      <div class="activity-time">${timeAgo}</div>
    `;
    
    return item;
  },

  // 获取活动图标类
  getActivityIconClass(type) {
    const iconMap = {
      'student_join': 'blue',
      'submission': 'green',
      'discussion': 'yellow',
      'default': 'blue'
    };
    return iconMap[type] || 'blue';
  },

  // 获取相对时间
  getTimeAgo(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    
    if (minutes < 60) {
      return `${minutes} 分钟前`;
    } else if (hours < 24) {
      return `${hours} 小时前`;
    } else if (days < 7) {
      return `${days} 天前`;
    } else {
      return EduSystem.Utils.formatDate(dateString);
    }
  },

  // 获取最近活动
  async getRecentActivities() {
    // 模拟数据，实际应该从API获取
    return [
      {
        type: 'student_join',
        title: '新学生加入',
        description: '张伟加入了高等数学 A 班',
        icon: 'fas fa-user-plus',
        createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
      },
      {
        type: 'submission',
        title: '作业提交',
        description: '王丽提交了线性代数作业',
        icon: 'fas fa-file-upload',
        createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString()
      },
      {
        type: 'discussion',
        title: '新的讨论',
        description: '计算机科学基础班有了新讨论',
        icon: 'fas fa-comment-dots',
        createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString()
      }
    ];
  },

  // 加载班级列表
  async loadClasses() {
    try {
      const response = await EduSystem.API.class.getList();
      this.state.classes = response.data.map(EduSystem.DataTransform.transformClass);
      this.renderClasses();
    } catch (error) {
      console.error('加载班级列表失败:', error);
    }
  },

  // 渲染班级列表
  renderClasses() {
    const tbody = document.getElementById('classes-table-body');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    this.state.classes.forEach(cls => {
      const row = this.createClassRow(cls);
      tbody.appendChild(row);
    });
    
    // 更新班级选择下拉框
    this.updateClassSelects();
  },

  // 创建班级行
  createClassRow(cls) {
    const row = document.createElement('tr');
    row.className = 'hover:bg-gray-50';
    
    row.innerHTML = `
      <td class="px-6 py-4 whitespace-nowrap">${cls.name}</td>
      <td class="px-6 py-4 whitespace-nowrap">${cls.inviteCode}</td>
      <td class="px-6 py-4 whitespace-nowrap">${EduSystem.Utils.formatDate(cls.createdAt)}</td>
      <td class="px-6 py-4 whitespace-nowrap text-sm">
        <button class="text-blue-500 hover:text-blue-700 mr-3" onclick="TeacherApp.editClass(${cls.id})">
          <i class="fas fa-edit"></i>
        </button>
        <button class="text-red-500 hover:text-red-700" onclick="TeacherApp.deleteClass(${cls.id})">
          <i class="fas fa-trash"></i>
        </button>
      </td>
    `;
    
    return row;
  },

  // 创建班级
  async createClass() {
    const name = document.getElementById('class-name').value;
    const inviteCode = document.getElementById('class-invite-code').value;
    
    if (!name || !inviteCode) {
      EduSystem.Notification.show('请填写完整的班级信息', 'warning');
      return;
    }
    
    try {
      await EduSystem.API.class.create({
        name,
        inviteCode,
        description: ''
      });
      
      EduSystem.Notification.show('班级创建成功', 'success');
      document.getElementById('create-class-form').reset();
      await this.loadClasses();
    } catch (error) {
      console.error('创建班级失败:', error);
    }
  },

  // 编辑班级
  editClass(classId) {
    const cls = this.state.classes.find(c => c.id === classId);
    if (!cls) return;
    
    // 填充表单
    document.getElementById('class-name').value = cls.name;
    document.getElementById('class-invite-code').value = cls.inviteCode;
    
    // 切换到班级管理标签页
    this.switchTab('classes');
  },

  // 删除班级
  async deleteClass(classId) {
    if (!confirm('确定要删除这个班级吗？此操作不可恢复。')) {
      return;
    }
    
    try {
      await EduSystem.API.class.delete(classId);
      EduSystem.Notification.show('班级删除成功', 'success');
      await this.loadClasses();
    } catch (error) {
      console.error('删除班级失败:', error);
    }
  },

  // 加载课程列表
  async loadCourses() {
    try {
      const response = await EduSystem.API.course.getList();
      this.state.courses = response.data.map(EduSystem.DataTransform.transformCourse);
      this.renderCourses();
    } catch (error) {
      console.error('加载课程列表失败:', error);
    }
  },

  // 渲染课程列表
  renderCourses() {
    const tbody = document.getElementById('courses-table-body');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    this.state.courses.forEach(course => {
      const row = this.createCourseRow(course);
      tbody.appendChild(row);
    });
    
    // 更新课程选择下拉框
    this.updateCourseSelects();
  },

  // 创建课程行
  createCourseRow(course) {
    const row = document.createElement('tr');
    row.className = 'hover:bg-gray-50';
    
    row.innerHTML = `
      <td class="px-6 py-4 whitespace-nowrap">${course.title}</td>
      <td class="px-6 py-4 whitespace-nowrap">${course.className}</td>
      <td class="px-6 py-4 whitespace-nowrap text-sm">
        <button class="text-red-500 hover:text-red-700" onclick="TeacherApp.deleteCourse(${course.id})">
          <i class="fas fa-trash"></i>
        </button>
      </td>
    `;
    
    return row;
  },

  // 创建课程
  async createCourse() {
    const classId = document.getElementById('course-class-id').value;
    const title = document.getElementById('course-title').value;
    const description = document.getElementById('course-description').value;
    
    if (!classId || !title) {
      EduSystem.Notification.show('请填写完整的课程信息', 'warning');
      return;
    }
    
    try {
      await EduSystem.API.course.create({
        classId,
        title,
        description
      });
      
      EduSystem.Notification.show('课程创建成功', 'success');
      document.getElementById('create-course-form').reset();
      await this.loadCourses();
    } catch (error) {
      console.error('创建课程失败:', error);
    }
  },

  // 删除课程
  async deleteCourse(courseId) {
    if (!confirm('确定要删除这个课程吗？此操作不可恢复。')) {
      return;
    }
    
    try {
      await EduSystem.API.course.delete(courseId);
      EduSystem.Notification.show('课程删除成功', 'success');
      await this.loadCourses();
    } catch (error) {
      console.error('删除课程失败:', error);
    }
  },

  // 加载作业列表
  async loadAssignments() {
    try {
      const response = await EduSystem.API.assignment.getList();
      this.state.assignments = response.data.map(EduSystem.DataTransform.transformAssignment);
      this.renderAssignments();
    } catch (error) {
      console.error('加载作业列表失败:', error);
    }
  },

  // 渲染作业列表
  renderAssignments() {
    const tbody = document.getElementById('assignments-table-body');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    this.state.assignments.forEach(assignment => {
      const row = this.createAssignmentRow(assignment);
      tbody.appendChild(row);
    });
    
    // 更新作业选择下拉框
    this.updateAssignmentSelects();
  },

  // 创建作业行
  createAssignmentRow(assignment) {
    const row = document.createElement('tr');
    row.className = 'hover:bg-gray-50';
    
    row.innerHTML = `
      <td class="px-6 py-4 whitespace-nowrap">${assignment.title}</td>
      <td class="px-6 py-4 whitespace-nowrap">${EduSystem.Utils.formatDateTime(assignment.deadline)}</td>
      <td class="px-6 py-4 whitespace-nowrap text-sm">
        <button class="text-red-500 hover:text-red-700" onclick="TeacherApp.deleteAssignment(${assignment.id})">
          <i class="fas fa-trash"></i>
        </button>
      </td>
    `;
    
    return row;
  },

  // 创建作业
  async createAssignment() {
    const courseId = document.getElementById('assignment-course-id').value;
    const title = document.getElementById('assignment-title').value;
    const deadline = document.getElementById('assignment-deadline').value;
    const description = document.getElementById('assignment-description').value;
    
    if (!courseId || !title || !deadline) {
      EduSystem.Notification.show('请填写完整的作业信息', 'warning');
      return;
    }
    
    try {
      await EduSystem.API.assignment.create({
        courseId,
        title,
        deadline,
        description
      });
      
      EduSystem.Notification.show('作业发布成功', 'success');
      document.getElementById('create-assignment-form').reset();
      await this.loadAssignments();
    } catch (error) {
      console.error('创建作业失败:', error);
    }
  },

  // 删除作业
  async deleteAssignment(assignmentId) {
    if (!confirm('确定要删除这个作业吗？此操作不可恢复。')) {
      return;
    }
    
    try {
      await EduSystem.API.assignment.delete(assignmentId);
      EduSystem.Notification.show('作业删除成功', 'success');
      await this.loadAssignments();
    } catch (error) {
      console.error('删除作业失败:', error);
    }
  },

  // 加载提交数据
  async loadSubmissionsData() {
    try {
      // 加载班级列表用于筛选
      await this.loadClasses();
      
      // 加载提交列表
      const response = await EduSystem.API.submission.getList();
      this.state.submissions = response.data.map(EduSystem.DataTransform.transformSubmission);
      this.renderSubmissions();
    } catch (error) {
      console.error('加载提交数据失败:', error);
    }
  },

  // 渲染提交列表
  renderSubmissions() {
    const tbody = document.getElementById('submissions-table-body');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    this.state.submissions.forEach(submission => {
      const row = this.createSubmissionRow(submission);
      tbody.appendChild(row);
    });
  },

  // 创建提交行
  createSubmissionRow(submission) {
    const row = document.createElement('tr');
    row.className = 'hover:bg-gray-50';
    
    const statusClass = this.getStatusClass(submission.status);
    const statusText = this.getStatusText(submission.status);
    
    row.innerHTML = `
      <td class="px-6 py-4 whitespace-nowrap">${submission.studentName}</td>
      <td class="px-6 py-4 whitespace-nowrap">${submission.studentEmail}</td>
      <td class="px-6 py-4 whitespace-nowrap">${EduSystem.Utils.formatDateTime(submission.submittedAt)}</td>
      <td class="px-6 py-4 whitespace-nowrap">
        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${statusClass}">
          ${statusText}
        </span>
      </td>
      <td class="px-6 py-4 whitespace-nowrap">${submission.score || '-'}</td>
      <td class="px-6 py-4 whitespace-nowrap text-sm">
        <button onclick="TeacherApp.viewSubmission(${submission.id})" class="text-blue-500 hover:text-blue-700">
          查看详情
        </button>
      </td>
    `;
    
    return row;
  },

  // 获取状态样式类
  getStatusClass(status) {
    const classMap = {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'SUBMITTED': 'bg-green-100 text-green-800',
      'GRADED': 'bg-blue-100 text-blue-800'
    };
    return classMap[status] || 'bg-gray-100 text-gray-800';
  },

  // 获取状态文本
  getStatusText(status) {
    const textMap = {
      'PENDING': '未提交',
      'SUBMITTED': '已提交',
      'GRADED': '已评分'
    };
    return textMap[status] || '未知';
  },

  // 查看提交详情
  viewSubmission(submissionId) {
    const submission = this.state.submissions.find(s => s.id === submissionId);
    if (!submission) return;
    
    this.state.currentSubmission = submission;
    
    // 填充模态框内容
    document.getElementById('submission-answer').textContent = submission.content || '暂无内容';
    document.getElementById('submission-attachment').textContent = submission.attachments[0]?.name || '无附件';
    document.getElementById('score-input').value = submission.score || '';
    document.getElementById('feedback-input').value = submission.feedback || '';
    
    // 显示模态框
    document.getElementById('submission-modal').classList.remove('hidden');
  },

  // 关闭提交详情模态框
  closeSubmissionModal() {
    document.getElementById('submission-modal').classList.add('hidden');
    this.state.currentSubmission = null;
  },

  // 评分提交
  async gradeSubmission() {
    if (!this.state.currentSubmission) return;
    
    const score = document.getElementById('score-input').value;
    const feedback = document.getElementById('feedback-input').value;
    
    if (!score || score < 0 || score > 100) {
      EduSystem.Notification.show('请输入有效的分数（0-100）', 'warning');
      return;
    }
    
    try {
      await EduSystem.API.submission.grade(this.state.currentSubmission.id, {
        score,
        feedback
      });
      
      EduSystem.Notification.show('评分成功', 'success');
      this.closeSubmissionModal();
      await this.loadSubmissionsData();
    } catch (error) {
      console.error('评分失败:', error);
    }
  },

  // 加载班级成员
  async loadMembers() {
    try {
      await this.loadClasses();
      
      if (this.state.classes.length > 0) {
        const firstClass = this.state.classes[0];
        await this.loadClassMembers(firstClass.id);
      }
    } catch (error) {
      console.error('加载班级成员失败:', error);
    }
  },

  // 加载指定班级的成员
  async loadClassMembers(classId) {
    try {
      const response = await EduSystem.API.class.getMembers(classId);
      const members = response.data.map(EduSystem.DataTransform.transformUser);
      this.renderMembers(members);
    } catch (error) {
      console.error('加载班级成员失败:', error);
    }
  },

  // 渲染成员列表
  renderMembers(members) {
    const tbody = document.getElementById('members-table-body');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    members.forEach(member => {
      const row = this.createMemberRow(member);
      tbody.appendChild(row);
    });
  },

  // 创建成员行
  createMemberRow(member) {
    const row = document.createElement('tr');
    row.className = 'hover:bg-gray-50';
    
    row.innerHTML = `
      <td class="px-6 py-4 whitespace-nowrap">${member.studentId || '-'}</td>
      <td class="px-6 py-4 whitespace-nowrap">${member.realName || member.username}</td>
      <td class="px-6 py-4 whitespace-nowrap">${member.email}</td>
      <td class="px-6 py-4 whitespace-nowrap">${EduSystem.Utils.formatDate(member.createdAt)}</td>
      <td class="px-6 py-4 whitespace-nowrap text-sm">
        <button class="text-blue-500 hover:text-blue-700 mr-3">查看提交</button>
        <button class="text-red-500 hover:text-red-700" onclick="TeacherApp.removeMember(${member.id})">移除成员</button>
      </td>
    `;
    
    return row;
  },

  // 移除成员
  async removeMember(userId) {
    if (!confirm('确定要移除这个成员吗？')) {
      return;
    }
    
    try {
      // 需要先获取当前班级ID
      const classId = this.state.classes[0]?.id;
      if (!classId) return;
      
      await EduSystem.API.class.removeMember(classId, userId);
      EduSystem.Notification.show('成员移除成功', 'success');
      await this.loadClassMembers(classId);
    } catch (error) {
      console.error('移除成员失败:', error);
    }
  },

  // 初始化讨论区
  async initDiscussions() {
    await this.loadClasses();
    this.updateDiscussionClassSelect();
  },

  // 更新讨论区班级选择
  updateDiscussionClassSelect() {
    const select = document.getElementById('discussion-class-select');
    if (!select) return;
    
    select.innerHTML = '<option value="">选择班级</option>';
    
    this.state.classes.forEach(cls => {
      const option = document.createElement('option');
      option.value = cls.id;
      option.textContent = cls.name;
      select.appendChild(option);
    });
  },

  // 加载讨论
  async loadDiscussions() {
    const classId = document.getElementById('discussion-class-select').value;
    if (!classId) {
      document.getElementById('discussions-selection').classList.remove('hidden');
      document.getElementById('discussions-view').classList.add('hidden');
      return;
    }
    
    try {
      const response = await EduSystem.API.discussion.getByClass(classId);
      const discussions = response.data.map(EduSystem.DataTransform.transformDiscussion);
      this.renderDiscussions(discussions);
      
      // 显示讨论区
      const cls = this.state.classes.find(c => c.id === parseInt(classId));
      document.getElementById('selected-class-name').textContent = cls?.name || '';
      document.getElementById('discussions-selection').classList.add('hidden');
      document.getElementById('discussions-view').classList.remove('hidden');
    } catch (error) {
      console.error('加载讨论失败:', error);
    }
  },

  // 渲染讨论
  renderDiscussions(discussions) {
    const container = document.querySelector('#discussions-view .space-y-6');
    if (!container) return;
    
    container.innerHTML = '';
    
    discussions.forEach(discussion => {
      const item = this.createDiscussionItem(discussion);
      container.appendChild(item);
    });
  },

  // 创建讨论项
  createDiscussionItem(discussion) {
    const item = document.createElement('div');
    item.className = 'border-b border-gray-100 pb-6 last:border-0';
    
    item.innerHTML = `
      <div class="flex items-start">
        <img src="${discussion.authorAvatar || '/img/default-avatar.png'}" alt="Avatar" class="w-10 h-10 rounded-full mr-4">
        <div class="flex-1">
          <div class="flex justify-between">
            <h4 class="font-semibold">${discussion.authorName}</h4>
            <span class="text-gray-500 text-sm">${EduSystem.Utils.getTimeAgo(discussion.createdAt)}</span>
          </div>
          <p class="mt-2 text-gray-700">${discussion.content}</p>
          <div class="mt-4 flex space-x-4">
            <button class="text-gray-500 hover:text-blue-500">
              <i class="far fa-thumbs-up mr-1"></i>${discussion.likeCount}
            </button>
            <button class="text-gray-500 hover:text-blue-500">
              <i class="far fa-comment mr-1"></i>回复
            </button>
          </div>
        </div>
      </div>
    `;
    
    return item;
  },

  // 显示讨论区选择
  showDiscussionSelection() {
    document.getElementById('discussions-view').classList.add('hidden');
    document.getElementById('discussions-selection').classList.remove('hidden');
    document.getElementById('discussion-class-select').value = '';
  },

  // 初始化云盘
  async initCloud() {
    await this.loadClasses();
    this.updateCloudClassSelect();
  },

  // 更新云盘班级选择
  updateCloudClassSelect() {
    const select = document.getElementById('cloud-class-select');
    if (!select) return;
    
    select.innerHTML = '<option value="">选择班级</option>';
    
    this.state.classes.forEach(cls => {
      const option = document.createElement('option');
      option.value = cls.id;
      option.textContent = cls.name;
      select.appendChild(option);
    });
  },

  // 加载云盘文件
  async loadCloudFiles() {
    const classId = document.getElementById('cloud-class-select').value;
    if (!classId) {
      document.getElementById('cloud-selection').classList.remove('hidden');
      document.getElementById('cloud-view').classList.add('hidden');
      return;
    }
    
    try {
      const response = await EduSystem.API.cloudStorage.getClassFiles(classId);
      const files = response.data.map(EduSystem.DataTransform.transformFile);
      this.renderCloudFiles(files);
      
      // 显示云盘
      const cls = this.state.classes.find(c => c.id === parseInt(classId));
      document.getElementById('selected-cloud-class-name').textContent = cls?.name || '';
      document.getElementById('cloud-selection').classList.add('hidden');
      document.getElementById('cloud-view').classList.remove('hidden');
    } catch (error) {
      console.error('加载云盘文件失败:', error);
    }
  },

  // 渲染云盘文件
  renderCloudFiles(files) {
    const grid = document.querySelector('#cloud-view .grid');
    if (!grid) return;
    
    grid.innerHTML = '';
    
    files.forEach(file => {
      const item = this.createFileItem(file);
      grid.appendChild(item);
    });
  },

  // 创建文件项
  createFileItem(file) {
    const item = document.createElement('div');
    item.className = 'border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer';
    
    const iconClass = this.getFileIconClass(file.type);
    
    item.innerHTML = `
      <div class="text-center">
        <i class="${iconClass} text-3xl mb-2"></i>
        <h3 class="font-medium truncate">${file.name}</h3>
        <p class="text-gray-500 text-sm">${EduSystem.Utils.formatFileSize(file.size)}</p>
        <button class="mt-2 text-blue-500 hover:text-blue-700 text-sm">
          <i class="fas fa-download mr-1"></i>下载
        </button>
      </div>
    `;
    
    return item;
  },

  // 获取文件图标类
  getFileIconClass(type) {
    const iconMap = {
      'pdf': 'fas fa-file-pdf text-red-500',
      'doc': 'fas fa-file-word text-blue-500',
      'docx': 'fas fa-file-word text-blue-500',
      'xls': 'fas fa-file-excel text-green-500',
      'xlsx': 'fas fa-file-excel text-green-500',
      'ppt': 'fas fa-file-powerpoint text-orange-500',
      'pptx': 'fas fa-file-powerpoint text-orange-500',
      'zip': 'fas fa-file-archive text-yellow-500',
      'rar': 'fas fa-file-archive text-yellow-500',
      'default': 'fas fa-file text-gray-500'
    };
    return iconMap[type.toLowerCase()] || iconMap.default;
  },

  // 显示云盘选择
  showCloudSelection() {
    document.getElementById('cloud-view').classList.add('hidden');
    document.getElementById('cloud-selection').classList.remove('hidden');
    document.getElementById('cloud-class-select').value = '';
  },

  // 根据班级加载课程
  async loadCoursesByClass() {
    const classId = document.getElementById('submission-class-id').value;
    const courseSelect = document.getElementById('submission-course-id');
    
    if (!classId) {
      courseSelect.disabled = true;
      courseSelect.innerHTML = '<option value="">请选择课程</option>';
      return;
    }
    
    try {
      const response = await EduSystem.API.course.getByClass(classId);
      const courses = response.data.map(EduSystem.DataTransform.transformCourse);
      
      courseSelect.disabled = false;
      courseSelect.innerHTML = '<option value="">请选择课程</option>';
      
      courses.forEach(course => {
        const option = document.createElement('option');
        option.value = course.id;
        option.textContent = course.title;
        courseSelect.appendChild(option);
      });
    } catch (error) {
      console.error('加载课程失败:', error);
    }
  },

  // 根据课程加载作业
  async loadAssignmentsByCourse() {
    const courseId = document.getElementById('submission-course-id').value;
    const assignmentSelect = document.getElementById('submission-assignment-id');
    
    if (!courseId) {
      assignmentSelect.disabled = true;
      assignmentSelect.innerHTML = '<option value="">请选择作业</option>';
      return;
    }
    
    try {
      const response = await EduSystem.API.assignment.getByCourse(courseId);
      const assignments = response.data.map(EduSystem.DataTransform.transformAssignment);
      
      assignmentSelect.disabled = false;
      assignmentSelect.innerHTML = '<option value="">请选择作业</option>';
      
      assignments.forEach(assignment => {
        const option = document.createElement('option');
        option.value = assignment.id;
        option.textContent = assignment.title;
        assignmentSelect.appendChild(option);
      });
    } catch (error) {
      console.error('加载作业失败:', error);
    }
  },

  // 加载提交
  async loadSubmissions() {
    const assignmentId = document.getElementById('submission-assignment-id').value;
    
    if (!assignmentId) {
      this.state.submissions = [];
      this.renderSubmissions();
      return;
    }
    
    try {
      const response = await EduSystem.API.submission.getByAssignment(assignmentId);
      this.state.submissions = response.data.map(EduSystem.DataTransform.transformSubmission);
      this.renderSubmissions();
    } catch (error) {
      console.error('加载提交失败:', error);
    }
  },

  // 更新班级选择下拉框
  updateClassSelects() {
    const selects = [
      'course-class-id',
      'submission-class-id'
    ];
    
    selects.forEach(selectId => {
      const select = document.getElementById(selectId);
      if (!select) return;
      
      const currentValue = select.value;
      select.innerHTML = '<option value="">请选择班级</option>';
      
      this.state.classes.forEach(cls => {
        const option = document.createElement('option');
        option.value = cls.id;
        option.textContent = cls.name;
        select.appendChild(option);
      });
      
      // 恢复之前的选择
      if (currentValue) {
        select.value = currentValue;
      }
    });
  },

  // 更新课程选择下拉框
  updateCourseSelects() {
    const selects = [
      'assignment-course-id'
    ];
    
    selects.forEach(selectId => {
      const select = document.getElementById(selectId);
      if (!select) return;
      
      const currentValue = select.value;
      select.innerHTML = '<option value="">请选择课程</option>';
      
      this.state.courses.forEach(course => {
        const option = document.createElement('option');
        option.value = course.id;
        option.textContent = course.title;
        select.appendChild(option);
      });
      
      // 恢复之前的选择
      if (currentValue) {
        select.value = currentValue;
      }
    });
  },

  // 更新作业选择下拉框
  updateAssignmentSelects() {
    const selects = [
      'submission-assignment-id'
    ];
    
    selects.forEach(selectId => {
      const select = document.getElementById(selectId);
      if (!select) return;
      
      const currentValue = select.value;
      select.innerHTML = '<option value="">请选择作业</option>';
      
      this.state.assignments.forEach(assignment => {
        const option = document.createElement('option');
        option.value = assignment.id;
        option.textContent = assignment.title;
        select.appendChild(option);
      });
      
      // 恢复之前的选择
      if (currentValue) {
        select.value = currentValue;
      }
    });
  }
};

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
  TeacherApp.init();
});

// 全局函数
window.TeacherApp = TeacherApp;