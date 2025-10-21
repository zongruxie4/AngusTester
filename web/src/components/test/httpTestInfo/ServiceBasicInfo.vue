<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

const { t } = useI18n();

interface Props {
  value: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({})
});

// Service basic information display configuration
const serviceBasicInfoConfig = [
  {
    dataIndex: 'total',
    title: t('xcan_httpTestInfo.totalInterfaces'),
    icon: 'icon-jiekouyongli2'
  },
  {
    dataIndex: 'completedRate',
    title: t('common.progress'),
    icon: 'icon-renwuyicixingtongguo'
  }
];
</script>
<template>
  <div class="flex justify-around space-x-2">
    <div
      v-for="infoItem in serviceBasicInfoConfig"
      :key="infoItem.dataIndex"
      class="text-center py-1 flex-1 bg-blue-bg3 rounded flex justify-center items-center space-x-4 pr-4">
      <Icon :icon="infoItem.icon" class="text-10" />
      <div>
        <div class="text-4 font-semibold">{{ props.value[infoItem.dataIndex] || '--' }}{{ infoItem.dataIndex === 'completedRate' ? '%' : '' }}</div>
        <div>{{ infoItem.title }}</div>
      </div>
    </div>
  </div>
</template>
