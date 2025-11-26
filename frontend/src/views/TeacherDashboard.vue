<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { assignmentApi, authApi, classApi, courseApi, enrollmentApi, submissionApi, discussionApi, cloudFileApi } from '../services/api';
import { clearAuth, getAuth } from '../services/auth';
import DiscussionList from '../components/DiscussionList.vue';
import DiscussionDetail from '../components/DiscussionDetail.vue';
import CreateDiscussion from '../components/CreateDiscussion.vue';
import CloudDrive from '../components/CloudDrive.vue';

const router = useRouter();
const auth = getAuth();

if (!auth.userId || auth.role !== 'TEACHER') {
  router.replace('/login');
}

const currentPage = ref('classes');

const loading = reactive({
  classes: false,
  courses: false,
  assignments: false,
  enrollments: false,
  submissions: false,
  memberSubmissions: false,
});

const toast = reactive({ type: 'info', text: '' });
const state = reactive({
  classes: [],
  courses: [],
  assignments: [],
  enrollments: [],
  submissions: [],
  memberSubmissions: [],
  selectedClassId: '',
  selectedCourseId: '',
  selectedAssignmentId: '',
  selectedStudentId: '',
  discussionView: 'list', // 'list', 'detail', 'create'
  selectedDiscussionId: null,
  classForm: {
    name: '',
    code: '',
  },
  courseForm: {
    title: '',
    description: '',
  },
  assignmentForm: {
    title: '',
    description: '',
    deadline: '',
  },
  gradingForm: {
    score: '',
    feedback: '',
  },
});

const showToast = (type, text) => {
  toast.type = type;
  toast.text = text;
  setTimeout(() => {
    toast.text = '';
  }, 3500);
};

const logout = () => {
  clearAuth();
  router.replace('/login');
};

const switchPage = (page) => {
  currentPage.value = page;
};

const pageTitle = computed(() => {
  const titles = {
    classes: '班级管理',
    courses: '课程管理',
    assignments: '作业发布',
    members: '班级成员',
    submissions: '提交管理',
    submissionDetail: '提交详情',
    discussions: '班级讨论',
    'cloud-drive': '班级云盘'
  };
  return titles[currentPage.value] || '控制面板';
});

const pageSubtitle = computed(() => {
  const subtitles = {
    classes: '创建和管理您的教学班级',
    courses: '为班级添加和管理课程',
    assignments: '发布和管理课程作业',
    members: '查看和管理班级学生成员',
    submissions: '查看学生作业提交情况',
    submissionDetail: '查看学生提交的具体内容',
    discussions: '管理班级讨论和学生交流',
    'cloud-drive': '管理班级共享文件和资源'
  };
  return subtitles[currentPage.value] || '教师端控制面板';
});

const fetchClasses = async () => {
  loading.classes = true;
  try {
    state.classes = await classApi.listByTeacher(auth.userId);
    if (!state.selectedClassId && state.classes.length > 0) {
      state.selectedClassId = state.classes[0].id;
    }
  } catch (error) {
    showToast('error', error.message || '获取班级失败');
  } finally {
    loading.classes = false;
  }
};

const fetchCourses = async () => {
  if (!state.selectedClassId) {
    state.courses = [];
    return;
  }
  loading.courses = true;
  try {
    state.courses = await courseApi.listByClass(state.selectedClassId);
    if (state.courses.length === 0) {
      state.selectedCourseId = '';
    } else if (!state.selectedCourseId) {
      state.selectedCourseId = state.courses[0].id;
    }
  } catch (error) {
    showToast('error', error.message || '获取课程失败');
  } finally {
    loading.courses = false;
  }
};

const fetchAssignments = async () => {
  if (!state.selectedCourseId) {
    state.assignments = [];
    return;
  }
  loading.assignments = true;
  try {
    state.assignments = await assignmentApi.listByCourse(state.selectedCourseId);
  } catch (error) {
    showToast('error', error.message || '获取作业失败');
  } finally {
    loading.assignments = false;
  }
};

const fetchEnrollments = async () => {
  if (!state.selectedClassId) {
    state.enrollments = [];
    return;
  }
  loading.enrollments = true;
  try {
    const enrollments = await enrollmentApi.listByClass(state.selectedClassId);
    
    // 获取学生详细信息
    const enrollmentsWithStudentInfo = await Promise.all(
      enrollments.map(async (enrollment) => {
        try {
          const profile = await authApi.getUserProfile(enrollment.studentId);
          return {
            ...enrollment,
            studentName: profile.realName,
            studentEmail: profile.email,
          };
        } catch (error) {
          return {
            ...enrollment,
            studentName: null,
            studentEmail: null,
          };
        }
      })
    );
    
    state.enrollments = enrollmentsWithStudentInfo;
  } catch (error) {
    showToast('error', error.message || '获取班级成员失败');
  } finally {
    loading.enrollments = false;
  }
};

const submitClass = async () => {
  if (!state.classForm.name || !state.classForm.code) {
    showToast('warning', '请输入班级名称与邀请码');
    return;
  }
  try {
    await classApi.create({ ...state.classForm, teacherId: auth.userId });
    state.classForm.name = '';
    state.classForm.code = '';
    showToast('success', '班级创建成功');
    await fetchClasses();
  } catch (error) {
    showToast('error', error.message || '创建班级失败');
  }
};

const submitCourse = async () => {
  if (!state.selectedClassId) {
    showToast('warning', '请先选择班级');
    return;
  }
  if (!state.courseForm.title) {
    showToast('warning', '请输入课程标题');
    return;
  }
  try {
    await courseApi.create({
      classId: state.selectedClassId,
      title: state.courseForm.title,
      description: state.courseForm.description,
      teacherId: auth.userId,
    });
    state.courseForm.title = '';
    state.courseForm.description = '';
    showToast('success', '课程创建成功');
    await fetchCourses();
  } catch (error) {
    showToast('error', error.message || '创建课程失败');
  }
};

const submitAssignment = async () => {
  if (!state.selectedCourseId) {
    showToast('warning', '请选择课程');
    return;
  }
  if (!state.assignmentForm.title) {
    showToast('warning', '请输入作业标题');
    return;
  }
  try {
    await assignmentApi.create({
      courseId: state.selectedCourseId,
      title: state.assignmentForm.title,
      description: state.assignmentForm.description,
      deadline: state.assignmentForm.deadline || null,
    });
    Object.assign(state.assignmentForm, { title: '', description: '', deadline: '' });
    showToast('success', '作业发布成功');
    await fetchAssignments();
  } catch (error) {
    showToast('error', error.message || '发布作业失败');
  }
};

const deleteCourse = async (id) => {
  if (!confirm('确认删除该课程？')) return;
  try {
    await courseApi.delete(id);
    showToast('success', '课程已删除');
    if (state.selectedCourseId === id) {
      state.selectedCourseId = '';
    }
    await fetchCourses();
    await fetchAssignments();
  } catch (error) {
    showToast('error', error.message || '删除课程失败');
  }
};

const deleteAssignment = async (id) => {
  if (!confirm('确认删除该作业？')) return;
  try {
    await assignmentApi.delete(id);
    showToast('success', '作业已删除');
    await fetchAssignments();
  } catch (error) {
    showToast('error', error.message || '删除作业失败');
  }
};

const removeStudent = async (studentId) => {
  showToast('warning', '当前后端未提供移除成员接口，可在数据库中手动处理');
};

const fetchSubmissions = async () => {
  if (!state.selectedAssignmentId) {
    state.submissions = [];
    return;
  }
  loading.submissions = true;
  try {
    const submissions = await submissionApi.listByAssignment(state.selectedAssignmentId);
    
    // 获取学生信息
    const submissionsWithStudentInfo = await Promise.all(
      submissions.map(async (submission) => {
        try {
          const profile = await authApi.getUserProfile(submission.studentId);
          return {
            ...submission,
            studentName: profile.realName || `学生${submission.studentId}`,
            studentEmail: profile.email,
          };
        } catch (error) {
          return {
            ...submission,
            studentName: `学生${submission.studentId}`,
            studentEmail: '未知',
          };
        }
      })
    );
    
    state.submissions = submissionsWithStudentInfo;
  } catch (error) {
    showToast('error', error.message || '获取提交列表失败');
  } finally {
    loading.submissions = false;
  }
};

const fetchMemberSubmissions = async () => {
  if (!state.selectedClassId || !state.selectedStudentId) {
    state.memberSubmissions = [];
    return;
  }
  
  // 如果没有选择课程，先获取该班级的第一个课程
  if (!state.selectedCourseId && state.courses.length > 0) {
    state.selectedCourseId = state.courses[0].id;
  }
  
  if (!state.selectedCourseId) {
    showToast('warning', '请先选择课程');
    state.memberSubmissions = [];
    return;
  }
  
  loading.memberSubmissions = true;
  try {
    // 获取该课程的所有作业
    const assignments = await assignmentApi.listByCourse(state.selectedCourseId);
    
    // 获取该学生在每个作业的提交情况
    const memberSubmissions = await Promise.all(
      assignments.map(async (assignment) => {
        try {
          const submission = await submissionApi.findUnique(assignment.id, state.selectedStudentId);
          return {
            ...assignment,
            submission: submission,
            status: submission ? '已提交' : '未提交'
          };
        } catch (error) {
          return {
            ...assignment,
            submission: null,
            status: '未提交'
          };
        }
      })
    );
    
    state.memberSubmissions = memberSubmissions;
  } catch (error) {
    showToast('error', error.message || '获取学生提交情况失败');
    state.memberSubmissions = [];
  } finally {
    loading.memberSubmissions = false;
  }
};

const viewSubmissionDetail = (submissionId, studentId) => {
  state.selectedStudentId = studentId;
  fetchMemberSubmissions();
  switchPage('submissionDetail');
};

const viewStudentSubmissions = (studentId) => {
  state.selectedStudentId = studentId;
  fetchMemberSubmissions();
  switchPage('submissions');
};

const gradeSubmission = async (submissionId) => {
  if (!state.gradingForm.score || !state.gradingForm.feedback) {
    showToast('warning', '请输入评分和反馈');
    return;
  }
  try {
    await submissionApi.grade(submissionId, state.gradingForm.score, state.gradingForm.feedback);
    showToast('success', '评分成功');
    state.gradingForm.score = '';
    state.gradingForm.feedback = '';
    await fetchSubmissions();
  } catch (error) {
    showToast('error', error.message || '评分失败');
  }
};

const isImageFile = (filePath) => {
  if (!filePath) return false;
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg'];
  const extension = filePath.substring(filePath.lastIndexOf('.')).toLowerCase();
  return imageExtensions.includes(extension);
};

const downloadFile = async (fileUrl, originalFileName) => {
  if (!fileUrl) return;
  
  try {
    // 构建完整的文件URL
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    const fullUrl = fileUrl.startsWith('http') ? fileUrl : `${baseUrl}${fileUrl}`;
    
    // 使用原始文件名，如果没有则从URL中提取
    const fileName = originalFileName || fileUrl.substring(fileUrl.lastIndexOf('/') + 1) || 'download';
    
    // 调试信息
    console.log('下载文件调试信息:', {
      fileUrl,
      originalFileName,
      fileName,
      fullUrl
    });
    
    // 使用fetch获取文件
    const response = await fetch(fullUrl);
    if (!response.ok) {
      throw new Error('文件下载失败');
    }
    
    // 创建blob
    const blob = await response.blob();
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.style.display = 'none';
    
    // 触发下载
    document.body.appendChild(link);
    link.click();
    
    // 清理
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
  } catch (error) {
    showToast('error', '文件下载失败，请稍后重试');
    console.error('Download error:', error);
  }
};

const selectDiscussion = (discussion) => {
  state.selectedDiscussionId = discussion.id;
  state.discussionView = 'detail';
};

const createDiscussion = () => {
  state.discussionView = 'create';
};

const discussionCreated = (discussion) => {
  showToast('success', '讨论发布成功');
  state.discussionView = 'list';
};

const backToDiscussionList = () => {
  state.discussionView = 'list';
  state.selectedDiscussionId = null;
};

onMounted(async () => {
  await fetchClasses();
  await fetchCourses();
  await fetchAssignments();
  await fetchEnrollments();
});

watch(
  () => state.selectedClassId,
  async () => {
    state.selectedCourseId = '';
    await fetchCourses();
    await fetchAssignments();
    await fetchEnrollments();
  }
);

watch(
  () => state.selectedCourseId,
  async () => {
    await fetchAssignments();
  }
);

watch(
  () => state.selectedAssignmentId,
  async () => {
    if (state.selectedAssignmentId) {
      await fetchSubmissions();
    } else {
      state.submissions = [];
    }
  }
);
</script>

<template>
  <section class="dashboard-page sidebar-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h3>教师控制台</h3>
        <p class="user-info">{{ auth.username || '未命名' }}</p>
      </div>
      <nav class="sidebar-nav">
        <button 
          @click="switchPage('classes')" 
          :class="['nav-item', { active: currentPage === 'classes' }]">
          班级管理
        </button>
        <button 
          @click="switchPage('courses')" 
          :class="['nav-item', { active: currentPage === 'courses' }]">
          课程管理
        </button>
        <button 
          @click="switchPage('assignments')" 
          :class="['nav-item', { active: currentPage === 'assignments' }]">
          作业发布
        </button>
        <button 
          @click="switchPage('members')" 
          :class="['nav-item', { active: currentPage === 'members' }]">
          班级成员
        </button>
        <button 
          @click="switchPage('discussions')" 
          :class="['nav-item', { active: currentPage === 'discussions' }]">
          班级讨论
        </button>
        <button 
          @click="switchPage('cloud-drive')" 
          :class="['nav-item', { active: currentPage === 'cloud-drive' }]">
          班级云盘
        </button>
        <button 
          @click="switchPage('submissions')" 
          :class="['nav-item', { active: currentPage === 'submissions' }]">
          提交管理
        </button>
      </nav>
      <div class="sidebar-footer">
        <button class="secondary" @click="logout" style="width: 100%;">退出登录</button>
      </div>
    </aside>

    <main class="main-content">
      <div class="section-title">
        <div>
          <h2>{{ pageTitle }}</h2>
          <p>{{ pageSubtitle }}</p>
        </div>
      </div>

      <div v-if="toast.text" :class="['notification', toast.type]">
        {{ toast.text }}
      </div>

      <div v-if="currentPage === 'classes'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>创建班级</h3>
          </div>
          <form class="form-grid" @submit.prevent="submitClass">
            <label>
              班级名称
              <input v-model="state.classForm.name" placeholder="如 2025 级软件 1 班" />
            </label>
            <label>
              加入邀请码
              <input v-model="state.classForm.code" placeholder="如 SW2025A" />
            </label>
            <div style="grid-column: 1 / -1;">
              <button class="primary" type="submit">创建班级</button>
            </div>
          </form>
        </div>

        <div class="card">
          <div class="section-title">
            <h3>我的班级</h3>
            <span>共 {{ state.classes.length }} 个班级</span>
          </div>
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>班级</th>
                  <th>邀请码</th>
                  <th>创建时间</th>
                  <th>当前选择</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cls in state.classes" :key="cls.id">
                  <td>{{ cls.name }}</td>
                  <td>{{ cls.code }}</td>
                  <td>{{ cls.createdAt || '-' }}</td>
                  <td>
                    <button class="secondary" @click="state.selectedClassId = cls.id" :disabled="state.selectedClassId === cls.id">
                      {{ state.selectedClassId === cls.id ? '已选择' : '选择' }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div v-if="currentPage === 'courses'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>创建课程</h3>
            <label>
              当前班级
              <select v-model="state.selectedClassId">
                <option disabled value="">请选择班级</option>
                <option v-for="cls in state.classes" :key="cls.id" :value="cls.id">{{ cls.name }}</option>
              </select>
            </label>
          </div>
          <form class="form-grid" @submit.prevent="submitCourse">
            <label>
              课程标题
              <input v-model="state.courseForm.title" placeholder="如 数据结构" />
            </label>
            <label>
              课程描述
              <input v-model="state.courseForm.description" placeholder="用于面向班级介绍" />
            </label>
            <div style="grid-column: 1 / -1;">
              <button class="primary" type="submit">创建课程</button>
            </div>
          </form>
        </div>

        <div class="card">
          <div class="section-title">
            <h3>课程列表</h3>
          </div>
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>课程</th>
                  <th>描述</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="course in state.courses" :key="course.id">
                  <td>{{ course.title }}</td>
                  <td>{{ course.description }}</td>
                  <td>
                    <button class="secondary" @click="state.selectedCourseId = course.id" :disabled="state.selectedCourseId === course.id">
                      {{ state.selectedCourseId === course.id ? '已选择' : '选择' }}
                    </button>
                    <button class="secondary" style="margin-left: 0.5rem" @click="deleteCourse(course.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="!state.courses.length">
                  <td colspan="3">暂无课程</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div v-if="currentPage === 'assignments'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>发布作业</h3>
            <span>绑定课程：{{ state.selectedCourseId || '未选择' }}</span>
          </div>
          <form class="form-grid" @submit.prevent="submitAssignment">
            <label>
              作业标题
              <input v-model="state.assignmentForm.title" placeholder="如 第一次课程实验" />
            </label>
            <label>
              截止时间
              <input v-model="state.assignmentForm.deadline" type="datetime-local" />
            </label>
            <label style="grid-column: 1 / -1;">
              作业说明
              <textarea v-model="state.assignmentForm.description" placeholder="包含作业内容、课件链接等"></textarea>
            </label>
            <div style="grid-column: 1 / -1;">
              <button class="primary" type="submit">发布作业</button>
            </div>
          </form>
        </div>

        <div class="card">
          <div class="section-title">
            <h3>作业列表</h3>
          </div>
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>作业</th>
                  <th>截止时间</th>
                  <th>描述</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="asn in state.assignments" :key="asn.id">
                  <td>{{ asn.title }}</td>
                  <td>{{ asn.deadline || '-' }}</td>
                  <td>{{ asn.description }}</td>
                  <td>
                    <button class="secondary" @click="deleteAssignment(asn.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="!state.assignments.length">
                  <td colspan="4">暂无作业</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div v-if="currentPage === 'members'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>班级成员</h3>
            <span>共 {{ state.enrollments.length }} 人</span>
          </div>
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>学生ID</th>
                  <th>姓名</th>
                  <th>邮箱</th>
                  <th>加入时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="en in state.enrollments" :key="en.id">
                  <td>{{ en.studentId }}</td>
                  <td>{{ en.studentName || '未填写' }}</td>
                  <td>{{ en.studentEmail || '未填写' }}</td>
                  <td>{{ en.joinedAt || '-' }}</td>
                  <td>
                    <button class="primary" @click="viewStudentSubmissions(en.studentId)">查看提交</button>
                    <button class="secondary" @click="removeStudent(en.studentId)" style="margin-left: 0.5rem;">移除 (占位)</button>
                  </td>
                </tr>
                <tr v-if="!state.enrollments.length">
                  <td colspan="5">暂无成员</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 班级讨论页面 -->
      <div v-if="currentPage === 'discussions'" class="page-content">
        <div v-if="!state.selectedClassId" class="card">
          <div class="section-title">
            <h3>班级讨论</h3>
            <span>请先选择一个班级</span>
          </div>
          <p style="color: #64748b; text-align: center; padding: 2rem;">
            请先在"班级管理"页面中选择一个班级，然后才能管理讨论。
          </p>
        </div>

        <div v-else-if="state.discussionView === 'list'" class="page-content">
          <DiscussionList
            :class-id="state.selectedClassId"
            @select-discussion="selectDiscussion"
            @create-discussion="createDiscussion"
          />
        </div>

        <div v-else-if="state.discussionView === 'detail'" class="page-content">
          <DiscussionDetail
            :discussion-id="state.selectedDiscussionId"
            @back-to-list="backToDiscussionList"
          />
        </div>

        <div v-else-if="state.discussionView === 'create'" class="page-content">
          <CreateDiscussion
            :class-id="state.selectedClassId"
            @discussion-created="discussionCreated"
            @cancel="backToDiscussionList"
          />
        </div>
      </div>

      <!-- 班级云盘页面 -->
      <div v-if="currentPage === 'cloud-drive'" class="page-content">
        <div v-if="!state.selectedClassId" class="card">
          <div class="section-title">
            <h3>班级云盘</h3>
            <span>请先选择一个班级</span>
          </div>
          <p style="color: #64748b; text-align: center; padding: 2rem;">
            请先在"班级管理"页面中选择一个班级，然后才能管理云盘文件。
          </p>
        </div>

        <div v-else class="page-content">
          <CloudDrive :class-id="state.selectedClassId" :is-teacher="true" />
        </div>
      </div>

      <!-- 提交管理页面 -->
      <div v-if="currentPage === 'submissions'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>选择作业</h3>
            <div style="display: flex; gap: 1rem; align-items: center;">
              <label>
                班级
                <select v-model="state.selectedClassId">
                  <option disabled value="">请选择班级</option>
                  <option v-for="cls in state.classes" :key="cls.id" :value="cls.id">{{ cls.name }}</option>
                </select>
              </label>
              <label>
                课程
                <select v-model="state.selectedCourseId">
                  <option disabled value="">请选择课程</option>
                  <option v-for="course in state.courses" :key="course.id" :value="course.id">{{ course.title }}</option>
                </select>
              </label>
              <label>
                作业
                <select v-model="state.selectedAssignmentId">
                  <option disabled value="">请选择作业</option>
                  <option v-for="assignment in state.assignments" :key="assignment.id" :value="assignment.id">{{ assignment.title }}</option>
                </select>
              </label>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="section-title">
            <h3>学生提交列表</h3>
            <span v-if="state.selectedAssignmentId">作业ID: {{ state.selectedAssignmentId }}</span>
          </div>
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>学生</th>
                  <th>邮箱</th>
                  <th>提交时间</th>
                  <th>状态</th>
                  <th>评分</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="submission in state.submissions" :key="submission.id">
                  <td>
                    <button 
                      class="link-button" 
                      @click="viewSubmissionDetail(submission.id, submission.studentId)">
                      {{ submission.studentName }}
                    </button>
                  </td>
                  <td>{{ submission.studentEmail || '未填写' }}</td>
                  <td>{{ submission.submittedAt || '-' }}</td>
                  <td>
                    <span class="status-pill" :class="submission ? 'success' : 'warning'">
                      已提交
                    </span>
                  </td>
                  <td>
                    {{ submission.score ?? '未评分' }}
                    <span v-if="submission.feedback" style="color: #64748b; font-size: 0.8rem;">
                      (有反馈)
                    </span>
                  </td>
                  <td>
                    <button class="primary" @click="viewSubmissionDetail(submission.id, submission.studentId)">
                      查看详情
                    </button>
                  </td>
                </tr>
                <tr v-if="!state.submissions.length && state.selectedAssignmentId">
                  <td colspan="6">暂无提交</td>
                </tr>
                <tr v-if="!state.selectedAssignmentId">
                  <td colspan="6">请先选择作业</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 提交详情页面 -->
      <div v-if="currentPage === 'submissionDetail'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>提交详情</h3>
            <button class="secondary" @click="switchPage('submissions')">返回列表</button>
          </div>
          
          <div v-if="loading.memberSubmissions" style="text-align: center; padding: 2rem;">
            加载中...
          </div>
          
          <div v-else-if="state.memberSubmissions.length > 0">
            <div v-for="item in state.memberSubmissions" :key="item.id" class="submission-item">
              <div class="submission-header">
                <h4>{{ item.title }}</h4>
                <span class="status-pill" :class="item.status === '已提交' ? 'success' : 'warning'">
                  {{ item.status }}
                </span>
              </div>
              
              <div v-if="item.submission" class="submission-content">
                <div class="content-section">
                  <h5>答案内容</h5>
                  <div class="answer-text">
                    {{ item.submission.answerText || '无文字内容' }}
                  </div>
                </div>
                
                <div v-if="item.submission.filePath" class="content-section">
                  <h5>附件</h5>
                  <div class="file-attachment">
                    <span>{{ item.submission.originalFileName || item.submission.filePath }}</span>
                    <div class="file-actions">
                      <button 
                        v-if="isImageFile(item.submission.filePath)" 
                        class="primary" 
                        @click="state.previewImage = item.submission.filePath">
                        预览图片
                      </button>
                      <button class="secondary" @click="downloadFile(item.submission.filePath, item.submission.originalFileName)">
                        下载文件
                      </button>
                    </div>
                  </div>
                </div>
                
                <div class="content-section">
                  <h5>评分信息</h5>
                  <div class="grading-info">
                    <div class="grade-display">
                      <span>当前评分: </span>
                      <strong>{{ item.submission.score ?? '未评分' }}</strong>
                    </div>
                    <div v-if="item.submission.feedback" class="feedback-display">
                      <span>教师反馈: </span>
                      <p>{{ item.submission.feedback }}</p>
                    </div>
                  </div>
                </div>
                
                <div class="content-section">
                  <h5>评分</h5>
                  <form class="grading-form" @submit.prevent="gradeSubmission(item.submission.id)">
                    <div class="form-row">
                      <label>
                        分数
                        <input 
                          v-model="state.gradingForm.score" 
                          type="number" 
                          min="0" 
                          max="100" 
                          placeholder="0-100"
                        />
                      </label>
                      <label>
                        反馈
                        <textarea 
                          v-model="state.gradingForm.feedback" 
                          placeholder="请输入评价和建议"
                          rows="3"
                        ></textarea>
                      </label>
                    </div>
                    <button type="submit" class="primary">提交评分</button>
                  </form>
                </div>
              </div>
              
              <div v-else class="no-submission">
                <p>该学生尚未提交此作业</p>
              </div>
            </div>
          </div>
          
          <div v-else style="text-align: center; padding: 2rem;">
            <p>无法加载提交信息</p>
          </div>
        </div>
        
        <!-- 图片预览模态框 -->
        <div v-if="state.previewImage" class="image-modal" @click="state.previewImage = null">
          <div class="modal-content" @click.stop>
            <button class="close-button" @click="state.previewImage = null">×</button>
            <img :src="state.previewImage" alt="图片预览" />
          </div>
        </div>
      </div>
    </main>
  </section>
</template>

<style scoped>
.dashboard-page.sidebar-layout {
  display: flex;
  flex-direction: row !important;
  gap: 0;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(15, 23, 42, 0.05);
  flex-shrink: 0;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
}

.sidebar-header h3 {
  margin: 0 0 0.25rem;
  color: #1e293b;
  font-size: 1.25rem;
}

.user-info {
  margin: 0;
  color: #64748b;
  font-size: 0.9rem;
}

.sidebar-nav {
  flex: 1;
  padding: 0.75rem;
  overflow-y: auto;
}

.nav-item {
  width: 100%;
  padding: 0.85rem 1rem;
  margin-bottom: 0.25rem;
  border: none;
  background: transparent;
  color: #475569;
  text-align: left;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #2563eb;
}

.nav-item.active {
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  color: #fff;
  box-shadow: 0 4px 6px rgba(37, 99, 235, 0.2);
}

.sidebar-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.main-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background: #f8fafc;
}

.page-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 提交管理相关样式 */
.link-button {
  background: none;
  border: none;
  color: #2563eb;
  text-decoration: underline;
  cursor: pointer;
  font-weight: 500;
}

.link-button:hover {
  color: #1d4ed8;
}

.status-pill {
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-pill.success {
  background: #dcfce7;
  color: #166534;
}

.status-pill.warning {
  background: #fef3c7;
  color: #92400e;
}

.submission-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  background: #fff;
}

.submission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #f1f5f9;
}

.submission-header h4 {
  margin: 0;
  color: #1e293b;
  font-size: 1.125rem;
}

.content-section {
  margin-bottom: 1.5rem;
}

.content-section h5 {
  margin: 0 0 0.5rem;
  color: #374151;
  font-size: 1rem;
  font-weight: 600;
}

.answer-text {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 1rem;
  min-height: 100px;
  white-space: pre-wrap;
  color: #374151;
}

.file-attachment {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 1rem;
}

.file-actions {
  display: flex;
  gap: 0.5rem;
}

.grading-info {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 1rem;
}

.grade-display {
  margin-bottom: 0.5rem;
}

.grade-display strong {
  color: #059669;
  font-size: 1.125rem;
}

.feedback-display p {
  margin: 0.5rem 0 0 0;
  color: #374151;
  line-height: 1.5;
}

.grading-form {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 150px 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-row input {
  width: 100%;
}

.no-submission {
  text-align: center;
  padding: 2rem;
  color: #64748b;
  background: #f8fafc;
  border-radius: 6px;
}

/* 图片预览模态框 */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  background: #fff;
  border-radius: 8px;
  padding: 1rem;
}

.close-button {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 50%;
  width: 2rem;
  height: 2rem;
  font-size: 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-button:hover {
  background: rgba(0, 0, 0, 0.7);
}

.modal-content img {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .sidebar-layout {
    flex-direction: column;
    height: auto;
  }
  
  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }
  
  .sidebar-nav {
    display: flex;
    overflow-x: auto;
    padding: 0.5rem;
  }
  
  .nav-item {
    white-space: nowrap;
    margin-bottom: 0;
    margin-right: 0.5rem;
  }
  
  .main-content {
    padding: 1rem;
  }
  
  .form-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
  
  .file-attachment {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .submission-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
