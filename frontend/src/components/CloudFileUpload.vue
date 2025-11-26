<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { cloudFileApi } from '../services/api';

const props = defineProps({
  classId: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['upload-success', 'upload-error']);

const fileInput = ref(null);
const form = reactive({
  file: null,
  description: '',
  isPublic: true
});

const uploading = ref(false);
const dragOver = ref(false);
const uploadProgress = ref(0);
const error = ref('');
const previewUrl = ref('');

// 计算是否为图片文件
const isImageFile = computed(() => {
  if (!form.file) return false;
  const imageTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp', 'image/svg+xml'];
  return imageTypes.includes(form.file.type);
});

// 监听文件变化，生成预览
watch(() => form.file, (newFile) => {
  if (newFile && isImageFile.value) {
    const reader = new FileReader();
    reader.onload = (e) => {
      previewUrl.value = e.target.result;
    };
    reader.readAsDataURL(newFile);
  } else {
    previewUrl.value = '';
  }
});

// 触发文件选择
const triggerFileSelect = () => {
  if (!uploading.value) {
    fileInput.value?.click();
  }
};

const validateFile = (file) => {
  // 检查文件大小 (100MB)
  const maxSize = 100 * 1024 * 1024;
  if (file.size > maxSize) {
    error.value = '文件大小超出限制，最大支持100MB';
    return false;
  }
  
  // 检查文件类型
  const fileName = file.name.toLowerCase();
  const dangerousTypes = [
    '.exe', '.bat', '.cmd', '.com', '.scr', '.msi', '.dll', '.so', '.dylib',
    '.sh', '.bash', '.zsh', '.fish', '.ps1', '.py', '.pl', '.rb', '.php',
    '.asp', '.jsp', '.js', '.vbs', '.wsf', '.jar', '.app', '.deb', '.rpm',
    '.dmg', '.pkg', '.iso', '.img', '.vmdk', '.ova', '.ovf'
  ];
  
  for (const type of dangerousTypes) {
    if (fileName.endsWith(type)) {
      error.value = `不允许上传 ${type} 文件`;
      return false;
    }
  }
  
  return true;
};

const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file) {
    processFile(file);
  }
};

// 处理拖拽相关事件
const handleDragOver = (event) => {
  event.preventDefault();
  event.stopPropagation();
};

const handleDragEnter = (event) => {
  event.preventDefault();
  event.stopPropagation();
  dragOver.value = true;
};

const handleDragLeave = (event) => {
  event.preventDefault();
  event.stopPropagation();
  if (event.target === event.currentTarget) {
    dragOver.value = false;
  }
};

const handleDrop = (event) => {
  event.preventDefault();
  event.stopPropagation();
  dragOver.value = false;
  
  const files = event.dataTransfer.files;
  if (files.length > 0) {
    processFile(files[0]);
  }
};

// 处理文件
const processFile = (file) => {
  if (validateFile(file)) {
    form.file = file;
    error.value = '';
  }
};

// 移除文件
const removeFile = () => {
  form.file = null;
  previewUrl.value = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const uploadFile = async () => {
  if (!form.file) {
    error.value = '请选择要上传的文件';
    return;
  }
  
  uploading.value = true;
  uploadProgress.value = 0;
  error.value = '';
  
  try {
    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += Math.random() * 20;
      }
    }, 200);
    
    const result = await cloudFileApi.uploadFile(
      props.classId,
      form.file,
      form.description.trim() || undefined,
      form.isPublic
    );
    
    clearInterval(progressInterval);
    uploadProgress.value = 100;
    
    emit('upload-success', result);
    
    setTimeout(() => {
      uploading.value = false;
      uploadProgress.value = 0;
      
      // 重置表单
      form.file = null;
      form.description = '';
      form.isPublic = true;
      previewUrl.value = '';
      
      // 重置文件选择器
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    }, 500);
    
  } catch (e) {
    uploading.value = false;
    uploadProgress.value = 0;
    emit('upload-error', e.message || '上传失败');
  }
};

const cancel = () => {
  form.file = null;
  form.description = '';
  form.isPublic = true;
  previewUrl.value = '';
  error.value = '';
  
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const formatFileSize = (size) => {
  if (size < 1024) return size + ' B';
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB';
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(1) + ' MB';
  return (size / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
};

const getFileIcon = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase();
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];
  const docExts = ['pdf', 'doc', 'docx', 'txt', 'rtf', 'odt'];
  const spreadsheetExts = ['xls', 'xlsx', 'csv', 'ods'];
  const presentationExts = ['ppt', 'pptx', 'odp'];
  const archiveExts = ['zip', 'rar', '7z', 'tar', 'gz'];
  
  if (imageExts.includes(ext)) return '🖼️';
  if (docExts.includes(ext)) return '📄';
  if (spreadsheetExts.includes(ext)) return '📊';
  if (presentationExts.includes(ext)) return '📑';
  if (archiveExts.includes(ext)) return '📦';
  return '📁';
};
</script>

<template>
  <div class="file-upload-container">
    <div
      class="upload-area"
      :class="{
        'drag-over': dragOver,
        'has-file': form.file,
        'uploading': uploading
      }"
      @click="triggerFileSelect"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
    >
      <input
        ref="fileInput"
        type="file"
        class="file-input"
        @change="handleFileSelect"
      />
      
      <!-- 图片预览 -->
      <div v-if="form.file && isImageFile" class="image-preview">
        <img :src="previewUrl" :alt="form.file.name" />
        <div class="preview-overlay">
          <button class="remove-btn" @click.stop="removeFile">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
              <path d="M2.5 1a1 1 0 0 0-1 1v1a1 1 0 0 0 1 1H3v9a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V4h.5a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H10a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1H2.5zm3 4a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 .5-.5zM8 5a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7A.5.5 0 0 1 8 5zm3 .5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 1 0z"/>
            </svg>
            删除
          </button>
          <div class="file-name">{{ form.file.name }}</div>
        </div>
      </div>
      
      <!-- 非图片文件或无文件时的默认显示 -->
      <div v-else class="upload-content">
        <div class="upload-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
        </div>
        
        <div class="upload-text">
          <p class="primary-text">
            {{ form.file ? form.file.name : (uploading ? '上传中...' : '点击或拖拽文件到此处') }}
          </p>
          <p class="secondary-text">
            {{ form.file ? formatFileSize(form.file.size) : '支持最大100MB的文件' }}
          </p>
        </div>
        
        <button v-if="form.file && !uploading" class="remove-btn-small" @click.stop="removeFile">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M2.5 1a1 1 0 0 0-1 1v1a1 1 0 0 0 1 1H3v9a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V4h.5a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H10a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1H2.5zm3 4a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 .5-.5zM8 5a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7A.5.5 0 0 1 8 5zm3 .5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 1 0z"/>
          </svg>
        </button>
      </div>
    </div>
    
    <!-- 上传进度条 -->
    <div v-if="uploading" class="progress-bar">
      <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
    </div>

    <!-- 文件选项 -->
    <div v-if="form.file && !uploading" class="upload-options">
      <div class="form-group">
        <label for="description">文件描述（可选）</label>
        <textarea
          id="description"
          v-model="form.description"
          placeholder="添加文件描述..."
          rows="3"
          maxlength="500"
        ></textarea>
      </div>
      
      <div class="form-group">
        <label>
          <input type="checkbox" v-model="form.isPublic" />
          公开文件（所有学生可见）
        </label>
      </div>

      <div v-if="error" class="error">
        {{ error }}
      </div>

      <div class="upload-actions">
        <button class="secondary" @click="cancel">取消</button>
        <button class="primary" @click="uploadFile">
          上传文件
        </button>
      </div>
    </div>

    <div class="upload-tips">
      <h4>💡 上传提示：</h4>
      <ul>
        <li>支持图片、文档、表格、演示文稿、压缩包等常见文件格式</li>
        <li>单个文件最大支持100MB</li>
        <li>不允许上传可执行文件和脚本文件</li>
        <li>公开文件所有学生都可以下载，私有文件仅自己可见</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.file-upload-container {
  width: 100%;
}

.upload-area {
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f8fafc;
  position: relative;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}

.upload-area.drag-over {
  border-color: #3b82f6;
  background: #dbeafe;
  transform: scale(1.02);
}

.upload-area.has-file {
  border-color: #10b981;
  background: #ecfdf5;
}

.upload-area.uploading {
  border-color: #f59e0b;
  background: #fef3c7;
  cursor: not-allowed;
}

.file-input {
  display: none;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.upload-icon {
  color: #64748b;
  transition: color 0.3s ease;
}

.upload-area:hover .upload-icon {
  color: #3b82f6;
}

.upload-text {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.primary-text {
  margin: 0;
  font-weight: 500;
  color: #1e293b;
  font-size: 1rem;
}

.secondary-text {
  margin: 0;
  color: #64748b;
  font-size: 0.875rem;
}

.remove-btn-small {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 0.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

.remove-btn-small:hover {
  background: #dc2626;
}

.image-preview {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 8px;
}

.image-preview:hover .preview-overlay {
  opacity: 1;
}

.remove-btn {
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 0.75rem 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
  transition: background 0.3s ease;
  margin-bottom: 1rem;
}

.remove-btn:hover {
  background: #dc2626;
}

.file-name {
  color: white;
  font-weight: 500;
  text-align: center;
  padding: 0 1rem;
  word-break: break-all;
  max-width: 80%;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
  margin-top: 1rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #10b981);
  transition: width 0.3s ease;
  border-radius: 2px;
}

.upload-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #374151;
}

.form-group textarea {
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  resize: vertical;
  font-family: inherit;
}

.form-group input[type="checkbox"] {
  margin-right: 0.5rem;
}

.error {
  background: #fee2e2;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 4px;
  font-size: 0.9rem;
}

.upload-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.upload-tips {
  background: #f1f5f9;
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
  margin-top: 1rem;
}

.upload-tips h4 {
  margin: 0 0 0.75rem 0;
  color: #1e293b;
  font-size: 1rem;
}

.upload-tips ul {
  margin: 0;
  padding-left: 1.5rem;
  color: #64748b;
}

.upload-tips li {
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.upload-tips li:last-child {
  margin-bottom: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .upload-area {
    padding: 1.5rem 1rem;
    min-height: 150px;
  }
  
  .upload-icon {
    width: 36px;
    height: 36px;
  }
  
  .primary-text {
    font-size: 0.9rem;
  }
  
  .secondary-text {
    font-size: 0.8rem;
  }
}
</style>