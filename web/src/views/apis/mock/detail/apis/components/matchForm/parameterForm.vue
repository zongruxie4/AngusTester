<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Composite, Icon, Input, Validate } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import SelectEnum from '@/components/selectEnum/index.vue';
import { Condition } from './PropsType';

const { t } = useI18n();

type Parameter = {
  condition: Condition;
  expected: string|undefined;
  expression: string|undefined;
  in: 'header'|'query';
  name: string;
}

interface Props {
  in:'header'|'query';
  value: Parameter[];
  notify:number;
}

const props = withDefaults(defineProps<Props>(), {
  in: 'header',
  value: () => [],
  notify: 0
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'change', value:string[]):void;
}>();

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: Parameter&{value:string|undefined} }>({});
const nameErrorSet = ref<Set<string>>(new Set());
const valueErrorSet = ref<Set<string>>(new Set());
const errorMessageMap = ref<Map<string, string>>(new Map());

const excludes = ({ value }) => {
  return ['XPATH_MATCH', 'JSON_PATH_MATCH'].includes(value);
};

const nameChange = (event:{target:{value:string;}}, id:string) => {
  const value = event.target.value;
  dataMap.value[id].name = value;
  nameErrorSet.value.delete(id);
};

const conditionChange = (value:Condition, id:string) => {
  dataMap.value[id].condition = value;
  if (['IS_EMPTY', 'IS_NULL', 'NOT_EMPTY', 'NOT_NULL'].includes(value)) {
    dataMap.value[id].value = undefined;
    valueErrorSet.value.delete(id);
  }
};

const valueChange = (event:{target:{value:string;}}, id:string) => {
  const value = event.target.value;
  dataMap.value[id].value = value;
  valueErrorSet.value.delete(id);
  errorMessageMap.value.delete(id);
};

const deleteHandler = (index:number, id: string) => {
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  nameErrorSet.value.delete(id);
  valueErrorSet.value.delete(id);
  errorMessageMap.value.delete(id);
};

const isValid = ():boolean => {
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
  errorMessageMap.value.clear();
  if (!idList.value.length) {
    return true;
  }

  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { name, condition, value } = dataMap.value[id];
    if (!name) {
      nameErrorSet.value.add(id);
    }

    if (!['IS_EMPTY', 'IS_NULL', 'NOT_EMPTY', 'NOT_NULL'].includes(condition)) {
      if (!value) {
        valueErrorSet.value.add(id);
      }
    }
  }

  return !nameErrorSet.value.size && !valueErrorSet.value.size;
};

const reset = () => {
  idList.value = [];
  dataMap.value = {};
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
  errorMessageMap.value.clear();
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
      const data = newValue[i];
      const id = utils.uuid();
      idList.value.push(id);
      dataMap.value[id] = {
        ...data,
        value: undefined
      };
      if (EXPRESSION_ENUMS.includes(data.condition)) {
        dataMap.value[id].value = data.expression;
      } else {
        dataMap.value[id].value = data.expected;
      }
    }
  }, { immediate: true });

  watch(() => idList.value, (newValue) => {
    emit('change', newValue);
  }, { immediate: true });
});

const create = (index:number) => {
  if (index < idList.value.length - 1) {
    return;
  }

  add();
};

const add = () => {
  const id = utils.uuid();
  idList.value.push(id);
  dataMap.value[id] = {
    condition: 'EQUAL',
    expected: undefined,
    expression: undefined,
    in: props.in,
    name: '',
    value: undefined
  };
};

const getData = ():Parameter[]|undefined => {
  if (!idList.value.length) {
    return undefined;
  }

  const map = dataMap.value;
  const _in = props.in;
  return idList.value.map(item => {
    const data = map[item];
    const _data:Parameter = {
      in: _in,
      name: data.name,
      condition: data.condition,
      expected: undefined,
      expression: undefined
    };

    if (EXPRESSION_ENUMS.includes(data.condition)) {
      _data.expression = data.value;
    } else {
      _data.expected = data.value;
    }

    return _data;
  });
};

defineExpose({
  getData,
  isValid,
  add
});

const EXPRESSION_ENUMS = ['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'];
const placeholderMap = computed(() => {
  const ids = idList.value;
  const data = dataMap.value;
  const emptyConditions = ['IS_EMPTY', 'IS_NULL', 'NOT_EMPTY', 'NOT_NULL'];
  const map:{[key:string]:string} = {};
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const condition = data[id].condition;
    if (emptyConditions.includes(condition)) {
      map[id] = '';
    } else if (condition === 'REG_MATCH') {
      map[id] = t('mock.mockApisComp.matchForm.parameterForm.regMatch');
    } else {
      map[id] = t('mock.mockApisComp.matchForm.parameterForm.parameterValue');
    }
  }

  return map;
});
const disabledMap = computed(() => {
  const ids = idList.value;
  const data = dataMap.value;
  const disabledConditions = ['IS_EMPTY', 'IS_NULL', 'NOT_EMPTY', 'NOT_NULL'];
  const map:{[key:string]:boolean} = {};
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const condition = data[id].condition;
    if (disabledConditions.includes(condition)) {
      map[id] = true;
    }
  }

  return map;
});
</script>
<template>
  <div v-if="!!idList.length" class="leading-5">
    <div class="flex items-center mb-0.5">{{ t('mock.mockApisComp.matchForm.parameterForm.queryParameters') }}</div>
    <div class="space-y-2">
      <div
        v-for="(item,index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <Composite>
          <Input
            v-model:value="dataMap[item].name"
            :maxlength="400"
            :error="nameErrorSet.has(item)"
            style="flex: 0 0 calc((100% - 200px) * 2/5);"
            trim
            :placeholder="t('mock.mockApisComp.matchForm.parameterForm.parameterName')"
            @change="nameChange($event,item)" />
          <SelectEnum
            :value="dataMap[item].condition"
            :excludes="excludes"
            enumKey="FullMatchCondition"
            class="flex-shrink-0 w-48"
            placeholder="运算符"
            @change="conditionChange($event,item)" />
          <Validate
            class="flex-1"
            :fixed="true"
            :text="errorMessageMap[item]">
            <Input
              v-model:value="dataMap[item].value"
              :disabled="disabledMap[item]"
              :maxlength="4096"
              :error="valueErrorSet.has(item)"
              :placeholder="placeholderMap[item]"
              trim
              @change="valueChange($event,item)" />
          </Validate>
        </Composite>
        <div class="flex-shrink-0 space-x-1">
          <Button type="text" size="small">
            <Icon
              icon="icon-qingchu"
              class="text-3.5"
              @click="deleteHandler(index,item)" />
          </Button>
          <Button
            :class="{invisible:index<(idList.length-1)}"
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
.ant-btn{
  padding: 0;
}

.ant-btn:hover{
  color: var(--content-special-text-hover);
}
</style>
