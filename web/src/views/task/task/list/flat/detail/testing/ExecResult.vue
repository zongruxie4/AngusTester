<script setup lang="ts">
import { computed } from 'vue';
import { Colon, NoData, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/types';

// Component Props
type Props = {
  dataSource: TaskInfo;
  largePageLayout: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined,
  largePageLayout: undefined
});

// Composables
const { t } = useI18n();

/**
 * Color mapping for execution result status indicators
 */
const EXECUTION_RESULT_COLORS: {
  SUCCESS: string;
  FAILED: string;
} = {
  SUCCESS: '#52C41A',
  FAILED: '#F5222D'
};

/**
 * Extracts execution result information from task data
 */
const executionResult = computed(() => props.dataSource?.execResult);

/**
 * Extracts execution ID from task data
 */
const executionId = computed(() => props.dataSource?.execId);

/**
 * Extracts execution name from task data
 */
const executionName = computed(() => props.dataSource?.execName);

/**
 * Extracts executor name from task data
 */
const executorName = computed(() => props.dataSource?.execByName);

/**
 * Extracts last execution date from task data
 */
const lastExecutionDate = computed(() => props.dataSource?.execDate);

/**
 * Extracts script ID from task data
 */
const scriptId = computed(() => props.dataSource?.scriptId);

/**
 * Extracts script name from task data
 */
const scriptName = computed(() => props.dataSource?.scriptName);

/**
 * Determines whether to show content based on available data
 * <p>
 * Returns true if any execution-related data is available
 */
const shouldShowContent = computed(() => {
  return executionResult.value ||
         executionId.value ||
         executionName.value ||
         executorName.value ||
         lastExecutionDate.value ||
         scriptId.value ||
         scriptName.value;
});
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3">{{ t('task.testing.execResult.title') }}</div>
    </template>

    <template #default>
      <!-- Standard Layout: Two columns -->
      <div v-if="shouldShowContent && !props.largePageLayout" class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <!-- Execution Result and ID Row -->
        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execResult') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="flex items-center">
              <em
                :style="'background-color:' + EXECUTION_RESULT_COLORS[executionResult?.value]"
                class="block not-italic w-1.5 h-1.5 mr-1 rounded flex-shrink-0"></em>
              <span class="truncate">{{ executionResult?.message }}</span>
            </div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execId') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ executionId }}</div>
          </div>
        </div>

        <!-- Execution Name and Executor Name Row -->
        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              <RouterLink
                v-if="executionId"
                :to="`/execution/info/${executionId}`"
                target="_blank"
                class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                {{ executionName }}
              </RouterLink>
              <template v-else>
                {{ executionName }}
              </template>
            </div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execByName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ executorName }}</div>
          </div>
        </div>

        <!-- Script ID and Script Name Row -->
        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.scriptId') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ scriptId }}</div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.scriptName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              <RouterLink
                v-if="scriptId"
                :to="`/script/detail/${scriptId}?type=view`"
                target="_blank"
                class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                {{ scriptName }}
              </RouterLink>
              <template v-else>
                {{ scriptName }}
              </template>
            </div>
          </div>
        </div>

        <!-- Last Execution Time Row -->
        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.lastExecTime') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ lastExecutionDate }}</div>
          </div>
        </div>
      </div>

      <!-- Large Page Layout: Three columns -->
      <div v-else-if="shouldShowContent && props.largePageLayout" class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <!-- First Row: Execution Result, ID, and Name -->
        <div class="flex items-start space-x-5">
          <div class="relative w-1/3 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execResult') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="flex items-center">
              <em
                :style="'background-color:' + EXECUTION_RESULT_COLORS[executionResult?.value]"
                class="block not-italic w-1.5 h-1.5 mr-1 rounded flex-shrink-0"></em>
              <span class="truncate">{{ executionResult?.message }}</span>
            </div>
          </div>

          <div class="relative w-1/3 flex items-start">
            <div class="w-12.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execId') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ executionId }}</div>
          </div>

          <div class="relative w-1/3 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              <RouterLink
                v-if="executionId"
                :to="`/execution/info/${executionId}`"
                target="_blank"
                class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                {{ executionName }}
              </RouterLink>
              <template v-else>
                {{ executionName }}
              </template>
            </div>
          </div>
        </div>

        <!-- Second Row: Executor Name, Script ID, and Script Name -->
        <div class="flex items-start space-x-5">
          <div class="relative w-1/3 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execByName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ executorName }}</div>
          </div>

          <div class="relative w-1/3 flex items-start">
            <div class="w-12.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.scriptId') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ scriptId }}</div>
          </div>

          <div class="relative w-1/3 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.scriptName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              <RouterLink
                v-if="scriptId"
                :to="`/script/detail/${scriptId}?type=view`"
                target="_blank"
                class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                {{ scriptName }}
              </RouterLink>
              <template v-else>
                {{ scriptName }}
              </template>
            </div>
          </div>
        </div>

        <!-- Third Row: Last Execution Time -->
        <div class="flex items-start space-x-5">
          <div class="relative w-1/3 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.lastExecTime') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ lastExecutionDate }}</div>
          </div>
        </div>
      </div>

      <NoData
        v-else
        size="small"
        class="my-10" />
    </template>
  </Toggle>
</template>

<style scoped>
/* Layout width calculations for responsive design */
.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-1\/3 {
  width: calc((100% - 40px)/3);
}
</style>
