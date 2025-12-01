<script lang="ts" setup>
import { Button } from 'ant-design-vue';
import { Icon, Input, notification, Select, ShortDuration } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import ExpandGrid from './ExpandGrid.vue';
import { usePerformanceData } from './composables';
import { appContext } from '@xcan-angus/infra';
import apiUtils from '@/utils/apis/index';
// Initialize composables
const { t } = useI18n();
const {
  editable,
  editInfo,
  info,
  percentileOpt,
  toggleEditMode,
  validateDuration,
  validateRampUpInterval,
  savePerformanceInfo
} = usePerformanceData();

// Expose methods to template
defineExpose({
  validateDuration,
  validateRampUpInterval,
  savePerformanceInfo
});
</script>

<template>
  <ExpandGrid :title="t('indicator.performance.title')">
    <template #button>
      <div class="text-3 flex items-center">
        <template v-if="editable">
          <span class="cursor-pointer" @click.stop="toggleEditMode">
            <Icon icon="icon-zhongzhi2" class="mr-1" />{{ t('actions.cancel') }}
          </span>
          <Button
            type="text"
            class="ml-2 text-3 py-0 h-5"
            @click.stop="savePerformanceInfo">
            <Icon icon="icon-baocun" class="mr-1" />{{ t('actions.save') }}
          </Button>
        </template>
        <Button
          v-else
          v-show="appContext.isAdmin()"
          class="text-3 py-0 h-5"
          type="text"
          @click.stop="toggleEditMode">
          <Icon icon="icon-shuxie" class="mr-1" />{{ t('actions.edit') }}
        </Button>
      </div>
    </template>
    <template #default>
      <ul class="flex text-3 pt-3 quota-wrapper pl-3 space-x-8">
        <li>
          <div class="quota-label">{{ t('indicator.performance.labels.concurrentUsers') }}</div>
          <div v-if="editable">
            <Input
              v-model:value="editInfo.threads"
              dataType="number"
              class="w-20"
              size="small" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2">{{ info.threads }}</div>
        </li>
        <li>
          <div class="quota-label">{{ t('indicator.performance.labels.testDuration') }}</div>
          <div v-if="editable" class="flex items-center">
            <ShortDuration
              v-model:value="editInfo.duration"
              :inputProps="{
                class: 'w-20',
                onblur: validateDuration
              }"
              :selectProps="{ class: '!w-15' }" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2">{{ info.duration }}</div>
        </li>
        <li>
          <div class="quota-label">{{ t('protocol.responseTime') }}</div>
          <div v-if="editable" class="flex items-center">
            <Select
              v-model:value="editInfo.percentile"
              class="w-25 mr-2 quota-rt"
              :fieldNames="{label: 'message', value: 'value'}"
              :allowClear="false"
              size="small"
              :options="percentileOpt" />
            <span class="pr-2">&lt;=</span>
            <Input
              v-model:value="editInfo.art"
              dataType="number"
              class="w-20"
              size="small" />
            <span class="pl-2">ms</span>
          </div>
          <div v-else class="min-w-25 bg-gray-light rounded h-7 leading-7 px-2">
            {{ info.percentileName }}&lt;={{ info.art }}ms
          </div>
        </li>
        <li>
          <div class="quota-label">{{ t('indicator.performance.labels.tps') }}</div>
          <div v-if="editable" class="flex items-center">
            <span class="pr-2">>=</span>
            <Input
              v-model:value="editInfo.tps"
              dataType="number"
              class="w-20"
              size="small" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> >= {{ info.tps }}</div>
        </li>
        <li>
          <div class="quota-label">{{ t('indicator.performance.labels.errorRate') }}</div>
          <div v-if="editable">
            <span class="pr-2">&lt;=</span>
            <Input
              v-model:value="editInfo.errorRate"
              dataType="float"
              :decimalPoint="10"
              class="w-15"
              size="small" />
            <span class="pl-2">%</span>
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> &lt;= {{ info.errorRate }}% </div>
        </li>
        <li>
          <div class="quota-label">{{ t('indicator.performance.labels.rampUpThreads') }}</div>
          <div v-if="editable">
            <Input
              v-model:value="editInfo.rampUpThreads"
              dataType="number"
              class="w-20"
              size="small" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> {{ info.rampUpThreads }} </div>
        </li>
        <li>
          <div class="quota-label">{{ t('indicator.performance.labels.rampUpDuration') }}</div>
          <div v-if="editable">
            <ShortDuration
              v-model:value="editInfo.rampUpInterval"
              :inputProps="{
                class: 'w-20',
                onblur: validateRampUpInterval
              }"
              :selectProps="{ class: '!w-15' }" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> {{ info.rampUpInterval }} </div>
        </li>
      </ul>
    </template>
  </ExpandGrid>
</template>

<style scoped>
:deep(.ant-input-group-wrapper .ant-input-group-addon) {
  @apply bg-transparent border-none leading-7;
}

:deep(.ant-input-group-wrapper .ant-input-affix-wrapper) {
  @apply rounded w-20;
}

.quota-rt :deep(.ant-select-selector) {
  @apply h-7;
}

.quota-label {
  @apply mb-4 whitespace-nowrap;
}
</style>
