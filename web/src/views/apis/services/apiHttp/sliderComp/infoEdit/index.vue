<script lang="ts" setup>
import { computed, inject, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  Hints,
  Icon,
  IconCopy,
  Input,
  notification,
  Select,
  SelectUser,
  Tooltip,
  TreeSelect,
  IconText
  , VuexHelper
} from '@xcan-angus/vue-ui';
import { apis, services } from '@/api/tester';
import { TESTER, appContext } from '@xcan-angus/infra';
import { Button, Form, FormItem } from 'ant-design-vue';
import SelectEnum from '@/components/SelectEnum/index.vue'

interface Props {
  disabled:boolean
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  disabled: false
});

const { useMutations, useState } = VuexHelper;
const { stepVisible, stepKey, stepContent } = useState(['stepVisible', 'stepKey', 'stepContent'], 'guideStore');
const { updateGuideStep } = useMutations(['updateGuideStep'], 'guideStore');
// 更新左侧未归档列表
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshUnarchived = inject('refreshUnarchived', () => {});

// eslint-disable-next-line @typescript-eslint/no-empty-function
const handleCloseDrawer = inject('selectHandle', () => {});
const isUnarchivedApi = inject('isUnarchivedApi', { value: false }); // 当前 api 是否为未存档
const setApiInfo = inject('setApiInfo', (info) => (info));
const userInfo = ref(appContext.getUser());
const projectInfo = inject('projectInfo', ref({ id: '' }));
// eslint-disable-next-line @typescript-eslint/no-empty-function
const getParameter = inject('getParameter', () => ({} as any));
const globalConfigs = inject('globalConfigs', { VITE_API_SUMMARY_MAX_LENGTH: 400, VITE_API_CODE_MAX_LENGTH: 400, VITE_API_DESC_MAX_LENGTH: 20000 });

const isLoading = ref(false);
const tagsOpt = ref<{value: string}[]>([]);
const ownerOpt = ref();
const defaultProject = ref();

const state = reactive({
  id: inject('id', ''),
  treeDataList: []
});

const form = reactive({
  summary: '',
  operationId: '',
  ownerId: '',
  ownerName: '',
  serviceId: undefined,
  serviceName: '',
  description: '',
  assertions: [],
  authentication: {},
  requestBody: {
    apiContentRawType: '',
    apiContentType: '',
    formParam: [],
    rawContent: ''
  },
  host: '',
  method: '',
  protocol: '',
  requestHeaders: [],
  requestParams: [],
  response: [],
  status: 'UNKNOWN',
  tags: [],
  deprecated: false,
  externalDocs: {
    url: '',
    description: ''
  }
});

const disabled = computed(() => {
  return props.disabled || form.status === 'RELEASED';
});

const loadInfo = async () => {
  const [error, res] = isUnarchivedApi.value
    ? await apis.getUnarchivedApiDetail(state.id)
    : await apis.getApiDetail(state.id);
  if (error) {
    return;
  }
  Object.keys(res.data).forEach(key => {
    if (key === 'status') {
      form[key] = res.data[key]?.value || 'UNKNOWN';
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
    form.ownerId = userInfo.value.id;
    form.ownerName = userInfo.value.fullName;
  }
  form.tags = form.tags || undefined;
  ownerOpt.value = [{ fullName: form.ownerName, id: form.ownerId }];
  defaultProject.value = { id: form.serviceId, name: form.serviceName };
};

watch(() => state.id, async () => {
  if (state.id) {
    loadInfo();
  } else {
    const formParams = await getParameter();
    if (formParams.serviceId) {
      form.serviceId = formParams.serviceId;
      form.serviceName = formParams.serviceName;
      defaultProject.value = { id: form.serviceId, name: form.serviceName };
    }
    if (!form.ownerId && userInfo.value?.id) {
      form.ownerId = userInfo.value.id;
      form.ownerName = userInfo.value.fullName;
    }
  }
}, { immediate: true });

const rules = {
  summary: [{
    required: true, message: t('service.apiSliderSave.validation.summaryRequired'), trigger: 'blur'
  }],
  ownerId: [{
    required: true, message: t('service.apiSliderSave.validation.ownerRequired'), trigger: 'change'
  }],
  serviceId: [{
    required: true, message: t('service.apiSliderSave.validation.serviceRequired'), trigger: 'change'
  }],
  status: [{
    required: true, message: t('service.apiSliderSave.validation.statusRequired'), trigger: 'change'
  }]
};

const handleProjectChange = (value, name) => {
  form.serviceId = value;
  form.serviceName = name?.[0];
};

const loadTagfromProject = async () => {
  if (!form.serviceId || tagsOpt.value.length) {
    return;
  }
  const [error, resp] = await services.getTags(form.serviceId);
  if (error) {
    return;
  }
  tagsOpt.value = (resp.data || []).map(i => ({ value: i.name, label: i.name }));
};

const formRef = ref();

const save = async () => {
  formRef.value.validate().then(async () => {
    const formParams = await getParameter();
    delete formParams.id;
    const { ownerId, summary, operationId, serviceId, description, tags, status, deprecated, externalDocs } = form;
    const params = { ...formParams, ownerId, summary, operationId, serviceId, description, tags, status, deprecated, externalDocs };
    isLoading.value = true;
    const [error, res] = isUnarchivedApi.value && state.id
      ? await apis.addApi([{ ...params, unarchivedId: state.id }])
      : isUnarchivedApi.value && !state.id
        ? await apis.putApi([{ ...params }])
        : await apis.updateApi([{ ...formParams, externalDocs, ownerId, summary, operationId, serviceId, description, tags, status, deprecated, id: state.id }]);
    isLoading.value = false;
    if (error) {
      return;
    }
    setApiInfo({
      id: res.data?.[0].id || state.id,
      name: summary,
      ownerId,
      serviceId
    });
    if (isUnarchivedApi.value) {
      refreshUnarchived();
    }
    notification.success(t('tips.saveSuccess'));
    handleClose();
  });
};

// 取消保存
const handleClose = () => {
  handleCloseDrawer();
};

const scrollDivRef = ref(null);
const guideStep = (key:string) => {
  if (key === 'debugApiSeven') {
    scrollDivRef.value.scrollTop = scrollDivRef.value.scrollHeight;
  }
  updateGuideStep({ visible: true, key });

  if (key === 'hideProject') {
    save();
  }
};

onMounted(() => {
  if (stepKey.value === 'debugApiSix' && stepVisible.value) {
    form.summary = t('service.apiSliderSave.debug.example');
  }
});

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
          @click="save">
          {{ t('actions.save') }}
        </Button>
        <Button size="small" @click="handleClose">
          {{ t('actions.cancel') }}
        </Button>
        <Hints
          :text="t('service.apiSliderSave.tips.duplicateApi')"
          class="mb-2 mt-2" />
        <div v-if="state.id" class="mb-2 flex items-center space-x-2"><span>ID:  {{ state.id }}</span> <IconCopy :copyText="state.id" /></div>
        <p v-if="form.status === 'RELEASED'" class="text-3 text-status-orange mt-1">{{ t('service.apiSliderSave.tips.releasedApiNotEditable') }}</p>
      </FormItem>
              <FormItem :label="t('service.apiSliderSave.labels.summary')" name="summary">
        <Tooltip
          :visible="stepVisible && stepKey === 'debugApiSix'"
          :overlayStyle="{'min-width': '240px'}"
          placement="leftTop"
          destroyTooltipOnHide>
          <template #title>
            <div class="p-2 text-3">
              <div class="text-4 text-text-title">{{ stepContent.title }}</div>
              <div class="mt-2">{{ stepContent.content }}</div>
              <div class="flex justify-end mt-5">
                <Button
                  size="small"
                  type="primary"
                  @click="guideStep('debugApiSeven')">
                  {{ t('service.apiSliderSave.actions.nextStep') }}
                </Button>
              </div>
            </div>
          </template>
          <Input
            v-model:value="form.summary"
            :maxlength="globalConfigs.VITE_API_SUMMARY_MAX_LENGTH"
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
          :maxlength="globalConfigs.VITE_API_CODE_MAX_LENGTH"
          :disabled="disabled"
          :allowClear="false"
          dataType="mixin-en"
          includes=":_-."
          class="rounded"
          size="small"
          :placeholder="t('service.apiSliderSave.form.operationIdPlaceholder')" />
      </FormItem>
              <FormItem :label="t('service.apiSliderSave.labels.owner')" name="ownerId">
        <SelectUser
          v-model:value="form.ownerId"
          class="rounded-border"
          :options="ownerOpt"
          :disabled="disabled"
          size="small"
          :placeholder="t('service.apiSliderSave.form.ownerPlaceholder')"
          :allowClear="false" />
      </FormItem>
      <FormItem :label="t('service.apiSliderSave.labels.service')" name="serviceId">
        <TreeSelect
          v-model:defaultValue="defaultProject"
          :action="`${TESTER}/services?projectId=${projectInfo.id}&hasPermission=ADD&fullTextSearch=true`"
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
            <span>{{ t('service.apiSliderSave.labels.tags') }}</span>
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
          :options="tagsOpt"
          @dropdownVisibleChange="loadTagfromProject">
        </Select>
      </FormItem>
      <FormItem :label="t('service.apiSliderSave.labels.status')" name="status">
        <SelectEnum
          v-model:value="form.status"
          :disabled="disabled"
          :placeholder="t('service.apiSliderSave.form.statusPlaceholder')"
          enumKey="ApiStatus">
        </SelectEnum>
      </FormItem>
      <FormItem :label="t('service.apiSliderSave.labels.deprecated')" name="deprecated">
        <Select
          v-model:value="form.deprecated"
          :disabled="disabled"
          :options="[{label: t('service.apiSliderSave.options.normal'), value: false}, {label: t('service.apiSliderSave.options.deprecated'), value: true}]">
        </Select>
      </FormItem>
      <FormItem :label="t('service.apiSliderSave.labels.description')" name="externalDocs">
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
          v-model:value="form.externalDocs.url"
          :placeholder="t('service.apiSliderSave.form.externalDocsUrlPlaceholder')"
          :disabled="disabled"
          :maxLength="100" />
      </FormItem>
      <FormItem class="-mt-1">
        <Input
          v-model:value="form.externalDocs.description"
          :placeholder="t('service.apiSliderSave.form.externalDocsDescPlaceholder')"
          :disabled="disabled"
          :maxLength="200"
          type="textarea" />
      </FormItem>
    </Form>
  </div>
</template>
