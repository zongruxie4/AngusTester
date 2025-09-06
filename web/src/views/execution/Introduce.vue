<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

const { t } = useI18n();
const PieChart = defineAsyncComponent(() => import('@/views/execution/Count.vue'));

const countRef = ref();
const updateCount = () => {
  countRef.value.loadCount();
};

defineExpose({
  updateCount
});
</script>
<template>
  <div class="bg-gray-2 px-7.5 flex items-center rounded text-3 justify-between">
    <div class="flex content items-center">
      <img
        class="mr-7.75"
        src="../../assets/home/exec.svg"
        style="width: 180px;height: 180px;" />
      <div class="ml-3.25 text-content">
        <div>{{ t('execution.header.title') }}</div>
        <div>{{ t('execution.header.subTitle') }}</div>
        <div class="mt-2">{{ t('execution.header.contentTitle') }}</div>
        <div class="mt-1">
          <Icon icon="icon-duihaolv" class="mr-2.5" />{{ t('execution.header.contentList1') }}
        </div>
        <div class="mt-1">
          <Icon icon="icon-duihaolv" class="mr-2.5" />{{ t('execution.header.contentList2') }}
        </div>
      </div>
    </div>
    <PieChart ref="countRef" class="ml-10" />
  </div>
</template>
