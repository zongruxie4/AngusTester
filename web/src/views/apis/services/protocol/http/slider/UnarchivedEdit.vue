<script lang="ts" setup>
import { computed, inject, reactive, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints, Input, notification } from '@xcan-angus/vue-ui';
import { apis } from '@/api/tester';
import { Button, Form, FormItem } from 'ant-design-vue';
import { appContext } from '@xcan-angus/infra';
import { ApisFormEdit } from '@/views/apis/services/protocol/types';
import { ApiStatus } from '@/enums/enums';
import { API_SUMMARY_MAX_LENGTH, API_DESC_MAX_LENGTH } from '@/utils/constant';

interface Props {
  disabled: boolean;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  disabled: false
});

// Injected dependencies for parent component communication
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshUnarchived = inject('refreshUnarchived', () => {});
const projectId = inject<Ref<string>>('projectId', ref(''));

// Component state
const isLoading = ref(false); // Loading state for save operation

const state = reactive({
  id: '',
  treeDataList: [] // Unused - can be removed
});

/**
 * Form data for unarchived API editing
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
 */
const disabled = computed(() => {
  return props.disabled;
});

// Type definition for getParameter function
type GetParameter = () => Record<string, any>;

// Injected dependencies
const getParameter = inject('getParameter') as GetParameter;

state.id = inject('id', ''); // Current API ID
const handleCloseDrawer = inject('selectHandle', () => {});
const isUnarchivedApi = inject('isUnarchivedApi', { value: false }); // Current API is unarchived
const setUnarchivedApiInfo = inject('setUnarchivedApiInfo', (info) => (info));
const userInfo = ref(appContext.getUser());

/**
 * Loads unarchived API information from server
 * <p>Fetches API details and populates form with fetched data</p>
 */
const loadUnarchivedApiInfo = async () => {
  const [error, res] = await apis.getUnarchivedApiDetail(state.id);
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
};

const formRef = ref();

/**
 * Saves the unarchived API information
 * <p>Validates form, prepares parameters, and calls appropriate API endpoint</p>
 * <p>Handles both creating new and updating existing unarchived APIs</p>
 */
const saveUnarchivedApiInfo = async () => {
  formRef.value.validate().then(async () => {
    const formParams = await getParameter();
    const { summary, description } = form;
    const params = { ...formParams, summary, description, projectId: projectId.value };
    isLoading.value = true;

    // Determine which API endpoint to call based on whether API exists
    const [error, res] = isUnarchivedApi.value && state.id
      ? await apis.updateUnarchivedApi({ dto: [{ ...params }] })
      : await apis.addUnarchivedApi({ dto: [{ ...params }] });

    isLoading.value = false;
    if (error) {
      return;
    }

    // Refresh unarchived list for new APIs
    if (!state.id) {
      refreshUnarchived();
    }

    // Update parent component with new API info
    setUnarchivedApiInfo({
      id: res.data?.[0].id || state.id,
      name: summary
    });

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

/**
 * Watches for changes in API ID
 * <p>Loads API info when ID is present</p>
 */
watch(() => state.id, async () => {
  if (state.id) {
    await loadUnarchivedApiInfo();
  }
}, {
  deep: true,
  immediate: true
});

/**
 * Form validation rules
 */
const rules = {
  summary: [{
    required: true, message: t('service.apiUnarchivedrSave.validation.summaryRequired'), trigger: 'blur'
  }]
};
</script>
<template>
  <div class="py-3">
    <Hints
      :text="t('service.apiUnarchivedrSave.tips.unarchivedApiDescription')"
      class="mb-2" />
    <div v-if="state.id" class="mb-2">ID:  {{ state.id }}</div>
    <Form
      ref="formRef"
      layout="vertical"
      :model="form"
      :rules="rules">
      <FormItem :label="t('service.apiUnarchivedrSave.labels.summary')" name="summary">
        <Input
          v-model:value="form.summary"
          :maxlength="API_SUMMARY_MAX_LENGTH"
          :disabled="disabled"
          :allowClear="false"
          class="rounded"
          size="small"
          :placeholder="t('service.apiUnarchivedrSave.form.summaryPlaceholder')" />
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
          :placeholder="t('service.apiUnarchivedrSave.form.descriptionPlaceholder')" />
      </FormItem>
      <FormItem class="mt-5">
        <Button
          type="primary"
          class="mr-2"
          :loading="isLoading"
          :disabled="disabled"
          size="small"
          @click="saveUnarchivedApiInfo">
          {{ t('actions.save') }}
        </Button>
        <Button size="small" @click="handleClose">
          {{ t('actions.cancel') }}
        </Button>
      </FormItem>
    </Form>
  </div>
</template>
