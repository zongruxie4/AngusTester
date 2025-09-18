<script setup lang="ts">
import { computed } from 'vue';
import { Toggle, Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/types';

type Props = {
  projectId: string;
  userInfo: { id: string; fullName: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
  taskId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  taskId: undefined
});

const { t } = useI18n();

// Derived values
const totalProcessCount = computed(() => +(props.dataSource?.totalNum || 0));
const failedProcessCount = computed(() => +(props.dataSource?.failNum || 0));
const onePassStatusText = computed(() => {
  if (totalProcessCount.value <= 0) return '--';
  return failedProcessCount.value === 0 ? t('task.detailInfo.basic.columns.yes') : t('task.detailInfo.basic.columns.no');
});

// One pass status methods
const getOnePassIconClass = () => {
  if (totalProcessCount.value <= 0) return 'text-gray-500';
  return failedProcessCount.value === 0 ? 'text-green-500' : 'text-red-500';
};

const getOnePassTextClass = () => {
  if (totalProcessCount.value <= 0) return 'text-gray-600';
  return failedProcessCount.value === 0 ? 'text-green-600' : 'text-red-600';
};
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3.5 font-medium">{{ t('task.detailInfo.basic.processCount') }}</div>
    </template>

    <template #default>
      <div class="process-times-container">
        <!-- Process Statistics Grid -->
        <div class="process-grid">
          <!-- One Pass Status -->
          <div class="process-item process-item-wide">
            <div class="process-item-header">
              <div class="process-icon-wrapper">
                <Icon 
                  icon="icon-yicixingtongguoshu1" 
                  :class="getOnePassIconClass()" 
                  class="text-3.5" />
              </div>
              <span class="process-label">{{ t('task.detailInfo.basic.columns.onePass') }}</span>
            </div>
            <div class="process-value">
              <span 
                :class="getOnePassTextClass()"
                class="font-semibold">
                {{ onePassStatusText }}
              </span>
            </div>
          </div>
          
          <!-- Total Process Count -->
          <div class="process-item">
            <div class="process-item-header">
              <div class="process-icon-wrapper">
                <Icon icon="icon-shuju" class="text-3.5 text-blue-500" />
              </div>
              <span class="process-label">{{ t('task.detailInfo.basic.columns.totalProcessCount') }}</span>
            </div>
            <div class="process-value">{{ totalProcessCount }}</div>
          </div>

          <!-- Failed Process Count -->
          <div class="process-item">
            <div class="process-item-header">
              <div class="process-icon-wrapper">
                <Icon icon="icon-cuowu" class="text-3.5 text-red-500" />
              </div>
              <span class="process-label">{{ t('task.detailInfo.basic.columns.failedProcessCount') }}</span>
            </div>
            <div class="process-value">{{ failedProcessCount }}</div>
          </div>
        </div>

      </div>
    </template>
  </Toggle>
</template>

<style scoped>
/* Container and Layout */
.process-times-container {
  padding-top: 0.5rem;
  padding-left: 1.375rem;
  padding-right: 1.375rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* Process Statistics Grid */
.process-grid {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: auto auto;
}

.process-item {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  padding: 0.5rem;
  position: relative;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.process-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.process-item-wide {
  grid-column: 1 / -1;
}

.process-item-header {
  display: flex;
  align-items: center;
  flex: 1;
}

.process-icon-wrapper {
  width: 1.25rem;
  height: 1.25rem;
  border-radius: 50%;
  background-color: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0.375rem;
  flex-shrink: 0;
}

.process-label {
  font-size: 0.75rem;
  font-weight: 500;
  color: #374151;
  line-height: 1.2;
}

.process-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  text-align: right;
  min-width: 2rem;
  padding-left: 0.25rem;
}


/* Responsive Design */
@media (max-width: 768px) {
  .process-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto;
  }
  
  .process-item-wide {
    grid-column: 1;
  }
}

@media (max-width: 640px) {
  .process-times-container {
    padding-left: 0.75rem;
    padding-right: 0.75rem;
  }
  
  .process-item {
    padding: 0.375rem;
  }
  
  .process-grid {
    gap: 0.5rem;
  }
  
  .process-icon-wrapper {
    width: 1rem;
    height: 1rem;
    margin-right: 0.25rem;
  }
  
  .process-value {
    font-size: 0.75rem;
  }
}

/* Animation for smooth transitions */
.process-item {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Color variations for different states */
.text-blue-500 {
  color: #3b82f6;
}

.text-red-500 {
  color: #ef4444;
}

.text-green-500 {
  color: #10b981;
}

.text-green-600 {
  color: #059669;
}

.text-red-600 {
  color: #dc2626;
}

.text-gray-500 {
  color: #6b7280;
}

.text-gray-600 {
  color: #4b5563;
}
</style>
