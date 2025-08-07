<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Hints, Input, Modal, Select, notification } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { apis } from '@/api/tester';

interface Props {
  visible: boolean;
  designId?: string;
  projectId: string;
  servicesId?: string; // 设计固定服务或接口
  apisIds?: string[]; // 设计固定接口
  designScope?: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  designId: undefined,
  projectId: '',
  servicesId: undefined,
  apisIds: () => [],
  designScope: 'SERVICES'
});
const { t } = useI18n();
const emits = defineEmits<{(e: 'cancel'):void; (e: 'ok'):void; (e: 'update:visible', value: boolean):void}>();
const formState = ref({
  name: undefined,
  openapiSpecVersion: '3.0.1'
});

const loading = ref(false);
const formRef = ref();

const loadData = async (id: string) => {
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

const cancel = () => {
  formRef.value.resetFields();
  emits('update:visible', false);
  emits('cancel');
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    if (!props.designId) {
      await addOk();
    } else {
      await editOk();
    }
  });
};

const addOk = async () => {
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

const editOk = async () => {
  loading.value = true;
  const [error] = await apis.updateDesign({
    ...formState.value,
    id: props.designId
  });
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('设计成功');
  emits('ok');
  emits('update:visible', false);
};

onMounted(async () => {
  // origin.value = await site.getUrl('at');
  watch(() => props.visible, async (newValue) => {
    if (newValue) {
      if (props.designId) {
        loadData(props.designId);
      } else {
        formState.value = {
          name: undefined,
          openapiSpecVersion: '3.0.1'
        };
        selectApis.value = [];
        selectApiId.value = undefined;
      }
    }
  }, { immediate: true });
});

const versionOpt = ['3.0.0', '3.0.1', '3.0.2', '3.0.3', '3.1.0'].map(i => ({ value: i, label: i }));

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
          :placeholder="t('design.editModal.namePlaceholder')" />
      </FormItem>
      <FormItem
        :label="t('design.editModal.versionLabel')"
        name="openapiSpecVersion"
        required>
        <div class="flex items-center space-x-2">
          <Select
            v-model:value="formState.openapiSpecVersion"
            class="flex-1"
            :options="versionOpt" />
          <Hints :text="t('design.editModal.versionPlaceholder')" />
        </div>
      </FormItem>
    </Form>
  </Modal>
</template>
