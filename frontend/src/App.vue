<script setup>
import { RouterLink, RouterView } from 'vue-router';
</script>

<template>
  <div class="app-shell">
    <header>
      <div>
        <h1>EduSystem Loader 控制台</h1>
        <p class="subtitle">教师、学生一体化教学协作平台</p>
      </div>
      <nav>
        <RouterLink to="/login">登录</RouterLink>
        <RouterLink to="/register">注册</RouterLink>
        <RouterLink to="/teacher">教师端</RouterLink>
        <RouterLink to="/student">学生端</RouterLink>
      </nav>
    </header>
    <main class="page-body">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.subtitle {
  margin: 0.25rem 0 0;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.85);
}
</style>
