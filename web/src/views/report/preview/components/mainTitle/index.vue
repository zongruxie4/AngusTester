<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon } from '@xcan-angus/vue-ui';

import { ReportContent } from '../../PropsType';

const { t } = useI18n();

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const report = computed(() => {
  return props.dataSource?.report;
});
</script>

<template>
  <div class=" text-3.5 px-4.5 pt-1 pb-2.5 header-bg-container border">
    <img src="./images/logoName.png" class="h-11" />

    <div class="font-medium text-5 text-center mt-2">{{ report?.name }}</div>
    <div class="text-center space-x-5 mt-5">
      <div class="inline-flex items-start mb-1">
        <div class="flex-shrink-0 flex items-center mr-1.5">
          <span class="flex-shrink-0">{{ t('common.reporter') }}</span>
          <Colon />
        </div>
        <div>{{ report?.creator }}</div>
      </div>

      <div class="inline-flex items-start mb-1">
        <div class="flex-shrink-0 flex items-center mr-1.5">
          <span class="flex-shrink-0">{{ t('reportPreview.mainTitle.reportDate') }}</span>
          <Colon />
        </div>
        <div>{{ report?.createdDate }}</div>
      </div>

      <div class="inline-flex items-start mb-1">
        <div class="flex-shrink-0 flex items-center mr-1.5">
          <span class="flex-shrink-0">{{ t('reportPreview.mainTitle.reportVersion') }}</span>
          <Colon />
        </div>
        <div>{{ report?.version }}</div>
      </div>

      <div class="inline-flex items-start mb-1">
        <div class="flex-shrink-0 flex items-center mr-1.5">
          <span class="flex-shrink-0">{{ t('reportPreview.mainTitle.reportTemplate') }}</span>
          <Colon />
        </div>
        <div class="max-w-94.5">{{ report?.template?.message }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.header-bg-container {
  /* background-images: url("./images/bg.png"); */
  background-color: rgb(247, 248, 251);
}
</style>
