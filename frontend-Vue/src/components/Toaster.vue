<script setup>
import { toasts, dismissToast } from '../services/toast.js';
</script>

<template>
  <div class="toaster" aria-live="polite" aria-atomic="true">
    <transition-group name="toast-fade" tag="div">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="toast-item"
        :class="t.type"
        role="alert"
        @mouseenter="t._hover=true"
        @mouseleave="t._hover=false"
      >
        <div class="toast-content">{{ t.text }}</div>
        <button class="toast-close" aria-label="关闭" @click="dismissToast(t.id)">×</button>
      </div>
    </transition-group>
  </div>
</template>

<style scoped>
.toaster {
  position: fixed;
  z-index: 1000;
  right: 1.25rem;
  bottom: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: .5rem;
  pointer-events: none;
}
.toast-item {
  pointer-events: auto;
  min-width: 240px;
  max-width: 360px;
  background: #1e293b;
  color: #fff;
  padding: .75rem .9rem .6rem .9rem;
  border-radius: .5rem;
  box-shadow: 0 4px 14px rgba(0,0,0,.25);
  font-size: .9rem;
  line-height: 1.35;
  position: relative;
  border-left: 5px solid #6366f1;
  overflow: hidden;
}
.toast-item.success { border-left-color: #16a34a; }
.toast-item.error { border-left-color: #dc2626; }
.toast-item.warning { border-left-color: #d97706; }
.toast-item.info { border-left-color: #6366f1; }
.toast-close {
  position: absolute;
  top: 4px;
  right: 6px;
  background: transparent;
  border: none;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  line-height: 1;
  padding: 2px 4px;
  opacity: .8;
}
.toast-close:hover { opacity: 1; }
.toast-fade-enter-active, .toast-fade-leave-active { transition: all .25s ease; }
.toast-fade-enter-from, .toast-fade-leave-to { opacity: 0; transform: translateY(12px); }
</style>

