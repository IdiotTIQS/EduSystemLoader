const AUTH_KEY = 'edu-auth';
const STUDENT_CLASSES_KEY = 'edu-student-classes';

export const getAuth = () => {
  return JSON.parse(localStorage.getItem(AUTH_KEY)) || {};
};

export const setAuth = (payload) => {
  localStorage.setItem(AUTH_KEY, JSON.stringify(payload));
};

export const clearAuth = () => {
  localStorage.removeItem(AUTH_KEY);
};

export const getStudentClasses = () => {
  return JSON.parse(localStorage.getItem(STUDENT_CLASSES_KEY)) || [];
};

export const setStudentClasses = (classes) => {
  localStorage.setItem(STUDENT_CLASSES_KEY, JSON.stringify(classes));
};

export const getToken = () => {
  const auth = getAuth();
  return auth.token;
};

export const saveStudentClasses = (classes) => {
  setStudentClasses(classes);
};
