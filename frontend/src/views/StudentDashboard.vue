<script setup>
import { onMounted, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  assignmentApi,
  authApi,
  classApi,
  courseApi,
  enrollmentApi,
  submissionApi,
} from '../services/api';
import {
  clearAuth,
  getAuth,
  getStudentClasses,
  saveStudentClasses,
} from '../services/auth';

const router = useRouter();
const auth = getAuth();

if (!auth.userId || auth.role !== 'STUDENT') {
  router.replace('/login');
}

const toast = reactive({ type: 'info', text: '' });
const emptyProfile = { realName: '', email: '', phone: '' };
const state = reactive({
  joinedClasses: getStudentClasses(),
  currentClassId: '',
  courses: [],
  currentCourseId: '',
  assignments: [],
  currentAssignmentId: '',
  mySubmission: null,
  profile: { ...emptyProfile },
});

const joinForm = reactive({ code: '' });
const submissionForm = reactive({ answerText: '', filePath: '' });
const loading = reactive({ courses: false, assignments: false, submission: false });

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

const persistClasses = () => {
  saveStudentClasses(state.joinedClasses);
};

const fetchProfile = async () => {
  try {
    const profile = await authApi.getProfile();
    state.profile = profile || { ...emptyProfile };
  } catch (error) {
    showToast('error', error.message || '获取个人信息失败');
  }
};

const joinClass = async () => {
  if (!joinForm.code) {
    showToast('warning', '请输入班级邀请码');
    return;
  }
  try {
    const cls = await classApi.findByCode(joinForm.code);
    await enrollmentApi.enroll(cls.id, auth.userId);
    if (!state.joinedClasses.find((c) => c.id === cls.id)) {
      state.joinedClasses.push(cls);
      persistClasses();
    }
    state.currentClassId = cls.id;
    joinForm.code = '';
    showToast('success', `加入班级 ${cls.name} 成功`);
    await fetchCourses();
  } catch (error) {
    showToast('error', error.message || '加入班级失败');
  }
};

const fetchCourses = async () => {
  if (!state.currentClassId) {
    state.courses = [];
    return;
  }
  loading.courses = true;
  try {
    state.courses = await courseApi.listByClass(state.currentClassId);
    if (!state.currentCourseId && state.courses.length > 0) {
      state.currentCourseId = state.courses[0].id;
    }
  } catch (error) {
    showToast('error', error.message || '获取课程失败');
  } finally {
    loading.courses = false;
  }
};

const fetchAssignments = async () => {
  if (!state.currentCourseId) {
    state.assignments = [];
    state.currentAssignmentId = '';
    state.mySubmission = null;
    return;
  }
  loading.assignments = true;
  try {
    state.assignments = await assignmentApi.listByCourse(state.currentCourseId);
    state.currentAssignmentId = state.assignments[0]?.id || '';
    state.mySubmission = null;
    if (state.currentAssignmentId) {
      await loadMySubmission(state.currentAssignmentId);
    }
  } catch (error) {
    showToast('error', error.message || '获取作业失败');
  } finally {
    loading.assignments = false;
  }
};

const loadMySubmission = async (assignmentId) => {
  if (!assignmentId) return;
  state.currentAssignmentId = assignmentId;
  try {
    state.mySubmission = await submissionApi.findUnique(assignmentId, auth.userId);
    if (state.mySubmission) {
      submissionForm.answerText = state.mySubmission.answerText || '';
      submissionForm.filePath = state.mySubmission.filePath || '';
    } else {
      submissionForm.answerText = '';
      submissionForm.filePath = '';
    }
  } catch (error) {
    showToast('error', error.message || '获取作业提交失败');
  }
};

const submitAssignment = async (assignmentId = state.currentAssignmentId) => {
  if (!assignmentId) {
    showToast('warning', '请选择作业');
    return;
  }
  if (!submissionForm.answerText && !submissionForm.filePath) {
    showToast('warning', '请输入答案或上传链接');
    return;
  }
  loading.submission = true;
  try {
    if (state.mySubmission) {
      await submissionApi.updateContent(state.mySubmission.id, {
        answerText: submissionForm.answerText,
        filePath: submissionForm.filePath,
      });
      showToast('success', '已更新提交内容');
    } else {
      await submissionApi.submit({
        assignmentId,
        studentId: auth.userId,
        answerText: submissionForm.answerText,
        filePath: submissionForm.filePath,
      });
      showToast('success', '作业提交成功');
    }
    await loadMySubmission(assignmentId);
  } catch (error) {
    showToast('error', error.message || '提交失败');
  } finally {
    loading.submission = false;
  }
};

const saveProfileForm = async () => {
  try {
    const payload = {
      realName: state.profile.realName,
      email: state.profile.email,
      phone: state.profile.phone,
    };
    state.profile = await authApi.updateProfile(payload);
    showToast('success', '个人信息已更新');
  } catch (error) {
    showToast('error', error.message || '保存失败');
  }
};

onMounted(async () => {
  await fetchProfile();
  if (state.joinedClasses.length > 0) {
    state.currentClassId = state.joinedClasses[0].id;
  }
});

watch(
  () => state.currentClassId,
  async () => {
    state.currentCourseId = '';
    await fetchCourses();
    await fetchAssignments();
  }
);

watch(
  () => state.currentCourseId,
  async () => {
    await fetchAssignments();
  }
);
</script>

<template>
  <section class="dashboard-page">
    <div class="section-title">
      <div>
        <h2>学生端学习空间</h2>
        <p>当前学生：{{ auth.username || '未命名' }}（ID: {{ auth.userId }}）</p>
      </div>
      <button class="secondary" @click="logout">退出登录</button>
    </div>

    <div v-if="toast.text" :class="['notification', toast.type]">
      {{ toast.text }}
    </div>

    <div class="card">
      <div class="section-title">
        <h3>加入班级</h3>
        <span>已加入 {{ state.joinedClasses.length }} 个班级</span>
      </div>
      <form class="form-grid" @submit.prevent="joinClass">
        <label>
          邀请码
          <input v-model="joinForm.code" placeholder="输入教师提供的邀请码" />
        </label>
        <div style="display: flex; align-items: flex-end;">
          <button class="primary" type="submit">加入班级</button>
        </div>
      </form>
      <div class="tab-bar" style="margin-top: 1rem;">
        <button
          v-for="cls in state.joinedClasses"
          :key="cls.id"
          :class="{ active: state.currentClassId === cls.id }"
          @click="state.currentClassId = cls.id"
        >
          {{ cls.name }}
        </button>
        <p v-if="!state.joinedClasses.length" style="color: #94a3b8;">还未加入任何班级，先输入邀请码吧。</p>
      </div>
    </div>

    <div class="card">
      <div class="section-title">
        <h3>课程信息</h3>
        <span v-if="state.currentClassId">当前班级 ID: {{ state.currentClassId }}</span>
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
                <button class="secondary" @click="state.currentCourseId = course.id" :disabled="state.currentCourseId === course.id">
                  {{ state.currentCourseId === course.id ? '正在学习' : '选中' }}
                </button>
              </td>
            </tr>
            <tr v-if="!state.courses.length">
              <td colspan="3">暂无课程，请联系教师创建</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <div class="section-title">
        <h3>作业与提交</h3>
        <span v-if="state.currentCourseId">当前课程 ID: {{ state.currentCourseId }}</span>
      </div>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>作业</th>
              <th>截止时间</th>
              <th>说明</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="asn in state.assignments"
              :key="asn.id"
              @click="loadMySubmission(asn.id)"
              :style="{
                cursor: 'pointer',
                background: state.currentAssignmentId === asn.id ? '#eef2ff' : 'transparent',
              }"
            >
              <td>{{ asn.title }}</td>
              <td>{{ asn.deadline || '-' }}</td>
              <td>{{ asn.description }}</td>
              <td>
                <span class="status-pill" :class="state.mySubmission?.assignmentId === asn.id ? 'success' : 'warning'">
                  {{ state.mySubmission?.assignmentId === asn.id ? '已加载提交' : '点击加载' }}
                </span>
              </td>
            </tr>
            <tr v-if="!state.assignments.length">
              <td colspan="4">暂无作业</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card" style="margin-top: 1rem; background: #f8fafc;">
        <h4>提交 / 更新当前作业</h4>
        <div class="form-grid">
          <label>
            答案内容
            <textarea v-model="submissionForm.answerText" placeholder="粘贴答案或作业说明"></textarea>
          </label>
          <label>
            附件链接
            <input v-model="submissionForm.filePath" placeholder="网盘、Git 仓库等链接" />
          </label>
        </div>
        <button class="primary" style="margin-top: 1rem;" :disabled="loading.submission" @click="submitAssignment()">
          {{ loading.submission ? '提交中...' : '提交 / 更新' }}
        </button>
        <p v-if="state.mySubmission" style="color: #64748b; margin-top: 0.5rem;">
          上次提交：{{ state.mySubmission.submittedAt || '未知时间' }}，评分 {{ state.mySubmission.score ?? '未评分' }}
        </p>
      </div>
    </div>

    <div class="card">
      <div class="section-title">
        <h3>个人信息</h3>
        <span>信息将同步到服务器，教师端可用于联系确认</span>
      </div>
      <form class="form-grid" @submit.prevent="saveProfileForm">
        <label>
          昵称
          <input v-model="state.profile.nickname" />
        </label>
        <label>
          邮箱
          <input v-model="state.profile.email" type="email" />
        </label>
        <label style="grid-column: 1 / -1;">
          个人简介
          <textarea v-model="state.profile.bio"></textarea>
        </label>
        <div style="grid-column: 1 / -1;">
          <button class="primary" type="submit">保存信息</button>
        </div>
      </form>
    </div>
  </section>
</template>
