<!-- eslint-disable @typescript-eslint/no-empty-function -->
<!-- eslint-disable @typescript-eslint/ban-types -->
<script lang="ts" setup>
import { inject, onMounted, reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Form, FormItem } from 'ant-design-vue';
import { Input, notification, Select, SelectUser, TreeSelect, IconText } from '@xcan-angus/vue-ui';
import { TESTER, appContext } from '@xcan-angus/infra';
import SelectEnum from '@/components/selectEnum/index.vue';

import { apis, services } from '@/api/tester';

interface Props {
  getParameter: any;
  id: string;
  summary: string;
  operationId: string;
  serviceId: string;
  description: string;
  status: string;
  ownerId: string;
  deprecated: boolean;
  tabKey: string;
  packageParams: ()=> Record<string, any>;
}

// eslint-disable-next-line @typescript-eslint/no-empty-function
const replaceTabPane = inject<(key:string, data: any) => void>('replaceTabPane', () => { });
const close = inject('close', () => ({}));
const auths = inject('auths', ref<string[]>([]));
const userInfo = ref(appContext.getUser());
const isUnarchived = inject('isUnarchived', { value: false });
const projectInfo = inject('projectInfo', ref({ id: '' }));

// 更新左侧未归档列表
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshUnarchived = inject('refreshUnarchived', () => {});

// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

const defaultProject = ref();

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  getParameter: () => ({})
});

const emits = defineEmits<{(e: 'ok'): void}>();

const form = reactive({
  summary: '',
  operationId: '',
  ownerId: '',
  serviceId: '',
  projectName: '',
  description: '',
  status: 'UNKNOWN',
  deprecated: false,
  tags: []
});

const formRef = ref();
const rules = {
  summary: [{
    required: true, message: t('service.webSocketSave.form.summary.validation'), trigger: 'blur'
  }],
  ownerId: [{
    required: true, message: t('service.webSocketSave.form.ownerId.validation'), trigger: 'change'
  }],
  serviceId: [{
    required: true, message: t('service.webSocketSave.form.serviceId.validation'), trigger: 'change'
  }],
  status: [{
    required: true, message: t('service.webSocketSave.form.status.validation'), trigger: 'change'
  }]
};
const ownerOpt = ref([]);

const isLoading = ref(false);

const handleChanegProject = (value, name) => {
  form.serviceId = value;
  form.projectName = name?.[0];
};

const tagsOpt = ref<{value: string}[]>([]);
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

const save = () => {
  formRef.value.validate().then(async () => {
    const params = form;
    const apiInfo = await props.packageParams();
    if (apiInfo.protocol !== 'ws' && apiInfo.protocol !== 'wss') {
      notification.warning(t('service.webSocketSave.messages.protocolWarning'));
      return;
    }
    const [error, resp] = isUnarchived.value && props.id
      ? await apis.addApi([{ ...apiInfo, ...params, unarchivedId: props.id }])
      : props.id
        ? await apis.updateApi([{ ...apiInfo, ...params, id: props.id }])
        : await apis.putApi([{ ...apiInfo, ...params }]);
    if (error) {
      return;
    }
    if (isUnarchived.value || !props.id) {
      notification.success(t('service.webSocketSave.messages.addSuccess'));
      refreshUnarchived();
    } else {
      notification.success(t('service.webSocketSave.messages.updateSuccess'));
    }
    const id = props.id || resp.data[0]?.id;
    if (props.id) {
      updateTabPane({
        _id: id + 'socket',
        pid: props.tabKey,
        id: id,
        name: params.summary,
        unarchived: false,
        value: 'socket'
      });
    } else {
      replaceTabPane(props.tabKey, {
        _id: id + 'socket',
        pid: id + 'socket',
        name: params.summary,
        id: id,
        unarchived: false,
        value: 'socket'
      });
    }
    handleClose();
    emits('ok');
  });
};

const handleClose = () => {
  close();
};

onMounted(async () => {
  const apiInfo = await props.packageParams();
  form.summary = props.summary;
  form.operationId = props.operationId;
  form.ownerId = props.ownerId || userInfo.value.id;
  form.serviceId = apiInfo.serviceId || undefined;
  form.projectName = apiInfo.projectName || undefined;
  form.description = props.description;
  form.status = props.status || 'UNKNOWN';
  form.deprecated = props.deprecated;
  defaultProject.value = {
    name: form.projectName,
    id: form.serviceId,
    targetType: apiInfo.projectType
  };
});
</script>
<template>
  <Form
    ref="formRef"
    layout="vertical"
    :model="form"
    :rules="rules">
    <FormItem :label="t('service.webSocketSave.form.summary.label')" name="summary">
      <Input
        v-model:value="form.summary"
        :maxlength="40"
        :allowClear="false"
        :disabled="!auths.includes('MODIFY')"
        class="rounded"
        :placeholder="t('service.webSocketSave.form.summary.placeholder')" />
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.operationId.label')">
      <Input
        v-model:value="form.operationId"
        :maxlength="40"
        dataType="mixin-en"
        includes=":_-."
        :allowClear="false"
        :disabled="!auths.includes('MODIFY')"
        class="rounded"
        :placeholder="t('service.webSocketSave.form.operationId.placeholder')" />
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.ownerId.label')" name="ownerId">
      <SelectUser
        v-model:value="form.ownerId"
        class="rounded-border"
        :options="ownerOpt"
        :disabled="!auths.includes('MODIFY')"
        :placeholder="t('service.webSocketSave.form.ownerId.placeholder')"
        :allowClear="false" />
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.serviceId.label')" name="serviceId">
      <TreeSelect
        v-model:defaultValue="defaultProject"
        :action="`${TESTER}/services?projectId=${projectInfo.id}&hasPermission=ADD&fullTextSearch=true`"
        :allowClear="false"
        :disabled="!auths.includes('MODIFY')||!isUnarchived"
        :fieldNames="{children:'children', label:'name', value: 'id'}"
        :placeholder="t('service.webSocketSave.form.serviceId.placeholder')"
        :virtual="false"
        size="small"
        @change="handleChanegProject">
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
    <FormItem name="tags">
      <template #label>
        <div>
          <span>{{ t('service.webSocketSave.form.tags.label') }}</span>
          <Tooltip placement="left">
            <Icon icon="icon-tishi1" class="text-blue-tips ml-0.5 text-3.5" />
            <template #title> {{ t('service.webSocketSave.form.tags.description') }} </template>
          </Tooltip>
        </div>
      </template>
      <Select
        v-model:value="form.tags"
        mode="tags"
        :placeholder="t('service.webSocketSave.form.tags.placeholder')"
        :disabled="!auths.includes('MODIFY')"
        :options="tagsOpt"
        @dropdownVisibleChange="loadTagfromProject">
      </Select>
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.status.label')" name="status">
      <SelectEnum
        v-model:value="form.status"
        :disabled="!auths.includes('MODIFY')"
        enumKey="ApiStatus">
      </SelectEnum>
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.deprecated.label')" name="deprecated">
      <Select
        v-model:value="form.deprecated"
        :disabled="!auths.includes('MODIFY')"
        :options="[{label: t('service.webSocketSave.form.deprecated.options.normal'), value: false}, {label: t('service.webSocketSave.form.deprecated.options.deprecated'), value: true}]">
      </Select>
    </FormItem>
    <FormItem :label="t('service.webSocketSave.form.description.label')">
      <Input
        v-model:value="form.description"
        type="textarea"
        showCount
        :rows="4"
        :allowClear="false"
        :disabled="!auths.includes('MODIFY')"
        class="rounded-border"
        :placeholder="t('service.webSocketSave.form.description.placeholder')" />
    </FormItem>
    <FormItem>
      <Button
        type="primary"
        class="mr-2.5 rounded"
        size="small"
        :loading="isLoading"
        :disabled="!auths.includes('MODIFY')"
        @click="save">
        {{ t('actions.save') }}
      </Button>
      <Button
        class="rounded"
        size="small"
        @click="handleClose">
        {{ t('actions.cancel') }}
      </Button>
      <p v-if="form.status === 'RELEASED'" class="text-3 text-status-orange mt-1">{{ t('service.webSocketSave.messages.releasedWarning') }}</p>
    </FormItem>
  </Form>
</template>
