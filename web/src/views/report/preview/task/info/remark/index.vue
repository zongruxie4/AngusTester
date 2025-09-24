<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
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

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const taskType = computed(() => {
  return props.dataSource?.content?.task?.taskType?.value;
});

const index = computed(() => {
  if (taskType.value === 'API_TEST' || taskType.value === 'SCENARIO_TEST') {
    return 11;
  }

  return 8;
});

const indexText = computed(() => {
  if (index.value === 8) {
    return '八';
  }

  return '九';
});

const remarks = computed(() => {
  return props.dataSource?.content?.remarks;
});

const len = computed(() => {
  let _len = 0;
  if (remarks.value?.length) {
    _len = remarks.value?.length - 1;
  }
  return _len;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span :id="`a${index}`" class="text-4 text-theme-title font-medium">{{ indexText }}、<em class="inline-block w-0.25"></em>{{ t('reportPreview.task.info.remark.title') }}</span>
    </h1>

    <template v-if="len>0">
      <div
        v-for="(item) in remarks"
        :key="item.id"
        class="mb-2.5 last:mb-0">
        <div class="flex items-center mb-0.5 space-x-3">
          <div class="text-theme-content font-medium">{{ item.createdByName }}</div>
          <div>{{ item.createdDate }}</div>
        </div>

        <div class="browser-container">
          <RichEditor :value="item.content" mode="view" />
        </div>
      </div>
    </template>

    <div v-else class="content-text-container">{{ t('common.noData') }}</div>
  </div>
</template>

<style scoped>
.browser-container {
 text-indent: 2em;
}

.content-text-container{
  text-indent: 2em;
}
</style>
