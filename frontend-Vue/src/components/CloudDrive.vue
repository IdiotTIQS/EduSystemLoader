<script setup>
import { ref, onMounted, computed, watch, reactive } from 'vue';
import { cloudFileApi, cloudFolderApi } from '../services/api';
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
const folders = ref([]);
const currentFolder = ref(null);
const currentPath = ref([]);
const statistics = ref(null);
const loading = ref(false);
const error = ref('');
const selectedFile = ref(null);
const selectedFolder = ref(null);
const showUploadModal = ref(false);
const showCreateFolderModal = ref(false);
const showRenameModal = ref(false);
const filterType = ref('all');
const searchQuery = ref('');
const viewMode = ref('grid'); // 'grid', 'list', 'compact', 'large'

// 新建文件夹表单
const newFolderForm = reactive({
  name: '',
  parentFolderId: null
});

// 重命名表单
const renameForm = reactive({
  id: null,
  name: '',
  type: 'file' // 'file' or 'folder'
});

const fileTypes = [
  { value: 'all', label: '全部文件' },
  { value: 'image', label: '图片' },
  { value: 'document', label: '文档' },
  { value: 'spreadsheet', label: '表格' },
  { value: 'presentation', label: '演示文稿' },
  { value: 'archive', label: '压缩包' }
];

// 获取当前文件夹的文件和子文件夹
const fetchCurrentContent = async () => {
  if (!props.classId) return;
  loading.value = true;
  error.value = '';
  
  try {
    const folderId = currentFolder.value?.id;
    
    // 获取子文件夹
    if (folderId) {
      folders.value = await cloudFolderApi.getSubFolders(folderId);
    } else {
      folders.value = await cloudFolderApi.getRootFolders(props.classId);
    }
    
    // 获取文件
    const params = { classId: props.classId };
    if (filterType.value !== 'all') {
      params.fileType = filterType.value;
    }
    if (!props.isTeacher) {
      params.uploaderId = auth.userId;
    }
    if (folderId) {
      params.folderId = folderId;
    }
    
    const fetchedFiles = await cloudFileApi.listFiles(params.classId, params.fileType, params.uploaderId, params.folderId);
    files.value = fetchedFiles.map(file => enhanceFileObject(file));
    
  } catch (e) {
    error.value = e.message || '获取文件列表失败';
  } finally {
    loading.value = false;
  }
};

// 获取统计信息
const fetchStatistics = async () => {
  if (!props.classId) return;
  try {
    statistics.value = await cloudFileApi.getStatistics(props.classId);
  } catch (e) {
    console.error('获取统计信息失败:', e);
  }
};

// 进入文件夹
const enterFolder = (folder) => {
  currentPath.value.push(folder);
  currentFolder.value = folder;
  fetchCurrentContent();
};

// 返回上级文件夹
const goBack = () => {
  if (currentPath.value.length > 0) {
    currentPath.value.pop();
    currentFolder.value = currentPath.value.length > 0 ? currentPath.value[currentPath.value.length - 1] : null;
    fetchCurrentContent();
  }
};

// 跳转到指定路径层级
const navigateToPath = (index) => {
  currentPath.value = currentPath.value.slice(0, index + 1);
  currentFolder.value = index >= 0 ? currentPath.value[index] : null;
  fetchCurrentContent();
};

// 创建文件夹
const createFolder = async () => {
  if (!newFolderForm.name.trim()) {
    error.value = '请输入文件夹名称';
    return;
  }
  
  try {
    const parentFolderId = currentFolder.value?.id;
    await cloudFolderApi.createFolder(props.classId, newFolderForm.name, parentFolderId);
    
    newFolderForm.name = '';
    showCreateFolderModal.value = false;
    fetchCurrentContent();
    fetchStatistics();
  } catch (e) {
    error.value = e.message || '创建文件夹失败';
  }
};

// 重命名
const startRename = (item, type) => {
  renameForm.id = item.id;
  renameForm.name = item.name || item.originalFileName;
  renameForm.type = type;
  showRenameModal.value = true;
};

const confirmRename = async () => {
  if (!renameForm.name.trim()) {
    error.value = '请输入名称';
    return;
  }
  
  try {
    if (renameForm.type === 'folder') {
      await cloudFolderApi.renameFolder(renameForm.id, renameForm.name);
    } else {
      await cloudFileApi.updateFile(renameForm.id, { 
        originalFileName: renameForm.name 
      });
    }
    
    showRenameModal.value = false;
    fetchCurrentContent();
  } catch (e) {
    error.value = e.message || '重命名失败';
  }
};

// 删除文件夹
const deleteFolder = async (folder) => {
  if (!confirm(`确定要删除文件夹 "${folder.name}" 及其所有内容吗？`)) return;
  
  try {
    await cloudFolderApi.deleteFolder(folder.id);
    fetchCurrentContent();
    fetchStatistics();
  } catch (e) {
    error.value = e.message || '删除文件夹失败';
  }
};

// 移动文件或文件夹
const moveItem = async (item, type, targetFolderId) => {
  try {
    if (type === 'file') {
      await cloudFileApi.moveFile(item.id, targetFolderId);
    } else {
      await cloudFolderApi.moveFolder(item.id, targetFolderId);
    }
    fetchCurrentContent();
  } catch (e) {
    error.value = e.message || '移动失败';
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
  return '📄'; // 默认使用文档图标而不是文件夹图标
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

const enhanceFileObject = (file) => {
  return {
    ...file,
    getFormattedFileSize: () => formatFileSize(file.fileSize)
  };
};

const downloadFile = async (file) => {
  try {
    const response = await fetch(cloudFileApi.downloadFile(file.id), {
      headers: {
        'Authorization': `Bearer ${getToken()}`
      }
    });
    
    if (!response.ok) {
      throw new Error('下载失败');
    }
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = file.originalFileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    await fetchCurrentContent();
  } catch (e) {
    error.value = e.message || '下载失败';
  }
};

const deleteFile = async (file) => {
  if (!confirm(`确定要删除文件 "${file.originalFileName}" 吗？`)) return;
  
  try {
    await cloudFileApi.deleteFile(file.id);
    await fetchCurrentContent();
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
    await fetchCurrentContent();
  } catch (e) {
    error.value = e.message || '更新失败';
  }
};

// 搜索和过滤
const filteredFiles = computed(() => {
  return files.value.filter(file => {
    if (!searchQuery.value) return true;
    const query = searchQuery.value.toLowerCase();
    return file.originalFileName.toLowerCase().includes(query) ||
           (file.description && file.description.toLowerCase().includes(query));
  });
});

const filteredFolders = computed(() => {
  return folders.value.filter(folder => {
    if (!searchQuery.value) return true;
    return folder.name.toLowerCase().includes(searchQuery.value.toLowerCase());
  });
});

const handleFileUploaded = () => {
  showUploadModal.value = false;
  fetchCurrentContent();
  fetchStatistics();
  emit('file-uploaded');
};

// 监听筛选条件变化
watch([filterType, searchQuery], () => {
  fetchCurrentContent();
});

onMounted(() => {
  fetchCurrentContent();
  fetchStatistics();
});
</script>

<template>
  <div class="cloud-drive">
    <!-- 头部工具栏 -->
    <div class="drive-header">
      <div class="header-left">
        <h3>班级云盘</h3>
        <div class="breadcrumb">
          <button @click="navigateToPath(-1)" class="breadcrumb-item">根目录</button>
          <template v-for="(folder, index) in currentPath" :key="folder.id">
            <span class="breadcrumb-separator">/</span>
            <button @click="navigateToPath(index)" class="breadcrumb-item">{{ folder.name }}</button>
          </template>
        </div>
        <div v-if="statistics" class="statistics">
          <span>共 {{ statistics.fileCount }} 个文件</span>
          <span>总大小 {{ formatFileSize(statistics.totalSize) }}</span>
        </div>
      </div>
      <div class="header-right">
        <div class="filters">
          <select v-model="filterType">
            <option v-for="type in fileTypes" :key="type.value" :value="type.value">
              {{ type.label }}
            </option>
          </select>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索文件和文件夹..."
            class="search-input"
          />
        </div>
        <div class="view-toggle">
          <button 
            :class="['view-btn', { active: viewMode === 'grid' }]" 
            @click="viewMode = 'grid'"
            title="网格视图">
            ⊞
          </button>
          <button 
            :class="['view-btn', { active: viewMode === 'list' }]" 
            @click="viewMode = 'list'"
            title="列表视图">
            ☰
          </button>
          <button 
            :class="['view-btn', { active: viewMode === 'compact' }]" 
            @click="viewMode = 'compact'"
            title="紧凑视图">
            ⊟
          </button>
          <button 
            :class="['view-btn', { active: viewMode === 'large' }]" 
            @click="viewMode = 'large'"
            title="大图标视图">
            ⊡
          </button>
        </div>
        <button v-if="isTeacher" class="primary" @click="showCreateFolderModal = true">
          📁 新建文件夹
        </button>
        <button v-if="isTeacher" class="primary" @click="showUploadModal = true">
          📤 上传文件
        </button>
      </div>
    </div>

    <!-- 返回按钮 -->
    <div v-if="currentPath.length > 0" class="navigation-bar">
      <button @click="goBack" class="back-btn">
        ⬅️ 返回上级
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      加载中...
    </div>

    <!-- 错误信息 -->
    <div v-else-if="error" class="error">
      {{ error }}
    </div>

    <!-- 空状态 -->
    <div v-else-if="!filteredFiles.length && !filteredFolders.length" class="empty">
      <div class="empty-icon">📁</div>
      <p>当前文件夹为空</p>
      <p v-if="isTeacher" class="empty-hint">点击"新建文件夹"或"上传文件"开始使用云盘</p>
    </div>

    <!-- 文件夹和文件列表 -->
    <div v-else>
      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="content-grid">
        <!-- 文件夹 -->
        <div
          v-for="folder in filteredFolders"
          :key="folder.id"
          class="content-item folder-item"
          @dblclick="enterFolder(folder)"
          @click="selectedFolder = folder"
          :class="{ selected: selectedFolder?.id === folder.id }"
        >
          <div class="item-icon">📁</div>
          <div class="item-info">
            <h4 class="item-name" :title="folder.name">{{ folder.name }}</h4>
            <p class="item-meta">
              {{ folder.folderCount || 0 }} 个文件夹，{{ folder.fileCount || 0 }} 个文件
            </p>
            <p class="item-date">{{ formatDate(folder.createdAt) }}</p>
          </div>
          <div class="item-actions" @click.stop v-if="isTeacher">
            <button class="secondary small" @click="startRename(folder, 'folder')">
              重命名
            </button>
            <button class="danger small" @click="deleteFolder(folder)">
              删除
            </button>
          </div>
        </div>

        <!-- 文件 -->
        <div
          v-for="file in filteredFiles"
          :key="file.id"
          class="content-item file-item"
          @click="selectedFile = file"
          :class="{ selected: selectedFile?.id === file.id }"
        >
          <div class="item-icon">{{ getFileIcon(file) }}</div>
          <div class="item-info">
            <h4 class="item-name" :title="file.originalFileName">
              {{ file.originalFileName }}
            </h4>
            <p class="item-size">{{ file.getFormattedFileSize() }}</p>
            <p class="item-uploader">{{ file.uploaderName }}</p>
            <p class="item-date">{{ formatDate(file.createdAt) }}</p>
            <p v-if="file.description" class="item-description">{{ file.description }}</p>
            <div class="item-stats">
              <span>📥 {{ file.downloadCount }} 次下载</span>
              <span v-if="!file.isPublic" class="private-tag">私有</span>
            </div>
          </div>
          <div class="item-actions" @click.stop>
            <button class="secondary small" @click="downloadFile(file)">
              下载
            </button>
            <button v-if="isTeacher && file.uploaderId === auth.userId" 
                    class="secondary small" 
                    @click="startRename(file, 'file')">
              重命名
            </button>
            <button v-if="isTeacher && file.uploaderId === auth.userId" 
                    class="danger small" 
                    @click="deleteFile(file)">
              删除
            </button>
          </div>
        </div>
      </div>

      <!-- 紧凑视图 -->
      <div v-else-if="viewMode === 'compact'" class="content-compact">
        <!-- 文件夹 -->
        <div
          v-for="folder in filteredFolders"
          :key="folder.id"
          class="compact-item folder-item"
          @dblclick="enterFolder(folder)"
          @click="selectedFolder = folder"
          :class="{ selected: selectedFolder?.id === folder.id }"
        >
          <div class="compact-icon">📁</div>
          <div class="compact-info">
            <span class="compact-name" :title="folder.name">{{ folder.name }}</span>
            <span class="compact-meta">{{ formatDate(folder.createdAt) }}</span>
          </div>
          <div class="compact-actions" @click.stop v-if="isTeacher">
            <button class="secondary small" @click="startRename(folder, 'folder')">重命名</button>
            <button class="danger small" @click="deleteFolder(folder)">删除</button>
          </div>
        </div>

        <!-- 文件 -->
        <div
          v-for="file in filteredFiles"
          :key="file.id"
          class="compact-item file-item"
          @click="selectedFile = file"
          :class="{ selected: selectedFile?.id === file.id }"
        >
          <div class="compact-icon">{{ getFileIcon(file) }}</div>
          <div class="compact-info">
            <span class="compact-name" :title="file.originalFileName">{{ file.originalFileName }}</span>
            <span class="compact-meta">{{ file.getFormattedFileSize() }} • {{ formatDate(file.createdAt) }}</span>
          </div>
          <div class="compact-actions" @click.stop>
            <button class="secondary small" @click="downloadFile(file)">下载</button>
            <button v-if="isTeacher && file.uploaderId === auth.userId" 
                    class="secondary small" 
                    @click="startRename(file, 'file')">重命名</button>
            <button v-if="isTeacher && file.uploaderId === auth.userId" 
                    class="danger small" 
                    @click="deleteFile(file)">删除</button>
          </div>
        </div>
      </div>

      <!-- 大图标视图 -->
      <div v-else-if="viewMode === 'large'" class="content-large">
        <!-- 文件夹 -->
        <div
          v-for="folder in filteredFolders"
          :key="folder.id"
          class="large-item folder-item"
          @dblclick="enterFolder(folder)"
          @click="selectedFolder = folder"
          :class="{ selected: selectedFolder?.id === folder.id }"
        >
          <div class="large-icon">📁</div>
          <div class="large-info">
            <h4 class="large-name" :title="folder.name">{{ folder.name }}</h4>
            <p class="large-meta">
              {{ folder.folderCount || 0 }} 个文件夹，{{ folder.fileCount || 0 }} 个文件
            </p>
            <p class="large-date">{{ formatDate(folder.createdAt) }}</p>
          </div>
          <div class="large-actions" @click.stop v-if="isTeacher">
            <button class="secondary" @click="startRename(folder, 'folder')">重命名</button>
            <button class="danger" @click="deleteFolder(folder)">删除</button>
          </div>
        </div>

        <!-- 文件 -->
        <div
          v-for="file in filteredFiles"
          :key="file.id"
          class="large-item file-item"
          @click="selectedFile = file"
          :class="{ selected: selectedFile?.id === file.id }"
        >
          <div class="large-icon">{{ getFileIcon(file) }}</div>
          <div class="large-info">
            <h4 class="large-name" :title="file.originalFileName">
              {{ file.originalFileName }}
            </h4>
            <p class="large-size">{{ file.getFormattedFileSize() }}</p>
            <p class="large-uploader">{{ file.uploaderName }}</p>
            <p class="large-date">{{ formatDate(file.createdAt) }}</p>
            <p v-if="file.description" class="large-description">{{ file.description }}</p>
            <div class="large-stats">
              <span>📥 {{ file.downloadCount }} 次下载</span>
              <span v-if="!file.isPublic" class="private-tag">私有</span>
            </div>
          </div>
          <div class="large-actions" @click.stop>
            <button class="secondary" @click="downloadFile(file)">下载</button>
            <button v-if="isTeacher && file.uploaderId === auth.userId" 
                    class="secondary" 
                    @click="startRename(file, 'file')">重命名</button>
            <button v-if="isTeacher && file.uploaderId === auth.userId" 
                    class="danger" 
                    @click="deleteFile(file)">删除</button>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="content-list">
        <table class="list-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>类型</th>
              <th>大小</th>
              <th>修改时间</th>
              <th>上传者</th>
              <th v-if="isTeacher">操作</th>
            </tr>
          </thead>
          <tbody>
            <!-- 文件夹 -->
            <tr
              v-for="folder in filteredFolders"
              :key="'folder-' + folder.id"
              class="folder-row"
              @dblclick="enterFolder(folder)"
              @click="selectedFolder = folder"
              :class="{ selected: selectedFolder?.id === folder.id }"
            >
              <td class="name-cell">
                <span class="row-icon">📁</span>
                {{ folder.name }}
              </td>
              <td>文件夹</td>
              <td>-</td>
              <td>{{ formatDate(folder.createdAt) }}</td>
              <td>{{ folder.creatorName }}</td>
              <td v-if="isTeacher">
                <button class="secondary small" @click="startRename(folder, 'folder')">重命名</button>
                <button class="danger small" @click="deleteFolder(folder)">删除</button>
              </td>
            </tr>
            
            <!-- 文件 -->
            <tr
              v-for="file in filteredFiles"
              :key="'file-' + file.id"
              class="file-row"
              @click="selectedFile = file"
              :class="{ selected: selectedFile?.id === file.id }"
            >
              <td class="name-cell">
                <span class="row-icon">{{ getFileIcon(file) }}</span>
                {{ file.originalFileName }}
              </td>
              <td>{{ getFileCategory(file) }}</td>
              <td>{{ file.getFormattedFileSize() }}</td>
              <td>{{ formatDate(file.createdAt) }}</td>
              <td>{{ file.uploaderName }}</td>
              <td v-if="isTeacher">
                <button class="secondary small" @click="downloadFile(file)">下载</button>
                <button v-if="file.uploaderId === auth.userId" 
                        class="secondary small" 
                        @click="startRename(file, 'file')">重命名</button>
                <button v-if="file.uploaderId === auth.userId" 
                        class="danger small" 
                        @click="deleteFile(file)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
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

    <!-- 新建文件夹模态框 -->
    <div v-if="showCreateFolderModal" class="modal-overlay" @click="showCreateFolderModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>新建文件夹</h3>
          <button class="close" @click="showCreateFolderModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>文件夹名称</label>
            <input 
              v-model="newFolderForm.name" 
              type="text" 
              placeholder="请输入文件夹名称"
              @keyup.enter="createFolder"
              autofocus
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="secondary" @click="showCreateFolderModal = false">取消</button>
          <button class="primary" @click="createFolder">创建</button>
        </div>
      </div>
    </div>

    <!-- 重命名模态框 -->
    <div v-if="showRenameModal" class="modal-overlay" @click="showRenameModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>重命名</h3>
          <button class="close" @click="showRenameModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>新名称</label>
            <input 
              v-model="renameForm.name" 
              type="text" 
              placeholder="请输入新名称"
              @keyup.enter="confirmRename"
              autofocus
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="secondary" @click="showRenameModal = false">取消</button>
          <button class="primary" @click="confirmRename">确定</button>
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
            :folder-id="currentFolder?.id"
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
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
}

.drive-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.header-left h3 {
  margin: 0 0 0.5rem 0;
  color: #1e293b;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.breadcrumb-item {
  background: none;
  border: none;
  color: #3b82f6;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  transition: background 0.2s;
}

.breadcrumb-item:hover {
  background: #eff6ff;
}

.breadcrumb-separator {
  color: #94a3b8;
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
  flex-wrap: wrap;
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

.view-toggle {
  display: flex;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.view-btn {
  background: white;
  border: none;
  padding: 0.5rem 0.75rem;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 1rem;
}

.view-btn.active {
  background: #3b82f6;
  color: white;
}

.view-btn:hover:not(.active) {
  background: #f1f5f9;
}

.navigation-bar {
  margin-bottom: 1rem;
}

.back-btn {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #e2e8f0;
}

.loading, .error, .empty {
  text-align: center;
  padding: 3rem;
  color: #64748b;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
  overflow-y: auto;
  flex: 1;
}

.content-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  position: relative;
}

.content-item:hover {
  border-color: #3b82f6;
  background: #f8fafc;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.content-item.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.item-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  text-align: center;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta, .item-size, .item-uploader, .item-date, .item-description {
  margin: 0 0 0.25rem 0;
  font-size: 0.85rem;
  color: #64748b;
}

.item-description {
  color: #374151;
  line-height: 1.4;
}

.item-stats {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  font-size: 0.8rem;
  color: #94a3b8;
  margin-top: 0.5rem;
}

.private-tag {
  background: #fef3c7;
  color: #92400e;
  padding: 0.125rem 0.375rem;
  border-radius: 4px;
  font-size: 0.75rem;
}

.item-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
  opacity: 0;
  transition: opacity 0.2s;
}

.content-item:hover .item-actions {
  opacity: 1;
}

.content-list {
  overflow-y: auto;
  flex: 1;
}

.list-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.list-table th,
.list-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

.list-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #374151;
  position: sticky;
  top: 0;
  z-index: 10;
}

.folder-row,
.file-row {
  cursor: pointer;
  transition: background 0.2s;
}

.folder-row:hover,
.file-row:hover {
  background: #f8fafc;
}

.folder-row.selected,
.file-row.selected {
  background: #eff6ff;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.row-icon {
  font-size: 1.2rem;
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

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #374151;
}

.form-group input {
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 1rem;
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

/* 紧凑视图样式 */
.content-compact {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  overflow-y: auto;
  flex: 1;
}

.compact-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.compact-item:hover {
  border-color: #3b82f6;
  background: #f8fafc;
  transform: translateX(2px);
}

.compact-item.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.compact-icon {
  font-size: 1.2rem;
  margin-right: 0.75rem;
  min-width: 1.5rem;
  text-align: center;
}

.compact-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.compact-name {
  font-weight: 500;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.compact-meta {
  font-size: 0.8rem;
  color: #64748b;
}

.compact-actions {
  display: flex;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.2s;
}

.compact-item:hover .compact-actions {
  opacity: 1;
}

/* 大图标视图样式 */
.content-large {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

.large-item {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  position: relative;
  background: white;
}

.large-item:hover {
  border-color: #3b82f6;
  background: #f8fafc;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.large-item.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.large-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  text-align: center;
}

.large-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.large-name {
  margin: 0;
  font-size: 1.1rem;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 600;
}

.large-meta, .large-size, .large-uploader, .large-date, .large-description {
  margin: 0;
  font-size: 0.9rem;
  color: #64748b;
}

.large-description {
  color: #374151;
  line-height: 1.5;
  font-style: italic;
}

.large-stats {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  font-size: 0.85rem;
  color: #94a3b8;
  margin-top: 0.75rem;
}

.large-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
  opacity: 0;
  transition: opacity 0.2s;
}

.large-item:hover .large-actions {
  opacity: 1;
}

.large-actions .secondary,
.large-actions .danger {
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
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
  
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .content-large {
    grid-template-columns: 1fr;
  }
  
  .compact-actions {
    opacity: 1;
  }
  
  .large-actions {
    opacity: 1;
  }
  
  .list-table {
    font-size: 0.8rem;
  }
  
  .list-table th,
  .list-table td {
    padding: 0.5rem;
  }
  
  .view-toggle {
    flex-wrap: wrap;
  }
  
  .view-btn {
    flex: 1;
    min-width: 60px;
    padding: 0.4rem 0.6rem;
    font-size: 0.9rem;
  }
}
</style>