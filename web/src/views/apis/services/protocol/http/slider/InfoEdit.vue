<script lang="ts" setup>
import { computed, inject, onMounted, reactive, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  Hints,
  Icon,
  IconCopy,
  IconText,
  Input,
  notification,
  Select,
  SelectUser,
  Tooltip,
  TreeSelect,
  VuexHelper
} from '@xcan-angus/vue-ui';
import { apis, services } from '@/api/tester';
import { appContext, TESTER } from '@xcan-angus/infra';
import { Button, Form, FormItem } from 'ant-design-vue';
import { ApiStatus } from '@/enums/enums';
import { ApisFormEdit } from '@/views/apis/services/protocol/types';
import { API_SUMMARY_MAX_LENGTH, API_OPERATION_ID_MAX_LENGTH, API_DESC_MAX_LENGTH } from '@/utils/constant';

import SelectEnum from '@/components/enum/SelectEnum.vue';

/**
 * Props interface for InfoEdit component
 */
interface Props {
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false
});

const { t } = useI18n();

// Vuex store helpers for guide functionality
const { useMutations, useState } = VuexHelper;
const { stepVisible, stepKey, stepContent } = useState(['stepVisible', 'stepKey', 'stepContent'], 'guideStore');
const { updateGuideStep } = useMutations(['updateGuideStep'], 'guideStore');

// Injected dependencies for parent component communication
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshUnarchived = inject('refreshUnarchived', () => {});

// eslint-disable-next-line @typescript-eslint/no-empty-function
const handleCloseDrawer = inject('selectHandle', () => {});
const isUnarchivedApi = inject('isUnarchivedApi', { value: false }); // Current API is unarchived
const setApiInfo = inject('setApiInfo', (info) => (info));
const userInfo = ref(appContext.getUser());

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
// eslint-disable-next-line @typescript-eslint/no-empty-function
const getParameter = inject('getParameter', () => ({} as any));

// Component state
const isLoading = ref(false);
const tagsOptions = ref<{value: string}[]>([]);
const ownerOptions = ref();
const defaultProject = ref();

const state = reactive({
  id: inject('id', ''),
  treeDataList: [] // Unused - can be removed
});

/**
 * Form data for API information editing
 */
const form = reactive<ApisFormEdit>({
  summary: '',
  operationId: '',
  method: undefined,
  protocol: undefined,
  status: ApiStatus.UNKNOWN,
  ownerId: '',
  ownerName: '',
  serviceId: undefined,
  serviceName: '',
  description: '',
  assertions: [],
  authentication: undefined,
  requestBody: undefined,
  responses: undefined,
  tags: [],
  deprecated: false,
  externalDocs: undefined
});

/**
 * Computed property to determine if form should be disabled
 * <p>Form is disabled when props.disabled is true or API status is RELEASED</p>
 */
const disabled = computed(() => {
  return props.disabled || form.status === ApiStatus.RELEASED;
});

/**
 * Loads API information from server
 * <p>Fetches API details based on whether it's archived or unarchived</p>
 * <p>Populates form with fetched data and sets default values</p>
 */
const loadApiInfo = async () => {
  const [error, res] = isUnarchivedApi.value
    ? await apis.getUnarchivedApiDetail(state.id)
    : await apis.getApiDetail(state.id);
  if (error) {
    return;
  }

  // Map response data to form fields
  Object.keys(res.data).forEach(key => {
    if (key === 'status') {
      form[key] = res.data[key]?.value || ApiStatus.UNKNOWN;
    } else {
      form[key] = res.data[key];
    }
  });

  // Process assertions array
  form.assertions = res.data?.assertions?.map(item => ({
    ...item,
    condition: item.condition?.value,
    type: item.type?.value
  })) || [];

  // Set default owner if not present
  if (!form.ownerId && userInfo.value?.id) {
    form.ownerId = userInfo.value.id.toString();
    form.ownerName = userInfo.value.fullName;
  }

  form.tags = form.tags || undefined;
  ownerOptions.value = [{ fullName: form.ownerName, id: form.ownerId }];
  defaultProject.value = { id: form.serviceId, name: form.serviceName };
};

/**
 * Handles project/service selection change
 * <p>Updates form with selected service ID and name</p>
 */
const handleProjectChange = (value: string, name: string[]) => {
  form.serviceId = value;
  form.serviceName = name?.[0];
};

/**
 * Loads available tags from the selected service
 * <p>Fetches tags only if service is selected and tags haven't been loaded yet</p>
 */
const loadTagsFromService = async () => {
  if (!form.serviceId || tagsOptions.value.length) {
    return;
  }
  const [error, resp] = await services.getTags(form.serviceId);
  if (error) {
    return;
  }
  tagsOptions.value = (resp.data || []).map(i => ({ value: i.name, label: i.name }));
};

const formRef = ref();

/**
 * Saves the API information
 * <p>Validates form, prepares parameters, and calls appropriate API endpoint</p>
 * <p>Handles both archived and unarchived API updates</p>
 */
const saveApiInfo = async () => {
  formRef.value.validate().then(async () => {
    const formParams = await getParameter();
    delete formParams.id;
    const { ownerId, summary, operationId, serviceId, description, tags, status, deprecated, externalDocs } = form;
    const params = { ...formParams, ownerId, summary, operationId, serviceId, description, tags, status, deprecated, externalDocs };
    isLoading.value = true;

    // Determine which API endpoint to call based on API state
    const [error, res] = isUnarchivedApi.value && state.id
      ? await apis.addApi([{ ...params, unarchivedId: state.id }])
      : isUnarchivedApi.value && !state.id
        ? await apis.putApi([{ ...params }])
        : await apis.updateApi([{ ...formParams, externalDocs, ownerId, summary, operationId, serviceId, description, tags, status, deprecated, id: state.id }]);

    isLoading.value = false;
    if (error) {
      return;
    }

    // Update parent component with new API info
    setApiInfo({
      id: res.data?.[0].id || state.id,
      name: summary,
      ownerId,
      serviceId
    });

    // Refresh unarchived list if needed
    if (isUnarchivedApi.value) {
      refreshUnarchived();
    }

    notification.success(t('actions.tips.saveSuccess'));
    handleClose();
  });
};

/**
 * Closes the drawer and cancels editing
 */
const handleClose = () => {
  handleCloseDrawer();
};

const scrollDivRef = ref<HTMLElement | null>(null);

/**
 * Handles guide step navigation
 * <p>Manages user guide flow and performs specific actions for certain steps</p>
 */
const handleGuideStep = (key: string) => {
  if (key === 'debugApiSeven' && scrollDivRef.value) {
    scrollDivRef.value.scrollTop = scrollDivRef.value.scrollHeight;
  }
  updateGuideStep({ visible: true, key });

  if (key === 'hideProject') {
    saveApiInfo();
  }
};

onMounted(() => {
  // Set example text for guide step
  if (stepKey.value === 'debugApiSix' && stepVisible.value) {
    form.summary = t('service.apiSliderSave.debug.example');
  }
});

/**
 * Watches for changes in API ID
 * <p>Loads API info when ID is present, or initializes form with default values</p>
 */
watch(() => state.id, async () => {
  if (state.id) {
    await loadApiInfo();
  } else {
    // Initialize form with default values for new API
    const formParams = await getParameter();
    if (formParams.serviceId) {
      form.serviceId = formParams.serviceId;
      form.serviceName = formParams.serviceName;
      defaultProject.value = { id: form.serviceId, name: form.serviceName };
    }
    if (!form.ownerId && userInfo.value?.id) {
      form.ownerId = userInfo.value.id.toString();
      form.ownerName = userInfo.value.fullName;
    }
  }
}, { immediate: true });

/**
 * Form validation rules
 */
const rules = {
  summary: [{
    required: true,
    message: t('service.apiSliderSave.validation.summaryRequired'),
    trigger: 'blur'
  }] as any,
  ownerId: [{
    required: true,
    message: t('service.apiSliderSave.validation.ownerRequired'),
    trigger: 'change'
  }] as any,
  serviceId: [{
    required: true,
    message: t('service.apiSliderSave.validation.serviceRequired'),
    trigger: 'change'
  }] as any,
  status: [{
    required: true,
    message: t('service.apiSliderSave.validation.statusRequired'),
    trigger: 'change'
  }] as any
};
</script>

<template>
  <div ref="scrollDivRef" class="h-full py-3 overflow-y-auto">
    <Form
      ref="formRef"
      layout="vertical"
      :model="form"
      :rules="rules">
      <FormItem>
        <Button
          type="primary"
          class="mr-2"
          :loading="isLoading"
          :disabled="disabled"
          size="small"
          @click="saveApiInfo">
          {{ t('actions.save') }}
        </Button>
        <Button size="small" @click="handleClose">
          {{ t('actions.cancel') }}
        </Button>
        <Hints
          :text="t('service.apiSliderSave.tips.duplicateApi')"
          class="mb-2 mt-2" />
        <div v-if="state.id" class="mb-2 flex items-center space-x-2">
          <span>ID:  {{ state.id }}</span> <IconCopy :copyText="state.id" />
        </div>
        <p v-if="form.status === 'RELEASED'" class="text-3 text-status-orange mt-1">
          {{ t('service.apiSliderSave.tips.releasedApiNotEditable') }}
        </p>
      </FormItem>
      <FormItem :label="t('service.apiSliderSave.labels.summary')" name="summary">
        <Tooltip
          :visible="stepVisible && stepKey === 'debugApiSix'"
          :overlayStyle="{'min-width': '240px'}"
          placement="leftTop"
          destroyTooltipOnHide>
          <template #title>
            <div class="p-2 text-3">
              <div class="text-4 text-text-title">{{ (stepContent as any)?.title }}</div>
              <div class="mt-2">{{ (stepContent as any)?.content }}</div>
              <div class="flex justify-end mt-5">
                <Button
                  size="small"
                  type="primary"
                  @click="handleGuideStep('debugApiSeven')">
                  {{ t('actions.nextStep') }}
                </Button>
              </div>
            </div>
          </template>
          <Input
            v-model:value="form.summary"
            :maxlength="API_SUMMARY_MAX_LENGTH"
            :disabled="disabled"
            :allowClear="false"
            class="rounded"
            size="small"
            :placeholder="t('service.apiSliderSave.form.summaryPlaceholder')" />
        </Tooltip>
      </FormItem>
      <FormItem :label="t('service.apiSliderSave.labels.operationId')">
        <Input
          v-model:value="form.operationId"
          :maxlength="API_OPERATION_ID_MAX_LENGTH"
          :disabled="disabled"
          :allowClear="false"
          dataType="mixin-en"
          includes=":_-."
          class="rounded"
          size="small"
          :placeholder="t('service.apiSliderSave.form.operationIdPlaceholder')" />
      </FormItem>
      <FormItem :label="t('common.owner')" name="ownerId">
        <SelectUser
          v-model:value="form.ownerId"
          class="rounded-border"
          :options="ownerOptions"
          :disabled="disabled"
          size="small"
          :placeholder="t('common.placeholders.selectOwner')"
          :allowClear="false" />
      </FormItem>
      <FormItem :label="t('common.service')" name="serviceId">
        <TreeSelect
          v-model:defaultValue="defaultProject"
          :action="`${TESTER}/services?projectId=${projectId}&hasPermission=ADD&fullTextSearch=true`"
          :allowClear="false"
          :disabled="disabled || !isUnarchivedApi"
          :fieldNames="{children:'children', label:'name', value: 'id'}"
          :placeholder="t('service.apiSliderSave.form.servicePlaceholder')"
          :virtual="false"
          size="small"
          @change="handleProjectChange">
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
              <template #title>{{ t('service.apiSliderSave.tips.tagsDescription') }}</template>
            </Tooltip>
          </div>
        </template>
        <Select
          v-model:value="form.tags"
          mode="tags"
          :placeholder="t('service.apiSliderSave.form.tagsPlaceholder')"
          :disabled="disabled"
          :options="tagsOptions"
          @dropdownVisibleChange="loadTagsFromService">
        </Select>
      </FormItem>
      <FormItem :label="t('common.status')" name="status">
        <SelectEnum
          v-model:value="form.status"
          :disabled="disabled"
          :placeholder="t('service.apiSliderSave.form.statusPlaceholder')"
          enumKey="ApiStatus">
        </SelectEnum>
      </FormItem>
      <FormItem :label="t('service.apiSliderSave.labels.deprecated')" name="deprecated">
        <Select
          :value="form.deprecated ? 'true' : 'false'"
          :disabled="disabled"
          :options="[
            {label: t('service.apiSliderSave.options.normal'), value: 'false'},
            {label: t('service.apiSliderSave.options.deprecated'), value: 'true'}
          ]"
          @change="(value) => form.deprecated = value === 'true'">
        </Select>
      </FormItem>
      <FormItem :label="t('common.description')" name="externalDocs">
        <Input
          v-model:value="form.description"
          type="textarea"
          showCount
          :autoSize="{ minRows: 5, maxRows: 5 }"
          :disabled="disabled"
          :allowClear="false"
          :maxlength="API_DESC_MAX_LENGTH"
          class="rounded-border"
          size="small"
          :placeholder="t('service.apiSliderSave.form.descriptionPlaceholder')" />
      </FormItem>
      <FormItem>
        <template #label>
          <div>
            {{ t('service.apiSliderSave.labels.externalDocs') }}
            <Tooltip placement="left">
              <Icon icon="icon-tishi1" class="text-blue-tips ml-0.5 text-3.5" />
              <template #title>{{ t('service.apiSliderSave.tips.externalDocsDescription') }}</template>
            </Tooltip>
          </div>
        </template>
        <Input
          :value="form.externalDocs?.url"
          :placeholder="t('service.apiSliderSave.form.externalDocsUrlPlaceholder')"
          :disabled="disabled"
          :maxLength="100" />
      </FormItem>
      <FormItem class="-mt-1">
        <Input
          :value="form.externalDocs?.description"
          :placeholder="t('service.apiSliderSave.form.externalDocsDescPlaceholder')"
          :disabled="disabled"
          :maxLength="200"
          type="textarea" />
      </FormItem>
    </Form>
  </div>
</template>
