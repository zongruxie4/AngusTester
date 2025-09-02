<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Composite, Icon, Input, SelectInput } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

const { t } = useI18n();

type ParametersType = { name: string; value: string; disabled: boolean }

interface Props {
  enumKey: string;
  value: ParametersType[];
  fielaNames?: { label: string; value: string; };
  label?: string;
}

const props = withDefaults(defineProps<Props>(), {
  enumKey: undefined,
  value: () => [],
  fielaNames: () => ({ label: 'name', value: 'x-xc-value' }),
  label: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: string[]): void;
}>();

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: ParametersType }>({});
const nameErrorSet = ref<Set<string>>(new Set<string>());
const valueErrorSet = ref<Set<string>>(new Set<string>());

const labelKey = computed(() => props.fielaNames.label);
const valueKey = computed(() => props.fielaNames.value);

const nameChange = (value: string, id: string) => {
  nameErrorSet.value.delete(id);
  dataMap.value[id].name = value;
};

const nameSearch = (value: string, options: { _value: string; message: string }[]) => {
  return options.filter(item => item._value.includes(value) || item.message.includes(value));
};

const valueChange = (event: { target: { value: string } }, id: string) => {
  valueErrorSet.value.delete(id);
  dataMap.value[id].value = event.target.value;
};

const deleteHandler = (index: number, id: string) => {
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  nameErrorSet.value.delete(id);
  valueErrorSet.value.delete(id);
};

const reset = () => {
  idList.value = [];
  dataMap.value = {};
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue?.length) {
      return;
    }

    for (let i = 0, len = newValue.length; i < len; i++) {
      const id = utils.uuid();
      idList.value.push(id);
      const data = newValue[i];
      dataMap.value[id] = {
        disabled: data.disabled,
        name: data[labelKey.value],
        value: data[valueKey.value]
      };
    }
  }, { immediate: true });

  watch(() => idList.value, (newValue) => {
    emit('change', newValue);
  }, { immediate: true, deep: true });
});

const create = (index: number) => {
  if (index < idList.value.length - 1) {
    return;
  }

  add();
};

const add = () => {
  const id = utils.uuid();
  idList.value.push(id);
  dataMap.value[id] = {
    disabled: false,
    name: '',
    value: ''
  };
};

const isValid = (): boolean => {
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
  const ids = idList.value;
  const map = dataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (!map[id].name) {
      nameErrorSet.value.add(id);
    }

    if (!map[id].value) {
      valueErrorSet.value.add(id);
    }
  }

  return !nameErrorSet.value.size && !valueErrorSet.value.size;
};

const getData = (): { [key: string]: string }[] => {
  const _labelKey = labelKey.value;
  const _valueKey = valueKey.value;
  const map = dataMap.value;
  return idList.value.map(item => {
    const data = map[item];
    return {
      [_labelKey]: data.name,
      [_valueKey]: data.value
    };
  });
};

defineExpose({
  getData,
  isValid,
  add: (data?: { value: string; name: string; disabled: boolean, prevValue: string }) => {
    const id = utils.uuid();
    if (data) {
      const { name, value, disabled, prevValue } = data;
      const firstItemId = idList.value[0];
      if (firstItemId && dataMap.value[firstItemId]) {
        const firstItem = dataMap.value[firstItemId];
        if (firstItem.name === name && firstItem.value === prevValue) {
          dataMap.value[firstItemId].value = value;
          return;
        }
      }

      idList.value.unshift(id);
      dataMap.value[id] = {
        disabled,
        name,
        value
      };

      return;
    }

    idList.value.push(id);
    dataMap.value[id] = {
      disabled: false,
      name: '',
      value: ''
    };
  },
  del: ({ name, prevValue }: { prevValue: string, value: string; name: string; }) => {
    const ids = idList.value;
    for (let i = 0, len = ids.length; i < len; i++) {
      const id = ids[i];
      const data = dataMap.value[id];
      if (data.value === prevValue && name === data.name) {
        idList.value = idList.value.filter(item => item !== id);
        delete dataMap.value[id];
        break;
      }
    }
  },
  clear: () => {
    reset();
  }
});

const fieldNames = { label: '_value', value: 'id' };
const inputProps = {
  maxlength: 400
};
</script>
<template>
  <div v-if="!!idList.length" class="leading-5">
    <div v-if="props.label" class="flex items-center mt-4 mb-0.5">{{ props.label }}</div>
    <div class="space-y-2">
      <div
        v-for="(item, index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <Composite :disabled="dataMap[item].disabled">
          <SelectInput
            :value="dataMap[item].name"
            :enumKey="props.enumKey"
            :inputProps="inputProps"
            :fieldNames="fieldNames"
            :error="nameErrorSet.has(item)"
            :disabled="dataMap[item].disabled"
            :search="nameSearch"
            showSearch
            mode="combination"
            style="flex: 1 1 40%;"
            trim
            :placeholder="t('mock.mockApisComp.contentForm.selectInputForm.parameterName')"
            @change="nameChange($event, item)">
            <template #option="record">
              <div class="truncate" :title="record._value + '-' + record.message">
                {{ record._value }} - {{ record.message }}
              </div>
            </template>
          </SelectInput>
          <Input
            :value="dataMap[item].value"
            :maxlength="4096"
            :error="valueErrorSet.has(item)"
            :disabled="dataMap[item].disabled"
            trim
            style="flex: 1 1 60%;"
            :placeholder="t('mock.mockApisComp.contentForm.selectInputForm.parameterValue')"
            @change="valueChange($event, item)" />
        </Composite>
        <div class="flex-shrink-0 space-x-1">
          <Button
            :disabled="dataMap[item].disabled"
            type="text"
            size="small">
            <Icon
              icon="icon-qingchu"
              class="text-3.5"
              @click="deleteHandler(index, item)" />
          </Button>
          <Button
            :class="{ invisible: index < (idList.length - 1) }"
            type="text"
            size="small"
            @click="create(index)">
            <Icon icon="icon-jia" class="text-3.5" />
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.ant-btn {
  padding: 0;
}

.ant-btn:not([disabled]):hover {
  color: var(--content-special-text-hover);
}
</style>
