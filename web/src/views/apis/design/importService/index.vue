<script lang="ts" setup>
import { ref } from 'vue';
import { Modal, notification, IconText, TreeSelect } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import { apis } from '@/api/tester';
import { TESTER } from '@xcan-angus/tools';

interface Props {
  visible: boolean;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: ''
});

const emits = defineEmits<{(e: 'cancel'):void; (e: 'ok'):void; (e: 'update:visible', value: boolean):void}>();

const loading = ref(false);
const formRef = ref();
const formState = ref({
  serviceId: ''
});

const handleServiceChange = (serviceId) => {
  formState.value.serviceId = serviceId;
};

const cancel = () => {
  emits('update:visible', false);
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const [error] = await apis.addDesignByService(formState.value.serviceId);
    loading.value = false;
    if (error) {
      return;
    }
    notification.success('导入成功');
    cancel();
    emits('ok');
  });
};

</script>
<template>
  <Modal
    title="导入服务"
    :visible="props.visible"
    :width="500"
    :okButtonProps="{
      loading
    }"
    @cancel="cancel"
    @ok="ok">
    <Form
      ref="formRef"
      :model="formState">
      <FormItem
        name="serviceId"
        label="服务"
        required
        class="leading-8">
        <TreeSelect
          :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
          :fieldNames="{label:'name', value: 'id'}"
          placeholder="请选择所属服务"
          :virtual="false"
          size="small"
          @change="handleServiceChange">
          <template #title="{name}">
            <div
              class="flex items-center"
              :title="name">
              <IconText
                text="S"
                class="bg-blue-badge-s mr-2 text-3"
                style="width: 16px; height: 16px;" />
              <span class="truncate flex-1">{{ name }}</span>
            </div>
          </template>
        </TreeSelect>
      </FormItem>
    </Form>
  </Modal>
</template>
