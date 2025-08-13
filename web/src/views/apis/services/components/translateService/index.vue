<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { IconText, Modal, SelectEnum, TreeSelect, notification } from '@xcan-angus/vue-ui';
import { Form, FormItem, Button } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { services } from '@/api/tester';

const props = withDefaults(defineProps<{visible: boolean, service?: {id: string, name: string}, projectId: string}>(), {
  visible: false,
  service: undefined,
  projectId: ''
});

const { t } = useI18n();
const emits = defineEmits<{(e: 'update:visible', value: boolean)}>();
const formRef = ref();

const formData = ref<{
  serviceId?: string;
  sourceLanguage: 'zh_CN'| 'en',
  targetLanguage: 'zh_CN'| 'en',
}>({
  serviceId: undefined,
  sourceLanguage: 'zh_CN',
  targetLanguage: 'en'
});

const loading = ref(false);

const handleServiceId = (serviceId) => {
  formData.value.serviceId = serviceId;
};

const submit = () => {
  formRef.value.validate().then(async () => {
    const { serviceId, ...params } = formData.value;
    loading.value = true;
    const [error] = await services.translate(serviceId as string, { ...params });
    loading.value = false;
    if (error) {
      return;
    }
    notification.success(t('service.translateModal.messages.translateComplete'));
    emits('update:visible', false);
  });
};

const cancel = () => {
  emits('update:visible', false);
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (newValue) {
      loading.value = false;
      formData.value.serviceId = props.service?.id;
    }
  }, {
    immediate: true
  });
});
</script>
<template>
  <Modal
    :title="t('service.translateModal.title')"
    :visible="props.visible"
    :footer="false"
    :width="800"
    @cancel="cancel">
    <!-- 上方：OpenAPI翻译说明 -->
    <div>
      <div class="text-3.5 font-medium -mt-2">
        {{ t('service.translateModal.description.title') }}
      </div>

      <div class="grid-container leading-7 mt-2">
        <div class="bg-gray-bg p-2 rounded border">
          <div class="font-semibold">{{ t('service.translateModal.description.useCases.title') }}</div>
          <ul class="list-disc pl-4">
            <li>{{ t('service.translateModal.description.useCases.items.0') }}</li>
            <li>{{ t('service.translateModal.description.useCases.items.1') }}</li>
            <li>{{ t('service.translateModal.description.useCases.items.2') }}</li>
            <li>{{ t('service.translateModal.description.useCases.items.3') }}</li>
          </ul>
        </div>

        <div class="bg-gray-bg p-2 rounded border">
          <div class="font-semibold">{{ t('service.translateModal.description.notes.title') }}</div>
          <ul class="list-disc pl-4">
            <li>{{ t('service.translateModal.description.notes.items.0') }}</li>
            <li>{{ t('service.translateModal.description.notes.items.1') }}</li>
            <li>{{ t('service.translateModal.description.notes.items.2') }}</li>
            <li>{{ t('service.translateModal.description.notes.items.3') }}</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 下方：OpenAPI翻译配置 -->
    <div class="configuration-section mt-3">
      <div class="text-3.5 font-medium">{{ t('service.translateModal.configuration.title') }}</div>

      <Form
        ref="formRef"
        layout="vertical"
        class="mt-2 translate-form-wrap"
        :model="formData">
        <FormItem
          :label="t('service.translateModal.configuration.service')"
          name="serviceId"
          :rules="[{required: true, message: t('service.translateModal.messages.selectService')}]">
          <TreeSelect
            :defaultValue="props.service"
            :action="`${TESTER}/services?projectId=${props.projectId}&hasPermission=ADD&fullTextSearch=true`"
            :allowClear="false"
            :fieldNames="{children:'children', label:'name', value: 'id'}"
            :placeholder="t('service.translateModal.configuration.servicePlaceholder')"
            :virtual="false"
            size="small"
            @change="handleServiceId">
            <template #title="{name, targetType}">
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
        <div class="flex items-center space-x-2">
          <FormItem :label="t('service.translateModal.configuration.sourceLanguage')" class="flex-1 mb-0">
            <SelectEnum
              v-model:value="formData.sourceLanguage"
              enumKey="SupportedLanguage">
            </SelectEnum>
          </FormItem>
          <FormItem :label="t('service.translateModal.configuration.targetLanguage')" class="flex-1">
            <SelectEnum
              v-model:value="formData.targetLanguage"
              enumKey="SupportedLanguage">
            </SelectEnum>
          </FormItem>
        </div>
      </Form>
    </div>
    <div class="text-center space-x-3 mt-8">
      <Button
        @click="cancel">
        {{ t('actions.cancel') }}
      </Button>
      <Button
        type="primary"
        @click="submit">
        {{ t('actions.submit') }}
      </Button>
    </div>
  </Modal>
</template>
 <style scoped>
 .grid-container {
   display: grid;
   grid-template-columns: 1fr 1fr;
   gap: 16px;
 }
 :deep(.translate-form-wrap) .ant-form-item-label{
   font-weight: 600;
 }
 </style>
