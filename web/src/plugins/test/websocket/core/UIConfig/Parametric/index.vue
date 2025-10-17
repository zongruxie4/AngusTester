<script lang="ts" setup>
import { defineAsyncComponent, inject, ref, Ref } from 'vue';
import { appContext } from '@xcan-angus/infra';
import { ProjectInfo } from '@/layout/types';

type Props = {
  datasets: {[key:string]:any}[];
  actionOnEOF: 'RECYCLE' | 'STOP_THREAD';
  sharingMode: 'ALL_THREAD' | 'CURRENT_THREAD';
  errorNum: number;
}

const props = withDefaults(defineProps<Props>(), {
  datasets: () => [],
  actionOnEOF: undefined,
  sharingMode: undefined
});


const emit = defineEmits<{
  (e: 'update:datasets', value: {[key:string]:any}[]): void;
  (e: 'update:actionOnEOF', value:'RECYCLE' | 'STOP_THREAD'): void;
  (e: 'update:sharingMode', value: 'ALL_THREAD' | 'CURRENT_THREAD'): void;
}>();

const Dataset = defineAsyncComponent(() => import('@/plugins/test/components/UIConfigComp/ParametricDataset/index.vue'));

const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const appInfo = ref(appContext.getAccessApp());

const targetInfoChange = (data: { actionOnEOF?: 'RECYCLE' | 'STOP_THREAD'; sharingMode?: 'ALL_THREAD' | 'CURRENT_THREAD'; }) => {
  const { actionOnEOF, sharingMode } = data;
  if (actionOnEOF) {
    emit('update:actionOnEOF', actionOnEOF);
  }

  if (sharingMode) {
    emit('update:sharingMode', sharingMode);
  }
};

const datasetsChange = (data:{[key:string]:any}[]) => {
  emit('update:datasets', data);
};
</script>
<template>
  <Dataset
    :projectId="projectInfo?.id"
    :userInfo="userInfo"
    :appInfo="appInfo"
    :datasets="props.datasets"
    :actionOnEOF="props.actionOnEOF"
    :sharingMode="props.sharingMode"
    @targetInfoChange="targetInfoChange"
    @change="datasetsChange" />
</template>
