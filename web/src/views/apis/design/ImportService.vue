<script lang="ts" setup>
import { ref } from 'vue';
import { Modal, notification, IconText, TreeSelect } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { apis } from '@/api/tester';
import { TESTER } from '@xcan-angus/infra';

interface Props {
  visible: boolean;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: ''
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'cancel'):void;
  (e: 'ok'):void;
  (e: 'update:visible', value: boolean):void
}>();

const { t } = useI18n();

const loading = ref(false);
const formRef = ref();
const formState = ref({
  serviceId: ''
});

/**
 * Update selected service id when user chooses a node from tree.
 */
const handleServiceChange = (serviceId) => {
  formState.value.serviceId = serviceId;
};

/**
 * Close modal without importing from service.
 */
const cancel = () => {
  emits('update:visible', false);
};

/**
 * Validate selection and import design by service id.
 */
const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const [error] = await apis.addDesignByService(formState.value.serviceId);
    loading.value = false;
    if (error) {
      return;
    }
    notification.success(t('actions.tips.importSuccess'));
    cancel();
    emits('ok');
  });
};
</script>
<template>
  <Modal
    :title="t('design.importServiceModal.title')"
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
        :label="t('design.importServiceModal.serviceLabel')"
        required
        class="leading-8">
        <TreeSelect
          :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
          :fieldNames="{ label: 'name', value: 'id', children: 'children' }"
          :placeholder="t('design.importServiceModal.servicePlaceholder')"
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
