<script lang="ts" setup>
import { inject, onMounted, reactive, ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Form, FormItem } from 'ant-design-vue';
import { Hints, Input, notification } from '@xcan-angus/vue-ui';
import { ApisProtocol } from '@/enums/enums';
import { apis } from '@/api/tester';
import { Props } from '@/views/apis/services/protocol/websocket/types';

const props = withDefaults(defineProps<Props>(), {
  getParameter: () => ({})
});

const emits = defineEmits<{(e: 'ok'): void}>();

const replaceTabPane = inject<(key: string, data: any) => void>('replaceTabPane', () => { });
const closeDialog = inject('close', () => ({}));
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });
const refreshUnarchivedList = inject('refreshUnarchived', () => {});

/**
 * Injected project information
 */
const projectId = inject<Ref<string>>('projectId', ref(''));

const { t } = useI18n();

const saveForm = reactive({
  summary: '',
  description: ''
});

const formRef = ref();

const isSaving = ref(false);

/**
 * Handles saving unarchived WebSocket API
 * <p>
 * Validates form data, checks protocol compatibility, and saves the API to unarchived list
 * </p>
 */
const handleSaveUnarchivedApi = () => {
  formRef.value.validate().then(async () => {
    isSaving.value = true;

    try {
      const formData = saveForm;
      const apiInfo = await props.packageParams();

      // Validate WebSocket protocol
      if (apiInfo.protocol !== ApisProtocol.ws && apiInfo.protocol !== ApisProtocol.wss) {
        notification.warning(t('service.webSocketSaveUnarchived.messages.protocolWarning'));
        return;
      }

      const { currentServer, method, parameters, protocol, requestBody, endpoint } = apiInfo;
      const apiData = {
        currentServer,
        method,
        parameters,
        protocol,
        requestBody,
        endpoint,
        ...formData,
        projectId: projectId.value
      };

      const [error, response] = props.id
        ? await apis.updateUnarchivedApi({ dto: [{ ...apiData, id: props.id }] })
        : await apis.addUnarchivedApi({ dto: [apiData] });

      if (error) {
        console.error('Failed to save unarchived API:', error);
        return;
      }

      // Show success notification
      if (props.id) {
        notification.success(t('actions.tips.updateSuccess'));
      } else {
        notification.success(t('actions.tips.addSuccess'));
        refreshUnarchivedList();
      }

      // Update tab pane
      const apiId = props.id || response.data[0]?.id;
      const tabData = {
        _id: apiId + 'socket',
        name: formData.summary,
        id: apiId,
        unarchived: true,
        value: 'socket'
      };

      if (props.id) {
        updateTabPane({
          ...tabData,
          pid: props.tabKey
        });
      } else {
        replaceTabPane(props.tabKey, {
          ...tabData,
          pid: apiId + 'socket'
        });
      }

      handleCloseDialog();
      emits('ok');
    } catch (error) {
      console.error('Error saving unarchived API:', error);
    } finally {
      isSaving.value = false;
    }
  });
};

/**
 * Handles closing the dialog
 * <p>
 * Closes the current dialog/modal
 * </p>
 */
const handleCloseDialog = () => {
  closeDialog();
};

/**
 * Component mounted lifecycle hook
 * <p>
 * Initializes form data with props values
 * </p>
 */
onMounted(() => {
  saveForm.summary = props.summary;
  saveForm.description = props.description;
});

/**
 * Form validation rules
 */
const validationRules: any = {
  summary: [{
    required: true,
    message: t('service.webSocketSaveUnarchived.form.summary.validation'),
    trigger: 'blur'
  }]
};
</script>
<template>
  <Form
    ref="formRef"
    layout="vertical"
    :model="saveForm"
    :rules="validationRules">
    <Hints
      :text="t('service.webSocketSaveUnarchived.hints.unarchivedDescription')"
      class="mb-1.5" />
    <FormItem :label="t('service.webSocketSaveUnarchived.form.summary.label')" name="summary">
      <Input
        v-model:value="saveForm.summary"
        :maxlength="40"
        :allowClear="false"
        class="rounded"
        :placeholder="t('service.webSocketSaveUnarchived.form.summary.placeholder')" />
    </FormItem>
    <FormItem :label="t('service.webSocketSaveUnarchived.form.description.label')">
      <Input
        v-model:value="saveForm.description"
        type="textarea"
        showCount
        :rows="4"
        :allowClear="false"
        class="rounded-border"
        :placeholder="t('service.webSocketSaveUnarchived.form.description.placeholder')" />
    </FormItem>
    <FormItem>
      <Button
        type="primary"
        class="mr-2.5 rounded"
        size="small"
        :loading="isSaving"
        @click="handleSaveUnarchivedApi">
        {{ t('actions.save') }}
      </Button>
      <Button
        class="rounded"
        size="small"
        @click="handleCloseDialog">
        {{ t('actions.cancel') }}
      </Button>
    </FormItem>
  </Form>
</template>
