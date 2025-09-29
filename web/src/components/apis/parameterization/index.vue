<script lang="ts" setup>
// Vue core imports
import { defineAsyncComponent, inject, ref, Ref } from 'vue';

// Infrastructure imports
import { appContext, ActionOnEOF, SharingMode } from '@xcan-angus/infra';

/**
 * Component props interface for parameterization
 */
type Props = {
  targetId: string;
  targetType: 'API' | 'SCENARIO' | 'API_CASE';
  datasetActionOnEOF: ActionOnEOF;
  datasetSharingMode: SharingMode;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  targetId: undefined,
  targetType: undefined,
  datasetActionOnEOF: undefined,
  datasetSharingMode: undefined
});

// Component events
const emit = defineEmits<{
  (e: 'targetInfoChange', value: { id: string; datasetActionOnEOF: ActionOnEOF; datasetSharingMode: SharingMode; }): void;
}>();

// Async component imports
const DataSet = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/index.vue'));
const Variables = defineAsyncComponent(() => import('@/components/apis/parameterization/variable/index.vue'));

// Component state
const currentUserInfo = ref(appContext.getUser());
const currentProjectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const currentAppInfo = ref(appContext.getAccessApp());

/**
 * Handle target information change event
 */
const handleTargetInfoChange = (data: { id: string; datasetActionOnEOF: ActionOnEOF; datasetSharingMode: SharingMode; }) => {
  emit('targetInfoChange', data);
};
</script>
<template>
  <div class="space-y-10">
    <Variables
      :projectId="currentProjectInfo?.id"
      :userInfo="currentUserInfo"
      :appInfo="currentAppInfo"
      :targetId="props.targetId"
      :targetType="props.targetType" />
    <DataSet
      :projectId="currentProjectInfo?.id"
      :userInfo="currentUserInfo"
      :appInfo="currentAppInfo"
      :targetId="props.targetId"
      :targetType="props.targetType"
      :datasetActionOnEOF="props.datasetActionOnEOF"
      :datasetSharingMode="props.datasetSharingMode"
      @targetInfoChange="handleTargetInfoChange">
    </DataSet>
  </div>
</template>
