/**
 * 主应用JavaScript文件
 * 包含应用初始化和全局配置
 */

// 全局应用配置
window.EduSystemApp = {
  version: '1.0.0',
  name: '教育管理系统',
  
  // 应用初始化
  init() {
    console.log(`${this.name} v${this.version} 已启动`);
    
    // 检查浏览器兼容性
    this.checkBrowserCompatibility();
    
    // 设置全局错误处理
    this.setupErrorHandling();
    
    // 初始化主题
    this.initTheme();
  },
  
  // 检查浏览器兼容性
  checkBrowserCompatibility() {
    const requiredFeatures = [
      'fetch',
      'Promise',
      'localStorage',
      'sessionStorage'
    ];
    
    const missingFeatures = requiredFeatures.filter(feature => !(feature in window));
    
    if (missingFeatures.length > 0) {
      console.error('浏览器不兼容，缺少以下功能:', missingFeatures);
      alert('您的浏览器版本过低，请升级到最新版本以获得最佳体验。');
    }
  },
  
  // 设置全局错误处理
  setupErrorHandling() {
    // 捕获未处理的Promise拒绝
    window.addEventListener('unhandledrejection', (event) => {
      console.error('未处理的Promise拒绝:', event.reason);
      event.preventDefault();
    });
    
    // 捕获全局错误
    window.addEventListener('error', (event) => {
      console.error('全局错误:', event.error);
    });
  },
  
  // 初始化主题
  initTheme() {
    // 检查系统主题偏好
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      document.body.classList.add('dark-theme');
    }
    
    // 监听主题变化
    if (window.matchMedia) {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        if (e.matches) {
          document.body.classList.add('dark-theme');
        } else {
          document.body.classList.remove('dark-theme');
        }
      });
    }
  }
};

// 当DOM加载完成后初始化应用
document.addEventListener('DOMContentLoaded', () => {
  EduSystemApp.init();
});