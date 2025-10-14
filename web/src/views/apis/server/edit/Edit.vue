<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button, Radio } from 'ant-design-vue';
import { Icon, IconRequired, Input, notification, Select, Tooltip, Validate } from '@xcan-angus/vue-ui';
import { duration, TESTER, utils } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
import { ServerConfig } from '@/views/apis/server/types';

type Props = {
  projectId: string;
  value: ServerConfig;
  serviceId?: string;
  urlMap?: { [key: string]: string[] };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  value: undefined,
  serviceId: undefined,
  urlMap: () => ({})
});

const { t } = useI18n();

const serverId = ref<string>();
const url = ref<string>();
const urlError = ref(false);
const urlErrorMsg = ref<string>();
const serviceIdValue = ref<string>();
const serviceIdError = ref(false);
const description = ref<string>();
const variableIds = ref<string[]>([]);
const variableDataMap = ref<{ [key: string]: ServerConfig['variables'][number] }>({});
const defaultValueMap = ref<{ [key: string]: string | undefined }>({});
const nameErrorSet = ref<Set<string>>(new Set());
const nameErrorMsgMap = ref<{ [key: string]: string | undefined }>({});
const valueErrorSet = ref<Set<string>>(new Set());
const valueErrorMsgMap = ref<{ [key: string]: string | undefined }>({});
const insertIndexMap = ref<{ [key: string]: number }>({});

/**
 * Add a variable block and initialize its enum list.
 * When resetFlag is true, reset to a single empty variable state.
 */
const addVariable = (resetFlag = true) => {
  const id = utils.uuid();
  const firstEnumId = utils.uuid();
  if (resetFlag) {
    variableIds.value = [id];
    variableDataMap.value = {
      [id]: {
        default: '',
        description: '',
        id: id,
        enum: [{ id: firstEnumId, value: '' }],
        name: ''
      }
    };
    defaultValueMap.value = {
      [id]: ''
    };
    nameErrorSet.value.clear();
    valueErrorSet.value.clear();
  } else {
    variableIds.value.push(id);
    variableDataMap.value[id] = {
      default: '',
      description: '',
      id,
      enum: [{ id: firstEnumId, value: '' }],
      name: ''
    };
    defaultValueMap.value[id] = '';
  }

  defaultValueMap.value[id] = firstEnumId;
};

/**
 * Handle URL change with debounce and synchronize variables extracted from the URL.
 */
const onUrlChange = debounce(duration.delay, (event: { target: { value: string; } }) => {
  urlError.value = false;
  urlErrorMsg.value = undefined;

  const value = event.target.value;
  let uniqueNames: string[] = [];
  const matchItems = value.match(/\{[^{}]+\}/g);
  if (matchItems) {
    const _dataMap = Object.values(variableDataMap.value).reduce((prev, cur) => {
      prev[cur.name] = cur;
      return prev;
    }, {} as { [key: string]: ServerConfig['variables'][number] });
    // Filter duplicate variables extracted from the URL
    uniqueNames = matchItems?.reduce((prev, cur) => {
      if (!prev.includes(cur)) {
        prev.push(cur);
      }
      return prev;
    }, [] as string[]).map(item => item.replace(/\{(.+)\}/, '$1'));
    for (let i = 0, len = uniqueNames.length; i < len; i++) {
      // Check whether the variable already exists
      const _name = uniqueNames[i];
      if (!_dataMap[_name]) {
        const _vid = variableIds.value[i];
        if (variableDataMap.value[_vid]) {
          // The name at this index already exists in variables extracted from the URL
          if (uniqueNames.includes(variableDataMap.value[_vid].name)) {
            // Add a new variable
            const _id = utils.uuid();
            variableIds.value[i] = _id;
            variableDataMap.value[_id] = {
              default: '',
              description: '',
              id: _id,
              enum: [{ id: utils.uuid(), value: '' }],
              name: _name
            };
            defaultValueMap.value[_id] = '';
            defaultValueMap.value[_id] = variableDataMap.value[_id].enum[0].id;
          } else {
            variableDataMap.value[_vid].name = _name;
          }
        } else {
          // Add a new variable
          const _id = utils.uuid();
          variableIds.value[i] = _id;
          variableDataMap.value[_id] = {
            default: '',
            description: '',
            id: _id,
            enum: [{ id: utils.uuid(), value: '' }],
            name: _name
          };
          defaultValueMap.value[_id] = '';
          defaultValueMap.value[_id] = variableDataMap.value[_id].enum[0].id;
        }
      } else {
        variableIds.value[i] = _dataMap[_name].id;
      }
    }
  }

  // Remove variables that no longer exist in the URL
  const delIds = Object.values(variableDataMap.value).filter(item => !uniqueNames.includes(item.name)).map(item => item.id);
  variableIds.value = variableIds.value.filter(item => !delIds.includes(item)).reduce((prev, cur) => {
    if (!prev.includes(cur)) {
      prev.push(cur);
    }
    return prev;
  }, [] as string[]);
  for (let i = 0, len = delIds.length; i < len; i++) {
    const delId = delIds[i];
    const _enumList = variableDataMap.value[delId].enum;
    for (const item of _enumList) {
      valueErrorSet.value.delete(item.id);
    }
    delete variableDataMap.value[delId];
    delete defaultValueMap.value[delId];
    delete nameErrorMsgMap.value[delId];
    delete valueErrorMsgMap.value[delId];
    nameErrorSet.value.delete(delId);
  }

  insertIndexMap.value = {};
  // If all variables are removed, automatically add an empty one
  if (variableIds.value.length === 0) {
    addVariable();
  }
});

/**
 * Validate URL format and variable duplication when input loses focus.
 */
const onUrlBlur = (event: { target: { value: string; } }) => {
  const value = event.target.value;
  isServerUrl(value);
};

/**
 * Clear service selection error state when selection changes.
 */
const onServiceIdChange = () => {
  serviceIdError.value = false;
};

/**
 * Validate URL uniqueness and repeated placeholders.
 */
const isServerUrl = (_url: string): boolean => {
  // @TODO Validate via regex (to be refined)
  if (props.urlMap[_url]) {
    if (!serverId.value || props.urlMap[_url].length > 1 || !props.urlMap[_url].includes(serverId.value)) {
      urlError.value = true;
      urlErrorMsg.value = t('apiServer.messages.serverUrlExistedTip');
      return false;
    }
  }

  const names = _url.match(/\{[^{}]+\}/g);
  if (names) {
    const uniqueNames = names.reduce((prev, cur) => {
      if (!prev.includes(cur)) {
        prev.push(cur);
      }
      return prev;
    }, [] as string[]);
    if (names.length > uniqueNames.length) {
      urlError.value = true;
      urlErrorMsg.value = t('apiServer.messages.serverVariableDuplicatedTip');
      return false;
    }
  }

  return true;
};

/**
 * Remove a variable and sync URL placeholders accordingly.
 */
const deleteVariable = (id: string, index: number) => {
  variableIds.value.splice(index, 1);
  const delName = variableDataMap.value[id].name;
  const _enumList = variableDataMap.value[id].enum;
  _enumList.every(item => {
    valueErrorSet.value.delete(item.id);
    return true;
  });
  delete variableDataMap.value[id];
  delete defaultValueMap.value[id];
  delete nameErrorMsgMap.value[id];
  delete valueErrorMsgMap.value[id];
  nameErrorSet.value.delete(id);

  if (delName && url.value) {
    // Sync deletion in URL
    const rex = new RegExp('(:?\\/\\/)?\\/?\\{' + delName + '\\}', 'g');
    url.value = url.value.replace(rex, '$1');
  }

  // If all variables are removed, automatically add an empty one
  if (variableIds.value.length === 0) {
    addVariable();
  }
};

/**
 * Handle variable name changes and update URL placeholders.
 */
const nameChange = (event: { target: { value: string } }, id: string, index: number) => {
  nameErrorSet.value.delete(id);
  nameErrorMsgMap.value[id] = undefined;
  const value = event.target.value;
  if (url.value) {
    if (value) {
      const insertIndex = insertIndexMap.value[id];
      if (insertIndex >= 0) {
        url.value = url.value.slice(0, insertIndex) + `{${value}}` + url.value.slice(insertIndex);
        delete insertIndexMap.value[id];
      } else {
        // Replace variable name in URL
        const _variables = url.value.match(/\{[^{}]+\}/g)?.map(item => item.replace(/\{(.+)\}/, '$1')).reduce((prev, cur) => {
          if (!prev.includes(cur)) {
            prev.push(cur);
          }
          return prev;
        }, [] as string[]);
        if (_variables && _variables[index]) {
          const rex = new RegExp('\\{' + _variables[index] + '\\}', 'g');
          url.value = url.value.replace(rex, `{${value}}`);
        } else {
          // Variable not found; append to the end of the URL
          url.value += url.value.endsWith('/') ? `{${value}}` : `/{${value}}`;
        }
        urlError.value = false;
        urlErrorMsg.value = undefined;
        isServerUrl(url.value);
      }
    } else {
      // Record the index of the removed variable
      insertIndexMap.value[id] = url.value.indexOf('{' + variableDataMap.value[id].name + '}');
      // Remove variable name from URL
      const _variables = url.value.match(/\{[^{}]+\}/g)?.map(item => item.replace(/\{(.+)\}/, '$1')).reduce((prev, cur) => {
        if (!prev.includes(cur)) {
          prev.push(cur);
        }
        return prev;
      }, [] as string[]);
      if (_variables && _variables[index]) {
        const rex = new RegExp('(:?\\/\\/)?\\/?\\{' + _variables[index] + '\\}', 'g');
        url.value = url.value.replace(rex, '$1');
        urlError.value = false;
        urlErrorMsg.value = undefined;
        isServerUrl(url.value);
      }
    }
  } else {
    url.value = `{${value}}`;
  }

  variableDataMap.value[id].name = value;

  // Validate duplicate variable names
  validRepeatName();
};

/**
 * Handle variable enum value changes with debounce and validation.
 */
const variableValueChange = debounce(duration.delay, (event: { target: { value: string; } }, pid: string, cid: string, index: number) => {
  valueErrorSet.value.delete(cid);
  valueErrorMsgMap.value[cid] = undefined;
  const len = variableDataMap.value[pid].enum.length - 1;
  if (index === len) {
    // When the first enum receives input, select it as default
    if (len === 0 && event.target.value) {
      defaultValueMap.value[pid] = cid;
    }

    variableDataMap.value[pid].enum.push({ id: utils.uuid(), value: '' });
  }

  // Validate duplicate enum values
  validRepeatValue(pid);
});

/**
 * Update default enum selection for a variable.
 */
const defaultValueChange = (pid: string, cid: string) => {
  defaultValueMap.value[pid] = cid;
};

/**
 * Delete an enum value and adjust default selection if needed.
 */
const deleteVariableValue = (id: string, index: number) => {
  const defaultEnumId = defaultValueMap.value[id];
  const defaultEnum = variableDataMap.value[id].enum.find(item => item.id === defaultEnumId);
  const delEle = variableDataMap.value[id].enum.splice(index, 1)[0];
  if (defaultEnum?.value === delEle.value) {
    defaultValueMap.value[id] = variableDataMap.value[id].enum[0]?.id;
  }
};

/**
 * Reset all error-related states.
 */
const resetError = () => {
  serviceIdError.value = false;
  urlError.value = false;
  urlErrorMsg.value = undefined;
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
  nameErrorMsgMap.value = {};
  valueErrorMsgMap.value = {};
};

/**
 * Validate the form and return whether it is valid.
 */
const isValid = (): boolean => {
  resetError();
  let errorNum = 0;
  if (!url.value) {
    urlError.value = true;
    errorNum++;
  } else {
    if (!isServerUrl(url.value)) {
      errorNum++;
    }
  }

  if (!serviceIdValue.value) {
    serviceIdError.value = true;
    errorNum++;
  }

  const ids = variableIds.value;
  const dataMap = variableDataMap.value;
  if (variableIds.value.length === 1) {
    const id = ids[0];
    const { enum: enumList, name, description } = dataMap[id];
    // If name/description/any enum value is non-empty, run validation
    const validFlag = name || description || !!enumList.find(item => !!item.value);
    if (validFlag) {
      if (!name) {
        nameErrorSet.value.add(id);
        errorNum++;
      }
      errorNum += validVariableValue(enumList);
    }
  } else {
    const nameMap = Object.values(variableDataMap.value).reduce((prev, cur) => {
      const { name, id } = cur;
      if (prev[name]) {
        prev[name].push(id);
      } else {
        prev[name] = [id];
      }
      return prev;
    }, {} as { [key: string]: string[] });
    for (let i = 0, len = ids.length; i < len; i++) {
      const id = ids[i];
      const { enum: enumList, name } = dataMap[id];
      if (!name) {
        nameErrorSet.value.add(id);
        errorNum++;
      } else {
        if (nameMap[name].length > 1) {
          nameErrorMsgMap.value[id] = t('apiServer.messages.variableNameDuplicatedTip');
          nameErrorSet.value.add(id);
          errorNum++;
        }
      }
      errorNum += validVariableValue(enumList);
    }
  }
  return !errorNum;
};

const validVariableValue = (enumList: { id: string; value: string }[]): number => {
  let errorNum = 0;
  if (enumList.length === 1) {
    if (!enumList[0].value) {
      valueErrorSet.value.add(enumList[0].id);
      errorNum++;
    }
  } else {
    const repeatEnumMap = enumList.reduce((prev, cur) => {
      if (cur.value) {
        if (prev[cur.value]) {
          prev[cur.value] += 1;
        } else {
          prev[cur.value] = 1;
        }
      }

      return prev;
    }, {} as { [key: string]: number });

    for (let i = 0, len = enumList.length; i < len; i++) {
      const { id: _id, value: _value } = enumList[i];
      // The last item is an empty placeholder
      if (i < len - 1) {
        if (!_value) {
          valueErrorSet.value.add(_id);
          errorNum++;
        } else {
          if (repeatEnumMap[_value] && repeatEnumMap[_value] > 1) {
            valueErrorSet.value.add(_id);
            valueErrorMsgMap.value[_id] = t('apiServer.messages.variableValueDuplicatedTip');
            errorNum++;
          }
        }
      }
    }
  }
  return errorNum;
};

const validRepeatName = () => {
  const repeatMap = Object.values(variableDataMap.value).reduce((prev, cur) => {
    const _name = cur.name;
    if (_name) {
      if (prev[_name]) {
        prev[_name] += 1;
      } else {
        prev[_name] = 1;
      }
    }
    return prev;
  }, {});

  const ids = variableIds.value;
  const dataMap = variableDataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (repeatMap[dataMap[id].name] && repeatMap[dataMap[id].name] > 1) {
      nameErrorSet.value.add(id);
      nameErrorMsgMap.value[id] = t('apiServer.messages.variableNameDuplicatedTip');
    } else {
      if (nameErrorMsgMap.value[id]) {
        nameErrorSet.value.delete(id);
        nameErrorMsgMap.value[id] = undefined;
      }
    }
  }
};

const validRepeatValue = (id: string) => {
  const enumList = variableDataMap.value[id].enum;
  const repeatMap = Object.values(enumList).reduce((prev, cur) => {
    const _value = cur.value;
    if (_value) {
      if (prev[_value]) {
        prev[_value] += 1;
      } else {
        prev[_value] = 1;
      }
    }
    return prev;
  }, {});

  for (let i = 0, len = enumList.length; i < len; i++) {
    const { id: _id, value: _value } = enumList[i];
    if (repeatMap[_value] && repeatMap[_value] > 1) {
      valueErrorSet.value.add(_id);
      valueErrorMsgMap.value[_id] = t('apiServer.messages.variableValueDuplicatedTip');
    } else {
      if (valueErrorMsgMap.value[_id]) {
        valueErrorSet.value.delete(_id);
        valueErrorMsgMap.value[_id] = undefined;
      }
    }
  }
};

/**
 * Build ServerConfig object from current reactive state.
 */
const getData = (): ServerConfig => {
  const variables: ServerConfig['variables'] = [];
  const ids = variableIds.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { enum: enumList, name } = variableDataMap.value[id];
    if (name) {
      const _enums: { id: string; value: string }[] = [];
      // The last item is an empty placeholder
      let defaultEnum = '';
      const checkedId = defaultValueMap.value[id];
      for (let j = 0, _len = enumList.length - 1; j < _len; j++) {
        const _enum = enumList[j];
        if (_enum.id === checkedId) {
          defaultEnum = _enum.value;
        }
        _enums.push({ ..._enum });
      }
      variables.push({ ...variableDataMap.value[id], enum: _enums, default: defaultEnum });
    }
  }

  return {
    ...props.value,
    description: description.value,
    url: url.value as string,
    serviceId: serviceIdValue.value as string,
    variables
  };
};

/**
 * Reset all form states to initial values.
 */
const reset = () => {
  url.value = undefined;
  urlError.value = false;
  urlErrorMsg.value = undefined;
  description.value = undefined;
  variableIds.value = [];
  variableDataMap.value = {};
  defaultValueMap.value = {};
  nameErrorSet.value.clear();
  nameErrorMsgMap.value = {};
  valueErrorSet.value.clear();
  valueErrorMsgMap.value = {};
  insertIndexMap.value = {};
};

onMounted(() => {
  watch(() => props.serviceId, (newValue) => {
    serviceIdValue.value = newValue;
  }, { immediate: true });

  watch(() => props.value, () => {
    reset();
    if (props.value) {
      serverId.value = props.value.id;
      url.value = props.value.url;
      description.value = props.value.description;
      const variables = props.value.variables;
      if (variables?.length) {
        for (let i = 0, len = variables.length; i < len; i++) {
          const item = variables[i];
          const id = item.id;
          variableIds.value.push(id);
          variableDataMap.value[id] = JSON.parse(JSON.stringify(item));
          defaultValueMap.value[id] = item.enum?.find(_ele => _ele.value === item.default)?.id;
          variableDataMap.value[id].enum.push({ id: utils.uuid(), value: '' });
        }
      }
    }

    if (variableIds.value.length === 0) {
      addVariable();
    }
  }, { immediate: true });
});

const addServerBtnDisabled = computed(() => {
  return variableIds.value.length >= 50;
});

defineExpose({
  getData: () => {
    if (!isValid()) {
      notification.error(t('apiServer.messages.serverConfigErrorTip'));
      return;
    }
    const data = getData();
    return data;
  }
});
</script>
<template>
  <div class="flex-1 flex-col overflow-auto pr-3">
    <div class="flex items-center mb-3.5">
      <div class="flex items-center leading-7 justify-end text-right pr-3" style="width: 100px;">
        <IconRequired />
        <span class="font-semibold">URL</span>
      </div>
      <Validate class="flex-1" :text="urlErrorMsg">
        <Input
          v-model:value="url"
          :placeholder="t('apiServer.messages.urlPlaceholder') + 'https://{env}.xcan.cloud:{prot}/{path}'"
          trimAll
          :autoSize="true"
          :error="urlError"
          :maxlength="400"
          @change="onUrlChange"
          @blur="onUrlBlur" />
      </Validate>
    </div>

    <div class="flex items-center mb-3.5">
      <div class="flex items-center leading-7 justify-end text-right pr-3" style="width: 100px;">
        <IconRequired />
        <span class="font-semibold">{{ t('common.service') }}</span>
      </div>

      <div class="flex mr-1">
        <Select
          v-model:value="serviceIdValue"
          :placeholder="t('common.placeholders.selectService')"
          style="width:300px;"
          :fieldNames="{label:'name',value:'id'}"
          :error="serviceIdError"
          :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
          @change="onServiceIdChange" />
      </div>
      <div class="text-gray-400">
        <Icon icon="icon-tishi1" class="text-text-tip text-3.5 cursor-pointer mr-1" />
        {{ t('apiServer.messages.serverServiceTip') }}
      </div>
    </div>

    <div class="flex items-start mb-3.5">
      <div class="flex items-center leading-7 justify-end text-right pr-3" style="width: 100px;">
        <span class="font-semibold">{{ t('common.description') }}</span>
      </div>
      <Input
        v-model:value="description"
        type="textarea"
        class="flex-1"
        :autoSize="{ minRows: 3, maxRows: 5 }"
        :maxlength="400"
        trim
        :placeholder="t('apiServer.messages.serverDescriptionPlaceholder')" />
    </div>

    <div class="ml-5 mt-5">
      <div class="flex items-center justify-between mb-1.5">
        <div class="flex">
          <div class="font-semibold mr-1">{{ t('apiServer.columns.variableTitle') }}</div>
          <div class="text-gray-400 ml-4">
            <Icon icon="icon-tishi1" class="text-text-tip text-3.5 cursor-pointer mr-1" />
            {{ t('apiServer.messages.variableQuotaTip') }}
          </div>
        </div>
        <div class="flex items-center">
          <Button
            :disabled="addServerBtnDisabled"
            class="px-0 py-0 h-5 mr-1"
            type="link"
            size="small"
            @click="addVariable(false)">
            {{ t('apiServer.actions.addVariable') }}
          </Button>
        </div>
      </div>
      <div class="space-y-3.5 ml-20">
        <div
          v-for="(item, index) in variableIds"
          :key="item"
          class="border border-solid border-theme-text-box rounded p-3.5">
          <div class="mb-3.5">
            <div class="flex items-center justify-between mb-0.5">
              <div class="flex items-center">
                <IconRequired />
                <span class="mr-1">{{ t('common.name') }}</span>
              </div>
              <Button
                class="px-0 py-0 h-5"
                type="link"
                size="small"
                @click="deleteVariable(item, index)">
                {{ t('apiServer.actions.deleteVariable') }}
              </Button>
            </div>
            <Validate :text="nameErrorMsgMap[item]">
              <Input
                :value="variableDataMap[item].name"
                trimAll
                :placeholder="t('apiServer.messages.variableNamePlaceholder')"
                :maxlength="100"
                :error="nameErrorSet.has(item)"
                dataType="mixin-en"
                includes="!@$%^&*()_-+="
                @change="nameChange($event, item, index)" />
            </Validate>
          </div>

          <div class="mb-3.5">
            <div class="flex items-center mb-0.5">
              <IconRequired />
              <span class="mr-1">{{ t('common.value') }}</span>
            </div>

            <div class="space-y-2">
              <div
                v-for="(_enum, _index) in variableDataMap[item].enum"
                :key="_enum.id"
                class="flex items-start">
                <Validate :text="valueErrorMsgMap[_enum.id]" class="flex-1">
                  <Input
                    v-model:value="_enum.value"
                    :placeholder="t('apiServer.messages.variableValuePlaceholder')"
                    trim
                    :maxlength="400"
                    :error="valueErrorSet.has(_enum.id)"
                    @change="variableValueChange($event, item, _enum.id, _index)">
                    <template #suffix>
                      <div class="flex items-center leading-5">
                        <div v-if="defaultValueMap[item] === _enum.id" class="mr-1 text-text-sub-content text-3">
                          {{ t('common.default') }}
                        </div>
                        <Radio
                          size="small"
                          style="transform:translateY(-3px);"
                          :disabled="_index === variableDataMap[item].enum.length - 1"
                          :checked="defaultValueMap[item] === _enum.id"
                          @change="defaultValueChange(item, _enum.id)" />
                      </div>
                    </template>
                  </Input>
                </Validate>
                <Icon
                  icon="icon-qingchu"
                  :class="{ invisible: _index === (variableDataMap[item].enum.length - 1) }"
                  class="text-3.5 text-theme-text-hover cursor-pointer flex-shrink-0 ml-2 transform-gpu translate-y-1.75"
                  @click="deleteVariableValue(item, _index)" />
              </div>
            </div>
          </div>

          <div>
            <div class="flex items-center mb-0.5">
              <span>{{ t('common.description') }}</span>
            </div>
            <Input
              v-model:value="variableDataMap[item].description"
              type="textarea"
              :autoSize="{ minRows: 3, maxRows: 5 }"
              :maxlength="400"
              trim
              :placeholder="t('apiServer.messages.variableDescriptionPlaceholder')" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
