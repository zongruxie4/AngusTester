<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import {
  AsyncComponent,
  Hints,
  Icon,
  IconRequired,
  Input,
  notification,
  Toggle,
  Validate
  , SelectApisByServiceModal
} from '@xcan-angus/vue-ui';
import { isEqual } from 'lodash-es';
import { variable, apis } from '@/api/tester';

import SelectEnum from '@/components/selectEnum/index.vue';
import { VariableItem } from '../../PropsType';
import { FormState } from './PropsType';
import { getRequestConfigs } from './getRequestConfigs';

const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; };
  visible: boolean;
  dataSource?: VariableItem;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  visible: false,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'ok', data: VariableItem, isEdit: boolean): void;
  (e: 'delete', value: string): void;
  (e: 'export', value: string): void;
  (e: 'clone', value: string): void;
  (e: 'copyLink', value: string): void;
  (e: 'refresh', value: string): void;
}>();

const ButtonGroup = defineAsyncComponent(() => import('@/views/data/variable/detail/buttonGroup/index.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/variable/detail/previewData/index.vue'));
const VariableUseList = defineAsyncComponent(() => import('@/views/data/variable/detail/useList/index.vue'));
const MatchItemPopover = defineAsyncComponent(() => import('@/views/data/variable/detail/matchItemPopover/index.vue'));
const HTTPConfigs = defineAsyncComponent(() => import('@/views/data/variable/detail/httpVariable/httpConfigs/index.vue'));
// const SelectApiModal = defineAsyncComponent(() => import('./SelectApiModal/index.vue'));

const httpConfigsRef = ref();

const confirmLoading = ref(false);
const activeKey = ref<'value' | 'preview' | 'use'>('value');

const variableName = ref<string>('');
const variableNameError = ref(false);
const description = ref<string>('');

const method = ref<'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH'>('EXACT_VALUE');
const location = ref<FormState['extraction']['location']>('RESPONSE_BODY');
const parameterName = ref<string>('');
const defaultValue = ref<string>('');
const expression = ref<string>('');
const matchItem = ref<string>('');

const selectApiVisible = ref(false);
const requestConfigs = ref<{ url: string; }>({
  url: ''
});

const previewData = ref<{
  name: string;
  extraction: {
    source: 'HTTP';
    method: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
    expression: string;
    defaultValue: string;
    location: 'QUERY_PARAMETER' | 'PATH_PARAMETER' | 'REQUEST_HEADER' | 'FORM_PARAMETER' | 'REQUEST_RAW_BODY' | 'RESPONSE_HEADER' | 'RESPONSE_BODY';
    matchItem: string;
    parameterName: string;
    request: { url: string; };
  };
}>();

const toSelectApi = () => {
  selectApiVisible.value = true;
};

const selectApiOk = async (ids: string[]) => {
  selectApiVisible.value = false;
  const [error, res] = await apis.getApiDetail(ids[0], true);
  if (error) {
    return;
  }

  const data = res?.data;
  if (!data) {
    return;
  }

  requestConfigs.value = await getRequestConfigs(data);
};

const nameChange = () => {
  variableNameError.value = false;
};

const nameBlur = (event: { target: { value: string; } }) => {
  const name = event.target.value;
  if (!name) {
    return;
  }

  validName(name);
};

const validName = (name:string) => {
  // eslint-disable-next-line prefer-regex-literals
  const rex = new RegExp(/[^a-zA-Z0-9!$%^&*_\-+=./]/);
  if (rex.test(name)) {
    variableNameError.value = true;
    return false;
  }

  return true;
};

const buttonGroupClick = (key: 'ok' | 'delete'|'export' | 'clone' | 'copyLink' | 'refresh') => {
  if (key === 'ok') {
    ok();
    return;
  }

  if (key === 'delete') {
    emit('delete', variableId.value);
    return;
  }

  if (key === 'export') {
    emit('export', variableId.value);
    return;
  }

  if (key === 'clone') {
    emit('clone', variableId.value);
    return;
  }

  if (key === 'copyLink') {
    emit('copyLink', variableId.value);
    return;
  }

  if (key === 'refresh') {
    emit('refresh', variableId.value);
  }
};

const ok = async () => {
  if (!validName(variableName.value)) {
    return;
  }

  if (typeof httpConfigsRef.value?.isValid === 'function') {
    const validFlag = httpConfigsRef.value?.isValid();
    if (!validFlag) {
      return;
    }
  }

  if (editFlag.value) {
    toEdit();
    return;
  }

  toCreate();
};

const toEdit = async () => {
  const params = getParams();
  confirmLoading.value = true;
  const [error] = await variable.putVariables(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataVariable.detail.httpVariable.notifications.editSuccess'));
  emit('ok', params, true);
};

const toCreate = async () => {
  const params = getParams();
  confirmLoading.value = true;
  const [error, res] = await variable.addVariables(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataVariable.detail.httpVariable.notifications.addSuccess'));
  const id = res?.data?.id;
  emit('ok', { ...params, id }, false);
};

const getParams = (): FormState => {
  const params: FormState = {
    projectId: props.projectId,
    name: variableName.value,
    description: description.value,
    passwordValue: false,
    extraction: {
      source: 'HTTP',
      method: method.value,
      defaultValue: defaultValue.value,
      expression: expression.value,
      matchItem: matchItem.value,
      parameterName: parameterName.value,
      location: location.value,
      request: {
        url: ''
      }
    }
  };

  if (typeof httpConfigsRef.value?.getData === 'function') {
    params.extraction.request = httpConfigsRef.value.getData();
  }

  const id = variableId.value;
  if (id) {
    params.id = id;
  }

  return params;
};

const initialize = () => {
  const data = props.dataSource;
  if (!data) {
    return;
  }

  const { extraction } = data || {};
  variableName.value = data.name;
  description.value = data.description;

  method.value = extraction.method?.value || 'EXACT_VALUE';
  parameterName.value = extraction.parameterName;
  defaultValue.value = extraction.defaultValue;
  expression.value = extraction.expression;
  matchItem.value = extraction.matchItem;

  let _request = extraction.request;
  if (_request) {
    if (!_request?.url) {
      _request.url = '';
    }
  } else {
    _request = { url: '' };
  }

  requestConfigs.value = _request;
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue) {
      return;
    }

    initialize();
  }, { immediate: true });

  watch(() => activeKey.value, (newValue) => {
    if (newValue !== 'preview') {
      return;
    }

    let request = requestConfigs.value;
    if (typeof httpConfigsRef.value?.getData === 'function') {
      request = httpConfigsRef.value?.getData();
    }

    const newData = {
      name: variableName.value,
      extraction: {
        request,
        source: 'HTTP',
        method: method.value,
        expression: expression.value,
        defaultValue: defaultValue.value,
        location: location.value,
        matchItem: matchItem.value,
        parameterName: parameterName.value
      }
    };

    if (!isEqual(newData, previewData.value)) {
      previewData.value = newData;
    }
  }, { immediate: true });
});

const variableId = computed(() => {
  return props.dataSource?.id || '';
});

const editFlag = computed(() => {
  return !!props.dataSource?.id;
});

const okButtonDisabled = computed(() => {
  let disabled = !variableName.value ||
    !method.value ||
    !location.value;

  if (!disabled) {
    if (!['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(location.value)) {
      disabled = !parameterName.value;
    }
  }

  if (!disabled) {
    if (['JSON_PATH', 'REGEX', 'X_PATH'].includes(method.value)) {
      disabled = !expression.value;
    }
  }

  return disabled;
});
</script>
<template>
  <ButtonGroup
    :editFlag="editFlag"
    :okButtonDisabled="okButtonDisabled"
    class="mb-5"
    @click="buttonGroupClick" />

  <div class="flex items-start mb-3.5">
    <div class="flex items-center flex-shrink-0 mr-2.5 leading-7">
      <IconRequired />
      <span>{{ t('dataVariable.detail.httpVariable.name') }}</span>
    </div>
    <Validate
      class="flex-1"
      :text="t('dataVariable.detail.httpVariable.nameSupport')"
      mode="error"
      :error="variableNameError">
      <Input
        v-model:value="variableName"
        :maxlength="100"
        :error="variableNameError"
        dataType="mixin-en"
        excludes="{}"
        includes="\!\$%\^&\*_\-+=\.\/"
        :placeholder="t('dataVariable.detail.httpVariable.namePlaceholder')"
        trimAll
        @change="nameChange"
        @blur="nameBlur" />
    </Validate>
  </div>

  <div class="flex items-start">
    <div class="mr-2.5 flex items-center flex-shrink-0 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('dataVariable.detail.httpVariable.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataVariable.detail.httpVariable.descriptionPlaceholder')"
      trim />
  </div>

  <Tabs
    v-model:activeKey="activeKey"
    size="small"
    class="ant-tabs-nav-mb-2.5">
    <TabPane key="value">
      <template #tab>
        <div class="flex items-center font-normal">
          <IconRequired />
          <span>{{ t('dataVariable.detail.httpVariable.extract') }}</span>
        </div>
      </template>

      <div>
        <Hints class="mb-2.5" :text="t('dataVariable.detail.httpVariable.hints')" />

        <Toggle :title="t('dataVariable.detail.httpVariable.readConfig')" class="text-3 leading-5 mb-3.5">
          <div class="flex items-center justify-start mb-3.5">
            <div class="w-16 flex-shrink-0">
            </div>
            <Button
              type="link"
              size="small"
              class="flex items-center p-0 border-none h-3.5 leading-3.5 space-x-1"
              @click="toSelectApi">
              <Icon icon="icon-xuanze" class="text-3.5" />
              <span>{{ t('dataVariable.detail.httpVariable.selectApi') }}</span>
            </Button>
          </div>

          <div class="flex items-start mb-3.5">
            <div class="w-16 flex-shrink-0 transform-gpu translate-y-1">
              <IconRequired />
              <span>{{ t('dataVariable.detail.httpVariable.interfaceConfig') }}</span>
            </div>
            <HTTPConfigs
              ref="httpConfigsRef"
              :value="requestConfigs"
              class="mb-1.5" />
          </div>
        </Toggle>

        <Toggle :title="t('dataVariable.detail.httpVariable.extractConfig')" class="text-3 leading-5">
          <template v-if="method === 'EXACT_VALUE'">
            <template v-if="['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(location)">
              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractMethod') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="method"
                    enumKey="ExtractionMethod"
                    :placeholder="t('dataVariable.detail.httpVariable.extractMethodPlaceholder')"
                    class="w-full-16" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractPosition') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="location"
                    enumKey="HttpExtractionLocation"
                    class="w-full-16" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired class="invisible" />
                    <span>{{t('dataVariable.detail.httpVariable.defaultValue')}}</span>
                  </div>
                  <Input
                    v-model:value="defaultValue"
                    :placeholder="t('dataVariable.detail.httpVariable.defaultValuePlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="4096" />
                </div>
              </div>
            </template>

            <template v-else>
              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractMethod') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="method"
                    enumKey="ExtractionMethod"
                    :placeholder="t('dataVariable.detail.httpVariable.extractMethodPlaceholder')"
                    class="w-full-16" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractPosition') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="location"
                    enumKey="HttpExtractionLocation"
                    class="w-full-16" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.expression') }}</span>
                  </div>
                  <Input
                    v-model:value="parameterName"
                    :placeholder="t('dataVariable.detail.httpVariable.parameterNamePlaceholder')"
                    class="w-full-16"
                    trimAll
                    :maxlength="400" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.defaultValue') }}</span>
                  </div>
                  <Input
                    v-model:value="defaultValue"
                    :placeholder="t('dataVariable.detail.httpVariable.defaultValuePlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="4096" />
                </div>
              </div>
            </template>
          </template>

          <template v-else>
            <template v-if="['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(location)">
              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractMethod') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="method"
                    enumKey="ExtractionMethod"
                    :placeholder="t('dataVariable.detail.httpVariable.extractMethodPlaceholder')"
                    class="w-full-16" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractPosition') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="location"
                    enumKey="HttpExtractionLocation"
                    class="w-full-20.5" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.expression') }}</span>
                  </div>
                  <Input
                    v-model:value="expression"
                    :placeholder="t('dataVariable.detail.httpVariable.expressionPlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="1024" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.matchItem') }}</span>
                  </div>
                  <Input
                    v-model:value="matchItem"
                    :placeholder="t('dataVariable.detail.httpVariable.matchItemPlaceholder')"
                    class="w-full-20.5"
                    dataType="number"
                    trimAll
                    :max="2000"
                    :maxlength="4" />
                  <MatchItemPopover />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.defaultValue') }}</span>
                  </div>
                  <Input
                    v-model:value="defaultValue"
                    :placeholder="t('dataVariable.detail.httpVariable.defaultValuePlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="4096" />
                </div>
              </div>
            </template>

            <template v-else>
              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractMethod') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="method"
                    enumKey="ExtractionMethod"
                    :placeholder="t('dataVariable.detail.httpVariable.extractMethodPlaceholder')"
                    class="w-full-20.5" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractPosition') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="location"
                    enumKey="HttpExtractionLocation"
                    class="w-full-16" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired :class="{ invisible: ['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(location) }" />
                    <span>{{ t('dataVariable.detail.httpVariable.parameterName') }}</span>
                  </div>
                  <Input
                    v-model:value="parameterName"
                    :placeholder="t('dataVariable.detail.httpVariable.parameterNamePlaceholder')"
                    class="w-full-20.5"
                    trimAll
                    :maxlength="400" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.expression') }}</span>
                  </div>
                  <Input
                    v-model:value="expression"
                    :placeholder="t('dataVariable.detail.httpVariable.expressionPlaceholder ')"
                    class="w-full-16"
                    trim
                    :maxlength="1024" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.matchItem') }}</span>
                  </div>
                  <Input
                    v-model:value="matchItem"
                    :placeholder="t('dataVariable.detail.httpVariable.matchItemPlaceholder')"
                    class="w-full-20.5"
                    dataType="number"
                    trimAll
                    :max="2000"
                    :maxlength="4" />
                  <MatchItemPopover />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-16 flex-shrink-0">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.defaultValue') }}</span>
                  </div>
                  <Input
                    v-model:value="defaultValue"
                    :placeholder="t('dataVariable.detail.httpVariable.defaultValuePlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="4096" />
                </div>
              </div>
            </template>
          </template>
        </Toggle>
      </div>
    </TabPane>

    <TabPane key="preview">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.httpVariable.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="variableId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.httpVariable.use') }}</span>
        </div>
      </template>

      <VariableUseList :id="variableId" />
    </TabPane>
  </Tabs>

  <AsyncComponent :visible="selectApiVisible">

    <SelectApisByServiceModal
      v-model:visible="selectApiVisible"
      :selectSingle="true"
      :projectId="props.projectId"
      @ok="selectApiOk" />
  </AsyncComponent>
</template>

<style scoped>
:deep(.toggle-title) {
  color: var(--content-text-content);
  font-size: 12px;
  font-weight: normal;
}

:deep(.ant-collapse-ghost)>.ant-collapse-item>.ant-collapse-content {
  margin-top: 14px;
}

:deep(.ant-collapse) .ant-collapse-content>.ant-collapse-content-box {
  padding-left: 22px;
}
</style>

<style>
.ant-modal.overflow-auto-modal-container .ant-modal-content {
  overflow: hidden;
}

.ant-modal.overflow-auto-modal-container .ant-modal-content .ant-modal-body {
  max-height: calc(95vh - 84px);
  overflow: auto;
}

.ant-tabs.ant-tabs-nav-mb-2\.5>.ant-tabs-nav,
.ant-tabs>.ant-tabs-nav-mb-2\.5>div>.ant-tabs-nav {
  margin-bottom: 12px;
}

.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-full-20\.5 {
  width: calc(100% - 82px);
}

.w-full-16 {
  width: calc(100% - 64px);
}
</style>
