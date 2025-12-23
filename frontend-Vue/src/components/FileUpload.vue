<template>
  <div class="file-upload-container">
    <div
      class="upload-area"
      :class="{
        'drag-over': isDragOver,
        'has-file': selectedFile,
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
        :accept="accept"
      />
      
      <!-- 图片预览 -->
      <div v-if="isImageFile && previewUrl" class="image-preview">
        <img :src="previewUrl" :alt="selectedFile?.name" />
        <div class="preview-overlay">
          <button class="remove-btn" @click.stop="removeFile">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
              <path d="M2.5 1a1 1 0 0 0-1 1v1a1 1 0 0 0 1 1H3v9a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V4h.5a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H10a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1H2.5zm3 4a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 .5-.5zM8 5a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7A.5.5 0 0 1 8 5zm3 .5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 1 0z"/>
            </svg>
            删除
          </button>
          <div class="file-name">{{ selectedFile?.name }}</div>
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
            {{ selectedFile ? selectedFile.name : (uploading ? '上传中...' : '点击或拖拽文件到此处') }}
          </p>
          <p class="secondary-text">
            {{ selectedFile ? formatFileSize(selectedFile.size) : `支持 ${accept || '所有文件类型' }` }}
          </p>
        </div>
        
        <button v-if="selectedFile && !uploading" class="remove-btn-small" @click.stop="removeFile">
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
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

const props = defineProps({
  modelValue: {
    type: File,
    default: null
  },
  accept: {
    type: String,
    default: ''
  },
  uploadFunction: {
    type: Function,
    required: true
  }
});

const emit = defineEmits(['update:modelValue', 'upload-success', 'upload-error']);

const fileInput = ref(null);
const isDragOver = ref(false);
const uploading = ref(false);
const uploadProgress = ref(0);
const selectedFile = ref(props.modelValue);
const previewUrl = ref('');

// 计算是否为图片文件
const isImageFile = computed(() => {
  if (!selectedFile.value) return false;
  const imageTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp', 'image/svg+xml'];
  return imageTypes.includes(selectedFile.value.type);
});

// 监听文件变化，生成预览
watch(selectedFile, (newFile) => {
  if (newFile && isImageFile.value) {
    const reader = new FileReader();
    reader.onload = (e) => {
      previewUrl.value = e.target.result;
    };
    reader.readAsDataURL(newFile);
  } else {
    previewUrl.value = '';
  }
  
  emit('update:modelValue', newFile);
});

// 触发文件选择
const triggerFileSelect = () => {
  if (!uploading.value) {
    fileInput.value?.click();
  }
};

// 处理文件选择
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
  isDragOver.value = true;
};

const handleDragLeave = (event) => {
  event.preventDefault();
  event.stopPropagation();
  if (event.target === event.currentTarget) {
    isDragOver.value = false;
  }
};

const handleDrop = (event) => {
  event.preventDefault();
  event.stopPropagation();
  isDragOver.value = false;
  
  const files = event.dataTransfer.files;
  if (files.length > 0) {
    processFile(files[0]);
  }
};

// 处理文件
const processFile = async (file) => {
  // 客户端预先检查文件大小
  const maxSize = 100 * 1024 * 1024; // 100MB
  if (file.size > maxSize) {
    const error = new Error('文件大小超出限制，请选择小于100MB的文件');
    emit('upload-error', error);
    return;
  }
  
  selectedFile.value = file;
  
  try {
    uploading.value = true;
    uploadProgress.value = 0;
    
    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += Math.random() * 20;
      }
    }, 200);
    
    const result = await props.uploadFunction(file);
    
    clearInterval(progressInterval);
    uploadProgress.value = 100;
    
    emit('upload-success', result);
    
    setTimeout(() => {
      uploading.value = false;
      uploadProgress.value = 0;
    }, 500);
    
  } catch (error) {
    uploading.value = false;
    uploadProgress.value = 0;
    selectedFile.value = null;
    emit('upload-error', error);
  }
};

// 移除文件
const removeFile = () => {
  selectedFile.value = null;
  previewUrl.value = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
  emit('update:modelValue', null);
};

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};
</script>

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