<script setup lang="ts">
import { ref, watch, nextTick, inject, computed } from 'vue';
import { PureCard, notification } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { TESTER, http } from '@xcan-angus/tools';
import { mock } from '@/api/tester';

import { UserInfo } from './PropsType';
import DataField from './components/DataField.vue';
import DataConfig from './components/DataConfig.vue';
import ExcuteConfig from './components/ExcuteConfig.vue';
import viewScriptModal from './components/viewScriptModal.vue';

export interface Props {
  userInfo:UserInfo,
  cancel: () => void,
  params?: { [key: string]: any }
}

const props = withDefaults(defineProps<Props>(), {
  userInfo: undefined,
  cancel: () => undefined
});

const emits = defineEmits<{(e: 'formatChange', value: { [key: string]: any })}>();
const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const dataConfigRef = ref();
const dataFieldRef = ref();
const excuteConfigRef = ref();
// const format = ref('CVS');
const plugin = ref('MockCustom');
const viewDataVisible = ref(false); // 查看脚本弹窗
const scriptId = ref();
const scriptData = ref();

// 校验数据
const validateForm = async () => {
  return Promise.all([dataConfigRef.value.validate(), excuteConfigRef.value.validateForm(), dataFieldRef.value.validate()]);
};

// 整理保存数据
const getPackageData = () => {
  const dataConfigData = dataConfigRef.value.getData();
  const execConfigData = excuteConfigRef.value.getData();

  const { onError, nodeId, priority, threads, name, ...storeConfig } = execConfigData;
  const configuration = {
    onError: {
      action: onError
    },
    nodeSelectors: {
      availableNodeIds: [nodeId]
    },
    priority,
    thread: {
      threads: threads
    }
  };

  const mockData = {
    name: name,
    fields: dataFieldRef.value.getData().filter(i => !!i.name),
    settings: {
      ...dataConfigData,
      ...storeConfig
    }
  };
  return {
    configuration,
    mockData
  };
};

const getCurrentEditData = () => {
  const dataConfigData = dataConfigRef.value.getData();
  const execConfigData = excuteConfigRef.value.getCurrentData();
  const fileds = dataFieldRef.value.getData();
  return {
    dataConfigData,
    execConfigData,
    fileds
  };
};

// 保存脚本
const generateScript = async () => {
  validateForm().then(async () => {
    const { configuration, mockData } = await getPackageData();

    const [error, { data }] = await mock.mockData({ configuration, mockData, plugin: plugin.value, scriptId: scriptId.value, projectId: projectId.value });
    if (error) {
      return;
    }
    scriptId.value = data.id;
    notification.success('保存脚本成功');
  });
};

// 生成数据 + 脚本
const generatedata = async () => {
  validateForm().then(async () => {
    const { configuration, mockData } = await getPackageData();

    const [error, { data }] = await mock.execMockData({ configuration, mockData, plugin: plugin.value, scriptId: scriptId.value, projectId: projectId.value });
    if (error) {
      return;
    }
    scriptId.value = data.id;
    notification.success(`创建生成数据【${mockData.name}】成功，请在“执行”中查看进度和结果。`);
    cancel();
  });
};

// 查看生成的脚本
const viewScript = async () => {
  validateForm().then(async () => {
    const { configuration, mockData } = await getPackageData();
    const [error, { data }] = await mock.getGenerateScriptContent({ configuration, mockData, plugin: plugin.value, projectId: projectId.value });
    if (error) {
      return;
    }
    scriptData.value = data;
    viewDataVisible.value = true;
  });
};
const cancel = () => {
  if (typeof props.cancel === 'function') {
    props.cancel();
  }
};

const setData = () => {
  const { dataConfigData, execConfigData, fileds } = props.params;
  nextTick(() => {
    dataFieldRef.value.setData(fileds);
    dataConfigRef.value.setData(dataConfigData);
    excuteConfigRef.value.setData(execConfigData);
  });
};

// watch(() => format.value, async () => {
//   const params = getCurrentEditData();
//   emits('formatChange', { ...params, format: format.value, plugin: plugin.value });
// });

const onFormatChange = (value) => {
  const params = getCurrentEditData();
  emits('formatChange', { ...params, format: value });
};

watch(() => props.params, (newValue) => {
  if (newValue) {
    setData();
  }
}, {
  immediate: true
});

</script>

<template>
  <div class="flex flex-col space-y-2 h-full">
    <PureCard class="flex p-3.5 !shadow-none flex-1">
      <div class="flex-1 flex flex-col justify-between">
        <DataField
          ref="dataFieldRef"
          class="flex-1" />
        <div class="-mb-3">
          <DataConfig
            ref="dataConfigRef"
            format="CUSTOM"
            plugin="MockCustom"
            @formatChange="onFormatChange" />
        </div>
      </div>
      <div class="w-150 border-l border-border-divider ml-3.5 pl-3.5">
        <ExcuteConfig
          ref="excuteConfigRef"
          format="CUSTOM" />
      </div>
    </PureCard>
    <div class="flex space-x-3.5 justify-center" :style="{width: 'calc(100% - 600px)'}">
      <Button
        type="primary"
        size="small"
        @click="viewScript">
        查看脚本
      </Button>
      <Button
        type="primary"
        size="small"
        @click="generateScript">
        保存脚本
      </Button>
      <Button
        type="primary"
        size="small"
        @click="generatedata">
        生成数据
      </Button>
      <Button size="small" @click="cancel">取消</Button>
    </div>
    <viewScriptModal v-model:visible="viewDataVisible" :data="scriptData" />
  </div>
</template>
