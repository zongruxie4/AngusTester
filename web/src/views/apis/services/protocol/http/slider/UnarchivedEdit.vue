<script lang="ts" setup>
import { computed, inject, reactive, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints, Input, notification } from '@xcan-angus/vue-ui';
import { apis } from '@/api/tester';
import { Button, Form, FormItem } from 'ant-design-vue';
import { appContext } from '@xcan-angus/infra';
import { ApisFormEdit } from '@/views/apis/services/protocol/types';
import { ApiStatus } from '@/enums/enums';

interface Props {
  disabled:boolean
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  disabled: false
});

// 更新左侧未归档列表
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshUnarchived = inject('refreshUnarchived', () => {});
const projectId = inject<Ref<string>>('projectId', ref(''));

const closeFunCallBack = (_params: unknown) => ({ _params });
const isLoading = ref(false); // 提交中。。。

const state = reactive({
  id: '',
  treeDataList: []
});

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

const disabled = computed(() => {
  return props.disabled;
});

type GetParameter = ()=>Record<string, any>
const getParameter = inject('getParameter') as GetParameter;
const globalConfigs = inject('globalConfigs', { VITE_API_SUMMARY_MAX_LENGTH: 400, VITE_API_CODE_MAX_LENGTH: 400, VITE_API_DESC_MAX_LENGTH: 20000 });
// const tenantInfo = inject('tenantInfo', ref());

state.id = inject('id', ''); // 当前 api id;
const handleCloseDrawer = inject('selectHandle', closeFunCallBack);
const isUnarchivedApi = inject('isUnarchivedApi', { value: false }); // 当前 api 是否为未存档
const setUnarchivedApiInfo = inject('setUnarchivedApiInfo', (info) => (info));
const userInfo = ref(appContext.getUser());

const loadInfo = async () => {
  const [error, res] = await apis.getUnarchivedApiDetail(state.id);
  if (error) {
    return;
  }
  Object.keys(res.data).forEach(key => {
    if (key === 'status') {
      form[key] = res.data[key]?.value || ApiStatus.UNKNOWN;
    } else {
      form[key] = res.data[key];
    }
  });
  form.assertions = res.data?.assertions?.map(item => ({
    ...item,
    condition: item.condition?.value,
    type: item.type?.value
  })) || [];
  if (!form.ownerId && userInfo.value?.id) {
    form.ownerId = userInfo.value.id.toString();
    form.ownerName = userInfo.value.fullName;
  }
};

const rules = {
  summary: [{
    required: true, message: t('service.apiUnarchivedrSave.validation.summaryRequired'), trigger: 'blur'
  }]
};

const formRef = ref();

const save = async () => {
  formRef.value.validate().then(async () => {
    const formParams = await getParameter();
    const { summary, description } = form;
    const params = { ...formParams, summary, description, projectId: projectId.value };
    isLoading.value = true;
    const [error, res] = isUnarchivedApi.value && state.id
      ? await apis.updateUnarchivedApi({ dto: [{ ...params }] })
      : await apis.addUnarchivedApi({ dto: [{ ...params }] });
    isLoading.value = false;
    if (error) {
      return;
    }
    if (!state.id) {
      refreshUnarchived();
    }
    setUnarchivedApiInfo({
      id: res.data?.[0].id || state.id,
      name: summary
    });
    notification.success(t('actions.tips.saveSuccess'));
    handleClose();
  });
};

// 取消保存
const handleClose = () => {
  handleCloseDrawer(null);
};

watch(() => state.id, async () => {
  if (state.id) {
    loadInfo();
  }
}, {
  deep: true,
  immediate: true
});
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
          :maxlength="globalConfigs.VITE_API_SUMMARY_MAX_LENGTH"
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
          :maxlength="globalConfigs.VITE_API_DESC_MAX_LENGTH"
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
          @click="save">
          {{ t('actions.save') }}
        </Button>
        <Button size="small" @click="handleClose">
          {{ t('actions.cancel') }}
        </Button>
      </FormItem>
    </Form>
  </div>
</template>
