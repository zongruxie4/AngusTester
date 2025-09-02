<script setup lang="ts">
import { watch } from 'vue';
import { Button, Progress, Spin } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useCapacity } from './composables/useCapacity';

interface Props {
  id: string
}

const props = withDefaults(defineProps<Props>(), {});

const { t } = useI18n();

// Use the capacity composable
const { state, percent, cloudDiskUrl, loadCapacity, init } = useCapacity();

// Initialize the composable
init();

// Watch for space ID changes and load capacity data
watch(() => props.id, newValue => {
  if (!newValue) {
    return;
  }
  loadCapacity(newValue);
}, {
  immediate: true
});

</script>

<template>
  <div class="text-3 text-center px-4">
    <Spin
      :tip="t('fileSpace.capacity.loading')"
      :spinning="state.loading">
      <Progress
        type="circle"
        class="mt-12"
        :percent="percent"
        :stroke-width="4"
        :width="100"
        strokeColor="#52C41A">
        <template #format>
          <p class="text-3 text-theme-sub-content">{{ t('fileSpace.capacity.used') }}</p>
          <p class="text-6 my-4 text-theme-title font-semibold">{{ percent }}%</p>
        </template>
      </Progress>
      <Progress
        :percent="percent"
        :showInfo="false"
        :stroke-width="6"
        class="mt-7 block" />
      <p class="text-theme-sub-content">
        {{ state.source?.usedSize }} / {{ state.source?.quotaSize?.value || '' }}{{ state.source?.quotaSize?.unit?.message || '' }}
      </p>
      <div v-if="cloudDiskUrl" class="py-2 px-3 rounded bg-gray-light text-theme-special mt-5 cursor-pointer inline-block">
        <Button
          :href="cloudDiskUrl"
          type="link"
          size="small"
          target="_blank">
          <Icon icon="icon-shangchuan" class="mr-2" />{{ t('fileSpace.capacity.upgradeCapacity') }}
        </Button>
      </div>
    </Spin>
  </div>
</template>
