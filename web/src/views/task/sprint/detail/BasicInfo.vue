<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Progress } from 'ant-design-vue';
import { Colon } from '@xcan-angus/vue-ui';
import type { SprintInfo } from '../types';

interface Props {
  sprintData?: SprintInfo;
  completionRate: number;
}

const props = defineProps<Props>();
const { t } = useI18n();

const attachments = computed(() => props.sprintData?.attachments || []);
</script>

<template>
  <div class="max-w-250 mb-2 basic-card">
    <div class="text-theme-title mb-2 text-3.5 font-semibold basic-title">
      {{ t('taskSprint.columns.basicInfo') }}
    </div>

    <div class="space-y-2.5">
      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start mt-2.5">
          <div class="w-18 flex items-center justify-end whitespace-nowrap flex-shrink-0 text-right">
            <span class="font-semibold text-black">{{ t('taskSprint.columns.name') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all ml-2">{{ sprintData?.name }}</div>
        </div>

        <div class="w-1/2 flex items-start mt-2.5">
          <div class="w-24 flex items-center justify-end whitespace-nowrap flex-shrink-0 text-right">
            <span class="font-semibold text-black">{{ t('taskSprint.columns.timePlan') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="text-3 whitespace-nowrap ml-2">
            <span>{{ sprintData?.startDate }}</span>
            <span class="mx-2">-</span>
            <span>{{ sprintData?.deadlineDate }}</span>
          </div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-18 flex items-center justify-end whitespace-nowrap flex-shrink-0 text-right">
            <span class="font-semibold text-black">{{ t('taskSprint.columns.owner') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all ml-2">{{ sprintData?.ownerName }}</div>
        </div>

        <div class="w-1/2 flex items-start">
          <div class="w-24 flex items-center justify-end whitespace-nowrap flex-shrink-0 text-right">
            <span class="font-semibold text-black">{{ t('taskSprint.columns.taskPrefix') }}</span>
            <Colon class="w-1" />
          </div>

          <div
            class="whitespace-pre-wrap break-words break-all ml-2"
            :class="!sprintData?.taskPrefix ? 'muted' : ''">
            {{ sprintData?.taskPrefix || '--' }}
          </div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-center">
          <div class="w-18 flex items-center justify-end whitespace-nowrap flex-shrink-0 text-right">
            <span class="font-semibold text-black">{{ t('taskSprint.columns.status') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5 ml-2">
            <div class="h-1.5 w-1.5 rounded-full mr-1" :class="sprintData?.status?.value"></div>
            <div>{{ sprintData?.status?.message }}</div>
          </div>
        </div>

        <div class="w-1/2 flex items-center">
          <div class="w-24 flex items-center justify-end whitespace-nowrap flex-shrink-0 text-right">
            <span class="font-semibold text-black">{{ t('taskSprint.columns.workloadAssessment') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all ml-2">
            {{ sprintData?.evalWorkloadMethod?.message }}
          </div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-center">
          <div class="w-18 flex items-center justify-end whitespace-nowrap flex-shrink-0 text-right mr-1.5">
            <span class="font-semibold text-black">{{ t('taskSprint.columns.progress') }}</span>
            <Colon class="w-1" />
          </div>

          <Progress :percent="completionRate" style="width:150px;"/>
        </div>
      </div>

      <div class="flex items-start">
        <div style="width:calc(50% - 10px);" class="flex items-start">
          <div class="w-18 flex items-center justify-end whitespace-nowrap flex-shrink-0 text-right">
            <span class="font-semibold text-black">{{ t('taskSprint.form.attachments') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="space-y-1 truncate ml-2">
            <template v-if="attachments.length">
              <a
                v-for="item in attachments"
                :key="item.id"
                class="block w-auto truncate"
                :download="item.name"
                :href="item.url">
                {{ item.name }}
              </a>
            </template>
            <span v-else class="muted">--</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.basic-card {
  background-color: #fff;
  padding: 10px 14px 12px 14px;
}

.basic-title {
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--border-text-box);
}

/* Row rhythm */
.space-y-2.5 > .flex.items-start { min-height: 28px; }

/* Value color */
.whitespace-pre-wrap, .text-3 { color: var(--theme-content, #334155); }

/* Attachment links */
.truncate a { color: #1890ff; }
.truncate a:hover { color: #096dd9; background: #eff6ff; border-radius: 4px; }

/* Progress bar polish */
:deep(.ant-progress-bg) { background: linear-gradient(90deg, #1890ff 0%, #52c41a 100%); }

.muted { color: #94a3b8; }

.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}
</style>
