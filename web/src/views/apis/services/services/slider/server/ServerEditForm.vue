<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Radio } from 'ant-design-vue';
import { Icon, IconRequired, Input, notification, Tooltip, Validate } from '@xcan-angus/vue-ui';
import { utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { ServerInfo } from '@/views/apis/server/types';

// Props: server item value, url duplication map, and whether to hide action buttons
type Props = {
  value: ServerInfo;
  urlMap:{[key:string]:string[]};
  hideButton: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  urlMap: () => ({}),
  hideButton: false
});

const { t } = useI18n();

// Emits: cancel or save with valid data
const emit = defineEmits<{
  (e:'cancel'):void;
  (e:'save', value:ServerInfo):void;
}>();

// Form state
const serverId = ref<string>();
const url = ref<string>();
const urlError = ref(false);
const urlErrorMsg = ref<string>();
const description = ref<string>();
const variableIds = ref<string[]>([]);
const variableDataMap = ref<{[key:string]:ServerInfo['variables'][number]}>({});
const defaultValueMap = ref<{[key:string]:string|undefined}>({});
const nameErrorSet = ref <Set<string>>(new Set());
const nameErrorMsgMap = ref <{[key:string]:string|undefined}>({});
const valueErrorSet = ref <Set<string>>(new Set());
const valueErrorMsgMap = ref <{[key:string]:string|undefined}>({});
// When variable name is cleared, remember insert index for re-insertion into URL later
const insertIndexMap = ref<{[key:string]:number}>({});

// Add a variable row; when resetFlag is true, reset the whole variables section
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

// URL change handler: parse placeholders and sync variables list
const urlChange = debounce(duration.delay, (event: any) => {
  urlError.value = false;
  urlErrorMsg.value = undefined;

  const value = (event?.target?.value || '') as string;
  let uniqueNames:string[] = [];
  const matchItems = value.match(/\{[^{}]+\}/g);
  if (matchItems) {
    const _dataMap = Object.values(variableDataMap.value).reduce((prev, cur) => {
      prev[cur.name] = cur;
      return prev;
    }, {} as {[key:string]:ServerInfo['variables'][number]});
    // Filter duplicate variable names
    uniqueNames = matchItems?.reduce((prev, cur) => {
      if (!prev.includes(cur)) {
        prev.push(cur);
      }
      return prev;
    }, [] as string[]).map(item => item.replace(/\{(.+)\}/, '$1'));
    for (let i = 0, len = uniqueNames.length; i < len; i++) {
      // Ensure variable row exists at index and reflects name
      const _name = uniqueNames[i];
      if (!_dataMap[_name]) {
        const _vid = variableIds.value[i];
        if (variableDataMap.value[_vid]) {
          // If the slot already taken by another name in URL, create a new row
          if (uniqueNames.includes(variableDataMap.value[_vid].name)) {
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
          // Create new row for the name
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

  // Remove variables not present in URL
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
    _enumList.every(item => {
      valueErrorSet.value.delete(item.id);
      return true;
    });
    delete variableDataMap.value[delId];
    delete defaultValueMap.value[delId];
    delete nameErrorMsgMap.value[delId];
    delete valueErrorMsgMap.value[delId];
    nameErrorSet.value.delete(delId);
  }

  insertIndexMap.value = {};
  // If all variables removed, auto add one empty row
  if (variableIds.value.length === 0) {
    addVariable();
  }
});

const urlBlur = (event: any) => {
  const value = (event?.target?.value || '') as string;
  isServerUrl(value);
};

// Validate server url uniqueness and basic placeholder rules
const isServerUrl = (_url:string):boolean => {
  // @TODO Improve url regex
  if (props.urlMap[_url]) {
    if (!serverId.value || props.urlMap[_url].length > 1 || !props.urlMap[_url].includes(serverId.value)) {
      urlError.value = true;
      urlErrorMsg.value = t('service.serverConfig.messages.serverUrlExists');
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
      urlErrorMsg.value = t('service.serverConfig.messages.variableNotRepeat');
      return false;
    }
  }

  return true;
};

// Remove a variable row and sync the url accordingly
const deleteVariable = (id:string, index:number) => {
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
    // Update url by removing placeholder
    const rex = new RegExp('(:?\\/\\/)?\\/?\\{' + delName + '\\}', 'g');
    url.value = url.value.replace(rex, '$1');
  }

  // If all variables removed, auto add one empty row
  if (variableIds.value.length === 0) {
    addVariable();
  }
};

// Variable name change handler. Inserts/replaces placeholders in the url
const nameChange = (event:{target:{value:string}}, id:string, index:number) => {
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
        // Replace existing name at same index if present
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
          // Append at the end if not found
          url.value += url.value.endsWith('/') ? `{${value}}` : `/{${value}}`;
        }
        urlError.value = false;
        urlErrorMsg.value = undefined;
        isServerUrl(url.value);
      }
    } else {
      // Remember current index for re-insertion when user types again
      const _urlIndex = url.value.indexOf('{' + variableDataMap.value[id].name + '}');
      insertIndexMap.value[id] = _urlIndex;
      // Remove name
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

  // Validate duplicates
  validRepeatName();
};

// Last value row auto-extends; validate duplicates and empties
const variableValueChange = debounce(duration.delay, (event:{target:{value:string;}}, pid:string, cid:string, index:number) => {
  valueErrorSet.value.delete(cid);
  valueErrorMsgMap.value[cid] = undefined;
  const len = variableDataMap.value[pid].enum.length - 1;
  if (index === len) {
    // Select as default when first non-empty is entered
    if (len === 0 && event.target.value) {
      defaultValueMap.value[pid] = cid;
    }

    variableDataMap.value[pid].enum.push({ id: utils.uuid(), value: '' });
  }

  // Validate duplicates
  validRepeatValue(pid);
});

// Choose default value by enum id
const defaultValueChange = (pid:string, cid:string) => {
  defaultValueMap.value[pid] = cid;
};

// Remove a value row; ensure default is still pointing to an existing value
const deleteVariableValue = (id:string, index:number) => {
  const defaultEnumId = defaultValueMap.value[id];
  const defaultEnum = variableDataMap.value[id].enum.find(item => item.id === defaultEnumId);
  const delEle = variableDataMap.value[id].enum.splice(index, 1)[0];
  if (defaultEnum?.value === delEle.value) {
    defaultValueMap.value[id] = variableDataMap.value[id].enum[0]?.id;
  }
};

// Emit cancel
const cancel = () => {
  emit('cancel');
};

// Validate and emit save with normalized payload
const save = () => {
  if (!isValid()) {
    notification.error(t('service.serverConfig.messages.configError'));
    return;
  }

  const data = getData();
  emit('save', data);
};

// Reset all error states
const resetError = () => {
  urlError.value = false;
  urlErrorMsg.value = undefined;
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
  nameErrorMsgMap.value = {};
  valueErrorMsgMap.value = {};
};

// Validate the entire form
const isValid = ():boolean => {
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

  const ids = variableIds.value;
  const dataMap = variableDataMap.value;
  if (variableIds.value.length === 1) {
    const id = ids[0];
    const { enum: enumList, name, description } = dataMap[id];
    // If any of name/description/enum values are set, validate
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
    }, {} as {[key:string]:string[]});
    for (let i = 0, len = ids.length; i < len; i++) {
      const id = ids[i];
      const { enum: enumList, name } = dataMap[id];
      if (!name) {
        nameErrorSet.value.add(id);
        errorNum++;
      } else {
        if (nameMap[name].length > 1) {
          nameErrorMsgMap.value[id] = t('service.serverConfig.messages.variableNameRepeat');
          nameErrorSet.value.add(id);
          errorNum++;
        }
      }

      errorNum += validVariableValue(enumList);
    }
  }

  return !errorNum;
};

// Validate variable enum values (no empties and no duplicates except last placeholder row)
const validVariableValue = (enumList:{id:string;value:string}[]):number => {
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
    }, {} as {[key:string]:number});

    for (let i = 0, len = enumList.length; i < len; i++) {
      const { id: _id, value: _value } = enumList[i];
      // Last row is a placeholder (empty allowed)
      if (i < len - 1) {
        if (!_value) {
          valueErrorSet.value.add(_id);
          errorNum++;
        } else {
          if (repeatEnumMap[_value] && repeatEnumMap[_value] > 1) {
            valueErrorSet.value.add(_id);
            valueErrorMsgMap.value[_id] = t('service.serverConfig.messages.variableValueRepeat');
            errorNum++;
          }
        }
      }
    }
  }

  return errorNum;
};

// Validate duplicate variable names across all variable rows
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
  }, {} as {[key:string]:number});

  const ids = variableIds.value;
  const dataMap = variableDataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (repeatMap[dataMap[id].name] && repeatMap[dataMap[id].name] > 1) {
      nameErrorSet.value.add(id);
      nameErrorMsgMap.value[id] = '变量名称重复';
    } else {
      if (nameErrorMsgMap.value[id]) {
        nameErrorSet.value.delete(id);
        nameErrorMsgMap.value[id] = undefined;
      }
    }
  }
};

// Validate duplicate enum values for a specific variable
const validRepeatValue = (id:string) => {
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
  }, {} as {[key:string]:number});

  for (let i = 0, len = enumList.length; i < len; i++) {
    const { id: _id, value: _value } = enumList[i];
    if (repeatMap[_value] && repeatMap[_value] > 1) {
      valueErrorSet.value.add(_id);
      valueErrorMsgMap.value[_id] = '变量值重复';
    } else {
      if (valueErrorMsgMap.value[_id]) {
        valueErrorSet.value.delete(_id);
        valueErrorMsgMap.value[_id] = undefined;
      }
    }
  }
};

// Normalize output payload for Save
const getData = ():ServerInfo => {
  const variables:ServerInfo['variables'] = [];
  const ids = variableIds.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { enum: enumList, name } = variableDataMap.value[id];
    if (name) {
      const _enums:{id:string;value:string}[] = [];
      // Last row is placeholder and should not be included in payload
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
    variables
  };
};

// Reset entire form state
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

// Add-variable button disabled when too many rows
const addServerBtnDisabled = computed(() => {
  return variableIds.value.length >= 50;
});

onMounted(() => {
  watch(() => props.value, () => {
    // Re-initialize from props on each change
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

// Expose getData for parent usage
defineExpose({
  getData: () => {
    if (!isValid()) {
      notification.error(t('service.serverConfig.messages.configError'));
      return;
    }
    const data = getData();
    return data;
  }
});
</script>
<template>
  <div class="leading-5 mb-3">
    <div class="mb-3.5">
      <div class="flex items-center mb-0.5">
        <IconRequired />
        <span>{{ t('common.url') }}</span>
      </div>
      <Validate :text="urlErrorMsg">
        <Input
          v-model:value="url"
          :placeholder="t('service.serverConfig.form.urlPlaceholder')"
          trimAll
          type="textarea"
          :autoSize="true"
          :error="urlError"
          :maxlength="400"
          @change="urlChange"
          @blur="urlBlur" />
      </Validate>
    </div>

    <div class="mb-3.5">
      <div class="flex items-center mb-0.5">
        <span>{{ t('common.description') }}</span>
      </div>
      <Input
        v-model:value="description"
        type="textarea"
        :autoSize="{ minRows: 3, maxRows: 5 }"
        :maxlength="400"
        trim
        :placeholder="t('service.serverConfig.form.descriptionPlaceholder')" />
    </div>

    <div>
      <div class="flex items-center justify-between mb-0.5">
        <div class="flex">
          <div class="font-semibold mr-1">{{ t('common.variables') }}</div>
          <Tooltip :title="t('service.serverConfig.tips.maxVariables')">
            <Icon icon="icon-tishi1" class="text-text-tip text-3.5 cursor-pointer" />
          </Tooltip>
        </div>
        <div class="flex items-center">
          <Button
            :disabled="addServerBtnDisabled"
            class="px-0 py-0 h-5 mr-1"
            type="link"
            size="small"
            @click="addVariable(false)">
            {{ t('service.serverConfig.actions.addVariable') }}
          </Button>
        </div>
      </div>
      <div class="border border-solid border-theme-text-box rounded px-3.5 py-3">
        <template v-for="(item,index) in variableIds" :key="item">
          <div>
            <div class="mb-3.5">
              <div class="flex items-center justify-between mb-0.5">
                <div class="flex items-center">
                  <IconRequired />
                  <span class="mr-1">{{ t('service.serverConfig.form.variableName') }}</span>
                  <Tooltip :title="t('service.serverConfig.tips.variableNameNotRepeat')">
                    <Icon icon="icon-tishi1" class="text-text-tip text-3.5 cursor-pointer" />
                  </Tooltip>
                </div>
                <Button
                  class="px-0 py-0 h-5"
                  type="link"
                  size="small"
                  @click="deleteVariable(item,index)">
                  {{ t('service.serverConfig.actions.deleteVariable') }}
                </Button>
              </div>
              <Validate :text="nameErrorMsgMap[item]">
                <Input
                  :value="variableDataMap[item].name"
                  trimAll
                  :placeholder="t('service.serverConfig.form.variableNamePlaceholder')"
                  :maxlength="100"
                  :error="nameErrorSet.has(item)"
                  dataType="mixin-en"
                  includes="!@$%^&*()_-+="
                  @change="nameChange($event,item,index)" />
              </Validate>
            </div>

            <div class="mb-3.5">
              <div class="flex items-center mb-0.5">
                <IconRequired />
                <span class="mr-1">{{ t('service.serverConfig.form.variableValue') }}</span>
                <Tooltip :title="t('service.serverConfig.tips.variableValueNotRepeat')">
                  <Icon icon="icon-tishi1" class="text-text-tip text-3.5 cursor-pointer" />
                </Tooltip>
              </div>

              <div class="space-y-2">
                <div
                  v-for="(_enum,_index) in variableDataMap[item].enum"
                  :key="_enum.id"
                  class="flex items-start">
                  <Validate :text="valueErrorMsgMap[_enum.id]" class="flex-1">
                    <Input
                      v-model:value="_enum.value"
                      :placeholder="t('service.serverConfig.form.variableValuePlaceholder')"
                      trim
                      :maxlength="400"
                      :error="valueErrorSet.has(_enum.id)"
                      @change="variableValueChange($event,item,_enum.id,_index)">
                      <template #suffix>
                        <div class="flex items-center leading-5">
                          <div v-if="defaultValueMap[item]===_enum.id" class="mr-1 text-text-sub-content text-3">{{ t('common.default') }}</div>
                          <Radio
                            size="small"
                            :disabled="_index===variableDataMap[item].enum.length-1"
                            :checked="defaultValueMap[item]===_enum.id"
                            @change="defaultValueChange(item,_enum.id)" />
                        </div>
                      </template>
                    </Input>
                  </Validate>
                  <Icon
                    icon="icon-qingchu"
                    :class="{invisible:_index===(variableDataMap[item].enum.length-1)}"
                    class="text-3.5 text-theme-text-hover cursor-pointer flex-shrink-0 ml-2 transform-gpu translate-y-1.75"
                    @click="deleteVariableValue(item,_index)" />
                </div>
              </div>
            </div>

            <div>
              <div class="flex items-center mb-0.5">
                <span>{{ t('service.serverConfig.form.variableDescription') }}</span>
              </div>
              <Input
                v-model:value="variableDataMap[item].description"
                type="textarea"
                :autoSize="{ minRows: 3, maxRows: 5 }"
                :maxlength="400"
                trim
                :placeholder="t('service.serverConfig.form.variableDescriptionPlaceholder')" />
            </div>
          </div>

          <div v-if="index<(variableIds.length-1)" class="border-t border-dashed border-theme-text-box mt-5 mb-3"></div>
        </template>
      </div>
    </div>

    <div v-if="!hideButton" class="flex items-center justify-end space-x-3 py-3">
      <Button
        class="p-0 leading-5 h-5"
        type="link"
        size="small"
        @click="cancel">
        {{ t('actions.cancel') }}
      </Button>
      <Button
        class="p-0 leading-5 h-5"
        type="link"
        size="small"
        @click="save">
        {{ t('actions.save') }}
      </Button>
    </div>
  </div>
</template>
