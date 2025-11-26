<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { assignmentApi, authApi, classApi, courseApi, enrollmentApi, submissionApi, uploadApi, discussionApi, cloudFileApi } from '../services/api';
import { clearAuth, getAuth, getStudentClasses, saveStudentClasses } from '../services/auth';
import FileUpload from '../components/FileUpload.vue';
import DiscussionList from '../components/DiscussionList.vue';
import DiscussionDetail from '../components/DiscussionDetail.vue';
import CreateDiscussion from '../components/CreateDiscussion.vue';
import CloudDrive from '../components/CloudDrive.vue';

const router = useRouter();
const auth = getAuth();
if (!auth.userId || auth.role !== 'STUDENT') { router.replace('/login'); }

const currentPage = ref('classes');

const toast = reactive({ type: 'info', text: '' });
const showToast = (type, text) => { toast.type = type; toast.text = text; setTimeout(() => { toast.text=''; }, 3500); };

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
  classMembers: [],
  currentClassForMembers: null,
  discussionView: 'list', // 'list', 'detail', 'create'
  selectedDiscussionId: null
});

const joinForm = reactive({ code: '' });
const submissionForm = reactive({ answerText: '', filePath: '', originalFileName: '', selectedFile: null });
const loading = reactive({ courses: false, assignments: false, submission: false, classMembers: false });

const switchPage = (page) => {
  currentPage.value = page;
};

const fetchClassMembers = async (classId) => {
  if (!classId) {
    state.classMembers = [];
    return;
  }
  loading.classMembers = true;
  try {
    const enrollments = await enrollmentApi.listByClass(classId);
    const membersWithDetails = await Promise.all(
      enrollments.map(async (enrollment) => {
        try {
          const profile = await authApi.getUserProfile(enrollment.studentId);
          return {
            ...enrollment,
            realName: profile.realName,
            email: profile.email,
            phone: profile.phone
          };
        } catch (error) {
          return {
            ...enrollment,
            realName: null,
            email: null,
            phone: null
          };
        }
      })
    );
    state.classMembers = membersWithDetails;
  } catch (e) {
    showToast('error', e.message || '获取班级成员失败');
  } finally {
    loading.classMembers = false;
  }
};

const handleFileUpload = async (file) => {
  // 客户端预先检查文件大小
  const maxSize = 100 * 1024 * 1024; // 100MB
  if (file.size > maxSize) {
    showToast('error', '文件大小超出限制，请选择小于100MB的文件');
    throw new Error('文件大小超出限制');
  }
  
  try {
    loading.submission = true;
    const fileUrl = await uploadApi.uploadFile(file);
    submissionForm.selectedFile = file;
    submissionForm.filePath = fileUrl;
    submissionForm.originalFileName = file.name; // 保存原始文件名
    showToast('success', `文件上传成功: ${file.name}`);
    return fileUrl;
  } catch (error) {
    let errorMessage = '文件上传失败';
    
    // 根据错误类型提供具体的错误信息
    if (error.response) {
      const status = error.response.status;
      if (status === 413) {
        errorMessage = '文件大小超出限制，请选择小于100MB的文件';
      } else if (status === 400) {
        errorMessage = error.response.data?.message || '文件格式不支持或文件损坏';
      } else if (status === 401) {
        errorMessage = '登录已失效，请重新登录';
      } else if (status === 429) {
        errorMessage = '上传过于频繁，请稍后再试';
      }
    } else if (error.message) {
      if (error.message.includes('Network Error') || error.message.includes('Request failed with status code 413')) {
        errorMessage = '文件大小超出限制，请选择小于100MB的文件';
      } else if (error.message.includes('timeout')) {
        errorMessage = '上传超时，请检查网络连接或文件大小';
      } else if (error.message.includes('文件大小超出限制')) {
        errorMessage = '文件大小超出限制，请选择小于100MB的文件';
      }
    }
    
    showToast('error', errorMessage);
    throw error;
  } finally {
    loading.submission = false;
  }
};

const handleFileUploadError = (error) => {
  let errorMessage = '文件上传失败';
  
  if (error.response) {
    const status = error.response.status;
    if (status === 413) {
      errorMessage = '文件大小超出限制，请选择小于100MB的文件';
    } else if (status === 400) {
      errorMessage = error.response.data?.message || '文件格式不支持或文件损坏';
    } else if (status === 401) {
      errorMessage = '登录已失效，请重新登录';
    }
  } else if (error.message) {
    if (error.message.includes('Network Error')) {
      errorMessage = '网络连接失败，请检查网络后重试';
    }
  }
  
  showToast('error', errorMessage);
  submissionForm.selectedFile = null;
  submissionForm.filePath = '';
};

const viewClassMembers = (classId) => {
  state.currentClassForMembers = state.joinedClasses.find(c => c.id === classId);
  state.currentClassId = classId;
  fetchClassMembers(classId);
  switchPage('class-members');
};

const pageTitle = computed(() => {
  const titles = {
    classes: '我的班级',
    'class-members': '班级成员',
    courses: '课程信息',
    assignments: '作业提交',
    discussions: '班级讨论',
    'cloud-drive': '班级云盘',
    profile: '个人信息'
  };
  return titles[currentPage.value] || '学习空间';
});

const pageSubtitle = computed(() => {
  const subtitles = {
    classes: '加入班级并查看班级信息',
    'class-members': '查看班级成员列表',
    courses: '查看已加入班级的课程',
    assignments: '查看和提交作业',
    discussions: '参与班级讨论和交流',
    'cloud-drive': '查看和下载班级共享文件',
    profile: '管理个人信息和联系方式'
  };
  return subtitles[currentPage.value] || '学生端学习空间';
});

const logout = () => { clearAuth(); router.replace('/login'); };
const persistClasses = () => { saveStudentClasses(state.joinedClasses); };

const fetchProfile = async () => { try { const profile = await authApi.getProfile(); state.profile = profile || { ...emptyProfile }; } catch (e){ showToast('error', e.message || '获取个人信息失败'); } };

const joinClass = async () => { if(!joinForm.code){ showToast('warning','请输入班级邀请码'); return; } try { const cls = await classApi.findByCode(joinForm.code); await enrollmentApi.enroll(cls.id, auth.userId); if(!state.joinedClasses.find(c=>c.id===cls.id)){ state.joinedClasses.push(cls); persistClasses(); } state.currentClassId = cls.id; joinForm.code=''; showToast('success', `加入班级 ${cls.name} 成功`); await fetchCourses(); } catch(e){ showToast('error', e.message || '加入班级失败'); } };

const fetchCourses = async () => { if(!state.currentClassId){ state.courses=[]; return; } loading.courses=true; try { state.courses = await courseApi.listByClass(state.currentClassId); if(!state.currentCourseId && state.courses.length>0){ state.currentCourseId = state.courses[0].id; } } catch(e){ showToast('error', e.message || '获取课程失败'); } finally { loading.courses=false; } };

const fetchAssignments = async () => { if(!state.currentCourseId){ state.assignments=[]; state.currentAssignmentId=''; state.mySubmission=null; return; } loading.assignments=true; try { state.assignments = await assignmentApi.listByCourse(state.currentCourseId); state.currentAssignmentId = state.assignments[0]?.id || ''; state.mySubmission=null; if(state.currentAssignmentId){ await loadMySubmission(state.currentAssignmentId); } } catch(e){ showToast('error', e.message || '获取作业失败'); } finally { loading.assignments=false; } };

const loadMySubmission = async (assignmentId) => { if(!assignmentId) return; state.currentAssignmentId=assignmentId; try { state.mySubmission = await submissionApi.findUnique(assignmentId, auth.userId); if(state.mySubmission){ submissionForm.answerText = state.mySubmission.answerText || ''; submissionForm.filePath = state.mySubmission.filePath || ''; submissionForm.originalFileName = state.mySubmission.originalFileName || ''; } else { submissionForm.answerText=''; submissionForm.filePath=''; submissionForm.originalFileName=''; } } catch(e){ showToast('error', e.message || '获取作业提交失败'); } };

const submitAssignment = async (assignmentId = state.currentAssignmentId) => { 
  if(!assignmentId){ 
    showToast('warning','请选择作业'); 
    return; 
  } 
  if(!submissionForm.answerText && !submissionForm.filePath){ 
    showToast('warning','请输入答案或上传附件'); 
    return; 
  } 
  loading.submission=true; 
  try { 
    if(state.mySubmission){ 
      await submissionApi.updateContent(state.mySubmission.id,{ 
        answerText: submissionForm.answerText, 
        filePath: submissionForm.filePath,
        originalFileName: submissionForm.originalFileName 
      }); 
      showToast('success','已更新提交内容'); 
    } else { 
      await submissionApi.submit({ 
        assignmentId, 
        studentId: auth.userId, 
        answerText: submissionForm.answerText, 
        filePath: submissionForm.filePath,
        originalFileName: submissionForm.originalFileName 
      }); 
      showToast('success','作业提交成功'); 
    } 
    await loadMySubmission(assignmentId); 
    // 重置文件上传区域
    submissionForm.selectedFile = null;
    submissionForm.originalFileName = '';
  } catch(e){ 
    showToast('error', e.message || '提交失败'); 
  } finally { 
    loading.submission=false; 
  } 
};

const saveProfileForm = async () => { try { const payload = { realName: state.profile.realName, email: state.profile.email, phone: state.profile.phone }; state.profile = await authApi.updateProfile(payload); showToast('success','个人信息已更新'); } catch(e){ showToast('error', e.message || '保存失败'); } };

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

onMounted(async () => { await fetchProfile(); if(state.joinedClasses.length>0){ state.currentClassId = state.joinedClasses[0].id; } });
watch(() => state.currentClassId, async () => { state.currentCourseId=''; await fetchCourses(); await fetchAssignments(); });
watch(() => state.currentCourseId, async () => { await fetchAssignments(); });
</script>

<template>
  <section class="dashboard-page sidebar-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h3>学生学习空间</h3>
        <p class="user-info">{{ auth.username || '未命名' }}</p>
      </div>
      <nav class="sidebar-nav">
        <button 
          @click="switchPage('classes')" 
          :class="['nav-item', { active: currentPage === 'classes' }]">
          我的班级
        </button>
        <button 
          @click="switchPage('courses')" 
          :class="['nav-item', { active: currentPage === 'courses' }]">
          课程信息
        </button>
        <button 
          @click="switchPage('assignments')" 
          :class="['nav-item', { active: currentPage === 'assignments' }]">
          作业提交
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
          @click="switchPage('profile')" 
          :class="['nav-item', { active: currentPage === 'profile' }]">
          个人信息
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

      <div v-if="toast.text" :class="['notification', toast.type]">{{ toast.text }}</div>

      <div v-if="currentPage === 'classes'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>加入新班级</h3>
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
        </div>

        <div class="card">
          <div class="section-title">
            <h3>已加入的班级</h3>
            <span>已加入 {{ state.joinedClasses.length }} 个班级</span>
          </div>
          <div class="tab-bar">
            <button
              v-for="cls in state.joinedClasses"
              :key="cls.id"
              :class="{ active: state.currentClassId === cls.id }"
              @click="viewClassMembers(cls.id)"
            >
              {{ cls.name }}
            </button>
            <p v-if="!state.joinedClasses.length" style="color: #94a3b8;">还未加入任何班级，先输入邀请码吧。</p>
          </div>
        </div>
      </div>

      <div v-if="currentPage === 'courses'" class="page-content">
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
      </div>

      <div v-if="currentPage === 'assignments'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>作业列表</h3>
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
                      {{ state.mySubmission?.assignmentId === asn.id ? '已提交' : '未提交' }}
                    </span>
                  </td>
                </tr>
                <tr v-if="!state.assignments.length">
                  <td colspan="4">暂无作业</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="card" style="background: #f8fafc;">
          <h4>提交 / 更新当前作业</h4>
          <div class="form-grid">
            <label>
              答案内容
              <textarea v-model="submissionForm.answerText" placeholder="粘贴答案或作业说明"></textarea>
            </label>
            </div>
          <div style="margin-top: 1rem;">
            <label style="margin-bottom: 0.5rem; display: block; font-weight: 500;">附件上传</label>
            <FileUpload
              v-model="submissionForm.selectedFile"
              :upload-function="handleFileUpload"
              @upload-error="handleFileUploadError"
              accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png,.gif,.zip,.rar"
            />
          </div>
          <div v-if="submissionForm.filePath" style="margin-top: 1rem; color: #64748b; font-size: 0.9rem;">
            已上传文件: {{ submissionForm.originalFileName || submissionForm.filePath }}
          </div>
          <button class="primary" style="margin-top: 1rem;" :disabled="loading.submission" @click="submitAssignment()">
            {{ loading.submission ? '提交中...' : '提交 / 更新' }}
          </button>
          <p v-if="state.mySubmission" style="color: #64748b; margin-top: 0.5rem;">
            上次提交：{{ state.mySubmission.submittedAt || '未知时间' }}，评分 {{ state.mySubmission.score ?? '未评分' }}
          </p>
        </div>
      </div>

      <div v-if="currentPage === 'class-members'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>班级成员列表</h3>
            <span v-if="state.currentClassForMembers">
              {{ state.currentClassForMembers.name }} (ID: {{ state.currentClassId }})
            </span>
          </div>
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>学生ID</th>
                  <th>姓名</th>
                  <th>邮箱</th>
                  <th>电话</th>
                  <th>加入时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="member in state.classMembers" :key="member.id">
                  <td>{{ member.studentId }}</td>
                  <td>{{ member.realName || '未填写' }}</td>
                  <td>{{ member.email || '未填写' }}</td>
                  <td>{{ member.phone || '未填写' }}</td>
                  <td>{{ member.joinedAt ? member.joinedAt.split('T')[0] : '-' }}</td>
                </tr>
                <tr v-if="!state.classMembers.length && !loading.classMembers">
                  <td colspan="5">暂无成员</td>
                </tr>
                <tr v-if="loading.classMembers">
                  <td colspan="5">加载中...</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div style="margin-top: 1rem;">
            <button class="secondary" @click="switchPage('classes')">返回班级列表</button>
          </div>
        </div>
      </div>

      <div v-if="currentPage === 'discussions'" class="page-content">
        <div v-if="!state.currentClassId" class="card">
          <div class="section-title">
            <h3>班级讨论</h3>
            <span>请先选择一个班级</span>
          </div>
          <p style="color: #64748b; text-align: center; padding: 2rem;">
            请先在"我的班级"页面中选择一个班级，然后才能参与讨论。
          </p>
        </div>

        <div v-else-if="state.discussionView === 'list'" class="page-content">
          <DiscussionList
            :class-id="state.currentClassId"
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
            :class-id="state.currentClassId"
            @discussion-created="discussionCreated"
            @cancel="backToDiscussionList"
          />
        </div>
      </div>

      <div v-if="currentPage === 'cloud-drive'" class="page-content">
        <div v-if="!state.currentClassId" class="card">
          <div class="section-title">
            <h3>班级云盘</h3>
            <span>请先选择一个班级</span>
          </div>
          <p style="color: #64748b; text-align: center; padding: 2rem;">
            请先在"我的班级"页面中选择一个班级，然后才能查看云盘文件。
          </p>
        </div>

        <div v-else class="page-content">
          <CloudDrive :class-id="state.currentClassId" :is-teacher="false" />
        </div>
      </div>

      <div v-if="currentPage === 'profile'" class="page-content">
        <div class="card">
          <div class="section-title">
            <h3>个人信息</h3>
            <span>信息同步服务器，教师端可用于联系</span>
          </div>
          <form class="form-grid" @submit.prevent="saveProfileForm">
            <label>
              真实姓名
              <input v-model="state.profile.realName" placeholder="输入真实姓名" />
            </label>
            <label>
              邮箱
              <input v-model="state.profile.email" type="email" placeholder="用于接收通知" />
            </label>
            <label>
              手机号
              <input v-model="state.profile.phone" placeholder="选填" />
            </label>
            <div style="grid-column: 1 / -1; display:flex; gap:.75rem;">
              <button class="primary" type="submit">保存信息</button>
              <button class="secondary" type="button" @click="fetchProfile">重新加载</button>
            </div>
          </form>
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
