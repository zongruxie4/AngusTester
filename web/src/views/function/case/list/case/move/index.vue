<script lang="ts" setup>
import { inject, ref } from 'vue';
import { Icon, Modal, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { funcCase } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { CaseListObj } from '../PropsType';

const { t } = useI18n();

interface Props {
  visible: boolean;
  type: 'batch' | 'one',
  selectedCase: CaseListObj;
  selectedRowKeys: string[];
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  type: 'one',
  selectedCase: undefined,
  selectedRowKeys: () => []
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'update'):void}>();

const formState = ref<{ targetPlanId?: string }>({ targetPlanId: undefined });
const projectInfo = inject('projectInfo', ref({ id: '' }));

const close = () => {
  emits('update:visible', false);
  formState.value.targetPlanId = undefined;
};

const loading = ref(false);
const onFinish = async () => {
  const ids = props.type === 'batch' ? props.selectedRowKeys : [props.selectedCase.id];
  loading.value = true;
  const [error] = await funcCase.moveCase(formState.value.targetPlanId as string, ids);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('functionCase.moveCaseModal.moveSuccess'));
  emits('update:visible', false);
  emits('update');
  formState.value.targetPlanId = undefined;
};

const format = (data) => {
  return { ...data, disabled: data.id === props.selectedCase?.planId };
};
</script>
<template>
  <Modal
    :title="t('functionCase.moveCaseModal.title')"
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
        :label="t('functionCase.moveCaseModal.selectPlan')"
        :rules="[{ required: true, message: t('functionCase.moveCaseModal.pleaseSelectPlan') }]"
        class="flex-1">
        <Select
          v-model:value="formState.targetPlanId"
          :action="`${TESTER}/func/plan?projectId=${projectInfo.id}&fullTextSearch=true`"
          :fieldNames="{ value: 'id', label: 'name' }"
          :format="format"
          showSearch
          allowClear
          :placeholder="t('functionCase.moveCaseModal.selectOrQueryPlan')">
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
