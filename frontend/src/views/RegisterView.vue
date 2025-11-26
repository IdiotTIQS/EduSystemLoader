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
    setAuth({ 
      username: data.username, 
      userId: data.userId, 
      role: data.role,
      token: data.token 
    });
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
  <div class="container">
    <div class="form-container">
      <div class="signin-signup">
        <form action="#" class="sign-up-form" @submit.prevent="handleRegister">
          <h2 class="title">注册账号</h2>
          <div class="input-field">
            <i><font-awesome-icon icon="fa-solid fa-user" /></i>
            <input v-model="form.username" type="text" placeholder="用户名" />
          </div>
          <div class="input-field">
            <i><font-awesome-icon icon="fa-solid fa-envelope" /></i>
            <input v-model="form.email" type="email" placeholder="邮箱" />
          </div>
          <div class="input-field">
            <i><font-awesome-icon icon="fa-solid fa-lock" /></i>
            <input v-model="form.password" type="password" placeholder="密码" />
          </div>
          <div class="input-field">
            <i><font-awesome-icon icon="fa-solid fa-lock" /></i>
            <input v-model="form.confirmPassword" type="password" placeholder="确认密码" />
          </div>
          <div class="input-field">
            <i><font-awesome-icon icon="fa-solid fa-graduation-cap" /></i>
            <select v-model="form.role">
              <option value="STUDENT">学生</option>
              <option value="TEACHER">教师</option>
            </select>
          </div>
          <div class="input-field">
            <i><font-awesome-icon icon="fa-solid fa-id-card" /></i>
            <input v-model="form.realName" type="text" placeholder="真实姓名（可选）" />
          </div>
          <div class="input-field">
            <i><font-awesome-icon icon="fa-solid fa-phone" /></i>
            <input v-model="form.phone" type="tel" placeholder="手机号（可选）" />
          </div>
          <input type="submit" class="btn" value="立即注册" :disabled="loading" />
          <p class="social-text">通过其他方式</p>
          <div class="social-media">
            <a href="#" class="social-icon">
              <font-awesome-icon icon="fa-brands fa-qq" />
            </a>
            <a href="#" class="social-icon">
              <font-awesome-icon icon="fa-brands fa-weixin" />
            </a>
            <a href="#" class="social-icon">
              <font-awesome-icon icon="fa-brands fa-weibo" />
            </a>
            <a href="#" class="social-icon">
              <font-awesome-icon icon="fa-brands fa-alipay" />
            </a>
          </div>
          <div class="login-link">
            <RouterLink to="/login">已有账号？去登录</RouterLink>
          </div>
        </form>
      </div>
    </div>
    <div v-if="notification.text" :class="['notification', notification.type]">{{ notification.text }}</div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@200;300;400;500;600;700;800&display=swap');

* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body,
input {
    font-family: 'Poppins', sans-serif;
}

.container {
    position: relative;
    width: 100%;
    background-color: #fff;
    min-height: 100vh;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
}

.form-container {
    width: 100%;
    max-width: 500px;
    padding: 2rem;
}

.signin-signup {
    width: 100%;
}

form {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    padding: 2rem;
    background: #f8f9fa;
    border-radius: 10px;
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.title {
    font-size: 2.2rem;
    color: #444;
    margin-bottom: 2rem;
    text-align: center;
}

.input-field {
    max-width: 380px;
    width: 100%;
    background-color: #f0f0f0;
    margin: 10px 0;
    height: 55px;
    border-radius: 55px;
    display: grid;
    grid-template-columns: 15% 85%;
    padding: 0 0.4rem;
    position: relative;
}

.input-field i {
    text-align: center;
    line-height: 55px;
    color: #acacac;
    transition: 0.5s;
    font-size: 1.1rem;
}

.input-field input, .input-field select {
    background: none;
    outline: none;
    border: none;
    line-height: 1;
    font-weight: 600;
    font-size: 1.1rem;
    color: #333;
}

.input-field input::placeholder {
    color: #aaa;
    font-weight: 500;
}

.social-text {
    padding: 0.7rem 0;
    font-size: 1rem;
    text-align: center;
}

.social-media {
    display: flex;
    justify-content: center;
}

.social-icon {
    height: 46px;
    width: 46px;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 0 0.45rem;
    color: #333;
    border-radius: 50%;
    border: 1px solid #333;
    text-decoration: none;
    font-size: 1.1rem;
    transition: 0.3s;
}

.social-icon:hover {
    color: #4481eb;
    border-color: #4481eb;
}

.btn {
    width: 150px;
    background-color: #5995fd;
    border: none;
    outline: none;
    height: 49px;
    border-radius: 49px;
    color: #fff;
    text-transform: uppercase;
    font-weight: 600;
    margin: 10px 0;
    cursor: pointer;
    transition: 0.5s;
}

.btn:hover {
    background-color: #4d84e2;
}

.login-link {
    margin-top: 1rem;
    text-align: center;
}

.login-link a {
    color: #5995fd;
    text-decoration: none;
    font-weight: 600;
    transition: color 0.3s;
}

.login-link a:hover {
    color: #4d84e2;
}

.notification {
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 12px 24px;
    border-radius: 4px;
    color: white;
    font-weight: 500;
    z-index: 1000;
    animation: slideIn 0.3s ease-out;
}

.notification.error {
    background-color: #ef4444;
}

.notification.success {
    background-color: #10b981;
}

.notification.info {
    background-color: #3b82f6;
}

@keyframes slideIn {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

@media (max-width: 768px) {
    .container {
        padding: 1rem;
    }
    
    .form-container {
        padding: 1rem;
    }
    
    form {
        padding: 1.5rem;
    }
    
    .title {
        font-size: 1.8rem;
    }
    
    .input-field {
        height: 50px;
    }
    
    .btn {
        width: 120px;
        height: 45px;
    }
}
</style>