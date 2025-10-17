<script setup lang="ts">
import { ref, onMounted, watch, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Input, Icon, Tooltip } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { TransStartConfig } from './PropsType';
import ActionsGroup from '../ActionsGroup/index.vue';

const { t } = useI18n();

export interface Props {
  value: TransStartConfig;
  repeatNames: string[];
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  repeatNames: () => []
});


const emit = defineEmits<{
  (e: 'change', value: Omit<TransStartConfig, 'id'>): void;
  (e: 'nameChange', value: string): void;
  (e: 'click', value:'delete'|'clone'):void;
  (e: 'enabledChange', value: boolean): void;
  (e: 'renderChange'): void;
}>();

const UUID = utils.uuid();
const activeKey = ref<string>(UUID);
const enabled = ref(false);
const name = ref<string>();
const description = ref<string>();
const nameError = ref(false);
const nameRepeatFlag = ref(false);

const nameChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  name.value = value;
  nameError.value = false;
  nameRepeatFlag.value = false;
  emit('nameChange', value);
};

const openChange = (_open: boolean) => {
  if (_open) {
    activeKey.value = UUID;
    return;
  }

  activeKey.value = '';
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

  const { name: _name, description: _description, enabled: _enabled } = props.value;
  name.value = _name;
  description.value = _description;
  enabled.value = _enabled;
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

const isValid = (): boolean => {
  nameError.value = false;
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

  return !errorNum;
};

const getData = (): Omit<TransStartConfig, 'id'> => {
  return {
    beforeName: '',
    transactionName: '',
    description: description.value,
    enabled: enabled.value,
    name: name.value,
    target: 'TRANS_START'
  };
};

defineExpose({
  getData,
  isValid,
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
  <Collapse
    :activeKey="activeKey"
    :class="{ 'opacity-70': !enabled }"
    class="trans-collapse-container text-3"
    style="background-color: #fff;"
    :bordered="false">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
          <Icon class="flex-shrink-0 mr-3 text-4" icon="icon-shiwu" />
          <div class="flex-1 flex items-center">
            <Tooltip
              :title="t('httpPlugin.uiConfig.transStart.duplicateName')"
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
                style="width: calc((100% - (144px))*2/5);"
                :placeholder="t('common.placeholders.searchKeyword')"
                @change="nameChange" />
            </Tooltip>
          </div>
          <ActionsGroup
            v-model:enabled="enabled"
            :open="activeKey === UUID"
            :arrowVisible="true"
            @openChange="openChange"
            @enabledChange="enabledChange"
            @click="actionClick" />
        </div>
      </template>
      <slot name="default"></slot>
    </CollapsePanel>
  </Collapse>
</template>
<style scoped>
.ant-collapse.trans-collapse-container {
  line-height: 20px;
}

.ant-collapse-borderless.trans-collapse-container > :deep(.ant-collapse-item) {
  border: none;
  border-radius: 4px;
}

.ant-collapse.trans-collapse-container> :deep(.ant-collapse-item)>.ant-collapse-header {
  align-items: center;
  height: 46px;
  padding: 0 12px 0 38px;
  border-radius: 4px;
  background-color: rgba(232, 240, 251, 100%);
  line-height: 20px;
}

.trans-collapse-container > :deep(.ant-collapse-item) > .ant-collapse-content > .ant-collapse-content-box {
  padding: 0;
  background-color: #fff;
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
