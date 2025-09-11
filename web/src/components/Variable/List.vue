<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue';
import { Icon, notification, Arrow, Input, IconRequired, SelectEnum, Colon, Scroll } from '@xcan-angus/vue-ui';
import { Button, Textarea, Radio, RadioGroup, Tooltip, Switch, Tabs, TabPane, InputGroup } from 'ant-design-vue';
import { EnumMessage, ExtractionMethod, utils, enumUtils, TESTER, duration, HttpExtractionLocation } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

import variableApi from './apis';
import { VariableObj, TargetType, Scope } from './PropsType';
import { RadioChangeEvent } from 'ant-design-vue/es/radio/interface';

export interface Props {
  id: string;
  disabled: boolean;
  type:TargetType;
  tabKey: Scope;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  disabled: false,
  type: 'PROJECT',
  tabKey: 'CURRENT'
});

// 空数据
const newData:VariableObj = {
  id: utils.uuid('api'),
  createdBy: '',
  createdByName: '',
  createdDate: '',
  description: '',
  elementName: '',
  enabled: true,
  extraction: {
    defaultValue: '',
    expression: '',
    failureMessage: '',
    finalValue: '',
    location: 'PATH_PARAMETER',
    method: { value: 'EXACT_VALUE', message: '' },
    name: '',
    parameterName: '',
    value: '',
    parameterNameErr: false,
    expressionErr: false
  },
  lastModifiedBy: '',
  lastModifiedByName: '',
  lastModifiedDate: '',
  name: '',
  // scope: { value: 'CURRENT', message: '' },
  targetId: props.id,
  targetName: '',
  targetType: { value: props.type, message: '' },
  value: '',
  isEdit: true,
  isExpand: true,
  isAdd: true,
  delLoading: false,
  saveloading: false,
  nameErr: false,
  valueErr: false,
  type: false,
  enableLoading: false
};

const loading = ref(false);
const notify = ref(0);
const total = ref(0);
const variableList = ref<VariableObj[]>([]);
const oldVaribleList = ref<VariableObj[]>([]);
const params = ref<{pageNo: number, filters?:{ key: 'name', value: string, op: 'MATCH_END' }[], targetId?:string, targetType?:TargetType}>({ pageNo: 1, targetId: props.id, targetType: props.type, filters: [] });
const action = computed(() => {
  return props.tabKey === 'INHERIT' ? `${TESTER}/variable/inherited` : `${TESTER}/variable`;
});
const lineHeight = computed(() => {
  if (props.tabKey === 'CURRENT') {
    return 138;
  } else if (props.tabKey === 'INHERIT') {
    return 138;
  } else {
    return 162;
  }
});
const getList = async (data) => {
  variableList.value = data.map(item => ({
    ...item,
    description: item.description || '',
    isEdit: false,
    isAdd: false,
    isExpand: false,
    delLoading: false,
    saveloading: false,
    nameErr: false,
    valueErr: false,
    enableLoading: false,
    type: !!(item?.extraction && Object.keys(item.extraction)?.length)
  }));
  // 记录历史数据
  oldVaribleList.value = JSON.parse(JSON.stringify(variableList.value));
  // 启用添加
  addBtnDisabled.value = false;
};

// 添加按钮禁用状态
const addBtnDisabled = ref(false);

// 添加新配置
const addVariable = () => {
  const hasEditData = variableList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    const checkRes = getCheckDataResult(hasEditData[0]);
    if (checkRes) {
      return;
    }
    if (getChenkUpdateRes()) {
      return;
    }
  }

  if (variableList.value[0]?.isAdd) {
    return;
  }
  // 列表开始位置添加一条新数据
  variableList.value.unshift({ ...JSON.parse(JSON.stringify(newData)), id: utils.uuid('api') });
  // 记录正在编辑的数据(编辑逻辑需要)
  currEditData.value = variableList.value[0];
  // 追加后禁用添加按钮
  addBtnDisabled.value = true;
  setEditFalseExceptId(variableList.value, currEditData.value.id);
};

// 删除配置
const hanldeDelete = async (variable:VariableObj) => {
  if (variable.delLoading) {
    return;
  }
  // 如果是新增数据 直接删除
  if (variable.isAdd) {
    // 判断列表是否剩余一条数据 剩余一条数据禁止删除
    if (variableList.value.length === 1) {
      return;
    }
    variableList.value = variableList.value.filter(item => item.id !== variable.id);
    return;
  }

  variable.delLoading = true;
  const [error] = await variableApi.delVariables({ ids: [variable.id] });
  variable.delLoading = false;
  if (error) {
    return;
  }
  notification.success(t('xcan_variable.deleteSuccess'));
  // 如果删除成功 todo 可能不需要重置到第一页
  params.value.pageNo = 1;
  notify.value++;
};

// eslint-disable-next-line prefer-regex-literals
const nameReg = new RegExp(/^[a-zA-Z0-9!@$%^&*()_\-+=./]+$/);

// 保存
const handleSave = (variable:VariableObj) => {
  // 校验有没有空项
  const checkRes = getCheckDataResult(variable);
  if (checkRes) {
    return;
  }
  // 如果是新建数据 判断变量有没有重复
  if (!variable.isAdd) {
    if (!chenkUpdate(variable)) {
      variable.isEdit = false;
      variable.isExpand = lastIsExpandState.value;
      return;
    }
    if (variable.name) {
      if (!nameReg.test(variable.name)) {
        return;
      }
    }
  }
  saveData(variable);
};

const saveData = async (variable:VariableObj) => {
  const params = getParams(variable);

  variable.saveloading = true;
  const [error, { data }] = variable.isAdd ? await variableApi.addVariables(params) : await variableApi.putVariables(params);
  variable.saveloading = false;
  if (error) {
    return false;
  }
  variable.isEdit = false;
  variable.isExpand = lastIsExpandState.value;
  addBtnDisabled.value = false;
  currEditData.value = undefined;
  notification.success(t('xcan_variable.saveSuccess'));
  notify.value++;
  if (variable.isAdd) {
    variable.isAdd = false;
    variable.id = data.id;
  }
};

// 组织参数
const getParams = (variable:VariableObj) => {
  let params:any = {};
  if (!variable.type) {
    params = {
      name: variable.name,
      // scope: variable.scope.value,
      value: variable.value,
      enabled: true,
      targetId: variable.targetId,
      targetType: variable.targetType.value
    };
  } else {
    params = {
      name: variable.name,
      enabled: true,
      targetId: variable.targetId,
      targetType: variable.targetType.value,
      // scope: variable.scope.value,
      extraction: variable.extraction.method.value === 'EXACT_VALUE'
        ? {
            defaultValue: variable.extraction.defaultValue,
            location: variable.extraction.location,
            method: variable.extraction.method.value
          }
        : {
            defaultValue: variable.extraction.defaultValue,
            expression: String.raw`${variable.extraction.expression}`,
            location: variable.extraction.location,
            method: variable.extraction.method.value
          }
    };
    if (!variable.isAdd) {
      params = {
        ...params,
        extraction: {
          ...params.extraction,
          failureMessage: variable.extraction.failureMessage,
          finalValue: variable.extraction.defaultValue
        }
      };
    }

    if (!['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(variable.extraction.location)) {
      params = {
        ...params,
        extraction: {
          ...params.extraction,
          parameterName: variable.extraction.parameterName
        }
      };
    }
  }
  if (!variable.isAdd) {
    params = {
      ...params,
      id: variable.id
    };
  }
  if (variable.description) {
    params = { ...params, description: variable.description };
  }
  return params;
};

// 检查提交的数据有没有空项 有空项返回true 否则返回false
const getCheckDataResult = (variable:VariableObj):boolean => {
  let hasEmpty = false;
  if (!variable.name) {
    variable.nameErr = true;
    hasEmpty = true;
  }

  if (!variable.type && !variable.value) {
    variable.valueErr = true;
    hasEmpty = true;
  }

  if (variable.type) {
    if (!['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(variable.extraction.location)) {
      if (!variable.extraction.parameterName) {
        variable.extraction.parameterNameErr = true;
      }
    }

    if (variable.extraction.method.value !== 'EXACT_VALUE') {
      if (!variable.extraction.expression) {
        variable.extraction.expressionErr = true;
      }
    }
  }

  return hasEmpty;
};

const variableSwitchChange = async (variable:VariableObj) => {
  if (variable.enableLoading) {
    return;
  }
  variable.enableLoading = true;
  const [error] = await variableApi.patchVariableEnabled([{ id: variable.id, enabled: !variable.enabled }]);
  variable.enableLoading = false;
  if (error) {
    return;
  }
  variable.enabled = !variable.enabled;
  notification.success(variable.enabled ? t('xcan_variable.enableSuccess') : t('xcan_variable.disableSuccess'));
};

// 记录正在编辑的数据 同时只有一个编辑
const currEditData = ref<VariableObj>();

// 展开收起 开启关闭编辑
const handleExpand = (event, variable:VariableObj) => {
  event.stopPropagation();
  const hasEditData = variableList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      variableList.value = variableList.value.filter(item => item.id !== hasEditData[0].id);
    } else {
      const checkRes = getCheckDataResult(hasEditData[0]);
      if (checkRes) {
        return;
      }
      if (getChenkUpdateRes()) {
        return;
      }
    }
  }

  variable.isExpand = !variable.isExpand;
  if (!variable.isExpand) {
    variable.isEdit = false;
  }
  setEditFalseExceptId(variableList.value, variable.id);
  currEditData.value = undefined;
  addBtnDisabled.value = false;
};

// 提起公共代码 校验数据未保存
const getChenkUpdateRes = () => {
  if (currEditData.value) {
    const hasUpdate = chenkUpdate(variableList.value.filter(item => item.id === currEditData.value?.id)[0]);
    if (hasUpdate) {
      notification.warning(t('xcan_variable.dataNotSaved'));
      return true;
    }
  }
  return false;
};

const lastIsExpandState = ref(false);
// 开启关闭编辑 同时修改展开收起
const handleEdit = (event, variable:VariableObj) => {
  event.stopPropagation();
  const hasEditData = variableList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      variableList.value = variableList.value.filter(item => item.id !== hasEditData[0].id);
    } else {
      const checkRes = getCheckDataResult(hasEditData[0]);
      if (checkRes) {
        return;
      }
      if (getChenkUpdateRes()) {
        return;
      }
    }
  }
  lastIsExpandState.value = variable.isExpand;
  variable.isEdit = true;
  variable.isExpand = true;
  currEditData.value = variable;
  setEditFalseExceptId(variableList.value, variable.id);
  addBtnDisabled.value = false;
};

// 取消编辑
const cancelEdit = (event, variable:VariableObj) => {
  event.stopPropagation();
  // 如果取消的是新增的数据
  if (variable.isAdd) {
    // 如果列表有多条数据 取消后删除新增的数据 并启用添加按钮
    variableList.value = variableList.value.filter(item => item.id !== variable.id);
    addBtnDisabled.value = false;
    currEditData.value = undefined;
    return;
  }

  //  如果取消的是历史数据 判断数据有没有修改，然后收起详情并取消编辑状态
  const hasUpdate = chenkUpdate(variable);
  //  如果有修改,取消编辑先恢复数据
  if (hasUpdate) {
    const oldSync = oldVaribleList.value.find(item => item.id === variable.id);
    for (let i = 0; i < variableList.value.length; i++) {
      if (variable.id === variableList.value[i].id) {
        variableList.value[i] = oldSync ? JSON.parse(JSON.stringify(oldSync)) : variable;
        break;
      }
    }
  }
  variable.isEdit = false;
  variable.isExpand = lastIsExpandState.value;
  currEditData.value = undefined;
  addBtnDisabled.value = false;
};

// 判断编辑的数据有无改变
const chenkUpdate = (newData:VariableObj) => {
  const _oldDataList = oldVaribleList.value.filter(item => item?.id === newData?.id);
  if (!_oldDataList?.length) {
    return true;
  }

  // eslint-disable-next-line
  const { isEdit: _oldEdit, isExpand: _oldExpand, ..._oldData } = _oldDataList[0];
  // eslint-disable-next-line
  const { isEdit, isExpand, ..._newData } = newData;

  return !utils.deepCompare(_oldData, _newData);
};

// 收起当前数据之外的数据并取消编辑
const setEditFalseExceptId = (arr:VariableObj[], id:string):void => {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i].id !== id) {
      arr[i].isEdit = false;
      arr[i].isExpand = false;
    }
  }
};

// 变量名称校验
const variableNameChange = (value:string, variable:VariableObj):void => {
  variable.nameErr = !value;
};
// 变量值校验
const variableValueChange = (value:string, variable:VariableObj):void => {
  variable.valueErr = !value;
};

const typeChange = (value:RadioChangeEvent, variable:VariableObj) => {
  if (value && !variable?.extraction) {
    variable.extraction = {
      defaultValue: '',
      expression: '',
      failureMessage: '',
      finalValue: '',
      location: 'PATH_PARAMETER',
      method: { value: 'EXACT_VALUE', message: '' },
      name: '',
      parameterName: '',
      parameterNameErr: false,
      expressionErr: false,
      value: ''
    };
  }

  variable.valueErr = false;
  variable.extraction.parameterNameErr = false;
  variable.extraction.expressionErr = false;
};

const name = ref('');
const handleSearch = debounce(duration.search, (event) => {
  params.value.pageNo = 1;
  const value = event.target.value;
  if (value) {
    params.value.filters = [{ key: 'name', value: value, op: 'MATCH_END' }];
  } else {
    params.value.filters = [];
  }
  notify.value++;
});

const methodOptions = ref<EnumMessage<ExtractionMethod>[]>();
const getMethodOptions = async () => {
  methodOptions.value = enumUtils.enumToMessages(ExtractionMethod);
};

const nameTips = computed(() => {
  if (props.tabKey === 'CURRENT') {
    if (props.type === 'API') {
      return t('xcan_variable.sameInterfaceVariableNameNotAllowed');
    }
    if (props.type === 'PROJECT') {
      return t('xcan_variable.sameProjectVariableNameNotAllowed');
    }
    if (props.type === 'SERVICE') {
      return t('xcan_variable.sameServiceVariableNameNotAllowed');
    }
  }

  return t('xcan_variable.globalVariableNameNotAllowed');
});

const scopeTips = computed(() => {
  if (props.tabKey === 'CURRENT') {
    return t('xcan_variable.currentScopeDescription');
  }

  return t('xcan_variable.globalScopeDescription');
});

const parameterNameChange = (value:string, variable:VariableObj) => {
  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(variable.extraction.location)) {
    variable.extraction.parameterNameErr = false;
    return;
  }

  variable.extraction.parameterNameErr = !value;
};

const locationChange = (value:string, variable:VariableObj) => {
  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(value)) {
    variable.extraction.parameterNameErr = false;
  }
};
const expressionChange = (value:string, variable:VariableObj) => {
  variable.extraction.expressionErr = !value;
};
const methodChange = (variable:VariableObj) => {
  variable.extraction.parameterNameErr = false;
  variable.extraction.expressionErr = false;
};

watch(() => props.id, (newValue) => {
  params.value.targetId = newValue;
  notify.value++;
});

onMounted(() => {
  getMethodOptions();
});
</script>
<template>
  <div class="flex items-end my-2">
    <Input
      v-model:value="name"
      allowClear
      :placeholder="t('xcan_variable.searchVariableName')"
      @change="handleSearch" />
    <template v-if="props.tabKey !== 'INHERIT'">
      <Button
        size="small"
        type="primary"
        class="flex items-center ml-2"
        :disabled="props.disabled || variableList.length > 49 || addBtnDisabled || total > 49"
        @click="addVariable">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('xcan_variable.add') }}
      </Button>
    </template>
  </div>
  <Scroll
    v-model:spinning="loading"
    v-model:total="total"
    :hideNoData="!!variableList.length"
    :action="action"
    :params="params"
    :lineHeight="lineHeight"
    :transition="false"
    :notify="notify"
    class="flex-1 -mr-3.5"
    @change="getList">
    <div
      v-for="variable,index in variableList"
      :key="variable.id"
      class="border border-border-divider p-2 rounded mr-2"
      :class="{'mt-2':index !== 0}">
      <div v-if="!variable.isAdd && !variable.isEdit">
        <div class="flex text-3 leading-4">
          <div class="flex flex-col text-theme-sub-content space-y-2">
            <div>{{ t('xcan_variable.name') }}<Colon /></div>
            <template v-if="variable.type">
                              <div>{{ t('xcan_variable.extractionMethod') }}<Colon /></div>
              <template v-if="variable.extraction.method.value === 'EXACT_VALUE'">
                <!-- <div>提取值<Colon /></div> -->
              </template>
              <template v-else-if="variable.extraction.method.value === 'REGEX'">
                <div>{{ t('xcan_variable.regexExpression') }}<Colon /></div>
              </template>
              <template v-else>
                                  <div>{{ t('xcan_variable.extractionExpression') }}<Colon /></div>
              </template>
            </template>
            <template v-else>
                              <div>{{ t('xcan_variable.value') }}<Colon /></div>
            </template>
            <template v-if="props.tabKey !== 'CURRENT'">
                              <div>{{ t('xcan_variable.source') }}<Colon /></div>
            </template>
                          <div>{{ t('xcan_variable.scope') }}<Colon /></div>
              <div>{{ t('xcan_variable.lastModified') }}<Colon /></div>
          </div>
          <div class="flex flex-col text-theme-content space-y-2 ml-2">
            <div class="truncate h-4" style="max-width: 160px;">
              <Tooltip :title="variable.name" placement="top">
                {{ variable.name }}
              </Tooltip>
            </div>
            <template v-if="variable.type">
              <div class="h-4">{{ variable.extraction.method.message }}</div>
              <template v-if="variable.extraction.method.value === 'EXACT_VALUE'">
                <!-- <div class="truncate h-4" style="max-width: 160px;">
                    <Tooltip :title="variable.extraction.value" placement="top">
                      {{ variable.extraction.value }}
                    </Tooltip>
                  </div> -->
              </template>
              <template v-else-if="variable.extraction.method.value === 'REGEX'">
                <div class="truncate h-4" style="max-width: 160px;">
                  <Tooltip :title="variable.extraction.expression" placement="top">
                    {{ variable.extraction.expression }}
                  </Tooltip>
                </div>
              </template>
              <template v-else>
                <div class="truncate h-4" style="max-width: 160px;">
                  <Tooltip :title="variable.extraction.expression" placement="top">
                    {{ variable.extraction.expression }}
                  </Tooltip>
                </div>
              </template>
            </template>
            <template v-else>
              <div class="truncate h-4" style="max-width: 160px;">
                <Tooltip :title="variable.value" placement="top">
                  {{ variable.value }}
                </Tooltip>
              </div>
            </template>
            <template v-if="props.tabKey !== 'CURRENT'">
              <div class="flex items-center">
                <template v-if="variable.targetType.value === 'PROJECT'">
                  <Tooltip :title="t('xcan_variable.project')" placement="top">
                    <label class="w-4 h-4 rounded-full text-white text-center leading-4 bg-blue-badge-p">
                      P
                    </label>
                  </Tooltip>
                </template>
                <template v-if="variable.targetType.value === 'SERVICE'">
                  <Tooltip :title="t('xcan_variable.service')" placement="top">
                    <label class="w-4 h-4 rounded-full text-white text-center leading-4 bg-blue-badge-s">
                      S
                    </label>
                  </Tooltip>
                </template>
                <template v-if="variable.targetType.value === 'API'">
                  <Tooltip :title="t('xcan_variable.api')" placement="top">
                    <label class="w-4 h-4 rounded-full text-white text-center leading-4 bg-blue-300">
                      A
                    </label>
                  </Tooltip>
                </template>
                <div class="truncate h-4 ml-1" style="max-width: 160px;">
                  <Tooltip :title="variable.targetName" placement="top">
                    {{ variable.targetName }}
                  </Tooltip>
                </div>
              </div>
            </template>
            <!--            <div class="h-4">{{ variable.scope.message }}</div>-->
            <div class="flex">
              <div class="mr-2 truncate h-4" style="max-width: 60px;">
                <Tooltip :title="variable.createdByName" placement="top">
                  {{ variable.createdByName }}
                </Tooltip>
              </div>
              <div class="h-4">{{ variable.createdDate }}</div>
            </div>
          </div>
        </div>
        <div class="text-3 flex justify-end ml-2 mt-2 items-center">
          <template v-if="props.tabKey === 'CURRENT'">
            <Switch
              :checked="variable.enabled"
              :disabled="props.disabled"
              size="small"
              class="w-8 mr-2"
              @change="variableSwitchChange(variable)" />
            <Tooltip :title="t('xcan_variable.edit')" placement="top">
              <template v-if="props.disabled">
                <Icon
                  icon="icon-xiugai"
                  class="mr-1 cursor-not-allowed text-text-disabled" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-xiugai"
                  class="mr-1"
                  :class="variable.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                  @click="(e) => handleEdit(e, variable)" />
              </template>
            </Tooltip>
            <Tooltip :title="t('xcan_variable.delete')" placement="top">
              <template v-if="props.disabled">
                <Icon
                  icon="icon-qingchu"
                  class="mr-1 cursor-not-allowed text-text-disabled" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-qingchu"
                  class="mr-1"
                  :class="variable.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                  @click="hanldeDelete(variable)" />
              </template>
            </Tooltip>
          </template>
          <template v-if="props.tabKey === 'GLOBAL' && variable.targetId === props.id">
            <Tooltip :title="t('xcan_variable.edit')" placement="top">
              <template v-if="props.disabled">
                <Icon
                  icon="icon-xiugai"
                  class="mr-1 cursor-not-allowed text-text-disabled" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-xiugai"
                  class="mr-1"
                  :class="variable.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                  @click="(e) => handleEdit(e, variable)" />
              </template>
            </Tooltip>
            <Tooltip :title="t('xcan_variable.delete')" placement="top">
              <template v-if="props.disabled">
                <Icon
                  icon="icon-qingchu"
                  class="mr-1 cursor-not-allowed text-text-disabled" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-qingchu"
                  class="mr-1"
                  :class="variable.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                  @click="hanldeDelete(variable)" />
              </template>
            </Tooltip>
          </template>
          <Arrow
            :open="variable.isExpand"
            @click="(e) => handleExpand(e, variable)" />
        </div>
      </div>
      <div :class="{'border-t border-border-divider border-dashed pt-2 mt-2':!variable.isAdd && !variable.isEdit && variable.isExpand}"></div>
      <div
        :class="variable.isExpand ? 'open-info' : 'stop-info'"
        class="transition-height duration-500 overflow-hidden leading-3 text-3">
        <template v-if="variable.isExpand">
          <div class="flex items-center">
            <IconRequired />{{ t('xcan_variable.name') }}
            <Tooltip
              :title="nameTips"
              placement="top"
              :overlayStyle="{maxWidth:'300px'}">
              <Icon
                icon="icon-tishi1"
                class="text-tips ml-1 text-3.5" />
            </Tooltip>
          </div>
          <Input
            v-model:value="variable.name"
            :error="variable.nameErr"
            :maxlength="400"
            :disabled="!variable.isEdit"
            dataType="mixin-en"
            includes="!@$%^&*()_-+="
            class="mt-2 mb-5"
            :placeholder="t('xcan_variable.inputVariableName')"
            size="small"
            @change="(event)=>variableNameChange(event.target.value,variable)" />
          <div><IconRequired />{{ t('xcan_variable.type') }}</div>
          <RadioGroup
            v-model:value="variable.type"
            :disabled="!variable.isEdit"
            class="mt-2 mb-5"
            @change="(value)=>typeChange(value,variable)">
            <Radio :value="false">{{ t('xcan_variable.define') }}</Radio>
            <Radio :value="true" :disabled="props.type !== 'API'">
              {{ t('xcan_variable.extract') }}
              <Tooltip placement="top" :title="t('xcan_variable.extractVariableNotSupportDependency')">
                <Icon icon="icon-tishi1" class="text-tips cursor-pointer text-3.5 -mt-0.5" />
              </Tooltip>
            </Radio>
          </RadioGroup>
          <template v-if="!variable.type">
            <div><IconRequired />{{ t('xcan_variable.value') }}</div>
            <Input
              v-model:value="variable.value"
              :error="variable.valueErr"
              :maxlength="400"
              :disabled="!variable.isEdit"
              :placeholder="t('xcan_variable.inputVariableValue')"
              size="small"
              class="mt-2 mb-5"
              @change="(event)=>variableValueChange(event.target.value,variable)" />
          </template>
          <template v-else>
            <Tabs
              v-model:props.tabKey="variable.extraction.method.value"
              class="variable-method-tab"
              @change="methodChange(variable)">
              <TabPane
                v-for="tab in methodOptions"
                :key="tab.value"
                :tab="tab.message" />
            </Tabs>
            <div class="flex flex-col mt-1">
              <InputGroup compact class="w-full">
                <SelectEnum
                  v-model:value="variable.extraction.location"
                  :disabled="!variable.isEdit"
                  style="width: 40%;"
                  :enumKey="HttpExtractionLocation"
                  @change="(value)=>locationChange(value,variable)" />
                <Input
                  v-model:value="variable.extraction.parameterName"
                  :disabled="!variable.isEdit || ['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(variable.extraction.location)"
                  :error="variable.extraction.parameterNameErr"
                  :maxlength="400"
                  style="width: 60%;"
                  :placeholder="t('xcan_variable.inputExtractionParameterName')"
                  @change="(event)=>parameterNameChange(event.target.value,variable)" />
              </InputGroup>
              <template v-if="variable.extraction?.method?.value ==='REGEX'">
                <Input
                  v-model:value="variable.extraction.expression"
                  :maxlength="1024"
                  :disabled="!variable.isEdit"
                  :error="variable.extraction.expressionErr"
                  :placeholder="t('xcan_variable.inputRegexExpression')"
                  size="small"
                  class="mt-3"
                  @change="(event)=>expressionChange(event.target.value,variable)" />
              </template>
              <template v-else-if="variable.extraction?.method?.value ==='EXACT_VALUE'">
                <!-- <Input
                    v-model:value="variable.extraction.value"
                    :maxlength="400"
                    :disabled="!variable.isEdit"
                    placeholder="输入提取值"
                    size="small"
                    class="mt-3" /> -->
              </template>
              <template v-else>
                <Input
                  v-model:value="variable.extraction.expression"
                  :maxlength="1024"
                  :disabled="!variable.isEdit"
                  :error="variable.extraction.expressionErr"
                  :placeholder="t('xcan_variable.inputExtractionExpression')"
                  size="small"
                  class="mt-3"
                  @change="(event)=>expressionChange(event.target.value,variable)" />
              </template>
              <Input
                v-model:value="variable.extraction.defaultValue"
                :maxlength="4096"
                :disabled="!variable.isEdit"
                :placeholder="t('xcan_variable.inputDefaultValue')"
                size="small"
                class="mt-3 mb-5" />
            </div>
          </template>
          <!--          <div class="flex items-center">-->
          <!--            <IconRequired />作用范围-->
          <!--            <Tooltip-->
          <!--              :title="scopeTips"-->
          <!--              placement="top"-->
          <!--              :overlayStyle="{maxWidth:'300px'}">-->
          <!--              <Icon-->
          <!--                icon="icon-tishi1"-->
          <!--                class="text-tips ml-1 text-3.5" />-->
          <!--            </Tooltip>-->
          <!--          </div>-->
          <!--          <SelectEnum-->
          <!--            v-model:value="variable.scope.value"-->
          <!--            :enumKey="VariableScope"-->
          <!--            class="w-full mt-2"-->
          <!--            disabled />-->
          <template v-if="variable.isEdit">
            <div class="pl-1.75 mt-5">{{ t('xcan_variable.description') }}</div>
            <Textarea
              v-model:value="variable.description"
              :placeholder="t('xcan_variable.inputDescription')"
              size="small"
              class="mt-2"
              :maxlength="400"
              :rows="3" />
          </template>
          <template v-else>
            <template v-if="variable.description">
              <div class="pl-1.75 mt-5">{{ t('xcan_variable.description') }}</div>
              <Textarea
                :value="variable.description"
                size="small"
                disabled
                class="mt-2"
                :autoSize="{ minRows: 2, maxRows: 6 }" />
            </template>
          </template>
          <div class="flex justify-end mt-3">
            <template v-if="variable.isEdit">
              <Button
                size="small"
                class="p-0"
                type="link"
                :disabled="variable.saveloading || !variableList.length"
                @click="handleSave(variable)">
                {{ t('xcan_variable.save') }}
              </Button>
              <Button
                type="link"
                size="small"
                class="p-0 ml-2"
                @click="(e) => cancelEdit(e,variable)">
                {{ t('xcan_variable.cancel') }}
              </Button>
            </template>
          </div>
        </template>
      </div>
    </div>
  </Scroll>
</template>
<style scoped>
.open-info {
  max-height: auto;
}

.stop-info {
  height: 0;
}

:deep(.hints-text > svg) {
  visibility: hidden;
}

:deep(.variable-method-tab .ant-tabs-tab) {
  padding: 0;
}

:deep(.variable-method-tab .ant-tabs-tab+.ant-tabs-tab) {
  margin: 0;
}

:deep(.variable-method-tab .ant-tabs-nav-list) {
  @apply justify-between flex-1;
}

:deep(.variable-method-tab .ant-tabs-nav::before) {
  border: 0;
}

:deep(.variable-method-tab .ant-tabs-ink-bar) {
  display: none;
}
</style>
