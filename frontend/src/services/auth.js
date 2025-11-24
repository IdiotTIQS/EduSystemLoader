const AUTH_KEY = 'edu-auth';
const STUDENT_CLASSES_KEY = 'edu-student-classes';

export function getAuth() {
  try {
    return JSON.parse(localStorage.getItem(AUTH_KEY)) || {};
  } catch (error) {
    return {};
  }
}

export function setAuth(payload) {
  localStorage.setItem(AUTH_KEY, JSON.stringify(payload));
}

export function clearAuth() {
  localStorage.removeItem(AUTH_KEY);
}

export function getStudentClasses() {
  try {
    return JSON.parse(localStorage.getItem(STUDENT_CLASSES_KEY)) || [];
  } catch (error) {
    return [];
  }
}

export function saveStudentClasses(classes) {
  localStorage.setItem(STUDENT_CLASSES_KEY, JSON.stringify(classes));
}
