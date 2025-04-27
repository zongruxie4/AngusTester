<script lang="ts" setup>
import { defineAsyncComponent, inject, ref, Ref } from 'vue';

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

const userInfo = inject<Ref<{ id: string }>>('tenantInfo');
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = inject<Ref<{ id: string; name: string; }>>('appInfo', ref({ id: '', name: '' }));

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
