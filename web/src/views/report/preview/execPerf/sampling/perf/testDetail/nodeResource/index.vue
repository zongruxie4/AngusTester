<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const Cpu = defineAsyncComponent(() => import('./cpu.vue'));
const Disk = defineAsyncComponent(() => import('./disk.vue'));
const Memory = defineAsyncComponent(() => import('./memory.vue'));
const Network = defineAsyncComponent(() => import('./network.vue'));

interface NodeItem {
  agentPort: string;
  domain: string;
  id: string;
  ip: string;
  name: string;
}

interface Props {
  execNodes: NodeItem[]; // 执行节点选项
  appNodes: NodeItem[]; // 应用节点选项
  startTime: string; // 开始时间
  endTime: string; // 结束时间
  reportInterval: string; // 采集数据间隔时间
  status: string;
  delayInSeconds: number; // 执行过程中的调接口间隔时间
  activeChart: string;
}

const props = withDefaults(defineProps<Props>(), {
  execNodes: () => ([]),
  appNodes: () => ([]),
  startTime: '',
  endTime: '',
  delayInSeconds: 3000
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'loadingChange', value: boolean): void;
}>();

const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};
</script>
<template>
  <div class="space-y-7">
    <div>
      <div class="flex items-center space-x-1.5">
        <em class="inline-block w-1.25 h-1.25 rounded bg-gray-500"></em>
        <span>{{ t('reportPreview.execPerf.sampling.testDetail.nodeResource.cpu') }}</span>
      </div>
      <Cpu v-bind="props" @loadingChange="loadingChange" />
    </div>

    <div>
      <div class="flex items-center space-x-1.5">
        <em class="inline-block w-1.25 h-1.25 rounded bg-gray-500"></em>
        <span>{{ t('reportPreview.execPerf.sampling.testDetail.nodeResource.memory') }}</span>
      </div>
      <Memory v-bind="props" @loadingChange="loadingChange" />
    </div>

    <div>
      <div class="flex items-center space-x-1.5">
        <em class="inline-block w-1.25 h-1.25 rounded bg-gray-500"></em>
        <span>{{ t('reportPreview.execPerf.sampling.testDetail.nodeResource.disk') }}</span>
      </div>
      <Disk v-bind="props" @loadingChange="loadingChange" />
    </div>

    <div>
      <div class="flex items-center space-x-1.5">
        <em class="inline-block w-1.25 h-1.25 rounded bg-gray-500"></em>
        <span>{{ t('reportPreview.execPerf.sampling.testDetail.nodeResource.network') }}</span>
      </div>
      <Network v-bind="props" @loadingChange="loadingChange" />
    </div>
  </div>
</template>
