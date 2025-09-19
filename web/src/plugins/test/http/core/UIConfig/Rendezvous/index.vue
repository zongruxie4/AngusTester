<script setup lang="ts">
import { ref, onMounted, watch, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Icon, Tooltip } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { RendezvousConfig } from './PropsType';
import ActionsGroup from '../ActionsGroup/index.vue';

const { t } = useI18n();

export interface Props {
  value: RendezvousConfig;
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
  (e: 'change', value: Omit<RendezvousConfig, 'id'>): void;
  (e: 'nameChange', value: string): void;
  (e: 'click', value:'delete'|'clone'):void;
  (e: 'enabledChange', value: boolean): void;
  (e: 'renderChange'): void;
}>();

const enabled = ref(false);
const name = ref<string>();
const description = ref<string>();
const threads = ref<string>();
const timeoutInMs = ref<string>();

const timeoutInMsError = ref(false);
const threadsError = ref(false);
const nameError = ref(false);
const nameRepeatFlag = ref(false);

const nameChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  name.value = value;
  nameError.value = false;
  nameRepeatFlag.value = false;
  emit('nameChange', value);
};

const threadsChange = (event: { target: { value: string } }) => {
  threads.value = event.target.value;
  threadsError.value = false;
};

const timeoutInMsChange = (event: { target: { value: string } }) => {
  timeoutInMs.value = event.target.value;
  timeoutInMsError.value = false;
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
    name: _name,
    description: _description,
    enabled: _enabled,
    threads: _threads,
    timeoutInMs: _timeoutInMs
  } = props.value;
  name.value = _name;
  description.value = _description;
  enabled.value = _enabled;
  threads.value = _threads;
  timeoutInMs.value = _timeoutInMs;
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
  threadsError.value = false;
  timeoutInMsError.value = false;
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

  if (utils.isEmpty(threads.value)) {
    errorNum++;
    threadsError.value = true;
  }

  if (utils.isEmpty(timeoutInMs.value)) {
    errorNum++;
    timeoutInMsError.value = true;
  }

  return !errorNum;
};

const getData = ():Omit<RendezvousConfig, 'id'> => {
  return {
    beforeName: '',
    transactionName: '',
    description: description.value,
    enabled: enabled.value,
    name: name.value,
    target: 'RENDEZVOUS',
    threads: threads.value,
    timeoutInMs: timeoutInMs.value
  };
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
</script>

<template>
  <div :class="{ 'opacity-70': !enabled && props.enabled }" class="h-11.5 flex items-center pl-9.5 pr-3 rounded bg-gray-light">
    <Icon class="flex-shrink-0 mr-3 text-4" icon="icon-jihedian1" />
    <div class="flex-1 flex items-center space-x-5 mr-5">
      <Tooltip
        :title="t('httpPlugin.uiConfig.rendezvous.duplicateName')"
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
          class="point-name-input"
          :placeholder="t('httpPlugin.uiConfig.rendezvous.namePlaceholder')"
          @change="nameChange" />
      </Tooltip>
      <div class="flex items-center space-x-2 text-theme-content">
        <div>{{ t('httpPlugin.uiConfig.rendezvous.userCount') }}</div>
        <Input
          :value="threads"
          :maxlength="7"
          :min="1"
          :max="7200000"
          :error="threadsError"
          trimAll
          dataType="number"
          class="w-25"
          placeholder="1 ~ 7200000"
          @change="threadsChange" />
      </div>
      <div class="flex items-center space-x-2">
        <div>{{ t('httpPlugin.uiConfig.rendezvous.waitTimeout') }}</div>
        <Input
          :value="timeoutInMs"
          :maxlength="7"
          :min="1"
          :max="7200000"
          :error="timeoutInMsError"
          trimAll
          dataType="number"
          class="w-25"
          placeholder="1 ~ 7200000"
          @change="timeoutInMsChange" />
        <div>ms</div>
      </div>
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
.point-name-input {
  flex: 0 0 calc((100% - (124px))*2/5);
}

.child-drag-container .point-name-input {
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
</style>
