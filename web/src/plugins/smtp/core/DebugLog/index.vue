<script setup lang="ts">
import { computed } from 'vue';
import { Colon, NoData, IconDownload } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

interface Props {
  value:{
    success:boolean;
    exitCode:string;
    console:string[];
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const showConsole = computed(() => {
  if (!props.value?.console?.length) {
    return [];
  }

  return props.value.console.map(item => {
    return {
      id: utils.uuid(),
      log: item
    };
  });
});

const downloadLog = () => {
  const content = props.value?.console?.join('\n');
  if (!content) {
    return;
  }

  const file = new File([content], 'scheduling.log', {
    type: 'text/plain'
  });
  const url = URL.createObjectURL(file);
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = 'scheduling.log';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
};
</script>

<template>
  <div class="h-full px-5 py-3 text-3">
    <template v-if="!!props.value">
      <div class="flex items-center leading-5 mb-2.5">
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">调度结果</span>
          <Colon class="mr-2" />
          <template v-if="props.value?.success">
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-success"></span>
            <span>成功</span>
          </template>
          <template v-else>
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-error"></span>
            <span>失败</span>
          </template>
        </div>
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">进程退出码</span>
          <Colon class="mr-2" />
          <span>{{ props.value?.exitCode }}</span>
        </div>
        <IconDownload class="text-4" @click="downloadLog" />
      </div>
      <div style="height:calc(100% - 30px);" class="px-3.5 py-2.5 space-y-1 rounded border border-solid border-theme-text-box overflow-auto">
        <div
          v-for="item in showConsole"
          :key="item.id"
          class="log-item">
          {{ item.log }}
        </div>
      </div>
    </template>
    <NoData v-else size="small" />
  </div>
</template>
