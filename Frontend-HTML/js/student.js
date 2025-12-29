/**
 * 学生端专用JavaScript功能
 * 包含学生学习空间的所有交互逻辑
 */

// 学生端应用对象
const StudentApp = {
  // 当前状态
  state: {
    activeTab: 'my-class',
    currentClass: null,
    currentCourse: null,
    currentAssignment: null,
    classes: [],
    courses: [],
    assignments: [],
    submissions: [],
    discussions: []
  },

  // 初始化应用
  init() {
    // 检查权限
    EduSystem.Page.init({
      requireAuth: true,
      requiredRole: 'STUDENT',
      title: '学生学习空间'
    });

    // 绑定事件
    this.bindEvents();
    
    // 加载初始数据
    this.loadInitialData();
    
    // 显示默认标签页
    this.switchTab('my-class');
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

    // 移动端菜单切换
    const mobileMenuToggle = document.getElementById('mobile-menu-toggle');
    if (mobileMenuToggle) {
      mobileMenuToggle.addEventListener('click', () => {
        this.toggleMobileSidebar();
      });
    }

    // 移动端遮罩点击
    const mobileOverlay = document.getElementById('mobile-overlay');
    if (mobileOverlay) {
      mobileOverlay.addEventListener('click', () => {
        this.closeMobileSidebar();
      });
    }

    // 加入班级表单
    const joinClassForm = document.getElementById('join-class-form');
    if (joinClassForm) {
      joinClassForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.joinClass();
      });
    }

    // 作业提交表单
    const submitAssignmentForm = document.getElementById('submit-assignment-form');
    if (submitAssignmentForm) {
      submitAssignmentForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.submitAssignment();
      });
    }

    // 文件上传
    const fileInput = document.getElementById('assignment-file');
    if (fileInput) {
      fileInput.addEventListener('change', (e) => {
        this.handleFileSelect(e);
      });
    }

    // 讨论创建表单
    const createDiscussionForm = document.getElementById('create-discussion-form');
    if (createDiscussionForm) {
      createDiscussionForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.createDiscussion();
      });
    }

    // 评论表单
    const commentForm = document.getElementById('comment-form');
    if (commentForm) {
      commentForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.addComment();
      });
    }

    // 返回按钮
    const backToClassBtn = document.getElementById('back-to-class-btn');
    if (backToClassBtn) {
      backToClassBtn.addEventListener('click', () => {
        this.switchTab('my-class');
      });
    }
  },

  // 从文本获取标签页名称
  getTabFromText(text) {
    const tabMap = {
      '我的班级': 'my-class',
      '班级成员': 'class-members',
      '课程信息': 'course-info',
      '作业提交': 'assignments',
      '班级讨论': 'discussions',
      '班级云盘': 'cloud-drive',
      '个人信息': 'profile'
    };
    return tabMap[text] || 'my-class';
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
    
    // 更新页面标题和描述
    this.updatePageHeader(tabName);
    
    // 加载对应数据
    this.loadTabData(tabName);
    
    // 关闭移动端侧边栏
    this.closeMobileSidebar();
  },

  // 获取标签页文本
  getTabText(tabName) {
    const textMap = {
      'my-class': '我的班级',
      'class-members': '班级成员',
      'course-info': '课程信息',
      'assignments': '作业提交',
      'discussions': '班级讨论',
      'cloud-drive': '班级云盘',
      'profile': '个人信息'
    };
    return textMap[tabName] || '';
  },

  // 更新页面标题和描述
  updatePageHeader(tabName) {
    const pageTitle = document.getElementById('page-title');
    const pageDescription = document.getElementById('page-description');
    
    const headers = {
      'my-class': {
        title: '我的班级',
        description: '管理您已加入的班级，查看班级成员和课程信息'
      },
      'class-members': {
        title: '班级成员',
        description: '查看班级中的所有成员信息'
      },
      'course-info': {
        title: '课程信息',
        description: '查看班级中的所有课程信息'
      },
      'assignments': {
        title: '作业提交',
        description: '查看并提交课程作业'
      },
      'discussions': {
        title: '班级讨论',
        description: '参与班级讨论，分享学习心得'
      },
      'cloud-drive': {
        title: '班级云盘',
        description: '下载班级共享的学习资源'
      },
      'profile': {
        title: '个人信息',
        description: '管理个人资料和账户设置'
      }
    };
    
    const header = headers[tabName];
    if (header) {
      if (pageTitle) pageTitle.textContent = header.title;
      if (pageDescription) pageDescription.textContent = header.description;
    }
  },

  // 加载标签页数据
  async loadTabData(tabName) {
    switch (tabName) {
      case 'my-class':
        await this.loadMyClasses();
        break;
      case 'class-members':
        await this.loadClassMembers();
        break;
      case 'course-info':
        await this.loadCourseInfo();
        break;
      case 'assignments':
        await this.loadAssignments();
        break;
      case 'discussions':
        await this.loadDiscussions();
        break;
      case 'cloud-drive':
        await this.loadCloudDrive();
        break;
      case 'profile':
        await this.loadProfile();
        break;
    }
  },

  // 加载初始数据
  async loadInitialData() {
    try {
      // 加载班级列表
      await this.loadMyClasses();
    } catch (error) {
      console.error('加载初始数据失败:', error);
    }
  },

  // 加载我的班级
  async loadMyClasses() {
    try {
      const response = await EduSystem.API.class.getList();
      this.state.classes = response.data.map(EduSystem.DataTransform.transformClass);
      this.renderMyClasses();
      
      // 如果有班级，加载第一个班级的成员
      if (this.state.classes.length > 0) {
        this.state.currentClass = this.state.classes[0];
        await this.loadClassMembers(this.state.currentClass.id);
      }
    } catch (error) {
      console.error('加载班级列表失败:', error);
    }
  },

  // 渲染我的班级
  renderMyClasses() {
    // 更新班级数量
    const classCount = document.getElementById('class-count');
    if (classCount) {
      classCount.textContent = `${this.state.classes.length} 个班级`;
    }
    
    // 渲染班级标签
    const classTabs = document.getElementById('class-tabs');
    if (!classTabs) return;
    
    classTabs.innerHTML = '';
    
    this.state.classes.forEach((cls, index) => {
      const tab = document.createElement('button');
      tab.className = `px-4 py-2 rounded-lg transition-colors ${
        index === 0 ? 'bg-blue-100 text-blue-700 font-medium' : 'bg-gray-100 hover:bg-gray-200'
      }`;
      tab.textContent = cls.name;
      tab.addEventListener('click', () => {
        this.selectClass(cls.id);
      });
      classTabs.appendChild(tab);
    });
  },

  // 选择班级
  async selectClass(classId) {
    this.state.currentClass = this.state.classes.find(c => c.id === classId);
    
    // 更新标签样式
    const classTabs = document.getElementById('class-tabs');
    if (classTabs) {
      classTabs.querySelectorAll('button').forEach((tab, index) => {
        const cls = this.state.classes[index];
        if (cls && cls.id === classId) {
          tab.className = 'px-4 py-2 rounded-lg transition-colors bg-blue-100 text-blue-700 font-medium';
        } else {
          tab.className = 'px-4 py-2 rounded-lg transition-colors bg-gray-100 hover:bg-gray-200';
        }
      });
    }
    
    // 重新加载当前标签页的数据
    await this.loadTabData(this.state.activeTab);
  },

  // 加入班级
  async joinClass() {
    const inviteCode = document.querySelector('#join-class-form input').value;
    
    if (!inviteCode) {
      EduSystem.Notification.show('请输入班级邀请码', 'warning');
      return;
    }
    
    try {
      await EduSystem.API.class.join({ inviteCode });
      EduSystem.Notification.show('加入班级成功', 'success');
      document.querySelector('#join-class-form input').value = '';
      await this.loadMyClasses();
    } catch (error) {
      console.error('加入班级失败:', error);
    }
  },

  // 加载班级成员
  async loadClassMembers(classId) {
    const targetClassId = classId || this.state.currentClass?.id;
    if (!targetClassId) return;
    
    try {
      const response = await EduSystem.API.class.getMembers(targetClassId);
      const members = response.data.map(EduSystem.DataTransform.transformUser);
      this.renderClassMembers(members);
    } catch (error) {
      console.error('加载班级成员失败:', error);
    }
  },

  // 渲染班级成员
  renderClassMembers(members) {
    const tableBody = document.getElementById('class-members-table');
    if (!tableBody) return;
    
    tableBody.innerHTML = '';
    
    members.forEach(member => {
      const row = this.createMemberRow(member);
      tableBody.appendChild(row);
    });
    
    // 更新当前班级显示
    const currentClassElement = document.querySelector('#class-members-content strong');
    if (currentClassElement && this.state.currentClass) {
      currentClassElement.textContent = `${this.state.currentClass.name} (${this.state.currentClass.inviteCode})`;
    }
  },

  // 创建成员行
  createMemberRow(member) {
    const row = document.createElement('tr');
    
    row.innerHTML = `
      <td class="px-4 py-3 text-sm text-gray-900">${member.studentId || '-'}</td>
      <td class="px-4 py-3 text-sm text-gray-900">${member.realName || member.username}</td>
      <td class="px-4 py-3 text-sm text-gray-900">${member.email}</td>
      <td class="px-4 py-3 text-sm text-gray-900">${member.phone || '-'}</td>
      <td class="px-4 py-3 text-sm text-gray-900">${EduSystem.Utils.formatDate(member.createdAt)}</td>
    `;
    
    return row;
  },

  // 加载课程信息
  async loadCourseInfo() {
    if (!this.state.currentClass) return;
    
    try {
      const response = await EduSystem.API.course.getByClass(this.state.currentClass.id);
      this.state.courses = response.data.map(EduSystem.DataTransform.transformCourse);
      this.renderCourseInfo();
    } catch (error) {
      console.error('加载课程信息失败:', error);
    }
  },

  // 渲染课程信息
  renderCourseInfo() {
    const tableBody = document.querySelector('#course-info-content tbody');
    if (!tableBody) return;
    
    tableBody.innerHTML = '';
    
    if (this.state.courses.length === 0) {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td colspan="3" class="px-4 py-8 text-center text-gray-500">
          暂无课程信息
        </td>
      `;
      tableBody.appendChild(row);
      return;
    }
    
    this.state.courses.forEach(course => {
      const row = this.createCourseRow(course);
      tableBody.appendChild(row);
    });
  },

  // 创建课程行
  createCourseRow(course) {
    const row = document.createElement('tr');
    
    row.innerHTML = `
      <td class="px-4 py-3 text-sm font-medium text-gray-900">${course.title}</td>
      <td class="px-4 py-3 text-sm text-gray-500">${course.description || '暂无描述'}</td>
      <td class="px-4 py-3 text-sm">
        <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800">
          进行中
        </span>
      </td>
    `;
    
    return row;
  },

  // 加载作业
  async loadAssignments() {
    try {
      const response = await EduSystem.API.assignment.getStudentAssignments();
      this.state.assignments = response.data.map(EduSystem.DataTransform.transformAssignment);
      this.renderAssignments();
    } catch (error) {
      console.error('加载作业失败:', error);
    }
  },

  // 渲染作业
  renderAssignments() {
    const container = document.getElementById('assignments-content');
    if (!container) return;
    
    // 清空现有内容
    const existingCards = container.querySelectorAll('.assignment-card');
    existingCards.forEach(card => card.remove());
    
    if (this.state.assignments.length === 0) {
      const emptyMessage = document.createElement('div');
      emptyMessage.className = 'text-center py-12 text-gray-500';
      emptyMessage.textContent = '暂无作业';
      container.appendChild(emptyMessage);
      return;
    }
    
    this.state.assignments.forEach(assignment => {
      const card = this.createAssignmentCard(assignment);
      container.appendChild(card);
    });
  },

  // 创建作业卡片
  createAssignmentCard(assignment) {
    const card = document.createElement('div');
    card.className = 'assignment-card';
    
    const status = this.getAssignmentStatus(assignment);
    const statusClass = this.getAssignmentStatusClass(status);
    
    card.innerHTML = `
      <div class="assignment-header">
        <h3 class="assignment-title">${assignment.title}</h3>
        <div class="assignment-deadline">
          <i class="far fa-clock"></i>
          ${EduSystem.Utils.formatDateTime(assignment.deadline)}
        </div>
      </div>
      <div class="assignment-description">${assignment.description || '暂无描述'}</div>
      <div class="d-flex justify-content-between align-items-center">
        <span class="assignment-status ${statusClass}">${status}</span>
        <button class="student-btn student-btn-primary" onclick="StudentApp.viewAssignment(${assignment.id})">
          查看详情
        </button>
      </div>
    `;
    
    return card;
  },

  // 获取作业状态
  getAssignmentStatus(assignment) {
    const now = new Date();
    const deadline = new Date(assignment.deadline);
    const submission = this.state.submissions.find(s => s.assignmentId === assignment.id);
    
    if (submission) {
      if (submission.score !== null) {
        return '已评分';
      }
      return '已提交';
    }
    
    if (now > deadline) {
      return '已逾期';
    }
    
    return '待提交';
  },

  // 获取作业状态样式类
  getAssignmentStatusClass(status) {
    const classMap = {
      '待提交': 'pending',
      '已提交': 'submitted',
      '已评分': 'submitted',
      '已逾期': 'overdue'
    };
    return classMap[status] || 'pending';
  },

  // 查看作业详情
  async viewAssignment(assignmentId) {
    const assignment = this.state.assignments.find(a => a.id === assignmentId);
    if (!assignment) return;
    
    this.state.currentAssignment = assignment;
    
    // 查找已有提交
    try {
      const response = await EduSystem.API.submission.getByAssignment(assignmentId);
      const submissions = response.data.map(EduSystem.DataTransform.transformSubmission);
      const mySubmission = submissions.find(s => s.studentId === EduSystem.Utils.getCurrentUser().id);
      
      this.showAssignmentModal(assignment, mySubmission);
    } catch (error) {
      console.error('加载作业提交失败:', error);
      this.showAssignmentModal(assignment);
    }
  },

  // 显示作业模态框
  showAssignmentModal(assignment, submission = null) {
    const modal = EduSystem.Modal.show({
      title: assignment.title,
      size: 'large',
      content: this.getAssignmentModalContent(assignment, submission)
    });
    
    // 绑定提交表单事件
    const form = document.getElementById('submit-assignment-form');
    if (form) {
      form.addEventListener('submit', (e) => {
        e.preventDefault();
        this.submitAssignment(modal);
      });
    }
  },

  // 获取作业模态框内容
  getAssignmentModalContent(assignment, submission) {
    const isSubmitted = !!submission;
    const isGraded = submission && submission.score !== null;
    
    return `
      <div class="assignment-detail">
        <div class="mb-4">
          <h4>作业描述</h4>
          <p>${assignment.description || '暂无描述'}</p>
        </div>
        
        <div class="mb-4">
          <h4>截止时间</h4>
          <p>${EduSystem.Utils.formatDateTime(assignment.deadline)}</p>
        </div>
        
        ${isGraded ? `
          <div class="mb-4">
            <h4>评分</h4>
            <p class="text-2xl font-bold text-blue-600">${submission.score} 分</p>
          </div>
          
          <div class="mb-4">
            <h4>教师反馈</h4>
            <p>${submission.feedback || '暂无反馈'}</p>
          </div>
        ` : ''}
        
        ${!isSubmitted ? `
          <form id="submit-assignment-form">
            <div class="student-form-group">
              <label class="student-form-label">作业内容</label>
              <textarea class="student-form-control student-form-textarea" 
                        id="assignment-content" 
                        rows="6" 
                        placeholder="请输入作业内容..."></textarea>
            </div>
            
            <div class="student-form-group">
              <label class="student-form-label">附件上传</label>
              <input type="file" class="student-form-control" id="assignment-file" multiple>
              <div class="mt-2 text-sm text-gray-500">支持多文件上传</div>
            </div>
            
            <div class="d-flex justify-content-end">
              <button type="submit" class="student-btn student-btn-primary">
                提交作业
              </button>
            </div>
          </form>
        ` : `
          <div class="alert alert-info">
            <i class="fas fa-check-circle mr-2"></i>
            您已于 ${EduSystem.Utils.formatDateTime(submission.submittedAt)} 提交此作业
            ${isGraded ? '，教师已完成评分。' : '，等待教师批改。'}
          </div>
        `}
      </div>
    `;
  },

  // 提交作业
  async submitAssignment(modal) {
    if (!this.state.currentAssignment) return;
    
    const content = document.getElementById('assignment-content').value;
    const fileInput = document.getElementById('assignment-file');
    
    if (!content.trim() && fileInput.files.length === 0) {
      EduSystem.Notification.show('请输入作业内容或上传附件', 'warning');
      return;
    }
    
    try {
      const submissionData = {
        assignmentId: this.state.currentAssignment.id,
        content: content.trim()
      };
      
      // 如果有文件，先上传文件
      if (fileInput.files.length > 0) {
        const formData = new FormData();
        Array.from(fileInput.files).forEach(file => {
          formData.append('files', file);
        });
        
        // 这里应该先上传文件获取文件ID，然后提交作业
        EduSystem.Notification.show('文件上传功能开发中...', 'info');
      }
      
      // 提交作业
      await EduSystem.API.submission.create(submissionData);
      
      EduSystem.Notification.show('作业提交成功', 'success');
      EduSystem.Modal.hide(modal);
      
      // 重新加载作业列表
      await this.loadAssignments();
    } catch (error) {
      console.error('提交作业失败:', error);
    }
  },

  // 处理文件选择
  handleFileSelect(event) {
    const files = event.target.files;
    const fileNames = Array.from(files).map(file => file.name).join(', ');
    
    // 显示已选择的文件名
    const fileInfo = document.createElement('div');
    fileInfo.className = 'mt-2 text-sm text-gray-600';
    fileInfo.textContent = `已选择: ${fileNames}`;
    
    // 移除之前的文件信息
    const existingInfo = event.target.parentNode.querySelector('.text-gray-600');
    if (existingInfo) {
      existingInfo.remove();
    }
    
    event.target.parentNode.appendChild(fileInfo);
  },

  // 加载讨论
  async loadDiscussions() {
    if (!this.state.currentClass) return;
    
    try {
      const response = await EduSystem.API.discussion.getByClass(this.state.currentClass.id);
      this.state.discussions = response.data.map(EduSystem.DataTransform.transformDiscussion);
      this.renderDiscussions();
    } catch (error) {
      console.error('加载讨论失败:', error);
    }
  },

  // 渲染讨论
  renderDiscussions() {
    const container = document.getElementById('discussions-content');
    if (!container) return;
    
    // 清空现有内容
    const existingItems = container.querySelectorAll('.discussion-item');
    existingItems.forEach(item => item.remove());
    
    if (this.state.discussions.length === 0) {
      const emptyMessage = document.createElement('div');
      emptyMessage.className = 'text-center py-12 text-gray-500';
      emptyMessage.textContent = '暂无讨论';
      container.appendChild(emptyMessage);
      return;
    }
    
    this.state.discussions.forEach(discussion => {
      const item = this.createDiscussionItem(discussion);
      container.appendChild(item);
    });
  },

  // 创建讨论项
  createDiscussionItem(discussion) {
    const item = document.createElement('div');
    item.className = 'discussion-item';
    
    item.innerHTML = `
      <div class="discussion-header">
        <img src="${discussion.authorAvatar || '/img/default-avatar.png'}" 
             alt="${discussion.authorName}" 
             class="discussion-avatar">
        <div class="discussion-meta">
          <div class="discussion-author">${discussion.authorName}</div>
          <div class="discussion-time">${EduSystem.Utils.getTimeAgo(discussion.createdAt)}</div>
        </div>
      </div>
      <div class="discussion-content">${discussion.content}</div>
      <div class="discussion-actions">
        <button class="discussion-action" onclick="StudentApp.likeDiscussion(${discussion.id})">
          <i class="${discussion.isLiked ? 'fas' : 'far'} fa-thumbs-up"></i>
          ${discussion.likeCount}
        </button>
        <button class="discussion-action" onclick="StudentApp.viewDiscussion(${discussion.id})">
          <i class="far fa-comment"></i>
          ${discussion.commentCount}
        </button>
      </div>
    `;
    
    return item;
  },

  // 点赞讨论
  async likeDiscussion(discussionId) {
    try {
      const discussion = this.state.discussions.find(d => d.id === discussionId);
      if (!discussion) return;
      
      if (discussion.isLiked) {
        await EduSystem.API.discussion.unlike(discussionId);
        discussion.likeCount--;
        discussion.isLiked = false;
      } else {
        await EduSystem.API.discussion.like(discussionId);
        discussion.likeCount++;
        discussion.isLiked = true;
      }
      
      // 重新渲染讨论
      this.renderDiscussions();
    } catch (error) {
      console.error('点赞失败:', error);
    }
  },

  // 查看讨论详情
  async viewDiscussion(discussionId) {
    const discussion = this.state.discussions.find(d => d.id === discussionId);
    if (!discussion) return;
    
    try {
      // 加载评论
      const response = await EduSystem.API.comment.getList(discussionId);
      const comments = response.data.map(EduSystem.DataTransform.transformComment);
      
      this.showDiscussionModal(discussion, comments);
    } catch (error) {
      console.error('加载讨论详情失败:', error);
    }
  },

  // 显示讨论模态框
  showDiscussionModal(discussion, comments) {
    const modal = EduSystem.Modal.show({
      title: '讨论详情',
      size: 'large',
      content: this.getDiscussionModalContent(discussion, comments)
    });
    
    // 绑定评论表单事件
    const form = document.getElementById('comment-form');
    if (form) {
      form.addEventListener('submit', (e) => {
        e.preventDefault();
        this.addComment(discussion.id, modal);
      });
    }
  },

  // 获取讨论模态框内容
  getDiscussionModalContent(discussion, comments) {
    return `
      <div class="discussion-detail">
        <div class="discussion-item mb-4">
          <div class="discussion-header">
            <img src="${discussion.authorAvatar || '/img/default-avatar.png'}" 
                 alt="${discussion.authorName}" 
                 class="discussion-avatar">
            <div class="discussion-meta">
              <div class="discussion-author">${discussion.authorName}</div>
              <div class="discussion-time">${EduSystem.Utils.formatDateTime(discussion.createdAt)}</div>
            </div>
          </div>
          <div class="discussion-content">${discussion.content}</div>
        </div>
        
        <div class="comments-section">
          <h5>评论 (${comments.length})</h5>
          <div class="comments-list">
            ${comments.map(comment => this.createCommentHTML(comment)).join('')}
          </div>
        </div>
        
        <form id="comment-form" class="mt-4">
          <div class="student-form-group">
            <textarea class="student-form-control student-form-textarea" 
                      id="comment-content" 
                      rows="3" 
                      placeholder="写下你的评论..."></textarea>
          </div>
          <div class="d-flex justify-content-end">
            <button type="submit" class="student-btn student-btn-primary">
              发表评论
            </button>
          </div>
        </form>
      </div>
    `;
  },

  // 创建评论HTML
  createCommentHTML(comment) {
    return `
      <div class="comment-item mb-3">
        <div class="d-flex">
          <img src="${comment.authorAvatar || '/img/default-avatar.png'}" 
               alt="${comment.authorName}" 
               class="discussion-avatar">
          <div class="flex-1 ml-3">
            <div class="d-flex justify-content-between align-items-center">
              <div class="discussion-author">${comment.authorName}</div>
              <div class="discussion-time">${EduSystem.Utils.getTimeAgo(comment.createdAt)}</div>
            </div>
            <div class="discussion-content">${comment.content}</div>
          </div>
        </div>
      </div>
    `;
  },

  // 添加评论
  async addComment(discussionId, modal) {
    const content = document.getElementById('comment-content').value;
    
    if (!content.trim()) {
      EduSystem.Notification.show('请输入评论内容', 'warning');
      return;
    }
    
    try {
      await EduSystem.API.comment.create(discussionId, { content });
      
      EduSystem.Notification.show('评论发表成功', 'success');
      document.getElementById('comment-content').value = '';
      
      // 重新加载讨论
      await this.loadDiscussions();
      
      // 更新模态框内容
      await this.viewDiscussion(discussionId);
    } catch (error) {
      console.error('发表评论失败:', error);
    }
  },

  // 加载云盘
  async loadCloudDrive() {
    if (!this.state.currentClass) return;
    
    try {
      const response = await EduSystem.API.cloudStorage.getClassFiles(this.state.currentClass.id);
      const files = response.data.map(EduSystem.DataTransform.transformFile);
      this.renderCloudDrive(files);
    } catch (error) {
      console.error('加载云盘文件失败:', error);
    }
  },

  // 渲染云盘
  renderCloudDrive(files) {
    const grid = document.querySelector('#cloud-drive-content .file-grid');
    if (!grid) return;
    
    grid.innerHTML = '';
    
    if (files.length === 0) {
      const emptyMessage = document.createElement('div');
      emptyMessage.className = 'col-12 text-center py-12 text-gray-500';
      emptyMessage.textContent = '暂无文件';
      grid.appendChild(emptyMessage);
      return;
    }
    
    files.forEach(file => {
      const item = this.createFileItem(file);
      grid.appendChild(item);
    });
  },

  // 创建文件项
  createFileItem(file) {
    const item = document.createElement('div');
    item.className = 'file-item';
    
    const iconClass = this.getFileIconClass(file.type);
    
    item.innerHTML = `
      <div class="file-icon ${iconClass}">
        <i class="${this.getFileIcon(file.type)}"></i>
      </div>
      <div class="file-name">${file.name}</div>
      <div class="file-size">${EduSystem.Utils.formatFileSize(file.size)}</div>
      <button class="file-download" onclick="StudentApp.downloadFile(${file.id})">
        <i class="fas fa-download mr-1"></i>下载
      </button>
    `;
    
    return item;
  },

  // 获取文件图标
  getFileIcon(type) {
    const iconMap = {
      'pdf': 'fas fa-file-pdf',
      'doc': 'fas fa-file-word',
      'docx': 'fas fa-file-word',
      'xls': 'fas fa-file-excel',
      'xlsx': 'fas fa-file-excel',
      'ppt': 'fas fa-file-powerpoint',
      'pptx': 'fas fa-file-powerpoint',
      'zip': 'fas fa-file-archive',
      'rar': 'fas fa-file-archive',
      'default': 'fas fa-file'
    };
    return iconMap[type.toLowerCase()] || iconMap.default;
  },

  // 获取文件图标类
  getFileIconClass(type) {
    const classMap = {
      'pdf': 'pdf',
      'doc': 'doc',
      'docx': 'doc',
      'xls': 'xls',
      'xlsx': 'xls',
      'ppt': 'ppt',
      'pptx': 'ppt',
      'zip': 'zip',
      'rar': 'zip',
      'default': ''
    };
    return classMap[type.toLowerCase()] || classMap.default;
  },

  // 下载文件
  async downloadFile(fileId) {
    try {
      const response = await EduSystem.API.cloudStorage.downloadFile(fileId);
      
      // 创建下载链接
      const url = window.URL.createObjectURL(new Blob([response]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'file');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
      EduSystem.Notification.show('文件下载成功', 'success');
    } catch (error) {
      console.error('下载文件失败:', error);
    }
  },

  // 加载个人信息
  async loadProfile() {
    try {
      const response = await EduSystem.API.user.getProfile();
      const user = EduSystem.DataTransform.transformUser(response.data);
      this.renderProfile(user);
    } catch (error) {
      console.error('加载个人信息失败:', error);
    }
  },

  // 渲染个人信息
  renderProfile(user) {
    const profileForm = document.getElementById('profile-form');
    if (!profileForm) return;
    
    // 填充表单数据
    const fields = {
      'profile-username': user.username,
      'profile-email': user.email,
      'profile-realName': user.realName || '',
      'profile-studentId': user.studentId || '',
      'profile-phone': user.phone || ''
    };
    
    Object.entries(fields).forEach(([fieldId, value]) => {
      const field = document.getElementById(fieldId);
      if (field) field.value = value;
    });
    
    // 更新头像
    const avatarImg = document.getElementById('profile-avatar');
    if (avatarImg && user.avatar) {
      avatarImg.src = user.avatar;
    }
  },

  // 切换移动端侧边栏
  toggleMobileSidebar() {
    const sidebar = document.getElementById('mobile-sidebar');
    const overlay = document.getElementById('mobile-overlay');
    
    if (sidebar && overlay) {
      sidebar.classList.toggle('open');
      overlay.classList.toggle('hidden');
    }
  },

  // 关闭移动端侧边栏
  closeMobileSidebar() {
    const sidebar = document.getElementById('mobile-sidebar');
    const overlay = document.getElementById('mobile-overlay');
    
    if (sidebar && overlay) {
      sidebar.classList.remove('open');
      overlay.classList.add('hidden');
    }
  }
};

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
  StudentApp.init();
});

// 全局函数
window.StudentApp = StudentApp;