<script setup>
import { ref, onMounted, computed } from 'vue';
import { cloudFileApi } from '../services/api';
import { getAuth, getToken } from '../services/auth';
import CloudFileUpload from './CloudFileUpload.vue';

const props = defineProps({
  classId: {
    type: Number,
    required: true
  },
  isTeacher: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['file-uploaded']);

const auth = getAuth();
const files = ref([]);
const statistics = ref(null);
const loading = ref(false);
const error = ref('');
const selectedFile = ref(null);
const showUploadModal = ref(false);
const filterType = ref('all');
const searchQuery = ref('');

const fileTypes = [
  { value: 'all', label: '全部文件' },
  { value: 'image', label: '图片' },
  { value: 'document', label: '文档' },
  { value: 'spreadsheet', label: '表格' },
  { value: 'presentation', label: '演示文稿' },
  { value: 'archive', label: '压缩包' }
];

const fetchFiles = async () => {
  if (!props.classId) return;
  loading.value = true;
  error.value = '';
  try {
    const params = { classId: props.classId };
    if (filterType.value !== 'all') {
      params.fileType = filterType.value;
    }
    if (!props.isTeacher) {
      params.uploaderId = auth.userId;
    }
    const fetchedFiles = await cloudFileApi.listFiles(params.classId, params.fileType, params.uploaderId);
    files.value = fetchedFiles.map(file => enhanceFileObject(file));
  } catch (e) {
    error.value = e.message || '获取文件列表失败';
  } finally {
    loading.value = false;
  }
};

const fetchStatistics = async () => {
  if (!props.classId) return;
  try {
    statistics.value = await cloudFileApi.getStatistics(props.classId);
  } catch (e) {
    console.error('获取统计信息失败:', e);
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

const getFileIcon = (file) => {
  if (isImage(file)) return '🖼️';
  if (isDocument(file)) return '📄';
  if (isSpreadsheet(file)) return '📊';
  if (isPresentation(file)) return '📑';
  if (isArchive(file)) return '📦';
  return '📁';
};

const isImage = (file) => {
  if (!file.fileType) return false;
  const imageTypes = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'];
  return imageTypes.includes(file.fileType.toLowerCase());
};

const isDocument = (file) => {
  if (!file.fileType) return false;
  const docTypes = ['pdf', 'doc', 'docx', 'txt', 'rtf', 'odt'];
  return docTypes.includes(file.fileType.toLowerCase());
};

const isSpreadsheet = (file) => {
  if (!file.fileType) return false;
  const sheetTypes = ['xls', 'xlsx', 'csv', 'ods'];
  return sheetTypes.includes(file.fileType.toLowerCase());
};

const isPresentation = (file) => {
  if (!file.fileType) return false;
  const pptTypes = ['ppt', 'pptx', 'odp'];
  return pptTypes.includes(file.fileType.toLowerCase());
};

const isArchive = (file) => {
  if (!file.fileType) return false;
  const archiveTypes = ['zip', 'rar', '7z', 'tar', 'gz'];
  return archiveTypes.includes(file.fileType.toLowerCase());
};

const getFileCategory = (file) => {
  if (isImage(file)) return '图片';
  if (isDocument(file)) return '文档';
  if (isSpreadsheet(file)) return '表格';
  if (isPresentation(file)) return '演示文稿';
  if (isArchive(file)) return '压缩包';
  return '其他';
};

const formatFileSize = (size) => {
  if (!size || size === 0) return '0 B';
  if (size < 1024) return size + ' B';
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB';
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(1) + ' MB';
  return (size / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
};

// 为后端返回的文件对象添加格式化大小方法
const enhanceFileObject = (file) => {
  return {
    ...file,
    getFormattedFileSize: () => formatFileSize(file.fileSize)
  };
};

const downloadFile = async (file) => {
  try {
    // 使用fetch API获取文件数据，确保携带认证头
    const response = await fetch(cloudFileApi.downloadFile(file.id), {
      headers: {
        'Authorization': `Bearer ${getToken()}`
      }
    });
    
    if (!response.ok) {
      throw new Error('下载失败');
    }
    
    // 创建blob并下载
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = file.originalFileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    // 刷新文件列表以更新下载次数
    await fetchFiles();
  } catch (e) {
    error.value = e.message || '下载失败';
  }
};

const deleteFile = async (file) => {
  if (!confirm(`确定要删除文件 "${file.originalFileName}" 吗？`)) return;
  
  try {
    await cloudFileApi.deleteFile(file.id);
    await fetchFiles();
    await fetchStatistics();
  } catch (e) {
    error.value = e.message || '删除失败';
  }
};

const updateFile = async (file) => {
  try {
    await cloudFileApi.updateFile(file.id, {
      description: file.description,
      isPublic: file.isPublic
    });
    await fetchFiles();
  } catch (e) {
    error.value = e.message || '更新失败';
  }
};

const filteredFiles = computed(() => {
  return files.value.filter(file => {
    if (!searchQuery.value) return true;
    const query = searchQuery.value.toLowerCase();
    return file.originalFileName.toLowerCase().includes(query) ||
           (file.description && file.description.toLowerCase().includes(query));
  });
});

const handleFileUploaded = () => {
  showUploadModal.value = false;
  fetchFiles();
  fetchStatistics();
  emit('file-uploaded');
};

onMounted(() => {
  fetchFiles();
  fetchStatistics();
});
</script>

<template>
  <div class="cloud-drive">
    <div class="drive-header">
      <div class="header-left">
        <h3>班级云盘</h3>
        <div v-if="statistics" class="statistics">
          <span>共 {{ statistics.fileCount }} 个文件</span>
          <span>总大小 {{ formatFileSize(statistics.totalSize) }}</span>
        </div>
      </div>
      <div class="header-right">
        <div class="filters">
          <select v-model="filterType" @change="fetchFiles">
            <option v-for="type in fileTypes" :key="type.value" :value="type.value">
              {{ type.label }}
            </option>
          </select>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索文件..."
            class="search-input"
          />
        </div>
        <button v-if="isTeacher" class="primary" @click="showUploadModal = true">
          📤 上传文件
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      加载中...
    </div>

    <div v-else-if="error" class="error">
      {{ error }}
    </div>

    <div v-else-if="!filteredFiles.length" class="empty">
      <div class="empty-icon">📁</div>
      <p>暂无文件</p>
      <p v-if="isTeacher" class="empty-hint">点击"上传文件"开始使用云盘</p>
    </div>

    <div v-else class="file-grid">
      <div
        v-for="file in filteredFiles"
        :key="file.id"
        class="file-item"
        @click="selectedFile = file"
      >
        <div class="file-icon">{{ getFileIcon(file) }}</div>
        <div class="file-info">
          <h4 class="file-name" :title="file.originalFileName">
            {{ file.originalFileName }}
          </h4>
          <p class="file-size">{{ file.getFormattedFileSize() }}</p>
          <p class="file-uploader">{{ file.uploaderName }}</p>
          <p class="file-date">{{ formatDate(file.createdAt) }}</p>
          <p v-if="file.description" class="file-description">{{ file.description }}</p>
          <div class="file-stats">
            <span>📥 {{ file.downloadCount }} 次下载</span>
            <span v-if="!file.isPublic" class="private-tag">私有</span>
          </div>
        </div>
        <div class="file-actions" @click.stop>
          <button class="secondary small" @click="downloadFile(file)">
            下载
          </button>
          <button v-if="isTeacher && file.uploaderId === auth.userId" 
                  class="secondary small" 
                  @click="selectedFile = file">
            编辑
          </button>
          <button v-if="isTeacher && file.uploaderId === auth.userId" 
                  class="danger small" 
                  @click="deleteFile(file)">
            删除
          </button>
        </div>
      </div>
    </div>

    <!-- 文件详情模态框 -->
    <div v-if="selectedFile" class="modal-overlay" @click="selectedFile = null">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>文件详情</h3>
          <button class="close" @click="selectedFile = null">×</button>
        </div>
        <div class="modal-body">
          <div class="file-detail">
            <div class="detail-item">
              <label>文件名:</label>
              <span>{{ selectedFile.originalFileName }}</span>
            </div>
            <div class="detail-item">
              <label>大小:</label>
              <span>{{ selectedFile.getFormattedFileSize() }}</span>
            </div>
            <div class="detail-item">
              <label>类型:</label>
              <span>{{ getFileCategory(selectedFile) }}</span>
            </div>
            <div class="detail-item">
              <label>上传者:</label>
              <span>{{ selectedFile.uploaderName }}</span>
            </div>
            <div class="detail-item">
              <label>上传时间:</label>
              <span>{{ formatDate(selectedFile.createdAt) }}</span>
            </div>
            <div class="detail-item">
              <label>下载次数:</label>
              <span>{{ selectedFile.downloadCount }}</span>
            </div>
            <div v-if="isTeacher && selectedFile.uploaderId === auth.userId" class="detail-item">
              <label>描述:</label>
              <textarea v-model="selectedFile.description" placeholder="添加文件描述..."></textarea>
            </div>
            <div v-if="isTeacher && selectedFile.uploaderId === auth.userId" class="detail-item">
              <label>
                <input type="checkbox" v-model="selectedFile.isPublic" />
                公开文件
              </label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="primary" @click="downloadFile(selectedFile)">下载</button>
          <button v-if="isTeacher && selectedFile.uploaderId === auth.userId" 
                  class="secondary" 
                  @click="updateFile(selectedFile); selectedFile = null;">
            保存
          </button>
          <button class="secondary" @click="selectedFile = null">关闭</button>
        </div>
      </div>
    </div>

    <!-- 上传文件模态框 -->
    <div v-if="showUploadModal" class="modal-overlay" @click="showUploadModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>上传文件</h3>
          <button class="close" @click="showUploadModal = false">×</button>
        </div>
        <div class="modal-body">
          <CloudFileUpload
            :class-id="props.classId"
            @upload-success="handleFileUploaded"
            @upload-error="error = $event"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cloud-drive {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.drive-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.header-left h3 {
  margin: 0 0 0.5rem 0;
  color: #1e293b;
}

.statistics {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #64748b;
}

.header-right {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.filters {
  display: flex;
  gap: 0.5rem;
}

.filters select {
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background: white;
}

.search-input {
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  width: 200px;
}

.loading, .error, .empty {
  text-align: center;
  padding: 3rem;
  color: #64748b;
}

.error {
  color: #dc2626;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-hint {
  font-size: 0.9rem;
  color: #94a3b8;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.file-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  gap: 1rem;
}

.file-item:hover {
  border-color: #3b82f6;
  background: #f8fafc;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.file-icon {
  font-size: 2rem;
  display: flex;
  align-items: flex-start;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size, .file-uploader, .file-date, .file-description {
  margin: 0 0 0.25rem 0;
  font-size: 0.85rem;
  color: #64748b;
}

.file-description {
  color: #374151;
  line-height: 1.4;
}

.file-stats {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  font-size: 0.8rem;
  color: #94a3b8;
}

.private-tag {
  background: #fef3c7;
  color: #92400e;
  padding: 0.125rem 0.375rem;
  border-radius: 4px;
  font-size: 0.75rem;
}

.file-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 0;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  color: #1e293b;
}

.close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #64748b;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 1.5rem;
}

.file-detail {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.detail-item label {
  font-weight: 500;
  color: #374151;
}

.detail-item span {
  color: #64748b;
}

.detail-item textarea {
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  resize: vertical;
  min-height: 60px;
  font-family: inherit;
}

.modal-footer {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  padding: 1rem 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.small {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

.danger {
  background: #dc2626;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.danger:hover {
  background: #b91c1c;
}

@media (max-width: 768px) {
  .drive-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .header-right {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filters {
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
  
  .file-grid {
    grid-template-columns: 1fr;
  }
}
</style>