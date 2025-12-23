<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { authApi } from '../services/api';
import { setAuth } from '../services/auth';

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const notification = reactive({ type: 'info', text: '' });
const isLogin = ref(false);
const form = reactive({
  username: '',
  password: '',
  email: '',
  confirmPassword: '',
  role: 'STUDENT',
  realName: '',
  phone: '',
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
  <div :class="['container', { 'sign-up-mode': isLogin }]">
    <div class="form-container">
      <div class="signin-signup">
        <form action="#" class="sign-in-form" @submit.prevent="handleLogin">
          <img src="@/assets/images/Logo.png" alt="Logo" class="logo" />
          <h2 class="title">登录</h2>
          <div class="input-field">
            <i><font-awesome-icon icon="fa-solid fa-user" /></i>
            <input v-model="form.username" type="text" placeholder="用户名" />
          </div>
          <div class="input-field">
            <i><font-awesome-icon icon="fa-solid fa-lock" /></i>
            <input v-model="form.password" type="password" placeholder="密码" />
          </div>
          <input type="submit" value="立即登录" class="btn solid" :disabled="loading" />
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
        </form>
        <form action="#" class="sign-up-form" @submit.prevent="handleRegister">
          <img src="@/assets/images/Logo.png" alt="Logo" class="logo" />
          <h2 class="title">注册</h2>
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
        </form>
      </div>
    </div>

    <div class="panels-container">
      <div class="panel left-panel">
        <div class="content">
          <h3>加入我们</h3>
          <p>加入我们，成为本站的一份子。</p>
          <button class="btn transparent" @click="isLogin = !isLogin">
            去注册
          </button>
        </div>
        <img src="@/assets/images/signin.svg" class="image" alt="" />
      </div>
      <div class="panel right-panel">
        <div class="content">
          <h3>已有帐号？</h3>
          <p>立即登录已有帐号，享受独家权益。</p>
          <button class="btn transparent" @click="isLogin = !isLogin">
            去登录
          </button>
        </div>
        <img src="@/assets/images/signup.svg" class="image" alt="" />
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
}

.forms-container {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}

.signin-signup {
    position: absolute;
    top: 50%;
    transform: translate(-50%, -50%);
    left: 75%;
    width: 50%;
    transition: 1s 0.7s ease-in-out;
    display: grid;
    grid-template-columns: 1fr;
    z-index: 5;
}

form {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    padding: 0rem 5rem;
    transition: all 0.2s 0.7s;
    overflow: hidden;
    grid-column: 1 / 2;
    grid-row: 1 / 2;
}

form.sign-up-form {
    opacity: 0;
    z-index: 1;
}

form.sign-in-form {
    z-index: 2;
}

.title {
    font-size: 2.2rem;
    color: #444;
    margin-bottom: 10px;
}

.logo {
    width: 80px;
    margin-bottom: 10px;
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
.panels-container {
    position: absolute;
    height: 100%;
    width: 100%;
    top: 0;
    left: 0;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
}

.container:before {
    content: '';
    position: absolute;
    height: 2000px;
    width: 2000px;
    top: -10%;
    right: 48%;
    transform: translateY(-50%);
    background-image: linear-gradient(-45deg, #4481eb 0%, #04befe 100%);
    transition: 1.8s ease-in-out;
    border-radius: 50%;
    z-index: 6;
}

.image {
    width: 100%;
    transition: transform 1.1s ease-in-out;
    transition-delay: 0.4s;
}

.panel {
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    text-align: center;
    z-index: 6;
}

.left-panel {
    pointer-events: all;
    padding: 3rem 17% 2rem 12%;
}

.right-panel {
    pointer-events: none;
    padding: 3rem 12% 2rem 17%;
}

.panel .content {
    color: #fff;
    transition: transform 0.9s ease-in-out;
    transition-delay: 0.6s;
}

.panel h3 {
    font-weight: 600;
    line-height: 1;
    font-size: 1.5rem;
}

.panel p {
    font-size: 0.95rem;
    padding: 0.7rem 0;
}

.btn.transparent {
    margin: 0;
    background: none;
    border: 2px solid #fff;
    width: 130px;
    height: 41px;
    font-weight: 600;
    font-size: 0.8rem;
}

.right-panel .image,
.right-panel .content {
    transform: translateX(800px);
}

/* ANIMATION */

.container.sign-up-mode:before {
    transform: translate(100%, -50%);
    right: 52%;
}

.container.sign-up-mode .left-panel .image,
.container.sign-up-mode .left-panel .content {
    transform: translateX(-800px);
}

.container.sign-up-mode .signin-signup {
    left: 25%;
}

.container.sign-up-mode form.sign-up-form {
    opacity: 1;
    z-index: 2;
}

.container.sign-up-mode form.sign-in-form {
    opacity: 0;
    z-index: 1;
}

.container.sign-up-mode .right-panel .image,
.container.sign-up-mode .right-panel .content {
    transform: translateX(0%);
}

.container.sign-up-mode .left-panel {
    pointer-events: none;
}

.container.sign-up-mode .right-panel {
    pointer-events: all;
}

@media (max-width: 870px) {
    .container {
        min-height: 800px;
        height: 100vh;
    }
    .signin-signup {
        width: 100%;
        top: 95%;
        transform: translate(-50%, -100%);
        transition: 1s 0.8s ease-in-out;
    }

    .signin-signup,
    .container.sign-up-mode .signin-signup {
        left: 50%;
    }

    .panels-container {
        grid-template-columns: 1fr;
        grid-template-rows: 1fr 2fr 1fr;
    }

    .panel {
        flex-direction: row;
        justify-content: space-around;
        align-items: center;
        padding: 2.5rem 8%;
        grid-column: 1 / 2;
    }

    .right-panel {
        grid-row: 3 / 4;
    }

    .left-panel {
        grid-row: 1 / 2;
    }

    .image {
        width: 200px;
        transition: transform 0.9s ease-in-out;
        transition-delay: 0.6s;
    }

    .panel .content {
        padding-right: 15%;
        transition: transform 0.9s ease-in-out;
        transition-delay: 0.8s;
    }

    .panel h3 {
        font-size: 1.2rem;
    }

    .panel p {
        font-size: 0.7rem;
        padding: 0.5rem 0;
    }

    .btn.transparent {
        width: 110px;
        height: 35px;
        font-size: 0.7rem;
    }

    .container:before {
        width: 1500px;
        height: 1500px;
        transform: translateX(-50%);
        left: 30%;
        bottom: 68%;
        right: initial;
        top: initial;
        transition: 2s ease-in-out;
    }

    .container.sign-up-mode:before {
        transform: translate(-50%, 100%);
        bottom: 32%;
        right: initial;
    }

    .container.sign-up-mode .left-panel .image,
    .container.sign-up-mode .left-panel .content {
        transform: translateY(-300px);
    }

    .container.sign-up-mode .right-panel .image,
    .container.sign-up-mode .right-panel .content {
        transform: translateY(0px);
    }

    .right-panel .image,
    .right-panel .content {
        transform: translateY(300px);
    }

    .container.sign-up-mode .signin-signup {
        top: 5%;
        transform: translate(-50%, 0);
    }
}

@media (max-width: 570px) {
    form {
        padding: 0 1.5rem;
    }

    .image {
        display: none;
    }
    .panel .content {
        padding: 0.5rem 1rem;
    }
    .container {
        padding: 1.5rem;
    }

    .container:before {
        bottom: 72%;
        left: 50%;
    }

    .container.sign-up-mode:before {
        bottom: 28%;
        left: 50%;
    }
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
</style>