<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  dataSource: {[key: string]: string}
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

const indicatorItem = [
  {
    label: t('xcan_httpTestInfo.concurrency'),
    dataIndex: 'threads'
  },
  {
    label: t('xcan_httpTestInfo.testDuration'),
    dataIndex: 'duration'
  },
  {
    label: t('xcan_httpTestInfo.rampUpConcurrency'),
    dataIndex: 'rampUpThreads'
  },
  {
    label: t('xcan_httpTestInfo.rampUpDuration'),
    dataIndex: 'rampUpInterval'
  },
  {
    label: t('xcan_httpTestInfo.responseTime'),
    dataIndex: 'art',
    compareOprate: '<='
  },
  {
    label: t('xcan_httpTestInfo.transactionsPerSecond'),
    dataIndex: 'tps',
    compareOprate: '>='
  },
  {
    label: t('xcan_httpTestInfo.errorRate'),
    dataIndex: 'errorRate',
    compareOprate: '<=',
    unit: '%'
  }
];

</script>
<template>
  <div class="flex border rounded text-3">
    <div class="flex-1 bg-gray-light flex flex-col p-2">
      <span v-for="item in indicatorItem" :key="item.dataIndex">
        {{ item.label }}
      </span>
    </div>
    <div class="flex-1 flex flex-col p-2">
      <div
        v-for="item in indicatorItem"
        :key="item.dataIndex"
        class="flex items-center space-x-2">
        <span v-if="item.dataIndex === 'art'">{{ props.dataSource.percentile?.value ? `${props.dataSource.percentile.value} <= ${props.dataSource.art}` : '--' }}</span>
        <span v-else>
          {{ props.dataSource[item.dataIndex] ? `${item.compareOprate || ''} ${props.dataSource[item.dataIndex]}${item.unit || ''}` : '--' }}
        </span>
      </div>
    </div>
  </div>
</template>
