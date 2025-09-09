<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  dataSource: {[key: string]: string}
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

const configInfo = [
  [{ label: t('xcan_httpTestInfo.totalTestCount'), dataIndex: 'enabledTestNum' }, { label: t('xcan_httpTestInfo.functionalTestCount'), dataIndex: 'enabledFuncTestNum' }],
  [{ label: t('xcan_httpTestInfo.performanceTestCount'), dataIndex: 'enabledPerfTestNum' }, { label: t('xcan_httpTestInfo.stabilityTestCount'), dataIndex: 'enabledStabilityTestNum' }]
];

</script>
<template>
  <div class="space-y-2 text-3">
    <li
      v-for="(line, idx) in configInfo"
      :key="idx"
      class="flex space-x-2">
      <div
        v-for="item in line"
        :key="item.dataIndex"
        class="flex flex-1 h-7 leading-7">
        <span class="flex-1 text-white bg-blue-6 px-2 rounded">{{ item.label }}</span>
        <span class="flex-1 bg-gray-light pl-2 rounded-r">{{ props.dataSource[item.dataIndex] || '--' }}</span>
      </div>
    </li>
  </div>
</template>
