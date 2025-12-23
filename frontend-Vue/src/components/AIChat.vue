<template>
  <div class="ai-chat-container">
    <div class="chat-header">
      <h2><i class="fas fa-robot"></i> AI作业问答助手</h2>
      <div class="model-selector">
        <label for="model-select">模型:</label>
        <select id="model-select" v-model="selectedModel" @change="onModelChange">
          <option v-for="model in availableModels" :key="model" :value="model">
            {{ getModelDisplayName(model) }}
          </option>
        </select>
      </div>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="(message, index) in messages" 
        :key="index" 
        :class="['message', message.type]"
      >
        <div class="message-content">
          <div class="message-icon">
            <i :class="message.type === 'user' ? 'fas fa-user' : 'fas fa-robot'"></i>
          </div>
          <div class="message-text">
          <div v-if="message.type === 'ai'" v-html="parseMarkdown(message.content)"></div>
          <p v-else>{{ message.content }}</p>
          <span class="message-time">{{ message.time }}</span>
        </div>
        </div>
      </div>
      
      <div v-if="isLoading" class="message ai loading">
        <div class="message-content">
          <div class="message-icon">
            <i class="fas fa-robot"></i>
          </div>
          <div class="message-text">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <div class="input-group">
        <textarea
          v-model="currentMessage"
          @keydown.enter.prevent="sendMessage"
          placeholder="请输入你的作业问题..."
          rows="3"
          :disabled="isLoading"
        ></textarea>
        <button 
          @click="sendMessage" 
          :disabled="isLoading || !currentMessage.trim()"
          class="send-button"
        >
          <i class="fas fa-paper-plane"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import api, { aiApi } from '../services/api'

export default {
  name: 'AIChat',
  setup() {
    const messages = ref([])
    const currentMessage = ref('')
    const isLoading = ref(false)
    const messagesContainer = ref(null)
    const availableModels = ref([])
    const selectedModel = ref('tstars2.0')

    const sendMessage = async () => {
      if (!currentMessage.value.trim() || isLoading.value) return

      const userMessage = {
        type: 'user',
        content: currentMessage.value,
        time: new Date().toLocaleTimeString()
      }

      messages.value.push(userMessage)
      const question = currentMessage.value
      currentMessage.value = ''
      isLoading.value = true

      await nextTick()
      scrollToBottom()

      try {
        const response = await aiApi.chat({
          userId: 1, // 这里应该从认证状态获取
          question: question,
          context: '', // 可选的上下文
          model: selectedModel.value
        })

        console.log('AI聊天API响应:', response)

        // 检查响应格式 - 经过拦截器处理后，数据直接在response中
        let answer = null
        console.log('检查响应结构:', {
          hasResponse: !!response,
          hasAnswer: !!(response && response.answer),
          responseKeys: response ? Object.keys(response) : []
        })
        
        if (response && response.answer) {
          answer = response.answer
          console.log('找到answer字段:', answer)
        } else {
          console.log('AI响应数据结构:', response)
          throw new Error('AI响应格式错误')
        }

        const aiMessage = {
          type: 'ai',
          content: answer,
          time: new Date().toLocaleTimeString()
        }

        messages.value.push(aiMessage)
      } catch (error) {
        console.error('AI问答失败:', error)
        const errorMessage = {
          type: 'ai',
          content: '抱歉，AI服务暂时不可用，请稍后再试。',
          time: new Date().toLocaleTimeString()
        }
        messages.value.push(errorMessage)
      } finally {
        isLoading.value = false
        await nextTick()
        scrollToBottom()
      }
    }

    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    const fetchAvailableModels = async () => {
      try {
        const response = await aiApi.getModels()
        console.log('模型列表API响应:', response)
        
        // 检查响应格式 - 数据直接在response中，不在response.data中
        if (response && Array.isArray(response)) {
          availableModels.value = response
          console.log('成功获取模型列表:', availableModels.value)
        } else if (response && response.data && Array.isArray(response.data)) {
          availableModels.value = response.data
          console.log('成功获取模型列表:', availableModels.value)
        } else if (response && response.data && response.data.data && Array.isArray(response.data.data)) {
          availableModels.value = response.data.data
          console.log('成功获取模型列表:', availableModels.value)
        } else {
          console.log('模型列表响应数据结构:', response)
          throw new Error('模型列表响应格式错误')
        }
      } catch (error) {
        console.error('获取可用模型失败:', error)
        // 使用默认模型列表
        availableModels.value = ['qwen3-coder-plus', 'qwen3-vl-plus', 'qwen3-max', 'glm-4.6', 'deepseek-v3.2', 'tstars2.0']
      }
    }

    const getModelDisplayName = (model) => {
      const modelNames = {
        'qwen3-coder-plus': '通义千问3-Coder Plus',
        'qwen3-vl-plus': '通义千问3-VL Plus',
        'qwen3-max': '通义千问3-Max',
        'glm-4.6': '智谱GLM-4.6',
        'deepseek-v3.2': 'DeepSeek V3.2',
        'tstars2.0': 'TStars 2.0'
      }
      return modelNames[model] || model
    }

    const onModelChange = () => {
      // 模型切换时可以添加提示
      console.log('切换到模型:', selectedModel.value)
    }

    const parseMarkdown = (text) => {
      if (!text) return ''
      
      return text
        // 标题
        .replace(/^### (.*$)/gim, '<h3>$1</h3>')
        .replace(/^## (.*$)/gim, '<h2>$1</h2>')
        .replace(/^# (.*$)/gim, '<h1>$1</h1>')
        // 粗体
        .replace(/\*\*(.*)\*\*/gim, '<strong>$1</strong>')
        // 斜体
        .replace(/\*(.*)\*/gim, '<em>$1</em>')
        // 代码块
        .replace(/```([^`]*)```/gim, '<code>$1</code>')
        // 行内代码
        .replace(/`([^`]*)`/gim, '<code>$1</code>')
        // 列表
        .replace(/^\* (.*)$/gim, '<li>$1</li>')
        .replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
        // 数字列表
        .replace(/^\d+\. (.*)$/gim, '<li>$1</li>')
        // 引用
        .replace(/^> (.*)$/gim, '<blockquote>$1</blockquote>')
        // 换行
        .replace(/\n\n/gim, '</p><p>')
        .replace(/\n/gim, '<br>')
        // 段落包装
        .replace(/^(.*)$/gim, '<p>$1</p>')
        // 清理空段落
        .replace(/<p><\/p>/g, '')
        .replace(/<p>(<h[1-6]>)/g, '$1')
        .replace(/(<\/h[1-6]>)<\/p>/g, '$1')
        .replace(/<p>(<ul>)/g, '$1')
        .replace(/(<\/ul>)<\/p>/g, '$1')
        .replace(/<p>(<blockquote>)/g, '$1')
        .replace(/(<\/blockquote>)<\/p>/g, '$1')
    }

    onMounted(async () => {
      await fetchAvailableModels()
      
      // 添加欢迎消息
      const welcomeMessage = {
        type: 'ai',
        content: `你好！我是AI作业问答助手，当前使用 ${getModelDisplayName(selectedModel.value)} 模型。可以帮助你解答作业中的问题。请问有什么可以帮助你的吗？`,
        time: new Date().toLocaleTimeString()
      }
      messages.value.push(welcomeMessage)
    })

    return {
      messages,
      currentMessage,
      isLoading,
      messagesContainer,
      availableModels,
      selectedModel,
      sendMessage,
      getModelDisplayName,
      onModelChange,
      parseMarkdown
    }
  }
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  background: #f8f9fa;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.model-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.model-selector label {
  font-size: 0.9rem;
  font-weight: 500;
}

.model-selector select {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  padding: 0.25rem 0.5rem;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.model-selector select:hover {
  background: rgba(255, 255, 255, 0.3);
}

.model-selector select:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.model-selector select option {
  background: #4a5568;
  color: white;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.message.user {
  flex-direction: row-reverse;
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  max-width: 70%;
}

.message.user .message-content {
  flex-direction: row-reverse;
}

.message-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.message.user .message-icon {
  background: #007bff;
  color: white;
}

.message.ai .message-icon {
  background: #6c757d;
  color: white;
}

.message-text {
  background: white;
  padding: 1rem;
  border-radius: 1rem;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  position: relative;
}

.message.user .message-text {
  background: #007bff;
  color: white;
}

.message-text p {
  margin: 0;
  line-height: 1.4;
}

.message-text {
  white-space: pre-wrap;
}

.message-text strong {
  font-weight: bold;
}

.message-text em {
  font-style: italic;
}

.message-text h1, .message-text h2, .message-text h3, .message-text h4 {
  margin: 0.5rem 0;
  font-weight: bold;
}

.message-text h1 {
  font-size: 1.5rem;
}

.message-text h2 {
  font-size: 1.3rem;
}

.message-text h3 {
  font-size: 1.1rem;
}

.message-text ul, .message-text ol {
  margin: 0.5rem 0;
  padding-left: 1.5rem;
}

.message-text li {
  margin: 0.25rem 0;
}

.message-text code {
  background: #f1f5f9;
  padding: 0.125rem 0.25rem;
  border-radius: 0.25rem;
  font-family: monospace;
  font-size: 0.9rem;
}

.message-text blockquote {
  border-left: 3px solid #e2e8f0;
  padding-left: 0.75rem;
  margin: 0.5rem 0;
  color: #64748b;
}

.message-time {
  font-size: 0.75rem;
  opacity: 0.7;
  margin-top: 0.5rem;
  display: block;
}

.chat-input {
  padding: 1rem;
  background: white;
  border-top: 1px solid #dee2e6;
}

.input-group {
  display: flex;
  gap: 0.5rem;
  align-items: flex-end;
}

textarea {
  flex: 1;
  border: 1px solid #ced4da;
  border-radius: 0.5rem;
  padding: 0.75rem;
  resize: none;
  font-family: inherit;
  font-size: 0.9rem;
}

textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,0.25);
}

.send-button {
  background: #007bff;
  color: white;
  border: none;
  border-radius: 0.5rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.send-button:hover:not(:disabled) {
  background: #0056b3;
}

.send-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.typing-indicator {
  display: flex;
  gap: 0.25rem;
  padding: 0.5rem 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6c757d;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.loading .message-text {
  background: #f8f9fa;
  color: #6c757d;
}

@media (max-width: 768px) {
  .ai-chat-container {
    height: 100vh;
    max-width: 100%;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .chat-header {
    padding: 0.75rem;
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }
  
  .chat-header h2 {
    font-size: 1.2rem;
  }
  
  .model-selector {
    width: 100%;
    justify-content: space-between;
  }
  
  .model-selector select {
    flex: 1;
    max-width: 200px;
  }
}
</style>