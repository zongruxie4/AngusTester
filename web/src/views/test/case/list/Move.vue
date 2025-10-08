<script lang="ts" setup>
import { inject, ref, Ref } from 'vue';
import { Icon, Modal, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { funcCase } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';

const { t } = useI18n();

// Component props interface
interface Props {
  visible: boolean;
  type: 'batch' | 'one',
  selectedCase: CaseDetail;
  selectedRowKeys: number[];
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  type: 'one',
  selectedCase: undefined,
  selectedRowKeys: () => []
});

// Component emits
const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'update'):void}>();

// Form state management
const formState = ref<{ targetPlanId?: number }>({ targetPlanId: undefined });
const projectId = inject<Ref<string>>('projectId', ref(''));
const loading = ref(false);

/**
 * Close the modal and reset form
 */
const close = () => {
  emits('update:visible', false);
  formState.value.targetPlanId = undefined;
};

/**
 * Handle form submission
 */
const onFinish = async () => {
  const ids = props.type === 'batch' ? props.selectedRowKeys : [props.selectedCase.id];
  loading.value = true;
  const [error] = await funcCase.moveCase(formState.value.targetPlanId, ids);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.moveSuccess'));
  emits('update:visible', false);
  emits('update');
  formState.value.targetPlanId = undefined;
};

/**
 * Format plan options to disable current plan
 * @param data - Plan data
 * @returns Formatted plan data
 */
const format = (data: any) => {
  return { ...data, disabled: data.id === props.selectedCase?.planId };
};
</script>
<template>
  <Modal
    :title="t('testCase.moveCaseModal.title')"
    :visible="props.visible"
    :width="600"
    :footer="null"
    @cancel="close">
    <Form
      :model="formState"
      size="small"
      layout="horizontal"
      @finish="onFinish">
      <FormItem
        name="targetPlanId"
        :label="t('testCase.moveCaseModal.selectPlan')"
        :rules="[{ required: true, message: t('testCase.moveCaseModal.pleaseSelectPlan') }]"
        class="flex-1">
        <Select
          v-model:value="formState.targetPlanId"
          :action="`${TESTER}/func/plan?projectId=${projectId}&fullTextSearch=true`"
          :fieldNames="{ value: 'id', label: 'name' }"
          :format="format"
          showSearch
          allowClear
          :placeholder="t('testCase.moveCaseModal.selectOrQueryPlan')">
          <template #option="item">
            <div class="flex items-center">
              <Icon icon="icon-jihua" class="mr-1 text-3.5" />
              <div
                style="max-width: 440px;"
                class="truncate"
                :title="item.name">
                {{ item.name }}
              </div>
            </div>
          </template>
        </Select>
      </FormItem>

      <FormItem class="mt-5">
        <div class="flex justify-end">
          <Button
            :loading="loading"
            type="primary"
            size="small"
            htmlType="submit"
            class="px-3">
            {{ t('actions.confirm') }}
          </Button>
          <Button
            size="small"
            class="ml-5 px-3"
            @click="close">
            {{ t('actions.cancel') }}
          </Button>
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>
