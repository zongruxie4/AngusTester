<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, IconCopy, IconRequired, Input, Tooltip, FunctionsButton, ParamInput } from '@xcan-angus/vue-ui';
import { utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

const { t } = useI18n();

export interface Option {
  name: string;
  value: string;
}

export interface Props {
  defaultValue: Option[];
}

const props = withDefaults(defineProps<Props>(), {
  defaultValue: () => []
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: Option[]): void;
}>();

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: Option }>({});
const errorMessage = ref<Map<string, string>>(new Map());
const nameErrorSet = ref<Set<string>>(new Set());
const valueErrorSet = ref<Set<string>>(new Set());

const nameChange = debounce(duration.delay, (event: { target: { value: string; } }, id: string, index: number) => {
  nameErrorSet.value.delete(id);
  errorMessage.value.delete(id);
  // 保证最后一条是空的
  if (index === idList.value.length - 1 && event.target.value) {
    addNewItem();
  }

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
    if (duplicates.includes(data[_id].name)) {
      nameErrorSet.value.add(_id);
      errorMessage.value.set(_id, t('dataset.detail.staticDataset.parameterInput.errors.duplicate'));
    } else {
      nameErrorSet.value.delete(_id);
      errorMessage.value.delete(_id);
    }
  }

  emitChange();
});

const nameBlur = (event: { target: { value: string; } }, id: string) => {
  const name = event.target.value;
  if (!name) {
    return;
  }

  // eslint-disable-next-line prefer-regex-literals
  const rex = new RegExp(/[^a-zA-Z0-9!$%^&*_\-+=./]/);
  if (rex.test(name)) {
    nameErrorSet.value.add(id);
    errorMessage.value.set(id, t('dataset.detail.staticDataset.parameterInput.errors.invalid'));
  }
};

const valueChange = (target: Element, id: string) => {
  const value = target?.innerText?.trim().replace('\n', '');
  dataMap.value[id].value = value;
  valueErrorSet.value.delete(id);
  emitChange();
};

const emitChange = () => {
  const data = getData();
  emit('change', data);
};

const addNewItem = () => {
  const id = utils.uuid();
  const data: Option = { name: '', value: '' };
  idList.value.push(id);
  dataMap.value[id] = data;
};

const deleteHandler = (id: string, index: number): void => {
  const length = idList.value.length - 1;
  idList.value.splice(index, 1);

  delete dataMap.value[id];
  errorMessage.value.delete(id);
  nameErrorSet.value.delete(id);
  valueErrorSet.value.delete(id);
  emitChange();

  // 删除的是最后一条，自动添加一条新断言
  if (index === length) {
    addNewItem();
  }
};

const reset = () => {
  idList.value = [];
  dataMap.value = {};
  errorMessage.value.clear();
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
};

onMounted(() => {
  watch(() => props.defaultValue, () => {
    reset();
    if (props.defaultValue?.length) {
      const dataList = props.defaultValue;
      for (let i = 0, len = dataList.length; i < len; i++) {
        const id = utils.uuid();
        idList.value.push(id);
        dataMap.value[id] = dataList[i];
      }
    }

    addNewItem();
  }, { immediate: true });
});

const isValid = (): boolean => {
  errorMessage.value.clear();
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
  const ids = idList.value;
  const data = dataMap.value;

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

  // 最后一条是空数据，不校验
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    const id = ids[i];
    const { name, value } = data[id];
    if (!name) {
      nameErrorSet.value.add(id);
    } else {
      if (duplicates.includes(name)) {
        nameErrorSet.value.add(id);
        errorMessage.value.set(id, '名称重复');
      }

      // eslint-disable-next-line prefer-regex-literals
      const rex = new RegExp(/[a-zA-Z0-9!$%^&*_\-+=./]+/);
      if (!rex.test(name)) {
        nameErrorSet.value.add(id);
        errorMessage.value.set(id, t('dataset.detail.staticDataset.parameterInput.errors.invalid'));
      }
    }

    if (!value) {
      valueErrorSet.value.add(id);
    }
  }

  return !(nameErrorSet.value.size + valueErrorSet.value.size);
};

const getData = () => {
  const ids = idList.value;
  const _dataMap = dataMap.value;
  const dataList: {
    name: string;
    value: string;
  }[] = [];
  // 最后一条是空数据
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    dataList.push({ ..._dataMap[ids[i]] });
  }

  return dataList;
};

defineExpose({
  getData,
  isValid
});
</script>
<template>
  <div>
    <div class="flex items-center space-x-2 mb-1">
      <div class="w-100 flex items-center">
        <IconRequired />
        <span>{{ t('dataset.detail.staticDataset.parameterInput.name') }}</span>
      </div>
      <div class="flex-1 flex items-center">
        <IconRequired />
        <span>{{ t('dataset.detail.staticDataset.parameterInput.value') }}</span>
      </div>
      <FunctionsButton class="text-3.5" />
    </div>

    <div class="space-y-2.5">
      <div
        v-for="(item, index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <div class="flex items-center flex-1 space-x-2">
          <div class="w-100 flex items-center">
            <Tooltip
              :title="errorMessage.get(item)"
              internal
              placement="right"
              destroyTooltipOnHide
              :visible="!!errorMessage.get(item)">
              <Input
                v-model:value="dataMap[item].name"
                :maxLength="100"
                :error="nameErrorSet.has(item)"
                excludes="{}"
                includes="\!\$%\^&\*_\-+=\.\/"
                dataType="mixin-en"
                :placeholder="t('dataset.detail.staticDataset.parameterInput.namePlaceholder')"
                size="small"
                tirmAll
                class="flex-1 has-suffix"
                @change="nameChange($event, item, index)"
                @blur="nameBlur($event, item)">
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
          </div>
          <ParamInput
            :value="dataMap[item].value"
            :maxlength="4096"
            :error="valueErrorSet.has(item)"
            class="flex-1"
            trim
            :placeholder="t('dataset.detail.staticDataset.parameterInput.valuePlaceholder')"
            @blur="valueChange($event, item)" />
          <!-- <Input
            v-model:value="dataMap[item].value"
            :maxlength="4096"
            :error="valueErrorSet.has(item)"
            class="flex-1"
            trim
            placeholder="参数值，值可以是常量或Mock函数，最长4096个字符，值示例：123456、true、@String(5,10)"
            @change="valueChange(item)" /> -->
        </div>
        <Button
          class="w-7 p-0"
          type="default"
          size="small"
          @click="deleteHandler(item, index)">
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
