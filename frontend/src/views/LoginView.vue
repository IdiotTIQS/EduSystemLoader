<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { authApi } from '../services/api';
import { setAuth } from '../services/auth';

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const notification = reactive({ type: 'info', text: '' });
const form = reactive({
  username: '',
  password: '',
});

const handleLogin = async () => {
  if (!form.username || !form.password) {
    notification.type = 'error';
    notification.text = '请输入用户名与密码';
    return;
  }
  loading.value = true;
  try {
    const data = await authApi.login(form);
    setAuth({ 
      userId: data.userId, 
      role: data.role, 
      username: data.username,
      token: data.token 
    });
    notification.type = 'success';
    notification.text = '登录成功';
    const redirect = route.query.redirect;
    router.push(redirect || (data.role === 'TEACHER' ? '/teacher' : '/student'));
  } catch (error) {
    notification.type = 'error';
    notification.text = error.message || '登录失败';
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
          <h2>登录</h2>
          <p>使用注册的用户名与密码登录，系统将根据角色跳转对应控制台</p>
        </div>
        <RouterLink to="/register">没有账号？去注册</RouterLink>
      </div>

      <form class="form-grid" @submit.prevent="handleLogin">
        <label>
          用户名
          <input v-model="form.username" placeholder="请输入注册时的用户名" />
        </label>
        <label>
          密码
          <input v-model="form.password" type="password" placeholder="请输入密码" />
        </label>
        <div style="grid-column: 1 / -1; display: flex; gap: 1rem; align-items: center;">
          <button class="primary" type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
          <p style="margin: 0; color: #64748b; font-size: 0.9rem;">登录成功后系统会自动写入 userId / role 供 API 鉴权使用</p>
        </div>
      </form>
      <div v-if="notification.text" :class="['notification', notification.type]">{{ notification.text }}</div>
    </div>
  </section>
</template>
