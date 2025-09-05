<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Composite, Icon, Input } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

interface Props {
  value?: { [key: string]: string; }[];
  fielaNames?: { label: string; value: string; };
  label?: string;
  notify?: number;
  valueRequired?:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: () => [],
  fielaNames: () => ({ label: 'name', value: 'x-xc-value' }),
  label: undefined,
  notify: 0,
  valueRequired: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: string[]): void;
}>();

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: { name: string; value: string; } }>({});
const nameErrorSet = ref<Set<string>>(new Set<string>());
const valueErrorSet = ref<Set<string>>(new Set<string>());

const labelKey = computed(() => props.fielaNames.label);
const valueKey = computed(() => props.fielaNames.value);

const nameChange = (event: { target: { value: string } }, id: string) => {
  nameErrorSet.value.delete(id);
  dataMap.value[id].name = event.target.value;
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
  watch(() => props.notify, () => {
    reset();
  });

  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue?.length) {
      return;
    }

    for (let i = 0, len = newValue.length; i < len; i++) {
      const id = utils.uuid();
      idList.value.push(id);
      dataMap.value[id] = {
        name: newValue[i][labelKey.value],
        value: newValue[i][valueKey.value]
      };
    }
  }, { immediate: true });

  watch(() => idList.value, (newValue) => {
    emit('change', newValue);
  }, { immediate: true, deep: true });
});

const create = (index: number) => {
  if (index < (idList.value.length - 1)) {
    return;
  }

  add();
};

const add = () => {
  const id = utils.uuid();
  idList.value.push(id);
  dataMap.value[id] = {
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

    if (props.valueRequired) {
      if (!map[id].value) {
        valueErrorSet.value.add(id);
      }
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
  add,
  clear: () => {
    reset();
  }
});
</script>
<template>
  <div v-if="!!idList.length" class="leading-5">
    <div v-if="props.label" class="flex items-center mt-4 mb-0.5">{{ props.label }}</div>

    <div class="space-y-2">
      <div
        v-for="(item, index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <Composite>
          <Input
            :value="dataMap[item].name"
            :maxlength="400"
            :error="nameErrorSet.has(item)"
            style="flex: 1 1 40%;"
            trim
            placeholder="参数名称，最大支持400个字符"
            @change="nameChange($event, item)" />
          <Input
            :value="dataMap[item].value"
            :maxlength="4096"
            :error="valueErrorSet.has(item)"
            trim
            style="flex: 1 1 60%;"
            placeholder="参数值，最大支持4096个字符"
            @change="valueChange($event, item)" />
        </Composite>
        <div class="flex-shrink-0 space-x-1">
          <Button type="text" size="small">
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

.ant-btn:hover {
  color: var(--content-special-text-hover);
}
</style>
