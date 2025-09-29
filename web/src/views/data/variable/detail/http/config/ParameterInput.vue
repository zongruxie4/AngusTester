
<script lang="ts" setup>
import { onMounted, ref, watch, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox } from 'ant-design-vue';
import { Icon, Input, Select } from '@xcan-angus/vue-ui';
import { utils, duration, ParameterIn } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { Parameter } from '@/views/data/variable/detail/http/types';

const { t } = useI18n();

export interface Props {
  defaultValue: Parameter[];
  value: Parameter[];
  errorNum:number;
  defaultIn?: ParameterIn;
  showInType?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  defaultValue: () => [],
  value: undefined,
  errorNum: 0,
  showInType: false,
  defaultIn: ParameterIn.query
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: Parameter[]): void;
  (e: 'changePath', value: Parameter[]): void;
  (e: 'update:errorNum', value: number): void;
}>();

const idList = ref<string[]>([]);
const dataMap = ref<{[key:string]:Parameter}>({});
const nameErrorSet = ref<Set<string>>(new Set());
const checkedSet = ref<Set<string>>(new Set());

// 启用 禁用
const enabledChange = (event:{target:{checked:boolean}}, id: string) => {
  dataMap.value[id].enabled = event.target.checked;
  checkedSet.value.add(id);
  if (dataMap.value[id].in === 'path') {
    changeInType();
  }
};

const nameBlur = (id:string): void => {
  if (dataMap.value[id].in === 'path') {
    changeInType();
  }
};

const nameChange = debounce(duration.delay, (event: { target: { value: string; } }, id:string, index: number) => {
  nameErrorSet.value.delete(id);
  if (!checkedSet.value.has(id)) {
    dataMap.value[id].enabled = true;
    checkedSet.value.add(id);
  }

  // 保证最后一条是空的
  if (index === idList.value.length - 1 && event.target.value) {
    addNewItem();
  }

  emitChange();
});

const valueChange = debounce(duration.delay, () => {
  emitChange();
});

const emitChange = () => {
  const data = getData();
  emit('change', data);
};

const addNewItem = () => {
  const id = utils.uuid();
  const data:Parameter = {
    id,
    name: '',
    value: '',
    in: props.defaultIn,
    enabled: false,
    disabled: false,
    type: 'string'
  };
  idList.value.push(id);
  dataMap.value[id] = data;
};

const deleteHandler = (id:string, index: number): void => {
  const length = idList.value.length - 1;
  idList.value.splice(index, 1);
  if (dataMap.value[id].in === 'path') {
    changeInType();
  }

  delete dataMap.value[id];
  nameErrorSet.value.delete(id);
  checkedSet.value.delete(id);
  emitChange();

  // 删除的是最后一条，自动添加一条新断言
  if (index === length) {
    addNewItem();
  }
};

const changeInType = () => {
  const dataList:Parameter[] = [];
  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const { in: _in, enabled, name } = dataMap.value[ids[i]];
    if (_in === 'path' && enabled && name) {
      dataList.push(dataMap.value[ids[i]]);
    }
  }

  emit('changePath', dataList);
  emitChange();
};

const reset = () => {
  idList.value = [];
  dataMap.value = {};
  nameErrorSet.value.clear();
  checkedSet.value.clear();
};

onMounted(() => {
  watch(() => props.defaultValue, () => {
    setData(props.defaultValue, true);
  }, { immediate: true, deep: true });

  watchEffect(() => {
    const size = nameErrorSet.value.size;
    emit('update:errorNum', size);
  });
});

const isValid = (): boolean => {
  nameErrorSet.value.clear();
  const ids = idList.value;
  const data = dataMap.value;
  // 最后一条是空数据，不校验
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    const id = ids[i];
    const { name } = data[id];
    if (!name) {
      nameErrorSet.value.add(id);
    }
  }

  return !nameErrorSet.value.size;
};

const getData = () => {
  const ids = idList.value;
  const _dataMap = dataMap.value;
  const dataList:Parameter[] = [];
  // 最后一条是空数据
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    dataList.push({ ..._dataMap[ids[i]] });
  }
  return dataList;
};

const setData = (data:Parameter[], resetFlag:boolean) => {
  if (resetFlag) {
    reset();
  }

  if (data?.length) {
    for (let i = 0, len = data.length; i < len; i++) {
      const id = utils.uuid();
      idList.value.push(id);
      dataMap.value[id] = { ...data[i] };
      checkedSet.value.add(id);
    }
  }

  addNewItem();
};

defineExpose({
  getData,
  isValid,
  setData
});

const options = [{ label: 'query', value: 'query' }, { label: 'path', value: 'path' }];
</script>
<template>
  <div class="space-y-3.5">
    <div
      v-for="(item, index) in idList"
      :key="item"
      class="flex items-center space-x-2"
      :class="{ 'opacity-70': !dataMap[item].enabled }">
      <Checkbox
        :checked="dataMap[item].enabled"
        @change="enabledChange($event, item)" />
      <div class="flex items-center flex-1 space-x-2">
        <Input
          v-model:value="dataMap[item].name"
          :maxLength="400"
          :error="nameErrorSet.has(item)"
          :placeholder="t('common.placeholders.searchKeyword')"
          size="small"
          tirmAll
          style="flex:0 0 calc((100% - 84px)*3.5/10);max-width: 400px;"
          @blur="nameBlur(item)"
          @change="nameChange($event, item, index)" />
        <Select
          v-if="props.showInType"
          v-model:value="dataMap[item].in"
          :options="options"
          class="w-20 flex-shrink-0"
          @change="changeInType" />
        <Input
          v-model:value="dataMap[item].value"
          :maxlength="4096"
          class="flex-1"
          trim
          :placeholder="t('common.placeholders.inputMockValue')"
          @change="valueChange" />
      </div>
      <Button
        class="w-7 p-0 !ml-3"
        type="default"
        size="small"
        @click="deleteHandler(item,index)">
        <Icon icon="icon-shanchuguanbi" />
      </Button>
    </div>
  </div>
</template>
