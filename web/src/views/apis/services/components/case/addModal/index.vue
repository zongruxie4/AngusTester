<script lang="ts" setup>
import { inject, reactive, ref, watch } from 'vue';
import { Icon, Input, Modal, notification, SelectEnum, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Tooltip } from 'ant-design-vue';
import { LoadingOutlined } from '@ant-design/icons-vue';
import { isEqual } from 'lodash-es';
import { localStore } from '@xcan-angus/tools';
import { apis } from '@/api/tester';

import { useI18n } from 'vue-i18n';

import ApiDebug from '@/views/apis/services/components/case/debug/index.vue';
import { ApiInfo } from '@/views/apis/services/components/case/debug/PropsType.ts';

const { t } = useI18n();

export interface Props {
  visible: boolean;
  debug?: boolean;
  caseId?: string;
  apisId: string;
  serviceId: string;
}
const useForm = Form.useForm;
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  debug: false,
  caseId: '',
  apisId: '',
  serviceId: ''
});

const userInfo: any = inject('tenantInfo');
const projectInfo = inject('projectInfo', ref({ id: '' }));

const emits = defineEmits<{(e: 'update:visible', value: boolean):void;
  (e: 'update', id?:string):void;
  (e: 'ok'):void;
}>();

const addCaseSizeKey = `${userInfo.id}${projectInfo.value.id}addApiCaseSize`;

const formState = ref({
  description: '',
  name: '',
  apisId: '',
  assertions: [],
  authentication: { type: null },
  method: '',
  parameters: [],
  requestBody: {},
  endpoint: '',
  currentServer: { url: '' },
  type: 'USER_DEFINED'
});

const rulesRef = reactive({
  name: [{ required: true, message: '请输入用例名称' }],
  type: [{ required: true }]
});

const { clearValidate, validate, validateInfos } = useForm(formState, rulesRef);

const caseInfo = reactive<ApiInfo>({
  apisId: '',
  summary: '',
  method: '',
  endpoint: '',
  parameters: [],
  requestBody: {},
  authentication: { type: null },
  currentServer: { url: '' },
  assertions: [],
  serviceId: '',
  serviceName: '',
  id: '',
  resolvedRefModels: {}
});

const resetApiInfo = () => {
  Object.keys(caseInfo).forEach(key => {
    if (['apisId', 'method', 'endpoint', 'serviceId', 'serviceName', 'summary', 'id'].includes(key)) {
      caseInfo[key] = '';
    } else if (['parameters', 'assertions'].includes(key)) {
      caseInfo[key] = [];
    } else if (['currentServer'].includes(key)) {
      caseInfo[key] = { url: '' };
    } else {
      caseInfo[key] = {};
    }
  });
};
const formRef = ref();
const resetFormState = () => {
  formRef.value && formRef.value.resetFields(Object.keys(formState.value));
  formState.value.name = undefined;
  clearValidate();
};

const resetApiDebug = () => {
  apiDebugRef.value && apiDebugRef.value.resetResponse();
};

const loadApiResolveModel = async () => {
  const [error, resp] = await apis.loadInfo(props.apisId, true);
  if (error) {
    return;
  }
  return resp.data?.resolvedRefModels || {};
};

const apiDebugRef = ref();

const oldFormState = ref();

const defaultDescription = ref('');

const caseInfoData = ref();
const getCaseInfo = async () => {
  const resolvedRefModels = await loadApiResolveModel();
  const [error, { data }] = await apis.loadCaseInfo(props.caseId);
  if (error) {
    return;
  }
  Object.keys(formState.value).forEach((key) => {
    if (key === 'method') {
      formState.value[key] = data[key].value;
    } else if (key === 'authentication') {
      formState.value[key] = data[key] || { type: null };
    } else {
      formState.value[key] = data[key];
    }
  });
  Object.keys(caseInfo).forEach(key => {
    if (key === 'method') {
      caseInfo.method = data.method.value;
    } else if (key === 'serviceId') {
      caseInfo.serviceId = data.apisServiceId;
    } else if (key === 'serviceName') {
      caseInfo.serviceName = data.apisServiceName;
    } else if (key === 'summary') {
      caseInfo.summary = data.apisSummary;
    } else if (key === 'currentServer') {
      caseInfo[key] = data.currentServer || { url: '' };
    } else {
      caseInfo[key] = data[key];
    }
  });
  caseInfo.resolvedRefModels = resolvedRefModels;

  caseInfoData.value = data;
  defaultDescription.value = data.description;
  stepDefaultValue.value = data.steps;
  oldFormState.value = await getParams(true);
};

const close = () => {
  clearValidate();
  loading.value = false;
  debuging.value = false;
  emits('update:visible', false);
};

const onSubmit = (close = true) => {
  if (loading.value) {
    return;
  }
  const _ruleKeys = Object.keys(rulesRef);
  validate(_ruleKeys)
    .then(async () => {
      if (props.caseId) {
        await editSave(close);
      } else {
        await addSave(close);
      }
    });
};

const getParams = async (first = false) => {
  const params = JSON.parse(JSON.stringify(formState.value));

  if (!params.description) {
    delete params.description;
  }
  const apiCaseParam = first ? {} : (await apiDebugRef.value?.getData() || {});
  return { ...params, ...apiCaseParam };
};

const loading = ref(false);
const editSave = async (closeModal = true) => {
  if (formState.value.description?.length > 2000) {
    notification.warning('备注内容过长(最多2000字符),请减少内容');
    return;
  }
  loading.value = true;
  const params = await getParams();

  if (isEqual(oldFormState.value, params)) {
    closeModal && close();
    loading.value = false;
    return;
  }

  const [error] = await apis.replaceCase([{ id: props.caseId, ...params }]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('修改成功');
  closeModal && close();
  emits('ok');
};
const addSave = async (closeModal = false) => {
  loading.value = true;
  const params = await getParams();
  const [error] = await apis.addApisCase([params]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('添加成功');
  closeModal && close();
  emits('ok');
};

const stepDefaultValue = ref<{ expectedResult: string; step: string;}[]>([]);

const debuging = ref(false);
// 调试用例
const debugCase = () => {
  apiDebugRef.value.debug();
};
// 终止调试
const stopDebugCase = () => {
  apiDebugRef.value.stopDebug();
};

const isZoom = ref(typeof localStore.get(addCaseSizeKey) === 'boolean' ? localStore.get(addCaseSizeKey) : false);
const handleZoom = () => {
  isZoom.value = !isZoom.value;
  localStore.set(addCaseSizeKey, isZoom.value);
};

watch(() => props.visible, async newValue => {
  clearValidate();
  if (newValue) {
    if (props.caseId) {
      await getCaseInfo();
    } else {
      resetApiInfo();
    }
  } else {
    resetFormState();
    resetApiDebug();
  }
}, {
  immediate: true
});

watch([() => apiDebugRef.value], () => {
  if (props.debug && apiDebugRef.value) {
    debugCase();
  }
}, {
  immediate: true
});

</script>
<template>
  <Modal
    :visible="props.visible"
    :title="props.caseId ? '编辑接口用例' : '生成接口用例'"
    :width="isZoom ? '100%' : '1260px'"
    class="addApiCaseModal"
    :wrapClassName="isZoom ? 'full-modal' : ''"
    @cancel="close">
    <template #footer>
      <div class="flex justify-end py-2 px-5">
        <Button
          type="primary"
          size="small"
          class="px-3"
          :loading="loading"
          @click.prevent="onSubmit(true)">
          {{ t('确认') }}
        </Button>
        <Button
          v-show="!props.caseId"
          type="primary"
          size="small"
          class="px-3 ml-5"
          :loading="loading"
          @click.prevent="onSubmit(false)">
          保存并添加下一条
        </Button>
        <Button
          size="small"
          class="ml-5 px-3"
          @click="close">
          {{ t('取消') }}
        </Button>
      </div>
    </template>
    <Tooltip :title="isZoom?'恢复':'全屏'">
      <Icon
        :icon="isZoom?'icon-tuichuzuida':'icon-zuidahua'"
        class="absolute right-10 top-3.5 text-3.5 cursor-pointer"
        @click="handleZoom" />
    </Tooltip>
    <Spin :spinning="loading||debuging" class="h-full">
      <Form
        ref="formRef"
        :model="formState"
        class="h-full"
        size="small"
        layout="vertical">
        <div
          class="overflow-y-hidden"
          :style="isZoom ? 'height: calc(100vh - 120px)' : 'height: calc(80vh - 100px)'">
          <div class="flex flex-col min-w-0 h-full">
            <div class="flex w-full">
              <FormItem
                label="名称"
                name="name"
                class="pr-2 flex-1"
                v-bind="validateInfos.name">
                <div>
                  <Input
                    v-model:value="formState.name"
                    placeholder="输入用例名称，最多可输入400个字符"
                    :maxlength="400" />
                </div>
              </FormItem>
              <FormItem
                label="类型"
                name="name"
                class="pr-2"
                required
                v-bind="validateInfos.type">
                <div class="flex items-center space-x-2">
                  <SelectEnum
                    v-model:value="formState.type"
                    enumKey="ApisCaseType"
                    placeholder="选择用例类型"
                    :defaultActiveFirstOption="true"
                    :lazy="false"
                    class="!w-50">
                  </SelectEnum>

                  <Button
                    v-if="debuging"
                    type="primary"
                    size="small"
                    class="w-20 px-0"
                    @click="stopDebugCase">
                    <LoadingOutlined />
                    终止
                  </Button>
                  <Button
                    v-else
                    type="primary"
                    size="small"
                    class="w-20 px-0"
                    @click="debugCase">
                    <Icon icon="icon-fasong" class="mr-1" />
                    调试用例
                  </Button>
                </div>
              </FormItem>
            </div>
            <FormItem
              label="描述"
              name="description"
              class="pr-20">
              <Input
                v-model:value="formState.description"
                placeholder="输入用例描述，最多可输入800个字符"
                type="textarea"
                :maxlength="800" />
            </FormItem>
            <ApiDebug
              v-if="props.visible && (props.caseId ? caseInfo.id : props.apisId)"
              ref="apiDebugRef"
              v-model:debuging="debuging"
              :serviceId="props.serviceId"
              :apisId="props.visible ? props.apisId : undefined"
              :validateInfos="validateInfos"
              :caseInfoData="caseInfoData"
              :case="caseInfo"
              @loaded="debugCase" />
          </div>
        </div>
      </Form>
    </Spin>
  </Modal>
</template>
<style scoped>
:deep(.ant-select.ant-select-sm.ant-select-multiple .ant-select-selector) {
  max-height: 76px;
  overflow-y: auto;
}

:deep(.tox-tinymce-aux > .tox-textarea > textarea::placeholder) {
  color: red !important;  /* 这里可以设置你想要的颜色 */
}

</style>
<style>
.addApiCaseModal .ant-modal-body {
  padding-right: 0;
  padding-bottom: 0;
}

.full-modal .ant-modal {
  top: 0;
  max-width: 100%;
  padding-bottom: 0;
}

.full-modal .ant-modal-content {
  display: flex;
  flex-direction: column;
  height: calc(100vh);
}

.full-modal .ant-modal-body {
  flex: 1;
  min-height: 100px;
}
</style>
