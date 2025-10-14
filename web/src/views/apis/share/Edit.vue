<script lang="ts" setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { Colon, DatePicker, Hints, HttpMethodText, Icon, Input, Modal, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Checkbox, Form, FormItem, RadioGroup, Textarea } from 'ant-design-vue';
import { AppOrServiceRoute, DomainManager, EnumMessage, enumUtils, TESTER, toClipboard } from '@xcan-angus/infra';
import { ApisShareScope } from '@/enums/enums';
import { apis } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { ShareEditForm } from '@/views/apis/share/types';

const { t } = useI18n();

interface Props {
  visible: boolean;
  shareId?: string;
  projectId: string;
  servicesId?: string;
  apisIds?: string[];
  shareScope?: ApisShareScope;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  shareId: undefined,
  projectId: '',
  servicesId: undefined,
  apisIds: () => [],
  shareScope: ApisShareScope.SERVICES
});

const emits = defineEmits<{(e: 'cancel'):void; (e: 'ok'):void; (e: 'update:visible', value: boolean):void}>();

const origin = ref('');
const baseUrl = computed(() => {
  return `${origin.value}/apis/share`;
});

const formState = ref<ShareEditForm>({
  name: undefined,
  remark: undefined,
  expiredDate: undefined,
  displayOptions: { includeServiceInfo: true, allowDebug: false, schemaStyle: 'TABLE' },
  shareScope: ApisShareScope.SERVICES,
  servicesId: props.servicesId || undefined,
  apisIds: props.apisIds as string[]
});

const apisShareScope = ref<EnumMessage<ApisShareScope>[]>([]);
// load options for share scope selector
const loadApisShareScopeOpt = () => {
  apisShareScope.value = enumUtils.enumToMessages(ApisShareScope);
};

const loading = ref(false);
const formRef = ref();

// load detail for editing when shareId exists
const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await apis.getShareDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data || {};
  if (!data) {
    return;
  }
  const { name, remark, expiredDate, displayOptions, shareScope, servicesId, apisIds } = data;
  formState.value = {
    name, remark, expiredDate, displayOptions, shareScope: shareScope.value, servicesId, apisIds
  };
  if (apisIds?.length) {
    const [error, { data }] = await apis.getShareList({
      projectId: props.projectId,
      filters: [{ value: apisIds, op: 'IN', key: 'id' }],
      pageSize: 1000
    });
    if (error) {
      return;
    }
    selectApis.value = data.list || [];
  }
};

// when share scope changes, normalize dependent fields
const handleScopeChange = () => {
  if (formState.value.shareScope === ApisShareScope.SERVICES) {
    formState.value.apisIds = [];
    selectApis.value = [];
  } else if (formState.value.shareScope === ApisShareScope.SINGLE_APIS && !!formState.value.apisIds.length) {
    formState.value.apisIds = [formState.value.apisIds[0]];
    selectApis.value = selectApis.value.filter(i => i.id === formState.value.apisIds[0]);
  }
};

// when service changed, reset selected apis
const handleServiceChange = () => {
  formState.value.apisIds = [];
};

// select single api for SINGLE_APIS scope
const handleSigngeApiChange = (value: any) => {
  formState.value.apisIds = [String(value)];
};

const selectApiId = ref();
const selectApis = ref<any[]>([]);

// add api to selection in PARTIAL_APIS scope
const addMultipleApis = (value: any, option: any) => {
  selectApis.value.push(option);
  formState.value.apisIds.push(String(value));
  nextTick(() => {
    selectApiId.value = undefined;
  });
};

// remove selected api by index
const delApis = (api: any, idx: number) => {
  selectApis.value.splice(idx, 1);
  formState.value.apisIds = formState.value.apisIds.filter(id => api.id !== id);
};

// close modal and reset form
const cancel = () => {
  formRef.value.resetFields();
  emits('update:visible', false);
  emits('cancel');
};

// validate and submit
const ok = async () => {
  formRef.value.validate().then(async () => {
    if (!props.shareId) {
      await addOk();
    } else {
      await editOk();
    }
  });
};

// create share and copy link on success
const addOk = async () => {
  loading.value = true;
  const [error, { data }] = await apis.addShare({
    ...formState.value,
    baseUrl: baseUrl.value,
    projectId: props.projectId
  });
  if (error) {
    loading.value = false;
    return;
  }
  const [_error1, resp] = await apis.getShareDetail(data.id);
  loading.value = false;
  if (resp.data) {
    toClipboard(t('actions.tips.copyLinkSuccess', { name: resp.data.name, url: resp.data.url })).then(() => {
      notification.success(t('actions.tips.copySuccess'));
    });
  } else {
    notification.success(t('actions.tips.addSuccess'));
  }
  emits('ok');
  emits('update:visible', false);
};

// update existing share
const editOk = async () => {
  loading.value = true;
  const [error] = await apis.patchShared({
    ...formState.value,
    id: props.shareId
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
  loadApisShareScopeOpt();
  origin.value = DomainManager.getInstance().getAppDomain(AppOrServiceRoute.tester);
  watch(() => props.visible, async (newValue) => {
    if (newValue) {
      if (props.shareId) {
        loadData(props.shareId);
      } else {
        formState.value = {
          name: undefined,
          remark: undefined,
          expiredDate: undefined,
          displayOptions: { includeServiceInfo: true, allowDebug: false, schemaStyle: 'TABLE' },
          shareScope: props.shareScope || ApisShareScope.SERVICES,
          servicesId: props.servicesId,
          apisIds: props.apisIds || []
        };
        selectApis.value = [];
        selectApiId.value = undefined;
      }
    }
  }, { immediate: true });
});

const schemaStyleOpt = [
  {
    value: 'TABLE',
    label: t('apiShare.schemaStyle.table')
  },
  {
    value: 'TREE',
    label: t('apiShare.schemaStyle.tree')
  }
];
</script>
<template>
  <Modal
    :title="props.shareId ? t('apiShare.actions.editShare') : t('apiShare.actions.addShare')"
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
        :label="t('common.name')">
        <Input
          v-model:value="formState.name"
          :maxlength="100"
          :placeholder="t('apiShare.messages.namePlaceholder')" />
      </FormItem>

      <FormItem
        :label="t('common.remark')"
        class="!mb-5"
        name="remark">
        <Textarea
          v-model:value="formState.remark"
          :maxlength="400"
          :placeholder="t('apiShare.namePlaceholder.remarkPlaceholder')">
        </Textarea>
      </FormItem>

      <FormItem
        name="expiredDate"
        :label="t('common.expiredDate')"
        class="min-w-0">
        <div class="flex items-center space-x-1">
          <DatePicker
            v-model:value="formState.expiredDate"
            showToday
            showTime
            class="flex-1 min-w-0" />
          <Hints :text="t('apiShare.namePlaceholder.expiredDateHint')" />
        </div>
      </FormItem>

      <FormItem
        :label="t('apiShare.columns.displayOptions')"
        class="!mb-5"
        name="displayOptions">
        <div class="flex items-center">
          <Checkbox
            v-model:checked="formState.displayOptions.includeServiceInfo">
            {{ t('apiShare.columns.includeServiceInfo') }}
          </Checkbox>
          <Checkbox
            v-model:checked="formState.displayOptions.allowDebug">
            {{ t('apiShare.columns.allowDebug') }}
          </Checkbox>

          <div class="inline-flex items-center text-3 ml-2">
            <span>{{ t('apiShare.columns.fieldStyle') }}</span>
            <Colon />
            <Select
              v-model:value="formState.displayOptions.schemaStyle"
              class="flex-1 ml-2"
              :options="schemaStyleOpt">
            </Select>
          </div>
        </div>
      </FormItem>

      <template v-if="!props.servicesId">
        <FormItem
          :label="t('apiShare.columns.shareScope')"
          name="shareScope"
          required>
          <RadioGroup
            v-model:value="formState.shareScope"
            :options="apisShareScope"
            @change="handleScopeChange">
          </RadioGroup>
        </FormItem>

        <FormItem
          :label="t('common.service')"
          name="servicesId"
          class="flex-1 min-w-0"
          required>
          <Select
            v-model:value="formState.servicesId"
            :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
            :placeholder="t('common.placeholders.selectService')"
            :fieldNames="{value: 'id', label: 'name'}"
            @change="handleServiceChange" />
        </FormItem>

        <FormItem
          v-if="[ApisShareScope.PARTIAL_APIS, ApisShareScope.SINGLE_APIS].includes(formState.shareScope)"
          :label="t('common.api')"
          name="apisIds"
          class="flex-1 min-w-0"
          required>
          <Select
            v-if="formState.shareScope === ApisShareScope.SINGLE_APIS"
            :value="formState.apisIds[0]"
            :disabled="!formState.servicesId"
            :placeholder="t('common.placeholders.selectApi')"
            :action="`${TESTER}/services/${formState.servicesId}/apis`"
            :fieldNames="{value: 'id', label: 'summary'}"
            @change="handleSigngeApiChange" />

          <div
            v-if="formState.shareScope === ApisShareScope.PARTIAL_APIS"
            class="flex items-center space-x-2 text-3">
            <Select
              v-model:value="selectApiId"
              :disabled="!formState.servicesId"
              class="flex-1 min-w-0"
              :placeholder="t('common.placeholders.selectApi')"
              :action="`${TESTER}/services/${formState.servicesId}/apis`"
              :disabledList="formState.apisIds"
              :fieldNames="{value: 'id', label: 'summary'}"
              @change="addMultipleApis" />
            <span>{{ t('apiShare.form.selectedApis', { count: formState.apisIds?.length || 0 }) }}</span>
          </div>
        </FormItem>

        <FormItem
          v-if="formState.shareScope === ApisShareScope.PARTIAL_APIS"
          label="">
          <div class="max-h-50 overflow-y-auto pl-22 ">
            <div
              v-for="(item, idx) in selectApis"
              :key="item.id"
              class="px-1 flex h-6 items-center ">
              <HttpMethodText :value="item.method" />
              <span class="min-w-0 truncate flex-1" :title="item.endpoint">{{ item.endpoint }}</span>
              <span class="min-w-0 truncate flex-1" :title="item.apisName || item.caseName || item.summary">
                {{ item.apisName || item.caseName || item.summary }}
              </span>
              <Button
                type="link"
                size="small"
                class="ml-2"
                @click="delApis(item, idx)">
                <Icon icon="icon-qingchu" />
              </Button>
            </div>
          </div>
        </FormItem>
      </template>
    </Form>
  </Modal>
</template>
