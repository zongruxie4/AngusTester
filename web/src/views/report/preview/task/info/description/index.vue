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

const description = computed(() => {
  return props.dataSource?.content?.task?.description;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a4" class="text-4 text-theme-title font-medium">
        {{ t('reportPreview.serial.4') }}
        <em class="inline-block w-0.25"></em>
        {{ t('common.description') }}</span>
    </h1>

    <RichEditor
      v-if="!!description?.length"
      :value="description"
      mode="view" />

    <div v-else class="content-text-container">{{ t('common.noData') }}</div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
