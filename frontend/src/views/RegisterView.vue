<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { authApi } from '../services/api';
import { setAuth } from '../services/auth';

const router = useRouter();
const loading = ref(false);
const notification = reactive({ type: 'info', text: '' });
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'STUDENT',
  realName: '',
  email: '',
  phone: '',
});

const handleRegister = async () => {
  if (!form.username || !form.password) {
    notification.type = 'error';
    notification.text = '请完善注册信息';
    return;
  }
  if (form.password !== form.confirmPassword) {
    notification.type = 'error';
    notification.text = '两次输入的密码不一致';
    return;
  }
  loading.value = true;
  try {
    const data = await authApi.register({
      username: form.username,
      password: form.password,
      role: form.role,
      realName: form.realName,
      email: form.email,
      phone: form.phone,
    });
    setAuth({ username: data.username, userId: data.userId, role: data.role });
    notification.type = 'success';
    notification.text = '注册成功，已自动登录';
    router.push(data.role === 'TEACHER' ? '/teacher' : '/student');
  } catch (error) {
    notification.type = 'error';
    notification.text = error.message || '注册失败';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <section class="auth-page">
    <div class="card">
      <div class="section-title">
        <div>
          <h2>注册账号</h2>
          <p>模拟账号注册流程，真实场景可对接后端用户模块</p>
        </div>
        <RouterLink to="/login">已有账号？去登录</RouterLink>
      </div>

      <form class="form-grid" @submit.prevent="handleRegister">
        <label>
          用户名
          <input v-model="form.username" placeholder="建议与后端保持一致" />
        </label>
        <label>
          角色
          <select v-model="form.role">
            <option value="STUDENT">学生</option>
            <option value="TEACHER">教师</option>
          </select>
        </label>
        <label>
          真实姓名（可选）
          <input v-model="form.realName" />
        </label>
        <label>
          邮箱（可选）
          <input v-model="form.email" type="email" />
        </label>
        <label>
          手机号（可选）
          <input v-model="form.phone" />
        </label>
        <label>
          密码
          <input v-model="form.password" type="password" />
        </label>
        <label>
          确认密码
          <input v-model="form.confirmPassword" type="password" />
        </label>
        <div style="grid-column: 1 / -1;">
          <button class="primary" type="submit" :disabled="loading">{{ loading ? '提交中...' : '注册并登录' }}</button>
        </div>
      </form>
      <div v-if="notification.text" :class="['notification', notification.type]">{{ notification.text }}</div>
    </div>
  </section>
</template>
