<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { assignmentApi, classApi, courseApi, enrollmentApi } from '../services/api';
import { clearAuth, getAuth } from '../services/auth';

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
});

const toast = reactive({ type: 'info', text: '' });
const state = reactive({
  classes: [],
  courses: [],
  assignments: [],
  enrollments: [],
  selectedClassId: '',
  selectedCourseId: '',
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
    members: '班级成员'
  };
  return titles[currentPage.value] || '控制面板';
});

const pageSubtitle = computed(() => {
  const subtitles = {
    classes: '创建和管理您的教学班级',
    courses: '为班级添加和管理课程',
    assignments: '发布和管理课程作业',
    members: '查看和管理班级学生成员'
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
    state.enrollments = await enrollmentApi.listByClass(state.selectedClassId);
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
                  <th>加入时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="en in state.enrollments" :key="en.id">
                  <td>{{ en.studentId }}</td>
                  <td>{{ en.joinedAt || '-' }}</td>
                  <td>
                    <button class="secondary" @click="removeStudent(en.studentId)">移除 (占位)</button>
                  </td>
                </tr>
                <tr v-if="!state.enrollments.length">
                  <td colspan="3">暂无成员</td>
                </tr>
              </tbody>
            </table>
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
}
</style>
