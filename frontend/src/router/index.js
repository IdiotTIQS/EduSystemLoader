import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import TeacherDashboard from '../views/TeacherDashboard.vue';
import StudentDashboard from '../views/StudentDashboard.vue';
import { getAuth } from '../services/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    {
      path: '/teacher',
      name: 'teacher-dashboard',
      component: TeacherDashboard,
      meta: { requiresAuth: true, role: 'TEACHER' },
    },
    {
      path: '/student',
      name: 'student-dashboard',
      component: StudentDashboard,
      meta: { requiresAuth: true, role: 'STUDENT' },
    },
  ],
});

router.beforeEach((to, from, next) => {
  const auth = getAuth();
  if (to.meta.requiresAuth && (!auth.userId || !auth.role)) {
    return next({ name: 'login', query: { redirect: to.fullPath } });
  }
  if (to.meta.role && auth.role !== to.meta.role) {
    const target = auth.role === 'TEACHER' ? 'teacher-dashboard' : 'student-dashboard';
    return next({ name: target });
  }
  if ((to.name === 'login' || to.name === 'register') && auth.userId && auth.role) {
    const target = auth.role === 'TEACHER' ? 'teacher-dashboard' : 'student-dashboard';
    return next({ name: target });
  }
  return next();
});

export default router;
