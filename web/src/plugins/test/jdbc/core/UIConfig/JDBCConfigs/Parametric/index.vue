<script lang="ts" setup>
import { defineAsyncComponent, inject, ref, Ref } from 'vue';
import { appContext } from '@xcan-angus/infra';

import { VariableInfo } from '../PropsType';
import { ProjectInfo } from '@/layout/types';
import { DatasetItem } from '@/plugins/test/types';

type Props = {
  variables: VariableInfo[];
  datasets: DatasetItem[];
  actionOnEOF: 'RECYCLE' | 'STOP_THREAD';
  sharingMode: 'ALL_THREAD' | 'CURRENT_THREAD';
  errorNum: number;
}

const props = withDefaults(defineProps<Props>(), {
  variables: () => [],
  datasets: () => [],
  actionOnEOF: undefined,
  sharingMode: undefined,
  errorNum: 0
});


const emit = defineEmits<{
  (e: 'variablesChange', value: VariableInfo[]): void;
  (e: 'datasetsChange', value: DatasetItem[]): void;
  (e: 'update:errorNum', value: number): void;
  (e: 'update:actionOnEOF', value:'RECYCLE' | 'STOP_THREAD'): void;
  (e: 'update:sharingMode', value: 'ALL_THREAD' | 'CURRENT_THREAD'): void;
}>();

const variablesRef = ref();

const Dataset = defineAsyncComponent(() => import('@/plugins/test/components/UIConfigComp/ParametricDataset/index.vue'));
const Variables = defineAsyncComponent(() => import('./Variables/index.vue'));

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

const datasetsChange = (data: DatasetItem[]) => {
  emit('datasetsChange', data);
};

const variablesChange = (data: VariableInfo[]) => {
  emit('variablesChange', data);
};

const errorChange = (value: number) => {
  emit('update:errorNum', value);
};

defineExpose({
  isValid: () => {
    if (typeof variablesRef.value?.isValid === 'function') {
      return variablesRef.value.isValid();
    }

    return true;
  }
});
</script>
<template>
  <div class="space-y-10">
    <Variables
      ref="variablesRef"
      :projectId="projectInfo?.id"
      :userInfo="userInfo"
      :appInfo="appInfo"
      :variables="props.variables"
      @change="variablesChange"
      @errorChange="errorChange" />
    <Dataset
      :projectId="projectInfo?.id"
      :userInfo="userInfo"
      :appInfo="appInfo"
      :datasets="props.datasets"
      :actionOnEOF="props.actionOnEOF"
      :sharingMode="props.sharingMode"
      @targetInfoChange="targetInfoChange"
      @change="datasetsChange">
    </dataset>
  </div>
</template>
