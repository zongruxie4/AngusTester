<script setup lang="ts">
import { defineAsyncComponent, nextTick, ref, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, NoData, Hints } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import Draggable from 'vuedraggable';

import { PipelineConfig } from '../PropsType';

const { t } = useI18n();

export interface Props {
  value: PipelineConfig[];
  loaded: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  loaded: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  // (e: 'errorNumChange', value: number): void;
  (e: 'renderChange', value: boolean): void;
  (e: 'addGlobalVariables', value): void;
}>();

const domId = utils.uuid();

const scrollToBottom = () => {
  const dom = document.getElementById(domId);
  if (!dom) {
    return;
  }

  nextTick(() => {
    setTimeout(() => {
      dom.scrollTop = dom.scrollHeight;
    }, 16.66);
  });
};

// const renderChange = (value: boolean) => {
//   emit('renderChange', value);
// };

const generateEmptSmtpData = ():PipelineConfig => {
  return {
    id: utils.uuid(),
    target: 'SMTP',
    name: '',
    description: '',
    enabled: !dataList.value.length,
    beforeName: '',
    server: {
      server: '',
      port: '',
      useAuth: false,
      username: '',
      password: '',
      readTimeout: '60s',
      connectTimeout: '6s',
      security: {
        use: 'NONE',
        trustAllCerts: true,
        enforceStartTLS: false,
        useLocalTrustStore: false,
        trustStorePath: '',
        trustStoreBase64Content: '',
        tlsProtocols: ''
      }
    },
    mail: {
      mailFrom: '',
      replyTo: '',
      receiverCC: '',
      receiverBCC: '',
      receiverTo: '',
      content: {
        subject: '',
        message: '',
        includeTimestamp: false,
        suppressSubject: false,
        sendEmlMessage: false, // {{ t('smtpPlugin.uiConfig.mailContent.comments.sendEmlMessage') }}
        plainBody: true,
        messageSizeStatistics: false,
        enableDebugLogging: false,
        localAttachFiles: [],
        localEmlMessageFile: '', // {{ t('smtpPlugin.uiConfig.mailContent.comments.localEmlMessageFile') }}
        localEmlMessageBase64Content: '', // {{ t('smtpPlugin.uiConfig.mailContent.comments.localEmlMessageBase64Content') }}
        headerFields: [],
        localAttachBase64Contents: undefined
      }
    }
  };
};

const insertSMTP = () => {
  const uuid = utils.uuid();
  const data: PipelineConfig = {
    uuid: uuid,
    activeKey: uuid,
    ...generateEmptSmtpData()
  };
  add(data);

  scrollToBottom();
};

// const errorNumChange = (value: number) => {
//   emit('errorNumChange', value);
// };

//   -----------------------------------------------------------------------------------

type DragDataConfig = {
  id: string;
  target: 'SMTP';
}

const SMTPConfigs = defineAsyncComponent(() => import('./SMTPConfigs/index.vue'));

const rendered = ref(false);

const refsMap = ref<{ [key: string]: any }>({});
const dataList = ref<DragDataConfig[]>([]);
const dataMap = ref<{ [key: string]: PipelineConfig }>({});
const errorNumMap = ref<{ [key: string]: number }>({});
const repeatNames = ref<string[]>([]);

const renderedNum = ref(0);
const totalPipelines = ref(0);

const actionClick = (value: 'delete' | 'clone', id: string) => {
  if (value === 'clone') {
    toClone(id);
    return;
  }

  if (value === 'delete') {
    toDelete(id);
  }
};

const toClone = (targetId: string) => {
  const list = dataList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    const { id, target } = list[i];
    if (id === targetId) {
      const pid = utils.uuid();
      const sourceData: PipelineConfig = refsMap.value[targetId].getData();
      dataMap.value[pid] = sourceData;
      dataMap.value[pid].name = sourceData.name ? (sourceData.name + ' copy') : '';
      dataMap.value[pid].enabled = false;
      const newData: DragDataConfig = { id: pid, target };
      dataList.value.splice(i + 1, 0, newData);
      return;
    }
  }
};

const toDelete = (targetId: string) => {
  const list = dataList.value;
  const delList: PipelineConfig[] = [];
  for (let i = 0, len = list.length; i < len; i++) {
    const { id } = list[i];
    if (id === targetId) {
      const name = refsMap.value[id]?.getName();
      repeatNames.value = repeatNames.value.filter(item => item !== name);
      delList.push(dataMap.value[id]);
      if (dataMap.value[id].enabled && list.length > 1) {
        const enabledTarget = list[i - 1] || list[i + 1];
        dataMap.value[enabledTarget.id].enabled = true;
      }
      delete dataMap.value[id];
      delete refsMap.value[id];
      dataList.value.splice(i, 1);
      break;
    }
  }

  setTimeout(() => {
    for (const key in refsMap.value) {
      if (!refsMap.value[key]) {
        delete refsMap.value[key];
      }
    }
  }, 16.66);
};

const nameChange = () => {
  const uniqueNames = new Set();
  repeatNames.value = [];
  const refs = Object.values(refsMap.value);
  for (let i = 0, len = refs.length; i < len; i++) {
    if (typeof refs[i]?.getName === 'function') {
      const name = refs[i].getName();
      if (name) {
        if (uniqueNames.has(name)) {
          repeatNames.value.push(name);
        } else {
          uniqueNames.add(name);
        }
      }
    }
  }
};

const enabledChange = (enabled: boolean, targetId: string) => {
  const list = dataList.value;
  if (enabled) {
    for (let i = 0, len = list.length; i < len; i++) {
      const { id } = list[i];
      if (id === targetId) {
        dataMap.value[id].enabled = enabled;
      } else if (enabled) {
        dataMap.value[id].enabled = false;
      }
    }
  } else {
    for (let i = 0, len = list.length; i < len; i++) {
      const { id } = list[i];
      if (id === targetId) {
        dataMap.value[id].enabled = enabled;
        if (list[i - 1]) {
          dataMap.value[list[i - 1].id].enabled = true;
        } else if (list[i + 1]) {
          dataMap.value[list[i + 1].id].enabled = true;
        }
      }
    }
  }
};

const change = (data: PipelineConfig, id: string) => {
  dataMap.value[id] = data;
};

const reset = () => {
  refsMap.value = {};
  dataList.value = [];
  dataMap.value = {};
  errorNumMap.value = {};
  repeatNames.value = [];
  renderedNum.value = 0;
  totalPipelines.value = 0;
};

const renderChange = () => {
  renderedNum.value += 1;
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue?.length) {
      return;
    }

    totalPipelines.value = newValue.length;
    const list = JSON.parse(JSON.stringify(newValue));
    for (let i = 0, len = list.length; i < len; i++) {
      const data = list[i];
      const { id, target } = data;
      const temp: DragDataConfig = { id, target };
      dataList.value.push(temp);
      // 没有设置enabled字段，默认值为true
      if (data.enabled !== false) {
        data.enabled = true;
        const uuid = utils.uuid();
        data.uuid = uuid;
        data.activeKey = uuid;
      }

      dataMap.value[id] = data;
    }
  }, { immediate: true });

  watch(() => renderedNum.value, (newValue) => {
    if (newValue === totalPipelines.value) {
      emit('renderChange', true);
      rendered.value = true;
    }
  }, { immediate: true });
});

const add = (data: PipelineConfig) => {
  const listItem: { id: string; target: 'SMTP' } = { id: data.id, target: data.target };
  dataMap.value[data.id] = data;
  dataList.value.push(listItem);
};

const getData = (): PipelineConfig[] => {
  const result: PipelineConfig[] = [];
  const list = dataList.value;
  let beforeName = '';
  const _refsMap = refsMap.value;
  for (let i = 0, len = list.length; i < len; i++) {
    const { id } = list[i];
    if (typeof _refsMap[id]?.getData === 'function') {
      const tempData = _refsMap[id].getData();
      if (tempData) {
        result.push({ ...tempData, beforeName });
        beforeName = tempData.name;
      }
    }
  }

  return JSON.parse(JSON.stringify(result));
};

const isValid = (): boolean => {
  const uniqueNames = new Set();
  repeatNames.value = [];
  const refs = Object.values(refsMap.value);
  for (let i = 0, len = refs.length; i < len; i++) {
    if (typeof refs[i]?.getName === 'function') {
      const name = refs[i].getName();
      if (name) {
        if (uniqueNames.has(name)) {
          repeatNames.value.push(name);
        } else {
          uniqueNames.add(name);
        }
      }
    }
  }

  let errorNum = 0;
  const list = dataList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    const { id } = list[i];
    if (typeof refsMap.value[id]?.isValid === 'function') {
      if (!refsMap.value[id].isValid()) {
        errorNum++;
      }
    }

    if (typeof refsMap.value[id]?.validateRepeatName === 'function') {
      if (!refsMap.value[id].validateRepeatName(repeatNames.value)) {
        errorNum++;
      }
    }
  }

  return !errorNum;
};

defineExpose({
  add,
  getData,
  isValid,
  generateEmptSmtpData
});

</script>

<template>
  <div class="py-5 h-full text-3 leading-5">
    <div class="flex items-center flex-nowrap whitespace-nowrap space-x-2 px-5">
      <Button
        type="default"
        size="small"
        @click="insertSMTP">
        <div class="flex items-center">
          <Icon icon="icon-chajianpeizhi" class="mr-1" />
          <span>{{ t('smtpPlugin.uiConfig.title') }}</span>
        </div>
      </Button>
      <Hints :text="t('common.description')"></Hints>
    </div>
    <template v-if="props.loaded && !dataList.length">
      <NoData />
    </template>
    <template v-else>
      <Draggable
        :id="domId"
        style="height: calc(100% - 40px);"
        class="my-4 pl-5 pr-4 space-y-3 overflow-y-auto overflow-x-hidden scroll-smooth"
        :list="dataList"
        :animation="300"
        group="scenario"
        itemKey="id"
        tag="ul"
        handle=".drag-handle">
        <template #item="{ element: { id, target } }: { element: DragDataConfig }">
          <li
            :id="id"
            :key="id"
            :type="target"
            class="drag-item relative">
            <Icon
              :class="{ invisible: !rendered }"
              icon="icon-yidong"
              class="drag-handle absolute top-3.75 left-3 z-10 text-4 cursor-move text-theme-sub-content" />
            <SMTPConfigs
              :ref="el => refsMap[id] = el"
              :value="dataMap[id]"
              :enabledDisabled="dataList.length < 2"
              :repeatNames="repeatNames"
              @actionClick="actionClick($event, id)"
              @nameChange="nameChange"
              @enabledChange="enabledChange($event, id)"
              @change="change($event, id)"
              @renderChange="renderChange" />
          </li>
        </template>
      </Draggable>
    </template>
  </div>
</template>
