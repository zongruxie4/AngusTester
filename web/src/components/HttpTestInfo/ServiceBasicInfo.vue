<script lang="ts" setup>
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  value: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({})
});

const InfoConfig = [
  {
    dataIndex: 'total',
    title: t('xcan_httpTestInfo.totalInterfaces'),
    icon: 'icon-jiekouyongli2'
  },
  {
    dataIndex: 'completedRate',
    title: t('xcan_httpTestInfo.progress'),
    icon: 'icon-renwuyicixingtongguo'
  }
];

</script>

<template>
  <div class="flex justify-around space-x-2">
    <div
      v-for="item in InfoConfig"
      :key="item.dataIndex"
      class="text-center py-1 flex-1 bg-blue-bg3 rounded flex justify-center items-center space-x-4 pr-4">
      <Icon :icon="item.icon" class="text-10" />
      <div>
        <div class="text-4 font-semibold">{{ props.value[item.dataIndex] || '--' }}{{ item.dataIndex === 'completedRate' ? '%' : '' }}</div>
        <div>{{ item.title }}</div>
      </div>
    </div>
  </div>
</template>
