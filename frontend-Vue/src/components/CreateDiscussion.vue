<script setup>
import { ref, reactive, computed } from 'vue';
import { discussionApi } from '../services/api';

const props = defineProps({
  classId: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['discussion-created', 'cancel']);

const form = reactive({
  title: '',
  content: ''
});

const loading = ref(false);
const error = ref('');

const validateForm = () => {
  if (!form.title.trim()) {
    error.value = '请输入讨论标题';
    return false;
  }
  if (!form.content.trim()) {
    error.value = '请输入讨论内容';
    return false;
  }
  if (form.title.length > 100) {
    error.value = '标题不能超过100个字符';
    return false;
  }
  if (form.content.length > 2000) {
    error.value = '内容不能超过2000个字符';
    return false;
  }
  return true;
};

const submitDiscussion = async () => {
  error.value = '';
  
  if (!validateForm()) {
    return;
  }

  loading.value = true;
  try {
    const discussion = await discussionApi.create({
      classId: props.classId,
      title: form.title.trim(),
      content: form.content.trim()
    });
    
    emit('discussion-created', discussion);
    
    // 重置表单
    form.title = '';
    form.content = '';
  } catch (e) {
    error.value = e.message || '创建讨论失败';
  } finally {
    loading.value = false;
  }
};

const cancel = () => {
  form.title = '';
  form.content = '';
  error.value = '';
  emit('cancel');
};

const charCount = {
  title: computed(() => form.title.length),
  content: computed(() => form.content.length)
};
</script>

<template>
  <div class="create-discussion">
    <div class="form-header">
      <h3>发起讨论</h3>
      <button class="secondary" @click="cancel">取消</button>
    </div>

    <div v-if="error" class="error">
      {{ error }}
    </div>

    <form @submit.prevent="submitDiscussion" class="discussion-form">
      <div class="form-group">
        <label for="title">
          讨论标题
          <span class="char-count">({{ charCount.title }}/100)</span>
        </label>
        <input
          id="title"
          v-model="form.title"
          type="text"
          placeholder="请输入讨论标题，简洁明了地表达讨论主题"
          maxlength="100"
          required
        />
      </div>

      <div class="form-group">
        <label for="content">
          讨论内容
          <span class="char-count">({{ charCount.content }}/2000)</span>
        </label>
        <textarea
          id="content"
          v-model="form.content"
          placeholder="详细描述你的讨论内容，可以包括问题背景、具体问题、期望的讨论方向等..."
          rows="8"
          maxlength="2000"
          required
        ></textarea>
      </div>

      <div class="form-actions">
        <button type="button" class="secondary" @click="cancel">
          取消
        </button>
        <button type="submit" class="primary" :disabled="loading || !form.title.trim() || !form.content.trim()">
          {{ loading ? '发布中...' : '发布讨论' }}
        </button>
      </div>
    </form>

    <div class="tips">
      <h4>💡 发起讨论的小贴士：</h4>
      <ul>
        <li>标题要简洁明了，让其他同学一眼就能了解讨论主题</li>
        <li>内容要详细具体，提供足够的背景信息</li>
        <li>可以提出具体问题，引导讨论方向</li>
        <li>保持友善和尊重的语气</li>
        <li>避免发布与课程无关的内容</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.create-discussion {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  margin: 0 auto;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.form-header h3 {
  margin: 0;
  color: #1e293b;
}

.error {
  background: #fee2e2;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.discussion-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.char-count {
  font-size: 0.85rem;
  color: #64748b;
  font-weight: normal;
}

.form-group input,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-family: inherit;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
  line-height: 1.5;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.tips {
  margin-top: 2rem;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
}

.tips h4 {
  margin: 0 0 0.75rem 0;
  color: #1e293b;
  font-size: 1rem;
}

.tips ul {
  margin: 0;
  padding-left: 1.5rem;
  color: #64748b;
}

.tips li {
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.tips li:last-child {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .create-discussion {
    padding: 1rem;
  }
  
  .form-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions button {
    width: 100%;
  }
}
</style>