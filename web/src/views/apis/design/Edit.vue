<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Hints, Input, Modal, Select, notification } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { apis } from '@/api/tester';
import { ApisDesignSource } from '@/enums/enums';
import { DEFAULT_OPENAPI_VERSION, SUPPORTED_OPENAPI_VERSION_OPTION } from '@/views/apis/design/types';

interface Props {
  visible: boolean;
  designId?: string;
  projectId: string;
  servicesId?: string;
  apisIds?: string[];
  designScope?: ApisDesignSource;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  designId: undefined,
  projectId: '',
  servicesId: undefined,
  apisIds: () => [],
  designScope: ApisDesignSource.MANUAL_CREATED
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'cancel'):void;
  (e: 'ok'):void;
  (e: 'update:visible', value: boolean):void
}>();

const formState = ref({
  name: undefined,
  openapiSpecVersion: DEFAULT_OPENAPI_VERSION
});

const loading = ref(false);
const formRef = ref();

/**
 * Load existing design data for editing form.
 */
const loadDesignForEdit = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await apis.getDesignDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data || {};
  if (!data) {
    return;
  }
  const { name, openapiSpecVersion } = data;
  formState.value = {
    name, openapiSpecVersion
  };
};

const selectApiId = ref();
const selectApis = ref<any[]>([]);

/**
 * Reset form and close modal without saving.
 */
const cancel = () => {
  formRef.value.resetFields();
  emits('update:visible', false);
  emits('cancel');
};

/**
 * Validate form and trigger create or update action by presence of designId.
 */
const ok = async () => {
  formRef.value.validate().then(async () => {
    if (!props.designId) {
      await createDesign();
    } else {
      await updateDesign();
    }
  });
};

/**
 * Create a new design with current form data.
 */
const createDesign = async () => {
  loading.value = true;
  const [error] = await apis.addDesign({
    ...formState.value,
    projectId: props.projectId
  });
  loading.value = false;
  if (error) {
    return;
  }
  emits('ok');
  emits('update:visible', false);
};

/**
 * Update an existing design with current form data.
 */
const updateDesign = async () => {
  loading.value = true;
  const [error] = await apis.updateDesign({
    ...formState.value,
    id: props.designId
  });
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.editSuccess'));
  emits('ok');
  emits('update:visible', false);
};

onMounted(async () => {
  // origin.value = await site.getUrl('at');
  watch(() => props.visible, async (newValue) => {
    if (newValue) {
      if (props.designId) {
        await loadDesignForEdit(props.designId);
      } else {
        formState.value = {
          name: undefined,
          openapiSpecVersion: DEFAULT_OPENAPI_VERSION
        };
        selectApis.value = [];
        selectApiId.value = undefined;
      }
    }
  }, { immediate: true });
});
</script>
<template>
  <Modal
    :title="props.designId ? t('design.editModal.editTitle') : t('design.editModal.addTitle')"
    :visible="props.visible"
    :width="680"
    :okButtonProps="{
      loading
    }"
    @cancel="cancel"
    @ok="ok">
    <Form
      ref="formRef"
      :model="formState"
      size="small"
      :labelCol="{ style: { width: '90px' } }"
      class="max-w-242.5"
      layout="horizontal">
      <FormItem
        required
        name="name"
        :label="t('design.editModal.nameLabel')">
        <Input
          v-model:value="formState.name"
          :maxlength="100"
          :placeholder="t('common.placeholders.searchKeyword')" />
      </FormItem>

      <FormItem
        :label="t('design.editModal.versionLabel')"
        name="openapiSpecVersion"
        required>
        <div class="flex items-center space-x-2">
          <Select
            v-model:value="formState.openapiSpecVersion"
            class="flex-1"
            :options="SUPPORTED_OPENAPI_VERSION_OPTION" />
          <Hints :text="t('design.editModal.versionPlaceholder')" />
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>
