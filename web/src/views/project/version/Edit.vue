<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { DatePicker, Input, Modal } from '@xcan-angus/vue-ui';
import { Form, FormItem, Textarea } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { software } from '@/api/tester';

interface Props {
  visible: boolean;
  versionId?: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  versionId: undefined,
  projectId: ''
});

const { t } = useI18n();

const emits = defineEmits<{(e: 'cancel'):void; (e: 'ok'):void; (e: 'update:visible', value: boolean):void}>();

const formState = ref({
  name: undefined,
  startDate: undefined,
  releaseDate: undefined,
  description: undefined
});

const loading = ref(false);
const formRef = ref();

const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await software.getSoftwareVersionDetail(id);

  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data || {};
  if (!data) {
    return;
  }
  const { name, startDate, releaseDate, description } = data;
  formState.value = {
    name, startDate, releaseDate, description
  };
};

const cancel = () => {
  emits('update:visible', false);
  emits('cancel');
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    if (!props.versionId) {
      await addOk();
    } else {
      await editOk();
    }
  });
};

const addOk = async () => {
  loading.value = true;
  const [error] = await software.addSoftwareVersion({
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
  const [error] = await software.updateSoftwareVersion({
    ...formState.value,
    id: props.versionId
  });
  loading.value = false;
  if (error) {
    return;
  }
  emits('ok');
  emits('update:visible', false);
};

onMounted(async () => {
  watch(() => props.visible, async (newValue) => {
    if (newValue) {
      if (props.versionId) {
        loadData(props.versionId);
      } else {
        formState.value = {
          name: undefined,
          startDate: undefined,
          releaseDate: undefined,
          description: undefined
        };
      }
    }
  }, { immediate: true });
});

</script>
<template>
  <Modal
    :title="props.versionId ? t('version.form.editVersion') : t('version.form.addVersion')"
    :visible="props.visible"
    :width="550"
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
        :label="t('version.form.versionName')">
        <Input
          v-model:value="formState.name"
          :maxlength="40"
          :placeholder="t('version.form.versionNamePlaceholder')" />
      </FormItem>
      <div class="flex space-x-2">
        <FormItem
          :label="t('version.form.startDate')"
          class="flex-1 min-w-0"
          name="date">
          <div class="flex items-center space-x-1">
            <DatePicker
              v-model:value="formState.startDate"
              showToday
              showTime
              class="flex-1 min-w-0" />
          </div>
        </FormItem>
        <FormItem
          :label="t('version.form.releaseDate')"
          class="flex-1 min-w-0"
          name="time">
          <div class="w-full flex items-center space-x-1">
            <DatePicker
              v-model:value="formState.releaseDate"
              showTime
              class="flex-1" />
          </div>
        </FormItem>
      </div>
      <FormItem
        :label="t('version.form.description')"
        class="flex-1 !mb-5"
        name="content">
        <Textarea
          v-model:value="formState.description"
          :maxlength="200"
          :placeholder="t('version.form.descriptionPlaceholder')">
          </Textarea>
      </FormItem>
    </Form>
  </Modal>
</template>
