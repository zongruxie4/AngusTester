<script setup lang="ts">
import { ref, onMounted, watch, watchEffect } from 'vue';
import { Input, Select, Icon, Tooltip } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/tools';

import { WaitingTimeConfig } from './PropsType';
import ActionsGroup from '../ActionsGroup/index.vue';

export interface Props {
  value: WaitingTimeConfig;
  repeatNames: string[];
  enabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  repeatNames: () => [],
  enabled: true
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: Omit<WaitingTimeConfig, 'id'>): void;
  (e: 'nameChange', value: string): void;
  (e: 'click', value:'delete'|'clone'):void;
  (e: 'enabledChange', value: boolean): void;
  (e: 'renderChange'): void;
}>();

const enabled = ref(false);
const name = ref<string>();
const description = ref<string>();
const waitType = ref<'fixed' | 'random'>('fixed');
const maxWaitTimeInMs = ref<string>();
const minWaitTimeInMs = ref<string>();
const minWaitTimeInMsError = ref(false);
const maxWaitTimeInMsError = ref(false);
const nameError = ref(false);
const nameRepeatFlag = ref(false);

const nameChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  name.value = value;
  nameError.value = false;
  nameRepeatFlag.value = false;
  emit('nameChange', value);
};

const waitTypeChange = (value: 'fixed' | 'random') => {
  waitType.value = value;
  minWaitTimeInMs.value = undefined;
  minWaitTimeInMsError.value = false;
  maxWaitTimeInMs.value = undefined;
  maxWaitTimeInMsError.value = false;
};

const maxWaitTimeInMsChange = (event: { target: { value: string } }) => {
  maxWaitTimeInMs.value = event.target.value;
  maxWaitTimeInMsError.value = false;
};

const maxWaitTimeInMsBlur = (event: { target: { value: string } }) => {
  maxWaitTimeInMs.value = event.target.value;
  maxWaitTimeInMsError.value = false;
  validateMaxTime();
};

const validateMaxTime = () => {
  if (utils.isEmpty(maxWaitTimeInMs.value)) {
    maxWaitTimeInMsError.value = true;
    return;
  }

  if (utils.isEmpty(minWaitTimeInMs.value)) {
    maxWaitTimeInMsError.value = false;
    return;
  }

  if (+minWaitTimeInMs.value > +maxWaitTimeInMs.value) {
    maxWaitTimeInMsError.value = true;
    return;
  }

  maxWaitTimeInMsError.value = false;
};

const minWaitTimeInMsChange = (event: { target: { value: string } }) => {
  minWaitTimeInMs.value = event.target.value;
  minWaitTimeInMsError.value = false;
};

const minWaitTimeInMsBlur = (event: { target: { value: string } }) => {
  const value = event.target.value;
  minWaitTimeInMs.value = value;
  validateMinTime();
};

const validateMinTime = () => {
  if (utils.isEmpty(minWaitTimeInMs.value)) {
    minWaitTimeInMsError.value = true;
    return;
  }

  if (utils.isEmpty(maxWaitTimeInMs.value)) {
    minWaitTimeInMsError.value = false;
    return;
  }

  if (+minWaitTimeInMs.value > +maxWaitTimeInMs.value) {
    maxWaitTimeInMsError.value = true;
    return;
  }

  minWaitTimeInMsError.value = false;
};

const enabledChange = (_enabled: boolean) => {
  enabled.value = _enabled;
  emit('enabledChange', _enabled);
};

const actionClick = (value: 'delete' | 'clone') => {
  emit('click', value);
};

const initializedData = () => {
  if (!props.value) {
    return;
  }

  const {
    maxWaitTimeInMs: _maxWaitTimeInMs,
    minWaitTimeInMs: _minWaitTimeInMs,
    name: _name,
    description: _description,
    enabled: _enabled
  } = props.value;

  // 如果只有最大等待时间，就是固定等待时间
  if (!utils.isEmpty(_maxWaitTimeInMs) && (_minWaitTimeInMs === undefined || _minWaitTimeInMs === null)) {
    waitType.value = 'fixed';
  } else {
    waitType.value = 'random';
  }

  name.value = _name;
  description.value = _description;
  enabled.value = _enabled;
  maxWaitTimeInMs.value = _maxWaitTimeInMs;
  minWaitTimeInMs.value = _minWaitTimeInMs;
};

onMounted(() => {
  emit('renderChange');
  initializedData();
  watch(() => props.repeatNames, (newValue) => {
    if (newValue.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
      return;
    }

    if (nameRepeatFlag.value) {
      nameError.value = false;
      nameRepeatFlag.value = false;
    }
  });

  watchEffect(() => {
    const data = getData();
    emit('change', data);
  });
});

const isValid = ():boolean => {
  nameError.value = false;
  maxWaitTimeInMsError.value = false;
  minWaitTimeInMsError.value = false;
  nameRepeatFlag.value = false;
  let errorNum = 0;
  if (!name.value) {
    errorNum++;
    nameError.value = true;
  } else {
    if (props.repeatNames.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
    }
  }
  // 校验最大等待时间
  validateMaxTime();
  // 只有随机等待时间才需要校验最小等待时间
  if (waitType.value === 'random') {
    validateMinTime();
  }

  return !errorNum && !maxWaitTimeInMsError.value && !minWaitTimeInMsError.value;
};

const getData = ():Omit<WaitingTimeConfig, 'id'> => {
  const data:Omit<WaitingTimeConfig, 'id'> = {
    beforeName: '',
    transactionName: '',
    description: description.value,
    enabled: enabled.value,
    maxWaitTimeInMs: maxWaitTimeInMs.value,
    name: name.value,
    target: 'WAITING_TIME'
  };

  if (waitType.value === 'random') {
    data.minWaitTimeInMs = minWaitTimeInMs.value;
  }

  return data;
};

defineExpose({
  isValid,
  getData,
  getName: ():string => {
    return name.value;
  },
  validateRepeatName: (value:string[]):boolean => {
    if (value.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
      return false;
    }

    return true;
  }
});

const waitTypeoptions = [
  { label: '固定等待', value: 'fixed' },
  { label: '随机等待', value: 'random' }
];
</script>

<template>
  <div :class="{'opacity-70':!enabled&&props.enabled}" class="h-11.5 flex items-center pl-9.5 pr-3 rounded bg-gray-light">
    <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-dengdaishijian" />
    <div class="flex-1 flex items-center space-x-5 mr-5">
      <Tooltip
        title="名称重复"
        internal
        placement="right"
        destroyTooltipOnHide
        :visible="nameRepeatFlag">
        <Input
          :value="name"
          :title="name"
          :maxlength="400"
          :error="nameError"
          trim
          class="time-name-input"
          placeholder="名称，最长400个字符"
          @change="nameChange" />
      </Tooltip>
      <Select
        :value="waitType"
        :border="false"
        :options="waitTypeoptions"
        class="w-25 flex-shrink-0"
        @change="waitTypeChange" />
      <template v-if="waitType === 'fixed'">
        <div class="flex items-center space-x-2">
          <Input
            :value="maxWaitTimeInMs"
            :maxlength="7"
            :min="0"
            :max="7200000"
            :error="maxWaitTimeInMsError"
            trimAll
            dataType="number"
            class="w-25"
            placeholder="0 ~ 7200000"
            @change="maxWaitTimeInMsChange"
            @blur="maxWaitTimeInMsBlur" />
          <div>ms</div>
        </div>
      </template>
      <template v-else>
        <div class="flex items-center space-x-2">
          <Input
            :value="minWaitTimeInMs"
            :maxlength="7"
            :min="0"
            :max="7200000"
            :error="minWaitTimeInMsError"
            trimAll
            dataType="number"
            class="w-25"
            placeholder="0 ~ 7200000"
            @change="minWaitTimeInMsChange"
            @blur="minWaitTimeInMsBlur" />
          <div>至</div>
          <Input
            :value="maxWaitTimeInMs"
            :maxlength="7"
            :min="0"
            :max="7200000"
            :error="maxWaitTimeInMsError"
            trimAll
            dataType="number"
            class="w-25"
            placeholder="0 ~ 7200000"
            @change="maxWaitTimeInMsChange"
            @blur="maxWaitTimeInMsBlur" />
          <div>ms</div>
        </div>
      </template>
    </div>
    <ActionsGroup
      v-model:enabled="enabled"
      :open="false"
      :arrowVisible="false"
      @enabledChange="enabledChange"
      @click="actionClick" />
  </div>
</template>

<style scoped>
.time-name-input {
  flex: 0 0 calc((100% - (124px))*2/5);
}

.child-drag-container .time-name-input {
  flex: 0 0 calc((100% - (134px))*2/5);
}

.bg-gray-light {
  background-color: rgba(239, 240, 243, 100%);
}

.ant-input-affix-wrapper {
  border-color: #fff;
  background-color: #fff;
}

.ant-input-affix-wrapper:not(.error):hover,
.ant-input-affix-wrapper:not(.error):focus,
.ant-input-affix-wrapper-focused:not(.error) {
  border-color: #40a9ff;
}

.ant-select :deep(.ant-select-selector) {
  border-color: #fff;
  background-color: #fff;
}
</style>
