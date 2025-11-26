<script setup>
import { ref, onMounted, computed } from 'vue';
import { discussionApi } from '../services/api';
import { getAuth } from '../services/auth';

const props = defineProps({
  discussionId: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['back-to-list']);

const auth = getAuth();
const discussion = ref(null);
const comments = ref([]);
const loading = ref(false);
const error = ref('');
const newComment = ref('');
const editingComment = ref(null);
const editContent = ref('');

const canEditDiscussion = computed(() => {
  return discussion.value && discussion.value.authorId === auth.userId;
});

const canPinDiscussion = computed(() => {
  return auth.role === 'TEACHER';
});

const fetchDiscussion = async () => {
  if (!props.discussionId) return;
  loading.value = true;
  error.value = '';
  try {
    discussion.value = await discussionApi.get(props.discussionId);
    await fetchComments();
  } catch (e) {
    error.value = e.message || '获取讨论详情失败';
  } finally {
    loading.value = false;
  }
};

const fetchComments = async () => {
  try {
    comments.value = await discussionApi.listComments(props.discussionId);
  } catch (e) {
    console.error('获取评论失败:', e);
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

const submitComment = async () => {
  if (!newComment.value.trim()) return;
  
  try {
    await discussionApi.createComment(props.discussionId, {
      content: newComment.value.trim(),
      parentId: null
    });
    newComment.value = '';
    await fetchComments();
  } catch (e) {
    error.value = e.message || '发表评论失败';
  }
};

const startEditComment = (comment) => {
  editingComment.value = comment.id;
  editContent.value = comment.content;
};

const cancelEditComment = () => {
  editingComment.value = null;
  editContent.value = '';
};

const updateComment = async (commentId) => {
  if (!editContent.value.trim()) return;
  
  try {
    await discussionApi.updateComment(commentId, {
      content: editContent.value.trim()
    });
    editingComment.value = null;
    editContent.value = '';
    await fetchComments();
  } catch (e) {
    error.value = e.message || '更新评论失败';
  }
};

const deleteComment = async (commentId) => {
  if (!confirm('确定要删除这条评论吗？')) return;
  
  try {
    await discussionApi.deleteComment(commentId);
    await fetchComments();
  } catch (e) {
    error.value = e.message || '删除评论失败';
  }
};

const togglePin = async () => {
  if (!discussion.value) return;
  
  try {
    await discussionApi.updatePinnedStatus(props.discussionId, !discussion.value.isPinned);
    await fetchDiscussion();
  } catch (e) {
    error.value = e.message || '更新置顶状态失败';
  }
};

const deleteDiscussion = async () => {
  if (!confirm('确定要删除这个讨论吗？删除后无法恢复！')) return;
  
  try {
    await discussionApi.delete(props.discussionId);
    emit('back-to-list');
  } catch (e) {
    error.value = e.message || '删除讨论失败';
  }
};

const canEditComment = (comment) => {
  return comment.authorId === auth.userId;
};

const renderCommentTree = (comments, parentId = null, level = 0) => {
  return comments
    .filter(comment => comment.parentId === parentId)
    .map(comment => ({
      ...comment,
      children: renderCommentTree(comments, comment.id, level + 1)
    }));
};

const commentTree = computed(() => {
  return renderCommentTree(comments.value);
});

onMounted(fetchDiscussion);
</script>

<template>
  <div class="discussion-detail">
    <div class="detail-header">
      <button class="secondary" @click="$emit('back-to-list')">← 返回列表</button>
      <h2>{{ discussion?.title || '讨论详情' }}</h2>
    </div>

    <div v-if="loading" class="loading">
      加载中...
    </div>

    <div v-else-if="error" class="error">
      {{ error }}
    </div>

    <div v-else-if="discussion" class="discussion-content">
      <div class="discussion-main">
        <div class="discussion-meta">
          <span class="author">{{ discussion.authorName }}</span>
          <span class="time">{{ formatDate(discussion.createdAt) }}</span>
          <span v-if="discussion.updatedAt !== discussion.createdAt" class="edited">已编辑</span>
          <span class="views">👁 {{ discussion.viewCount || 0 }}</span>
        </div>

        <div class="discussion-body">
          <p>{{ discussion.content }}</p>
        </div>

        <div v-if="canEditDiscussion || canPinDiscussion" class="discussion-actions">
          <button v-if="canEditDiscussion" class="secondary" @click="$emit('edit-discussion', discussion)">
            编辑
          </button>
          <button v-if="canPinDiscussion" class="secondary" @click="togglePin">
            {{ discussion.isPinned ? '取消置顶' : '置顶' }}
          </button>
          <button v-if="canEditDiscussion" class="danger" @click="deleteDiscussion">
            删除
          </button>
        </div>
      </div>

      <div class="comments-section">
        <h3>评论 ({{ comments.length }})</h3>

        <div class="comment-form">
          <textarea
            v-model="newComment"
            placeholder="写下你的评论..."
            rows="3"
          ></textarea>
          <button class="primary" @click="submitComment" :disabled="!newComment.trim()">
            发表评论
          </button>
        </div>

        <div v-if="!comments.length" class="no-comments">
          暂无评论，快来发表第一条评论吧！
        </div>

        <div v-else class="comments-tree">
          <div
            v-for="comment in commentTree"
            :key="comment.id"
            class="comment-item"
            :style="{ marginLeft: comment.level * 20 + 'px' }"
          >
            <div class="comment-header">
              <span class="comment-author">
                {{ comment.authorName }}
                <span class="comment-role">{{ comment.authorRole === 'TEACHER' ? '(教师)' : '(学生)' }}</span>
              </span>
              <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
              <span v-if="comment.isEdited" class="edited">已编辑</span>
            </div>

            <div v-if="editingComment === comment.id" class="comment-edit">
              <textarea v-model="editContent" rows="3"></textarea>
              <div class="edit-actions">
                <button class="primary" @click="updateComment(comment.id)">保存</button>
                <button class="secondary" @click="cancelEditComment">取消</button>
              </div>
            </div>

            <div v-else class="comment-content">
              <p>{{ comment.content }}</p>
              <div v-if="canEditComment(comment)" class="comment-actions">
                <button class="secondary small" @click="startEditComment(comment)">编辑</button>
                <button class="danger small" @click="deleteComment(comment.id)">删除</button>
              </div>
            </div>

            <div v-if="comment.children && comment.children.length" class="comment-children">
              <div
                v-for="child in comment.children"
                :key="child.id"
                class="comment-item child"
              >
                <div class="comment-header">
                  <span class="comment-author">
                    {{ child.authorName }}
                    <span class="comment-role">{{ child.authorRole === 'TEACHER' ? '(教师)' : '(学生)' }}</span>
                  </span>
                  <span class="comment-time">{{ formatDate(child.createdAt) }}</span>
                  <span v-if="child.isEdited" class="edited">已编辑</span>
                </div>

                <div v-if="editingComment === child.id" class="comment-edit">
                  <textarea v-model="editContent" rows="3"></textarea>
                  <div class="edit-actions">
                    <button class="primary" @click="updateComment(child.id)">保存</button>
                    <button class="secondary" @click="cancelEditComment">取消</button>
                  </div>
                </div>

                <div v-else class="comment-content">
                  <p>{{ child.content }}</p>
                  <div v-if="canEditComment(child)" class="comment-actions">
                    <button class="secondary small" @click="startEditComment(child)">编辑</button>
                    <button class="danger small" @click="deleteComment(child.id)">删除</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.discussion-detail {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.detail-header h2 {
  margin: 0;
  color: #1e293b;
  flex: 1;
}

.loading, .error {
  text-align: center;
  padding: 2rem;
  color: #64748b;
}

.error {
  color: #dc2626;
}

.discussion-main {
  margin-bottom: 2rem;
}

.discussion-meta {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: #64748b;
}

.author {
  font-weight: 500;
  color: #475569;
}

.edited {
  color: #f59e0b;
  font-size: 0.8rem;
}

.views {
  margin-left: auto;
}

.discussion-body {
  background: #f8fafc;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.discussion-actions {
  display: flex;
  gap: 0.5rem;
}

.comments-section {
  border-top: 1px solid #e2e8f0;
  padding-top: 1.5rem;
}

.comments-section h3 {
  margin: 0 0 1rem 0;
  color: #1e293b;
}

.comment-form {
  margin-bottom: 1.5rem;
}

.comment-form textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  resize: vertical;
  font-family: inherit;
  margin-bottom: 0.5rem;
}

.no-comments {
  text-align: center;
  padding: 2rem;
  color: #94a3b8;
  background: #f8fafc;
  border-radius: 8px;
}

.comments-tree {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.comment-item {
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
}

.comment-item.child {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.comment-author {
  font-weight: 500;
  color: #475569;
}

.comment-role {
  color: #64748b;
  font-size: 0.8rem;
}

.comment-time {
  color: #94a3b8;
  margin-left: auto;
}

.comment-content p {
  margin: 0;
  line-height: 1.5;
  color: #374151;
}

.comment-actions {
  margin-top: 0.5rem;
  display: flex;
  gap: 0.5rem;
}

.comment-edit {
  margin-bottom: 0.5rem;
}

.comment-edit textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  resize: vertical;
  font-family: inherit;
}

.edit-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.comment-children {
  margin-top: 1rem;
  padding-left: 1rem;
  border-left: 2px solid #e2e8f0;
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
</style>