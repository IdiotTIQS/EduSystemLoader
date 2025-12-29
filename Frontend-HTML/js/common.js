/**
 * 公共JavaScript功能
 * 包含教师端和学生端共用的工具函数和组件
 */

// 全局配置
const AppConfig = {
  apiBaseUrl: 'http://localhost:8080/api',
  tokenKey: 'edu_token',
  userKey: 'edu_user',
  refreshInterval: 30 * 60 * 1000, // 30分钟
  notificationTimeout: 5000 // 5秒
};

// 工具函数
const Utils = {
  /**
   * 获取存储的token
   */
  getToken() {
    return localStorage.getItem(AppConfig.tokenKey);
  },

  /**
   * 设置token
   */
  setToken(token) {
    localStorage.setItem(AppConfig.tokenKey, token);
  },

  /**
   * 清除token
   */
  clearToken() {
    localStorage.removeItem(AppConfig.tokenKey);
    localStorage.removeItem(AppConfig.userKey);
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    const userStr = localStorage.getItem(AppConfig.userKey);
    return userStr ? JSON.parse(userStr) : null;
  },

  /**
   * 设置当前用户信息
   */
  setCurrentUser(user) {
    localStorage.setItem(AppConfig.userKey, JSON.stringify(user));
  },

  /**
   * 检查用户是否已登录
   */
  isLoggedIn() {
    return !!this.getToken();
  },

  /**
   * 格式化日期时间
   */
  formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  },

  /**
   * 格式化日期
   */
  formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN');
  },

  /**
   * 格式化文件大小
   */
  formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  },

  /**
   * 生成随机ID
   */
  generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  },

  /**
   * 防抖函数
   */
  debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  },

  /**
   * 节流函数
   */
  throttle(func, limit) {
    let inThrottle;
    return function() {
      const args = arguments;
      const context = this;
      if (!inThrottle) {
        func.apply(context, args);
        inThrottle = true;
        setTimeout(() => inThrottle = false, limit);
      }
    };
  },

  /**
   * 深拷贝对象
   */
  deepClone(obj) {
    if (obj === null || typeof obj !== 'object') return obj;
    const clone = Array.isArray(obj) ? [] : {};
    Object.keys(obj).forEach(key => {
      clone[key] = this.deepClone(obj[key]);
    });
    return clone;
  },

  /**
   * 获取URL参数
   */
  getUrlParam(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
  },

  /**
   * 设置URL参数
   */
  setUrlParam(name, value) {
    const url = new URL(window.location);
    url.searchParams.set(name, value);
    window.history.replaceState({}, '', url);
  },

  /**
   * 移除URL参数
   */
  removeUrlParam(name) {
    const url = new URL(window.location);
    url.searchParams.delete(name);
    window.history.replaceState({}, '', url);
  }
};

// HTTP请求工具
const Http = {
  /**
   * 发送HTTP请求
   */
  async request(url, options = {}) {
    const token = Utils.getToken();
    const defaultOptions = {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      }
    };

    const config = {
      ...defaultOptions,
      ...options,
      headers: {
        ...defaultOptions.headers,
        ...options.headers
      }
    };

    try {
      const response = await fetch(`${AppConfig.apiBaseUrl}${url}`, config);
      
      // 检查响应状态
      if (!response.ok) {
        if (response.status === 401) {
          // Token过期，清除本地存储并跳转到登录页
          Utils.clearToken();
          window.location.href = '/pages/login.html';
          return;
        }
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('HTTP请求错误:', error);
      Notification.show(error.message || '请求失败，请稍后重试', 'error');
      throw error;
    }
  },

  /**
   * GET请求
   */
  get(url, params = {}) {
    const queryString = new URLSearchParams(params).toString();
    const fullUrl = queryString ? `${url}?${queryString}` : url;
    return this.request(fullUrl);
  },

  /**
   * POST请求
   */
  post(url, data = {}) {
    return this.request(url, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  /**
   * PUT请求
   */
  put(url, data = {}) {
    return this.request(url, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  /**
   * DELETE请求
   */
  delete(url) {
    return this.request(url, {
      method: 'DELETE'
    });
  },

  /**
   * 文件上传
   */
  upload(url, formData, onProgress) {
    const token = Utils.getToken();
    
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      
      // 监听上传进度
      if (onProgress && xhr.upload) {
        xhr.upload.addEventListener('progress', (event) => {
          if (event.lengthComputable) {
            const percentComplete = (event.loaded / event.total) * 100;
            onProgress(percentComplete);
          }
        });
      }
      
      // 请求完成处理
      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const response = JSON.parse(xhr.responseText);
            resolve(response);
          } catch (error) {
            reject(new Error('响应格式错误'));
          }
        } else if (xhr.status === 401) {
          Utils.clearToken();
          window.location.href = '/pages/login.html';
        } else {
          try {
            const errorResponse = JSON.parse(xhr.responseText);
            reject(new Error(errorResponse.message || '上传失败'));
          } catch {
            reject(new Error('上传失败'));
          }
        }
      });
      
      // 请求错误处理
      xhr.addEventListener('error', () => {
        reject(new Error('网络错误'));
      });
      
      // 设置请求头并发送
      xhr.open('POST', `${AppConfig.apiBaseUrl}${url}`);
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      }
      xhr.send(formData);
    });
  }
};

// 通知组件
const Notification = {
  /**
   * 显示通知
   */
  show(message, type = 'info', duration = AppConfig.notificationTimeout) {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
      <div class="notification-content">
        <div class="notification-icon">
          ${this.getIcon(type)}
        </div>
        <div class="notification-message">${message}</div>
        <button class="notification-close">&times;</button>
      </div>
    `;
    
    // 添加样式
    this.addStyles();
    
    // 添加到页面
    document.body.appendChild(notification);
    
    // 显示动画
    setTimeout(() => {
      notification.classList.add('show');
    }, 10);
    
    // 绑定关闭事件
    const closeBtn = notification.querySelector('.notification-close');
    closeBtn.addEventListener('click', () => {
      this.hide(notification);
    });
    
    // 自动隐藏
    setTimeout(() => {
      this.hide(notification);
    }, duration);
  },

  /**
   * 隐藏通知
   */
  hide(notification) {
    notification.classList.add('hide');
    setTimeout(() => {
      if (notification.parentNode) {
        notification.parentNode.removeChild(notification);
      }
    }, 300);
  },

  /**
   * 获取图标
   */
  getIcon(type) {
    const icons = {
      success: '<i class="fas fa-check-circle"></i>',
      error: '<i class="fas fa-exclamation-circle"></i>',
      warning: '<i class="fas fa-exclamation-triangle"></i>',
      info: '<i class="fas fa-info-circle"></i>'
    };
    return icons[type] || icons.info;
  },

  /**
   * 添加样式
   */
  addStyles() {
    if (document.getElementById('notification-styles')) return;
    
    const style = document.createElement('style');
    style.id = 'notification-styles';
    style.textContent = `
      .notification {
        position: fixed;
        top: 20px;
        right: 20px;
        min-width: 300px;
        max-width: 400px;
        padding: 16px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        z-index: 9999;
        transform: translateX(100%);
        opacity: 0;
        transition: all 0.3s ease;
      }
      
      .notification.show {
        transform: translateX(0);
        opacity: 1;
      }
      
      .notification.hide {
        transform: translateX(100%);
        opacity: 0;
      }
      
      .notification.success {
        background-color: #f0f9ff;
        border-left: 4px solid #10b981;
        color: #065f46;
      }
      
      .notification.error {
        background-color: #fef2f2;
        border-left: 4px solid #ef4444;
        color: #991b1b;
      }
      
      .notification.warning {
        background-color: #fffbeb;
        border-left: 4px solid #f59e0b;
        color: #92400e;
      }
      
      .notification.info {
        background-color: #eff6ff;
        border-left: 4px solid #3b82f6;
        color: #1e40af;
      }
      
      .notification-content {
        display: flex;
        align-items: center;
      }
      
      .notification-icon {
        margin-right: 12px;
        font-size: 18px;
      }
      
      .notification-message {
        flex: 1;
        font-size: 14px;
        line-height: 1.5;
      }
      
      .notification-close {
        background: none;
        border: none;
        font-size: 18px;
        cursor: pointer;
        opacity: 0.5;
        transition: opacity 0.2s;
        padding: 0;
        margin-left: 12px;
      }
      
      .notification-close:hover {
        opacity: 1;
      }
    `;
    document.head.appendChild(style);
  }
};

// 模态框组件
const Modal = {
  /**
   * 显示模态框
   */
  show(options = {}) {
    const {
      title = '提示',
      content = '',
      size = 'medium', // small, medium, large
      showClose = true,
      backdrop = true,
      keyboard = true
    } = options;

    // 创建模态框元素
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
      <div class="modal-backdrop"></div>
      <div class="modal-dialog modal-${size}">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">${title}</h5>
            ${showClose ? '<button type="button" class="modal-close">&times;</button>' : ''}
          </div>
          <div class="modal-body">${content}</div>
        </div>
      </div>
    `;
    
    // 添加样式
    this.addStyles();
    
    // 添加到页面
    document.body.appendChild(modal);
    document.body.style.overflow = 'hidden';
    
    // 显示动画
    setTimeout(() => {
      modal.classList.add('show');
    }, 10);
    
    // 绑定事件
    this.bindEvents(modal, keyboard);
    
    return modal;
  },

  /**
   * 隐藏模态框
   */
  hide(modal) {
    if (!modal) return;
    
    modal.classList.remove('show');
    setTimeout(() => {
      if (modal.parentNode) {
        modal.parentNode.removeChild(modal);
      }
      document.body.style.overflow = '';
    }, 300);
  },

  /**
   * 绑定事件
   */
  bindEvents(modal, keyboard) {
    // 关闭按钮事件
    const closeBtn = modal.querySelector('.modal-close');
    if (closeBtn) {
      closeBtn.addEventListener('click', () => {
        this.hide(modal);
      });
    }
    
    // 背景点击事件
    const backdrop = modal.querySelector('.modal-backdrop');
    if (backdrop) {
      backdrop.addEventListener('click', () => {
        this.hide(modal);
      });
    }
    
    // ESC键关闭
    if (keyboard) {
      const handleKeydown = (e) => {
        if (e.key === 'Escape') {
          this.hide(modal);
          document.removeEventListener('keydown', handleKeydown);
        }
      };
      document.addEventListener('keydown', handleKeydown);
    }
  },

  /**
   * 添加样式
   */
  addStyles() {
    if (document.getElementById('modal-styles')) return;
    
    const style = document.createElement('style');
    style.id = 'modal-styles';
    style.textContent = `
      .modal {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 1050;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      .modal-backdrop {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        opacity: 0;
        transition: opacity 0.3s ease;
      }
      
      .modal-dialog {
        position: relative;
        width: 90%;
        max-width: 500px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        transform: scale(0.7);
        opacity: 0;
        transition: all 0.3s ease;
        z-index: 1;
      }
      
      .modal-small {
        max-width: 400px;
      }
      
      .modal-medium {
        max-width: 600px;
      }
      
      .modal-large {
        max-width: 800px;
      }
      
      .modal.show .modal-backdrop {
        opacity: 1;
      }
      
      .modal.show .modal-dialog {
        transform: scale(1);
        opacity: 1;
      }
      
      .modal-content {
        width: 100%;
      }
      
      .modal-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 20px;
        border-bottom: 1px solid #e5e7eb;
      }
      
      .modal-title {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #111827;
      }
      
      .modal-close {
        background: none;
        border: none;
        font-size: 24px;
        color: #6b7280;
        cursor: pointer;
        padding: 0;
        line-height: 1;
      }
      
      .modal-close:hover {
        color: #374151;
      }
      
      .modal-body {
        padding: 20px;
      }
    `;
    document.head.appendChild(style);
  }
};

// 加载组件
const Loading = {
  /**
   * 显示加载状态
   */
  show(container, text = '加载中...') {
    const loadingElement = document.createElement('div');
    loadingElement.className = 'loading-container';
    loadingElement.innerHTML = `
      <div class="loading-spinner"></div>
      <div class="loading-text">${text}</div>
    `;
    
    // 添加样式
    this.addStyles();
    
    // 如果指定了容器，则添加到容器中，否则添加到body
    const target = container ? document.querySelector(container) : document.body;
    if (target) {
      target.appendChild(loadingElement);
    }
    
    return loadingElement;
  },

  /**
   * 隐藏加载状态
   */
  hide(loadingElement) {
    if (loadingElement && loadingElement.parentNode) {
      loadingElement.parentNode.removeChild(loadingElement);
    }
  },

  /**
   * 添加样式
   */
  addStyles() {
    if (document.getElementById('loading-styles')) return;
    
    const style = document.createElement('style');
    style.id = 'loading-styles';
    style.textContent = `
      .loading-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 20px;
      }
      
      .loading-spinner {
        width: 32px;
        height: 32px;
        border: 3px solid #f3f4f6;
        border-top: 3px solid #3b82f6;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-bottom: 12px;
      }
      
      .loading-text {
        font-size: 14px;
        color: #6b7280;
      }
      
      @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }
    `;
    document.head.appendChild(style);
  }
};

// 表单验证工具
const Validator = {
  /**
   * 验证邮箱
   */
  isEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  },

  /**
   * 验证手机号
   */
  isPhone(phone) {
    const phoneRegex = /^1[3-9]\d{9}$/;
    return phoneRegex.test(phone);
  },

  /**
   * 验证密码强度（至少8位，包含字母和数字）
   */
  isStrongPassword(password) {
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
    return passwordRegex.test(password);
  },

  /**
   * 验证必填字段
   */
  required(value) {
    return value !== null && value !== undefined && value.toString().trim() !== '';
  },

  /**
   * 验证长度
   */
  length(value, min, max) {
    const len = value ? value.toString().length : 0;
    return len >= min && len <= max;
  },

  /**
   * 验证数字
   */
  isNumber(value) {
    return !isNaN(parseFloat(value)) && isFinite(value);
  },

  /**
   * 验证整数
   */
  isInteger(value) {
    return Number.isInteger(Number(value));
  },

  /**
   * 验证范围
   */
  range(value, min, max) {
    const num = Number(value);
    return num >= min && num <= max;
  }
};

// 权限检查工具
const Auth = {
  /**
   * 检查用户角色
   */
  hasRole(role) {
    const user = Utils.getCurrentUser();
    return user && user.role === role;
  },

  /**
   * 检查是否为教师
   */
  isTeacher() {
    return this.hasRole('TEACHER');
  },

  /**
   * 检查是否为学生
   */
  isStudent() {
    return this.hasRole('STUDENT');
  },

  /**
   * 检查是否有权限访问资源
   */
  canAccess(resource) {
    const user = Utils.getCurrentUser();
    if (!user) return false;
    
    // 根据用户角色和资源类型检查权限
    switch (resource) {
      case 'teacher':
        return this.isTeacher();
      case 'student':
        return this.isStudent();
      case 'dashboard':
        return true; // 所有用户都可以访问仪表盘
      default:
        return false;
    }
  }
};

// 页面初始化工具
const Page = {
  /**
   * 检查登录状态
   */
  checkAuth() {
    if (!Utils.isLoggedIn()) {
      window.location.href = '/pages/login.html';
      return false;
    }
    return true;
  },

  /**
   * 初始化页面
   */
  init(options = {}) {
    const {
      requireAuth = true,
      requiredRole = null,
      title = ''
    } = options;

    // 设置页面标题
    if (title) {
      document.title = title;
    }

    // 检查登录状态
    if (requireAuth && !this.checkAuth()) {
      return;
    }

    // 检查角色权限
    if (requiredRole && !Auth.hasRole(requiredRole)) {
      Notification.show('您没有权限访问此页面', 'error');
      setTimeout(() => {
        window.location.href = '/pages/login.html';
      }, 2000);
      return;
    }

    // 更新用户信息显示
    this.updateUserInfo();
  },

  /**
   * 更新用户信息显示
   */
  updateUserInfo() {
    const user = Utils.getCurrentUser();
    if (!user) return;

    // 更新用户名
    const userNameElements = document.querySelectorAll('.user-name');
    userNameElements.forEach(element => {
      element.textContent = user.username || user.email || '未知用户';
    });

    // 更新用户邮箱
    const userEmailElements = document.querySelectorAll('.user-email');
    userEmailElements.forEach(element => {
      element.textContent = user.email || '';
    });

    // 更新用户头像
    const userAvatarElements = document.querySelectorAll('.user-avatar');
    userAvatarElements.forEach(element => {
      if (user.avatar) {
        element.src = user.avatar;
      }
    });
  },

  /**
   * 退出登录
   */
  logout() {
    Utils.clearToken();
    Notification.show('已安全退出', 'success');
    setTimeout(() => {
      window.location.href = '/pages/login.html';
    }, 1000);
  }
};

// 导出全局对象
window.EduSystem = {
  AppConfig,
  Utils,
  Http,
  Notification,
  Modal,
  Loading,
  Validator,
  Auth,
  Page
};

// DOM加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
  // 初始化页面
  Page.init();
});