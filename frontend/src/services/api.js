import axios from 'axios';
import { getToken, clearAuth } from './auth';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'code')) {
      if (payload.code !== 0) {
        return Promise.reject(new Error(payload.message || '业务异常'));
      }
      return payload.data;
    }
    return payload;
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  register: (body) => api.post('/api/auth/register', body),
  login: (body) => api.post('/api/auth/login', body),
  getProfile: () => api.get('/api/users/profile'),
  getUserProfile: (userId) => api.get(`/api/users/${userId}/profile`),
  updateProfile: (body) => api.put('/api/users/profile', body),
};

export const uploadApi = {
  uploadFile: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/api/upload/file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};

export const classApi = {
  create: (body) => api.post('/api/classes', body),
  get: (id) => api.get(`/api/classes/${id}`),
  listByTeacher: (teacherId) => api.get(`/api/classes/teacher/${teacherId}`),
  updateName: (id, name) => api.put(`/api/classes/${id}`, null, { params: { name } }),
  delete: (id) => api.delete(`/api/classes/${id}`),
  findByCode: (code) => api.get(`/api/classes/code/${code}`),
};

export const courseApi = {
  create: (body) => api.post('/api/courses', body),
  get: (id) => api.get(`/api/courses/${id}`),
  listByClass: (classId) => api.get('/api/courses', { params: { classId } }),
  update: (id, body) => api.put(`/api/courses/${id}`, body),
  delete: (id) => api.delete(`/api/courses/${id}`),
};

export const assignmentApi = {
  create: (body) => api.post('/api/assignments', body),
  get: (id) => api.get(`/api/assignments/${id}`),
  listByCourse: (courseId) => api.get('/api/assignments', { params: { courseId } }),
  update: (id, body) => api.put(`/api/assignments/${id}`, body),
  delete: (id) => api.delete(`/api/assignments/${id}`),
};

export const enrollmentApi = {
  enroll: (classId, studentId) => api.post('/api/enrollments', null, { params: { classId, studentId } }),
  listByClass: (classId) => api.get(`/api/enrollments/class/${classId}`),
  findUnique: (classId, studentId) => api.get('/api/enrollments/unique', { params: { classId, studentId } }),
};

export const submissionApi = {
  submit: (body) => api.post('/api/submissions', body),
  listByAssignment: (assignmentId) => api.get('/api/submissions', { params: { assignmentId } }),
  findUnique: (assignmentId, studentId) => api.get('/api/submissions/unique', { params: { assignmentId, studentId } }),
  updateContent: (id, body) => api.put(`/api/submissions/${id}`, body),
  grade: (id, score, feedback) => api.put(`/api/submissions/${id}/grade`, null, { params: { score, feedback } }),
};

export const discussionApi = {
  create: (body) => api.post('/api/discussions', body),
  get: (id) => api.get(`/api/discussions/${id}`),
  listByClass: (classId) => api.get(`/api/discussions/class/${classId}`),
  update: (id, body) => api.put(`/api/discussions/${id}`, body),
  delete: (id) => api.delete(`/api/discussions/${id}`),
  updatePinnedStatus: (id, isPinned) => api.put(`/api/discussions/${id}/pin`, null, { params: { isPinned } }),
  createComment: (discussionId, body) => api.post(`/api/discussions/${discussionId}/comments`, body),
  listComments: (discussionId) => api.get(`/api/discussions/${discussionId}/comments`),
  updateComment: (id, body) => api.put(`/api/discussions/comments/${id}`, body),
  deleteComment: (id) => api.delete(`/api/discussions/comments/${id}`),
};

export const cloudFileApi = {
  uploadFile: (classId, file, description, isPublic) => {
    const formData = new FormData();
    formData.append('classId', classId);
    formData.append('file', file);
    if (description) formData.append('description', description);
    formData.append('isPublic', isPublic !== false);
    return api.post('/api/cloud/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  listFiles: (classId, fileType, uploaderId) => api.get('/api/cloud/files', { 
    params: { classId, fileType, uploaderId } 
  }),
  getFile: (id) => api.get(`/api/cloud/files/${id}`),
  updateFile: (id, body) => api.put(`/api/cloud/files/${id}`, body),
  deleteFile: (id) => api.delete(`/api/cloud/files/${id}`),
  downloadFile: (id) => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    return `${baseUrl}/api/cloud/files/${id}/download`;
  },
  getStatistics: (classId) => api.get('/api/cloud/statistics', { params: { classId } }),
};

export default api;
