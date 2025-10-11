<script setup lang="ts">
import { ref, defineAsyncComponent } from 'vue';
import { Toggle, Icon, NoData } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';

const CaseStep = defineAsyncComponent(() => import('@/views/test/case/list/CaseSteps.vue'));

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  taskId?: number;
  actionAuth?: {[key: string]: any};
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  taskId: undefined,
  actionAuth: () => ({})
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

const conditionsExpand = ref(true);
const isEditSteps = ref(false);
const stepsContent = ref([]);
const saveStepsLoading = ref(false);

/**
 * <p>Enter editing mode for steps (deep clone original).</p>
 * <p>Sets the current steps content for editing with deep copy.</p>
 */
const handleEditSteps = () => {
  isEditSteps.value = true;
  stepsContent.value = JSON.parse(JSON.stringify(props.dataSource?.steps || []));
};

/**
 * <p>Cancel steps editing.</p>
 * <p>Exits editing mode without saving changes.</p>
 */
const cancelEditSteps = () => {
  isEditSteps.value = false;
};

/**
 * <p>Persist steps to backend.</p>
 * <p>Saves the updated steps if changed.</p>
 */
const saveSteps = async () => {
  if (!props.dataSource) {
    isEditSteps.value = false;
    return;
  }

  if (saveStepsLoading.value) {
    return;
  }
  saveStepsLoading.value = true;
  const [error] = await testCase.updateCase([{
    id: props.dataSource.id,
    steps: stepsContent.value
  }]);
  saveStepsLoading.value = false;
  if (error) {
    return;
  }
  isEditSteps.value = false;
  emit('change', { steps: stepsContent.value });
};
</script>

<template>
  <Toggle
    v-model:open="conditionsExpand"
    class="mt-3.5">
    <template #title>
      <div class="flex items-center space-x-2">
        <span class="text-3.5">{{ t('common.testSteps') }}</span>
        <template v-if="isEditSteps">
          <Button
            class="font-normal text-theme-special"
            type="link"
            size="small"
            @click="saveSteps">
            {{ t('actions.save') }}
          </Button>
          <Button
            class="font-normal text-theme-special"
            type="link"
            size="small"
            @click="cancelEditSteps">
            {{ t('actions.cancel') }}
          </Button>
        </template>
        <Icon
          v-else-if="props.actionAuth['edit']"
          icon="icon-xiugai"
          class="text-3.5 text-theme-special text-theme-text-hover cursor-pointer"
          @click="handleEditSteps" />
      </div>
    </template>

    <template #default>
      <template v-if="isEditSteps || dataSource?.steps?.length">
        <CaseStep
          v-model:value="stepsContent"
          class="mt-3 mx-2"
          :defaultValue="isEditSteps ? stepsContent : dataSource.steps"
          :readonly="!isEditSteps" />
      </template>

      <template v-else>
        <NoData
          :text="t('common.noData')"
          size="small"
          class="my-8" />
      </template>
    </template>
  </Toggle>
</template>
