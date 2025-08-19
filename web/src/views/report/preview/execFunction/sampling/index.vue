<script lang="ts" setup>
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { ExecContent, ExecInfo, ReportInfo } from '../../PropsType';

const { t } = useI18n();

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  reportInfo: ReportInfo;
  execInfo: ExecInfo;
  execContent: ExecContent[];
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  reportInfo: undefined,
  execInfo: undefined,
  execContent: undefined
});

const HttpTestDetail = defineAsyncComponent(() => import('@/views/report/preview/execFunction/sampling/http/index.vue'));

const plugin = computed(() => {
  return props.execInfo?.plugin;
});
</script>
<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a4" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.execFunction.catalog.sections.iterationResult') }}</span>
    </h1>

    <HttpTestDetail
      v-if="plugin==='Http'"
      :execInfo="props.execInfo"
      :execContent="props.execContent" />
  </div>
</template>
