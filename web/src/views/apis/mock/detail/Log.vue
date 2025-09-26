<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Icon, PureCard, Select } from '@xcan-angus/vue-ui';
import { Button, RadioGroup } from 'ant-design-vue';

import { useMockLog } from './composables/useMockLog';

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {});

const { t } = useI18n();

// Use the mock log composable
const {
  // State
  ip,
  port,
  showErr,
  errorInfo,
  content,
  refresh,
  isFullScreen,
  loadingLog,
  logTextParam,
  fileList,

  // Options
  linesOpt,

  // Computed
  fullScreenIcon,

  // Methods
  closeErrInfo,
  loadLogContent,
  downloadLog,
  handleZoom
} = useMockLog(props.id);
</script>
<template>
  <PureCard class="text-3 px-5 h-full flex flex-col pb-5 pt-3">
    <div v-if="showErr" class="border border-border-error rounded-xl px-2 py-1 flex items-center text-3 space-x-1 my-4 bg-bg-red">
      <Icon icon="icon-tishi1" class="text-blue-icon text-3.5" />
      <span class="text-rule flex-1">{{ errorInfo || t('mock.detail.log.errorInfo') + `http://${ip}:${port}` }}</span>
      <Icon
        icon="icon-cuowu"
        class="text-3.5 cursor-pointer"
        @click="closeErrInfo" />
    </div>
    <div class="flex items-center space-x-4 my-2">
      <span class="inline-block text-3.5 text-right font-semibold mr-2">{{ t('mock.detail.log.logFile') }}</span>
      <Select
        v-model:value="logTextParam.logName"
        class="w-70 ml-2"
        :options="fileList">
      </Select>
      <span class="inline-block w-30  text-3.5 text-right font-semibold mr-2">{{ t('mock.detail.log.browseLog') }}</span>
      <RadioGroup
        v-model:value="logTextParam.linesNum"
        :options="linesOpt"
        size="small" />
      <div class="inline-flex items-center flex-1 justify-end">
        <Button
          class="py-0 h-5"
          size="small"
          @click="downloadLog">
          {{ t('common.download') }}
        </Button>
        <Button
          class="py-0 h-5 ml-2"
          size="small"
          :disabled="!logTextParam.logName || loadingLog"
          @click="loadLogContent">
          {{ t('common.refresh') }}
        </Button>
        <Icon
          class="text-3.5 ml-2 cursor-pointer"
          :icon="fullScreenIcon"
          @click.stop="handleZoom" />
      </div>
    </div>
    <div class="border  bg-white log-text min-h-50 px-3 py-4 whitespace-pre-wrap break-words overflow-y-auto" :class="{'fixed left-0 top-0 right-0 bottom-0 h-full z-99': isFullScreen, 'flex-1': !isFullScreen}">
      <Icon
        v-if="isFullScreen"
        class="text-3.5 fixed right-7 top-7  cursor-pointer"
        :icon="fullScreenIcon"
        @click.stop="handleZoom" />
      {{ content }}
    </div>
  </PureCard>
</template>
<style scoped>
.content-fit-height {
  height: calc(100vh - 450px);
}

.log-text{
  /* color: #f2f2f2; */
  font-family: Monaco, Consolas, monospace !important;
}
</style>
