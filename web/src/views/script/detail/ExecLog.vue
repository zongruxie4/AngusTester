<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Colon, IconDownload, NoData, Spin } from '@xcan-angus/vue-ui';
import { useExecLog } from './composables/useExecLog';

const { t } = useI18n();

interface Props {
  execId:string;
  execNode:{id:string, name:string, ip:string, agentPort:string, publicIp:string};
  schedulingResult:{
    success:boolean;
    exitCode:string;
    console:string[]
  };
}

const props = withDefaults(defineProps<Props>(), {
  execId: '',
  execNode: undefined,
  schedulingResult: undefined
});

// Use exec log composable
const {
  execLogContent,
  execLogPath,
  execLogErr,
  errorText,
  loading,
  loadExecLog,
  downloadLog
} = useExecLog(props);

</script>

<template>
  <Spin
    class="flex-1 h-full overflow-y-auto"
    style="scrollbar-gutter: stable;"
    :spinning="loading"
    :delay="0">
    <div v-if="!!props.execNode?.id" class="h-full px-5 py-3 text-3">
      <div class="flex items-center leading-5 mb-2.5">
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('scriptDetail.log.node') }}</span>
          <Colon class="mr-2" />
          <span>{{ props.execNode.name }}({{ props.execNode.publicIp || props.execNode.ip }})</span>
        </div>
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">{{ t('scriptDetail.log.scheduleResult') }}</span>
          <Colon class="mr-2" />
          <template v-if="props.schedulingResult?.success">
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-success"></span>
            <span>{{ t('scriptDetail.log.success') }}</span>
          </template>
          <template v-else>
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-error"></span>
            <span>{{ t('scriptDetail.log.fail') }}</span>
          </template>
        </div>
        <div class="flex items-center mr-15">
          s<span class="text-theme-sub-content">{{ t('scriptDetail.log.processExitCode') }}</span>
          <Colon class="mr-2" />
          <span>{{ props.schedulingResult?.exitCode }}</span>
        </div>
        <IconDownload class="text-4" @click="downloadLog" />
      </div>
      <div style="height:calc(100% - 30px);" class="px-3.5 py-2.5 space-y-1 rounded border border-solid border-theme-text-box overflow-auto whitespace-pre-wrap">
        {{ execLogContent }}
      </div>
    </div>
    <NoData v-if="!loading && !props.execNode?.id" size="small" />
  </Spin>
</template>
