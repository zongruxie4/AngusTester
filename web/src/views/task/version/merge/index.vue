<script lang="ts" setup>
import { ref } from 'vue';
import { Icon, Modal, Select } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/tools';
import { software } from 'src/api/tester';

interface Props {
  visible: boolean;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: ''
});

const emits = defineEmits<{(e: 'cancel'):void; (e: 'ok', formId: string):void; (e: 'update:visible', value: boolean):void}>();

const formState = ref<{formId?: string; toId?: string}>({
  formId: undefined,
  toId: undefined
});

const loading = ref(false);
const formRef = ref();

const cancel = () => {
  emits('update:visible', false);
  emits('cancel');
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const [error] = await software.merge({
      ...formState.value
    });
    loading.value = false;
    if (error) {
      return;
    }
    emits('ok', formState.value.formId as string);
    emits('update:visible', false);
  });
};

const fieldNames = {
  value: 'id',
  label: 'name'
};

</script>
<template>
  <Modal
    title="版本合并"
    :visible="props.visible"
    :okButtonProps="{
      loading: loading,
      disabled: !formState.formId || !formState.toId || formState.toId === formState.formId
    }"
    @cancel="cancel"
    @ok="ok">
    <div class="border rounded border-status-warn py-5 px-2">
      <Icon icon="icon-jinggao" class="text-status-warn" />
      警告：版本一旦合成后，将无法还原！
    </div>
    <Form
      ref="formRef"
      :model="formState"
      size="small"
      :labelCol="{ style: { width: '90px' } }"
      class="max-w-242.5 mt-4"
      layout="horizontal">
      <FormItem
        required
        name="formId"
        label="合并版本">
        <Select
          v-model:value="formState.formId"
          :fieldNames="fieldNames"
          :lazy="false"
          :defaultActiveFirstOption="true"
          :action="`${TESTER}/software/version?projectId=${projectId}&fullTextSearch=true`" />
      </FormItem>
      <FormItem
        required
        label="合并版本至"
        class="flex-1 !mb-5"
        name="toId">
        <Select
          v-model:value="formState.toId"
          :fieldNames="fieldNames"
          :lazy="false"
          :defaultActiveFirstOption="true"
          :action="`${TESTER}/software/version?projectId=${projectId}&fullTextSearch=true`" />
      </FormItem>
    </Form>
  </Modal>
</template>
