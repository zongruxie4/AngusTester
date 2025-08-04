<script lang="ts" setup>
import { defineAsyncComponent, inject, ref, Ref } from 'vue';
import { appContext } from '@xcan-angus/infra';

type Props = {
  targetId: string;
  targetType: 'API' | 'SCENARIO' | 'API_CASE';
  datasetActionOnEOF: 'RECYCLE' | 'STOP_THREAD';
  datasetSharingMode: 'ALL_THREAD' | 'CURRENT_THREAD';
}

const props = withDefaults(defineProps<Props>(), {
  targetId: undefined,
  targetType: undefined,
  datasetActionOnEOF: undefined,
  datasetSharingMode: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'targetInfoChange', value: { id: string; datasetActionOnEOF: 'RECYCLE' | 'STOP_THREAD'; datasetSharingMode: 'ALL_THREAD' | 'CURRENT_THREAD'; }): void;
}>();

const DataSet = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/index.vue'));
const Variables = defineAsyncComponent(() => import('@/components/apis/parameterization/variable/index.vue'));

const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = ref(appContext.getAccessApp());

const targetInfoChange = (data: { id: string; datasetActionOnEOF: 'RECYCLE' | 'STOP_THREAD'; datasetSharingMode: 'ALL_THREAD' | 'CURRENT_THREAD'; }) => {
  emit('targetInfoChange', data);
};
</script>
<template>
  <div class="space-y-10">
    <Variables
      :projectId="projectInfo?.id"
      :userInfo="userInfo"
      :appInfo="appInfo"
      :targetId="props.targetId"
      :targetType="props.targetType" />
    <DataSet
      :projectId="projectInfo?.id"
      :userInfo="userInfo"
      :appInfo="appInfo"
      :targetId="props.targetId"
      :targetType="props.targetType"
      :datasetActionOnEOF="props.datasetActionOnEOF"
      :datasetSharingMode="props.datasetSharingMode"
      @targetInfoChange="targetInfoChange">
    </dataset>
  </div>
</template>
