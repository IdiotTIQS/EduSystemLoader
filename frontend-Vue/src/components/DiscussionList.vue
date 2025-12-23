<script setup>
import { ref, onMounted, computed } from 'vue';
import { discussionApi } from '../services/api';

const props = defineProps({
  classId: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['select-discussion', 'create-discussion']);

const discussions = ref([]);
const loading = ref(false);
const error = ref('');

const fetchDiscussions = async () => {
  if (!props.classId) return;
  loading.value = true;
  error.value = '';
  try {
    discussions.value = await discussionApi.listByClass(props.classId);
  } catch (e) {
    error.value = e.message || '获取讨论列表失败';
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

const getDiscussionStatus = (discussion) => {
  if (discussion.isPinned) return { text: '置顶', class: 'pinned' };
  if (discussion.commentCount > 0) return { text: `${discussion.commentCount} 回复`, class: 'has-replies' };
  return { text: '无回复', class: 'no-replies' };
};

onMounted(fetchDiscussions);
</script>

<template>
  <div class="discussion-list">
    <div class="list-header">
      <h3>班级讨论</h3>
      <button class="primary" @click="$emit('create-discussion')">发起讨论</button>
    </div>

    <div v-if="loading" class="loading">
      加载中...
    </div>

    <div v-else-if="error" class="error">
      {{ error }}
    </div>

    <div v-else-if="!discussions.length" class="empty">
      <p>暂无讨论，快来发起第一个讨论吧！</p>
    </div>

    <div v-else class="discussion-items">
      <div
        v-for="discussion in discussions"
        :key="discussion.id"
        class="discussion-item"
        @click="$emit('select-discussion', discussion)"
      >
        <div class="discussion-header">
          <h4 class="discussion-title">
            <span v-if="discussion.isPinned" class="pin-icon">📌</span>
            {{ discussion.title }}
          </h4>
          <span :class="['status-badge', getDiscussionStatus(discussion).class]">
            {{ getDiscussionStatus(discussion).text }}
          </span>
        </div>
        
        <p class="discussion-preview">
          {{ discussion.content.substring(0, 100) }}{{ discussion.content.length > 100 ? '...' : '' }}
        </p>
        
        <div class="discussion-meta">
          <span class="author">{{ discussion.authorName }}</span>
          <span class="time">{{ formatDate(discussion.updatedAt) }}</span>
          <span class="views">👁 {{ discussion.viewCount || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.discussion-list {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.list-header h3 {
  margin: 0;
  color: #1e293b;
}

.loading, .error, .empty {
  text-align: center;
  padding: 2rem;
  color: #64748b;
}

.error {
  color: #dc2626;
}

.discussion-items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.discussion-item {
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.discussion-item:hover {
  border-color: #3b82f6;
  background: #f8fafc;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.discussion-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.discussion-title {
  margin: 0;
  color: #1e293b;
  font-size: 1.1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.pin-icon {
  font-size: 1rem;
}

.status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.pinned {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.has-replies {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.no-replies {
  background: #f1f5f9;
  color: #64748b;
}

.discussion-preview {
  margin: 0.5rem 0;
  color: #64748b;
  line-height: 1.5;
}

.discussion-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.85rem;
  color: #94a3b8;
}

.author {
  font-weight: 500;
  color: #475569;
}

.views {
  margin-left: auto;
}
</style>