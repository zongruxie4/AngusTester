<script lang="ts" setup>
import { inject, onMounted, reactive, ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Form, FormItem } from 'ant-design-vue';
import { Input, notification, Select, SelectUser, TreeSelect, IconText } from '@xcan-angus/vue-ui';
import { TESTER, appContext } from '@xcan-angus/infra';
import { ApiStatus, ApiPermission, ApisProtocol } from '@/enums/enums';

import { apis, services } from '@/api/tester';
import { Props } from '@/views/apis/services/protocol/websocket/types';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';

const props = withDefaults(defineProps<Props>(), {
  getParameter: () => ({})
});

const emits = defineEmits<{(e: 'ok'): void}>();

const replaceTabPane = inject<(key: string, data: any) => void>('replaceTabPane', () => { });
const closeDialog = inject('close', () => ({}));
const userPermissions = inject('auths', ref<string[]>([]));

const isUnarchivedApi = inject('isUnarchived', { value: false });
const projectId = inject<Ref<string>>('projectId', ref(''));
const refreshUnarchivedList = inject('refreshUnarchived', () => {});
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

const currentUser = ref(appContext.getUser());
const defaultProjectSelection = ref();

const { t } = useI18n();

/**
 * Form data for saving WebSocket API
 */
const apiForm = reactive({
  summary: '',
  operationId: '',
  ownerId: '',
  serviceId: '',
  projectName: '',
  description: '',
  status: ApiStatus.UNKNOWN,
  deprecated: false,
  tags: []
});

const formRef = ref();

/**
 * Owner selection options
 */
const ownerOptions = ref([]);

/**
 * Loading state for save operation
 */
const isSaving = ref(false);

/**
 * Handles project selection change
 * <p>
 * Updates service ID and project name when project is selected
 * </p>
 * @param selectedValue - The selected service ID
 * @param selectedName - The selected project name array
 */
const handleProjectSelectionChange = (selectedValue: string, selectedName: string[]) => {
  apiForm.serviceId = selectedValue;
  apiForm.projectName = selectedName?.[0];
};

/**
 * Available tag options for the selected project
 */
const availableTagOptions = ref<{value: string, label: string}[]>([]);

/**
 * Loads tags from the selected project
 * <p>
 * Fetches available tags for the current service/project
 * </p>
 */
const loadTagsFromProject = async () => {
  if (!apiForm.serviceId || availableTagOptions.value.length) {
    return;
  }

  const [error, response] = await services.getTags(apiForm.serviceId);
  if (error) {
    console.warn('Failed to load tags from project:', error);
    return;
  }

  availableTagOptions.value = (response.data || []).map(tag => ({
    value: tag.name,
    label: tag.name
  }));
};

/**
 * Handles saving WebSocket API
 * <p>
 * Validates form data, checks protocol compatibility, and saves the API
 * </p>
 */
const handleSaveApi = () => {
  formRef.value.validate().then(async () => {
    isSaving.value = true;

    try {
      const formData = apiForm;
      const apiInfo = await props.packageParams();

      // Validate WebSocket protocol
      if (apiInfo.protocol !== ApisProtocol.ws && apiInfo.protocol !== ApisProtocol.wss) {
        notification.warning(t('service.webSocketSave.messages.protocolWarning'));
        return;
      }

      const apiData = { ...apiInfo, ...formData };
      let error, response;

      // Determine which API call to make based on context
      if (isUnarchivedApi.value && props.id) {
        [error, response] = await apis.addApi([{ ...apiData, unarchivedId: props.id }]);
      } else if (props.id) {
        [error, response] = await apis.updateApi([{ ...apiData, id: props.id }]);
      } else {
        [error, response] = await apis.putApi([apiData]);
      }

      if (error) {
        console.error('Failed to save API:', error);
        return;
      }

      // Show success notification
      if (isUnarchivedApi.value || !props.id) {
        notification.success(t('actions.tips.addSuccess'));
        refreshUnarchivedList();
      } else {
        notification.success(t('actions.tips.updateSuccess'));
      }

      // Update tab pane
      const apiId = props.id || response.data[0]?.id;
      const tabData = {
        _id: apiId + 'socket',
        name: formData.summary,
        id: apiId,
        unarchived: false,
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
      console.error('Error saving API:', error);
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
 * Initializes form data with props values and sets up default project selection
 * </p>
 */
onMounted(async () => {
  try {
    const apiInfo = await props.packageParams();

    // Initialize form data
    apiForm.summary = props.summary;
    apiForm.operationId = props.operationId;
    apiForm.ownerId = String(props.ownerId || currentUser.value?.id || '');
    apiForm.serviceId = apiInfo.serviceId || undefined;
    apiForm.projectName = apiInfo.projectName || undefined;
    apiForm.description = props.description;
    apiForm.status = props.status || ApiStatus.UNKNOWN;
    apiForm.deprecated = props.deprecated;

    // Set up default project selection
    defaultProjectSelection.value = {
      name: apiForm.projectName,
      id: apiForm.serviceId,
      targetType: apiInfo.projectType
    };
  } catch (error) {
    console.error('Error initializing form data:', error);
  }
});

/**
 * Form validation rules
 */
const validationRules: any = {
  summary: [{
    required: true,
    message: t('service.webSocketSave.form.summary.validation'),
    trigger: 'blur'
  }],
  ownerId: [{
    required: true,
    message: t('service.webSocketSave.form.ownerId.validation'),
    trigger: 'change'
  }],
  serviceId: [{
    required: true,
    message: t('service.webSocketSave.form.serviceId.validation'),
    trigger: 'change'
  }],
  status: [{
    required: true,
    message: t('service.webSocketSave.form.status.validation'),
    trigger: 'change'
  }]
};
</script>
<template>
  <Form
    ref="formRef"
    layout="vertical"
    :model="apiForm"
    :rules="validationRules">
    <FormItem :label="t('service.webSocketSave.form.summary.label')" name="summary">
      <Input
        v-model:value="apiForm.summary"
        :maxlength="40"
        :allowClear="false"
        :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
        class="rounded"
        :placeholder="t('service.webSocketSave.form.summary.placeholder')" />
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.operationId.label')">
      <Input
        v-model:value="apiForm.operationId"
        :maxlength="40"
        dataType="mixin-en"
        includes=":_-."
        :allowClear="false"
        :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
        class="rounded"
        :placeholder="t('service.webSocketSave.form.operationId.placeholder')" />
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.ownerId.label')" name="ownerId">
      <SelectUser
        v-model:value="apiForm.ownerId"
        class="rounded-border"
        :options="ownerOptions"
        :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
        :placeholder="t('service.webSocketSave.form.ownerId.placeholder')"
        :allowClear="false" />
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.serviceId.label')" name="serviceId">
      <TreeSelect
        v-model:defaultValue="defaultProjectSelection"
        :action="`${TESTER}/services?projectId=${projectId}&hasPermission=ADD&fullTextSearch=true`"
        :allowClear="false"
        :disabled="!userPermissions.includes(ApiPermission.MODIFY) || !isUnarchivedApi"
        :fieldNames="{children:'children', label:'name', value: 'id'}"
        :placeholder="t('service.webSocketSave.form.serviceId.placeholder')"
        :virtual="false"
        size="small"
        @change="handleProjectSelectionChange">
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
    <FormItem name="tags">
      <template #label>
        <div>
          <span>{{ t('common.tag') }}</span>
          <Tooltip placement="left">
            <Icon icon="icon-tishi1" class="text-blue-tips ml-0.5 text-3.5" />
            <template #title> {{ t('common.description') }} </template>
          </Tooltip>
        </div>
      </template>
      <Select
        v-model:value="apiForm.tags"
        mode="tags"
        :placeholder="t('service.webSocketSave.form.tags.placeholder')"
        :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
        :options="availableTagOptions"
        @dropdownVisibleChange="loadTagsFromProject">
      </Select>
    </FormItem>
    <FormItem :label="t('common.status')" name="status">
      <SelectEnum
        v-model:value="apiForm.status"
        :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
        enumKey="ApiStatus">
      </SelectEnum>
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.deprecated.label')" name="deprecated">
      <Select
        :value="apiForm.deprecated ? 'true' : 'false'"
        :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
        :options="[
          {label: t('service.webSocketSave.form.deprecated.options.normal'), value: 'false'},
          {label: t('service.webSocketSave.form.deprecated.options.deprecated'), value: 'true'}
        ]"
        @change="(value) => apiForm.deprecated = value === 'true'">
      </Select>
    </FormItem>
    <FormItem :label="t('common.description')">
      <Input
        v-model:value="apiForm.description"
        type="textarea"
        showCount
        :rows="4"
        :allowClear="false"
        :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
        class="rounded-border"
        :placeholder="t('service.webSocketSave.form.description.placeholder')" />
    </FormItem>
    <FormItem>
      <Button
        type="primary"
        class="mr-2.5 rounded"
        size="small"
        :loading="isSaving"
        :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
        @click="handleSaveApi">
        {{ t('actions.save') }}
      </Button>
      <Button
        class="rounded"
        size="small"
        @click="handleCloseDialog">
        {{ t('actions.cancel') }}
      </Button>
      <p v-if="apiForm.status === 'RELEASED'" class="text-3 text-status-orange mt-1">
        {{ t('service.webSocketSave.messages.releasedWarning') }}
      </p>
    </FormItem>
  </Form>
</template>
