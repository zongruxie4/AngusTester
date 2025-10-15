<script setup lang="ts">
import { ref, onMounted, watch, defineAsyncComponent, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, Tooltip, Input, IconCopy, SelectEnum } from '@xcan-angus/vue-ui';
import { utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

import { FormState } from './PropsType';

const { t } = useI18n();

type Props = {
  errorNum: number;
  variables: {
    name: string;
    method: {
      value: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
      message: string;
    };
    location: {
      value:
      'PATH_PARAMETER' |
      'QUERY_PARAMETER' |
      'REQUEST_HEADER' |
      'FORM_PARAMETER' |
      'REQUEST_RAW_BODY' |
      'RESPONSE_HEADER' |
      'RESPONSE_BODY';
      message: string;
    };
    parameterName: string;
    defaultValue: string;
    expression: string;
    matchItem: string;
    source: 'HTTP_SAMPLING'
  }[];
}

const props = withDefaults(defineProps<Props>(), {
  errorNum: 0,
  variables: () => []
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: FormState[]): void;
  (e: 'errorChange', value: number): void;
}>();

const MatchItemPopover = defineAsyncComponent(() => import('./MatchItemPopover/index.vue'));

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: FormState }>({});
const repeatNameSet = ref<Set<string>>(new Set());
const nameErrorSet = ref<Set<string>>(new Set());
const methodErrorSet = ref<Set<string>>(new Set());
const locationErrorSet = ref<Set<string>>(new Set());
const parameterNameErrorSet = ref<Set<string>>(new Set());
const expressionErrorSet = ref<Set<string>>(new Set());

const emitChange = () => {
  const data = getData();
  emit('change', data);
};

const toAdd = () => {
  const id = utils.uuid();
  const data: FormState = {
    name: '',
    method: undefined,
    location: undefined,
    parameterName: '',
    defaultValue: '',
    expression: '',
    matchItem: '',
    source: 'HTTP_SAMPLING'
  };
  idList.value.push(id);
  dataMap.value[id] = data;

  emitChange();
};

const nameChange = debounce(duration.delay, (id: string) => {
  nameErrorSet.value.delete(id);
  // 校验名称是否重复
  const duplicates: string[] = [];
  const uniqueNames = new Set();
  const names = Object.values(dataMap.value).map(item => item.name).filter(item => item);
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    if (uniqueNames.has(name)) {
      duplicates.push(name);
    } else {
      uniqueNames.add(name);
    }
  }

  const ids = idList.value;
  const data = dataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const _id = ids[i];
    const { name } = data[_id];
    if (duplicates.includes(name)) {
      repeatNameSet.value.add(_id);
    } else {
      repeatNameSet.value.delete(_id);
    }
  }

  emitChange();
});

const methodChange = (id: string) => {
  methodErrorSet.value.delete(id);
  emitChange();
};

const locationChange = (id: string) => {
  locationErrorSet.value.delete(id);
  emitChange();
};

const parameterNameChange = (id: string) => {
  parameterNameErrorSet.value.delete(id);
  emitChange();
};

const defaultValueChange = () => {
  emitChange();
};

const expressionChange = (id: string) => {
  expressionErrorSet.value.delete(id);
  emitChange();
};

const matchItemChange = () => {
  emitChange();
};

const toDelete = (id: string, index: number) => {
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  nameErrorSet.value.delete(id);
  methodErrorSet.value.delete(id);
  locationErrorSet.value.delete(id);
  parameterNameErrorSet.value.delete(id);
  expressionErrorSet.value.delete(id);
  repeatNameSet.value.delete(id);

  emitChange();
};

onMounted(() => {
  watch(() => props.variables, () => {
    idList.value = [];
    dataMap.value = {};
    nameErrorSet.value.clear();
    methodErrorSet.value.clear();
    locationErrorSet.value.clear();
    parameterNameErrorSet.value.clear();
    expressionErrorSet.value.clear();
    repeatNameSet.value.clear();

    if (props.variables?.length) {
      const dataList = props.variables;
      for (let i = 0, len = dataList.length; i < len; i++) {
        const id = utils.uuid();
        idList.value.push(id);
        dataMap.value[id] = { ...dataList[i] };
      }
    }

    emitChange();
  }, { immediate: true, deep: true });

  watchEffect(() => {
    const size = nameErrorSet.value.size + methodErrorSet.value.size + locationErrorSet.value.size + parameterNameErrorSet.value.size + expressionErrorSet.value.size;
    emit('errorChange', size);
  });
});

const isValid = (): boolean => {
  const ids = idList.value;
  let errorNum = 0;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { name, method, location, parameterName, expression } = dataMap.value[id];
    if (!name) {
      nameErrorSet.value.add(id);
      errorNum++;
    }

    if (!method) {
      methodErrorSet.value.add(id);
      errorNum++;
    } else if (['JSON_PATH', 'REGEX', 'X_PATH'].includes(method)) {
      if (!expression) {
        expressionErrorSet.value.add(id);
        errorNum++;
      }
    }

    if (!location) {
      locationErrorSet.value.add(id);
      errorNum++;
    } else if (['PATH_PARAMETER', 'QUERY_PARAMETER', 'REQUEST_HEADER', 'FORM_PARAMETER', 'RESPONSE_HEADER'].includes(location)) {
      if (!parameterName) {
        parameterNameErrorSet.value.add(id);
        errorNum++;
      }
    }
  }

  return !errorNum;
};

const getData = (): FormState[] => {
  return idList.value.map((item) => {
    return dataMap.value[item];
  });
};

defineExpose({
  getData,
  isValid
});
</script>

<template>
  <div class="text-3 leading-5">
    <div class="flex items-center flex-nowrap mb-1.5">
      <div class="flex-shrink-0 w-1 h-3.5 rounded bg-blue-400 mr-1.5"></div>
      <div class="flex-shrink-0 text-theme-title mr-2.5">{{ t('httpPlugin.uiConfig.httpConfigs.parametric.variables.title') }}</div>
      <Icon icon="icon-tishi1" class="flex-shrink-0 text-tips text-3.5 mr-1" />
      <div class="flex-shrink-0 break-all whitespace-pre-wrap">{{ t('common.description') }}</div>
    </div>
    <div class="mb-2">
      <Button
        type="link"
        size="small"
        class="flex items-center h-5 leading-5 p-0 space-x-1"
        @click="toAdd">
        <Icon icon="icon-jia" class="text-3.5" />
        <span class="ml-1">{{ t('httpPlugin.uiConfig.httpConfigs.parametric.variables.addVariable') }}</span>
      </Button>
    </div>

    <div v-if="idList.length === 0" class="flex-1 flex flex-col items-center justify-center">
      <img style="width:96px;" src="images/nodata.png">
      <div class="flex items-center text-theme-sub-content text-3">
        <span>{{ t('httpPlugin.uiConfig.httpConfigs.parametric.variables.noVariablesDefined') }}</span>
      </div>
    </div>

    <div class="space-y-2.5">
      <div
        v-for="(item, index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <Tooltip
          :title="t('httpPlugin.uiConfig.httpConfigs.parametric.variables.duplicateName')"
          internal
          placement="right"
          destroyTooltipOnHide
          :visible="repeatNameSet.has(item)">
          <Input
            v-model:value="dataMap[item].name"
            :maxLength="100"
            :error="nameErrorSet.has(item)"
            style="width:calc((100% - 96px)/10*2);"
            excludes="{}"
            :placeholder="t('common.placeholders.searchKeyword')"
            size="small"
            tirmAll
            class="flex-shrink-0 has-suffix"
            @change="nameChange(item)">
            <template #suffix>
              <div v-if="dataMap[item].name" class="h-full flex items-center overflow-hidden">
                <div :title="`{${dataMap[item].name}}`" class="flex-1 flex items-center text-3 overflow-hidden">
                  <span>{</span>
                  <span class="truncate">{{ dataMap[item].name }}</span>
                  <span>}</span>
                </div>
                <IconCopy :copyText="`{${dataMap[item].name}}`" class="flex-shrink-0 ml-1.75" />
              </div>
            </template>
          </Input>
        </Tooltip>

        <SelectEnum
          v-model:value="dataMap[item].method"
          :error="methodErrorSet.has(item)"
          enumKey="ExtractionMethod"
          :placeholder="t('httpPlugin.uiConfig.httpConfigs.parametric.variables.extractionMethodPlaceholder')"
          class="flex-shrink-0"
          style="width:calc((100% - 96px)/10*1);"
          @change="methodChange(item)" />

        <SelectEnum
          v-model:value="dataMap[item].location"
          :error="locationErrorSet.has(item)"
          enumKey="HttpExtractionLocation"
          :placeholder="t('httpPlugin.uiConfig.httpConfigs.parametric.variables.locationPlaceholder')"
          class="flex-shrink-0"
          style="width:calc((100% - 96px)/10*1);"
          @change="locationChange(item)" />

        <Input
          v-model:value="dataMap[item].parameterName"
          :maxlength="100"
          :error="parameterNameErrorSet.has(item)"
          :disabled="['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(dataMap[item].location)"
          class="flex-shrink-0"
          trim
          :placeholder="t('common.placeholders.enterParameterName')"
          style="width:calc((100% - 96px)/10*1.5);"
          @change="parameterNameChange(item)" />

        <Input
          v-model:value="dataMap[item].defaultValue"
          :maxlength="4096"
          trim
          :placeholder="t('httpPlugin.uiConfig.httpConfigs.parametric.variables.defaultValuePlaceholder')"
          class="flex-shrink-0"
          style="width:calc((100% - 96px)/10*2);"
          @change="defaultValueChange" />

        <Input
          v-model:value="dataMap[item].expression"
          trim
          :error="expressionErrorSet.has(item)"
          :maxlength="1024"
          :disabled="dataMap[item].method === 'EXACT_VALUE'"
          class="flex-shrink-0"
          :placeholder="t('httpPlugin.uiConfig.httpConfigs.parametric.variables.expressionPlaceholder')"
          style="width:calc((100% - 96px)/10*1.5);"
          @change="expressionChange(item)" />

        <div class="flex-shrink-0 flex items-center" style="width:calc((100% - 96px)/10*1);">
          <Input
            v-model:value="dataMap[item].matchItem"
            :placeholder="t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPlaceholder')"
            dataType="number"
            trimAll
            :max="2000"
            :maxlength="4"
            :disabled="dataMap[item].method === 'EXACT_VALUE'"
            @change="matchItemChange" />
          <MatchItemPopover class="flex-shrink-0" />
        </div>

        <Button
          class="w-7 p-0 flex-shrink-0 !ml-5"
          type="default"
          size="small"
          @click="toDelete(item, index)">
          <Icon icon="icon-shanchuguanbi" />
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.has-suffix :deep(.ant-input-suffix) {
  display: inline-block;
  width: 110px;
  margin-left: 4px;
  padding-left: 7px;
  overflow: hidden;
  border-left: 1px solid var(--border-text-box);
}
</style>
