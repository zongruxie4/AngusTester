<script setup lang="ts">
// Vue composition API imports
import { useI18n } from 'vue-i18n';

// UI component imports
import { Popover } from 'ant-design-vue';
import { Icon, Hints } from '@xcan-angus/vue-ui';

const { t } = useI18n();
</script>
<template>
  <Popover
    destroyTooltipOnHide
    placement="left"
    :trigger="['click']">
    <div class="flex-shrink-0 flex items-center cursor-pointer w-4 h-7">
      <Icon icon="icon-jieshaoshuoming" class="text-4 text-theme-text-hover" />
    </div>
    <template #content>
      <div style="max-height: 60vh; overflow: auto;" class="leading-4.5 text-3 text-theme-content">
        <div class="mb-4">
          <Hints :text="t('xcan_apiAssert.executionConditionExpressionTitle')" class="mb-2 !font-semibold !text-theme-content" />
          <div class="mb-3">{{ t('xcan_apiAssert.executionConditionExpressionDesc') }}</div>
        </div>
        <ol class="pl-4 space-y-3">
          <li style="list-style-type: circle;" class="space-y-2 text-theme-sub-content">
            <div class="text-theme-content font-semibold">{{ t('xcan_apiAssert.conditionExpressionFormat') }}</div>
            <div>{{ t('xcan_apiAssert.conditionExpressionFormatExample') }}</div>
          </li>
          <li style="list-style-type: circle;" class="space-y-2">
            <div class="font-semibold">{{ t('xcan_apiAssert.formatParameterDescription') }}</div>
            <ul style="padding-left: 16px;list-style-type: disc;" class="space-y-2 text-theme-sub-content">
              <li>{{ t('xcan_apiAssert.variableDescription') }}</li>
              <li>{{ t('xcan_apiAssert.operatorDescription') }}</li>
              <li>{{ t('xcan_apiAssert.valueDescription') }}</li>
            </ul>
          </li>
        </ol>
      </div>
    </template>
  </Popover>
</template>
<style scoped>
pre code {
  font-size: 13px;
  line-height: 18px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
