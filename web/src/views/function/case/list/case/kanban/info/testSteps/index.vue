<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, NoData } from '@xcan-angus/vue-ui';
import { cloneDeep } from 'lodash-es';
import { funcCase } from '@/api/tester';

import { CaseInfo } from '../../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseInfo;
  canEdit: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: CaseInfo): void;
  (event: 'update:dataSource', value: CaseInfo): void;
}>();

const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/case/add/caseSteps.vue'));

const editFlag = ref(false);
const steps = ref<{expectedResult:string;step:string;}[]>([]);
const stepsContent = ref<{expectedResult:string;step:string;}[]>([]);

const toEdit = () => {
  editFlag.value = true;
  stepsContent.value = cloneDeep(steps.value);
};

const cancel = () => {
  editFlag.value = false;
};

const ok = async () => {
  loadingChange(true);
  const [error] = await funcCase.updateCase([{
    id: props.dataSource?.id,
    steps: stepsContent.value
  }]);
  loadingChange(false);
  if (error) {
    return;
  }

  editFlag.value = false;
  change();
};

const change = async () => {
  const id = props.dataSource?.id;
  if (!id) {
    return;
  }

  loadingChange(true);
  const [error, res] = await funcCase.getCaseInfo(id);
  loadingChange(false);
  if (error) {
    return;
  }

  const data = res?.data || {};
  emit('change', data);
  emit('update:dataSource', data);
};

const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue) {
      steps.value = [];
      return;
    }

    steps.value = newValue.steps;
    stepsContent.value = cloneDeep(steps.value);
  }, { immediate: true });
});

const defaultValue = computed(() => {
  return editFlag.value ? stepsContent.value : steps.value;
});
</script>

<template>
  <div class="mt-4">
    <div class="flex items-center text-theme-title mb-1.75">
      <span class="font-semibold">测试步骤</span>
      <Button
        v-if="props.canEdit"
        v-show="!editFlag"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="toEdit">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>
    <NoData v-if="!editFlag&&!steps.length" size="small" />
    <CaseStep
      v-if="steps.length || editFlag"
      v-model:value="stepsContent"
      :stepView="props.dataSource?.stepView?.value"
      :defaultValue="defaultValue"
      :readonly="!editFlag" />
    <div v-show="editFlag" class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
      <Button size="small" @click="cancel">取消</Button>
      <Button
        size="small"
        type="primary"
        @click="ok">
        确定
      </Button>
    </div>
  </div>
</template>
