<script setup lang="ts">
import { onMounted, watch, watchEffect, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox } from 'ant-design-vue';
import { Input, Icon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { ParameterConfig } from './PropsType';

const { t } = useI18n();

export interface Props {
    type: 'query' | 'header';
    value: ParameterConfig[];
    errorNum: number;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'query',
  value: () => [],
  errorNum: 0
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:errorNum', value: number): void;
}>();

const idList = ref<string[]>([]);
const dataMap = ref<{[key:string]:ParameterConfig}>({});
const checkedIdSet = ref<Set<string>>(new Set());
const nameErrorSet = ref<Set<string>>(new Set());

const addNewItem = () => {
  const id = utils.uuid();
  idList.value.push(id);
  dataMap.value[id] = {
    description: '',
    enabled: false,
    in: props.type,
    name: '',
    type: 'string',
    value: ''
  };
};

const nameChange = (event:{target:{value:string}}, id: string, index: number) => {
  nameErrorSet.value.delete(id);
  const value = event.target.value;
  if (!checkedIdSet.value.has(id)) {
    if (value) {
      checkedIdSet.value.add(id);
      dataMap.value[id].enabled = true;
    }
  }

  // 保证最后一条是空的
  if (index === idList.value.length - 1) {
    if (value) {
      addNewItem();
    }
  }
};

const nameBlur = (event: { target: { value: string } }, index: number) => {
  // 保证最后一条是空的
  if (index === idList.value.length - 1) {
    if (event.target.value) {
      addNewItem();
    }
  }
};

const delHandler = (index: number, id: string) => {
  // 删除的是最后一条，自动添加一条新断言
  const length = idList.value.length - 1;
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  checkedIdSet.value.delete(id);
  nameErrorSet.value.delete(id);

  if (index === length) {
    addNewItem();
  }
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();
    if (newValue?.length) {
      for (let i = 0, len = newValue.length; i < len; i++) {
        const id = utils.uuid();
        idList.value.push(id);
        dataMap.value[id] = newValue[i];
        checkedIdSet.value.add(id);
      }
    }

    addNewItem();
  }, { immediate: true });

  watchEffect(() => {
    const size = nameErrorSet.value.size;
    emit('update:errorNum', size);
  });
});

const reset = () => {
  idList.value = [];
  dataMap.value = {};
  checkedIdSet.value.clear();
  nameErrorSet.value.clear();
};

const getInvalidNum = (): number => {
  nameErrorSet.value.clear();
  const ids = idList.value;
  const map = dataMap.value;
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    const id = ids[i];
    if (!map[id].name) {
      nameErrorSet.value.add(id);
    }
  }

  return nameErrorSet.value.size;
};

const getData = (): ParameterConfig[] => {
  const data:ParameterConfig[] = [];
  const ids = idList.value;
  const map = dataMap.value;
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    const id = ids[i];
    if (map[id].name) {
      data.push({ ...map[id] });
    }
  }

  return data;
};

defineExpose({
  getInvalidNum,
  getData
});
</script>

<template>
  <div class="space-y-4">
    <div
      v-for="(item,index) in idList"
      :key="item"
      :class="{'opacity-70':!dataMap[item].enabled}"
      class="flex items-center">
      <Checkbox v-model:checked="dataMap[item].enabled" class="flex-shrink-0 mr-2" />
      <div class="flex-1 flex items-center space-x-2 mr-3">
        <Input
          v-model:value="dataMap[item].name"
          :maxlength="400"
          :error="nameErrorSet.has(item)"
          trim
          style="flex: 1 1 calc((100% - (8px))*2/5);"
          :placeholder="t('websocketPlugin.uiConfig.websocketConfigs.paramsInput.parameterNamePlaceholder')"
          @blur="nameBlur($event,index)"
          @change="nameChange($event,item,index)" />
        <Input
          v-model:value="dataMap[item].value"
          :maxlength="4096"
          trim
          :placeholder="t('websocketPlugin.uiConfig.websocketConfigs.paramsInput.parameterValuePlaceholder')"
          style="flex: 1 1 calc((100% - (8px))*3/5);" />
      </div>
      <div class="w-9 h-7 flex items-center justify-start">
        <Icon
          class="flex-shrink-0 cursor-pointer text-theme-sub-content hover:text-text-link-hover"
          icon="icon-shanchuguanbi"
          @click="delHandler(index,item)" />
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-checkbox:not(.ant-checkbox-disabled)) {
    background-color: #fff;
}
</style>
