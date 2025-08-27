<script lang="ts" setup>
import { reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, notification } from '@xcan-angus/vue-ui';
import { CheckboxGroup, Form, FormItem } from 'ant-design-vue';

import { apis, scenario, services } from 'src/api/tester';

const { t } = useI18n();

interface Props {
  visible: boolean;
  id?: string;
  type: 'API' | 'SCENARIO' | 'SERVICE'
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  visible: false,
  type: undefined
});

const emits = defineEmits<{(e: 'update:visible', value: boolean) }>();

const OK_API_MAP = {
  SERVICE: services.deleteTest,
  API: apis.deleteTestTask,
  SCENARIO: scenario.deleteTestTask
};

const validated = ref(false);
const testTypes = [
  {
    label: t('commonComp.delTaskTestModal.functionalTest'),
    value: 'FUNCTIONAL'
  },
  {
    label: t('commonComp.delTaskTestModal.performanceTest'),
    value: 'PERFORMANCE'
  },
  {
    label: t('commonComp.delTaskTestModal.stabilityTest'),
    value: 'STABILITY'
  }
];

const sprintFormRef = ref();
const sprintData = reactive({
  testTypes: ['FUNCTIONAL', 'PERFORMANCE', 'STABILITY']
});

const resetPlanData = () => {
  if (typeof sprintFormRef.value?.clearValidate === 'function') {
    sprintFormRef.value.clearValidate();
  }
  sprintData.testTypes = ['FUNCTIONAL', 'PERFORMANCE', 'STABILITY'];
};

const validateTestType = async () => {
  if (sprintData.testTypes.length) {
    return Promise.resolve();
  }

  // eslint-disable-next-line prefer-promise-reject-errors
  return Promise.reject();
};

const loading = ref(false);
const handleDel = async () => {
  sprintFormRef.value.validate()
    .then(async () => {
      loading.value = true;
      const [error] = await OK_API_MAP[props.type](props.id, sprintData.testTypes);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('commonComp.delTaskTestModal.deleteSuccess'));
      emits('update:visible', false);
    });
};

const handleClose = () => {
  emits('update:visible', false);
};

watch(() => props.visible, () => {
  validated.value = false;
  resetPlanData();
}, {
  immediate: true
});

</script>
<template>
  <Modal
    :title="t('commonComp.delTaskTestModal.title')"
    :width="580"
    :confirmLoading="loading"
    :visible="props.visible"
    @cancel="handleClose"
    @ok="handleDel">
    <div class="mb-2">
      {{ t('commonComp.delTaskTestModal.confirmMessage') }}
    </div>
    <Form ref="sprintFormRef" :model="sprintData">
      <FormItem
        :rules="{ validator: validateTestType, message: t('commonComp.delTaskTestModal.selectTestType'), required: true }"
        :label="t('commonComp.delTaskTestModal.selectDeleteTestType')"
        class="w-full"
        name="testTypes">
        <CheckboxGroup v-model:value="sprintData.testTypes" :options="testTypes"></CheckboxGroup>
      </FormItem>
    </Form>
  </Modal>
</template>
