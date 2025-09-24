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
  [{ label: t('status.total'), dataIndex: 'totalNum', bgColor: 'bg-blue-1' }, { label: t('status.passed'), dataIndex: 'successNum', bgColor: 'bg-status-success' }],
  [{ label: t('status.failed'), dataIndex: 'failNum', bgColor: 'bg-status-error' }, { label: t('status.disabled'), dataIndex: 'disabledNum', bgColor: 'bg-gray-icon' }]
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
        <span class="flex-1 text-white px-2 rounded" :class="item.bgColor">{{ item.label }}</span>
        <span class="flex-1 bg-gray-light px-2 rounded-r">{{ props.dataSource[item.dataIndex] }}</span>
      </div>
    </li>
  </div>
</template>
