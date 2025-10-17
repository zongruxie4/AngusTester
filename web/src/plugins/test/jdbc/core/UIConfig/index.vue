<script setup lang="ts">
import { defineAsyncComponent, nextTick, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { PipelineConfig, TargetKey } from './PropsType';
import { JDBCConfig } from './JDBCConfigs/PropsType';
import { WaitingTimeConfig } from '@/plugins/test/components/UIConfigComp/WaitingTime/PropsType';
import { RendezvousConfig } from '@/plugins/test/components/UIConfigComp/Rendezvous/PropsType';
import { TransEndConfig } from '@/plugins/test/components/UIConfigComp/TransEnd/PropsType';
import { TransStartConfig } from '@/plugins/test/components/UIConfigComp/TransStart/PropsType';

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
  (e:'errorNumChange', value:number):void;
  (e:'renderChange', value:boolean):void;
}>();

const Draggable = defineAsyncComponent(() => import('./Draggable/index.vue'));

const dragRef = ref();
const domId = utils.uuid();
const isTransEnd = ref(false);

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

const renderChange = (value:boolean) => {
  emit('renderChange', value);
};

const insertJDBC = () => {
  const data: JDBCConfig = {
    arguments: [],
    assertions: [],
    variables: [],
    beforeName: '',
    transactionName: '',
    description: '',
    enabled: true,
    id: utils.uuid(),
    name: '',
    sql: '',
    target: 'JDBC',
    maxResultRows: '1000',
    timeoutInSecond: '60',
    type: undefined
  };
  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

const insertTransStart = () => {
  isTransEnd.value = true;
  const data: TransStartConfig = {
    id: utils.uuid(),
    target: 'TRANS_START',
    name: '',
    description: '',
    enabled: true,
    beforeName: '',
    transactionName: ''
  };

  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

const insertTransEnd = () => {
  isTransEnd.value = false;
  const data: TransEndConfig = {
    id: utils.uuid(),
    target: 'TRANS_END',
    name: '',
    description: '',
    beforeName: '',
    transactionName: '',
    enabled: true
  };

  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

let waitingTimeNum = 0;
const insertTime = () => {
  const data: WaitingTimeConfig = {
    id: utils.uuid(),
    beforeName: '',
    transactionName: '',
    target: 'WAITING_TIME',
    name: t('jdbcPlugin.UIConfigJdbc.waitingTime') + '-' + waitingTimeNum++,
    description: '',
    enabled: true,
    minWaitTimeInMs: '',
    maxWaitTimeInMs: ''
  };

  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

let rendezvousNum = 0;
const insertRendezvous = () => {
  const data: RendezvousConfig = {
    id: utils.uuid(),
    target: 'RENDEZVOUS',
    name: t('jdbcPlugin.UIConfigJdbc.rendezvous') + '-' + rendezvousNum++,
    description: '',
    enabled: true,
    beforeName: '',
    transactionName: '',
    timeoutInMs: '',
    threads: ''
  };

  if (typeof dragRef.value?.add === 'function') {
    dragRef.value.add(data);
  }

  scrollToBottom();
};

const deleteHandler = (data:{
  id: string;
  target: TargetKey;
  children?: {
    id: string;
    target: TargetKey;
  }[];
}[]) => {
  if (!data.length) {
    isTransEnd.value = false;
    return;
  }

  let transNum = 0;
  let hasUncloseTrans = false;
  for (let i = 0, len = data.length; i < len; i++) {
    const { target, children } = data[i];
    if (target === 'TRANS_START') {
      transNum++;
      if (!children?.length) {
        hasUncloseTrans = true;
        break;
      } else {
        const hasTransEnd = children.find(item => item.target === 'TRANS_END');
        if (!hasTransEnd) {
          hasUncloseTrans = true;
          break;
        }
      }
    }
  }

  if (transNum) {
    isTransEnd.value = hasUncloseTrans;
  } else {
    isTransEnd.value = false;
  }
};

const getData = () => {
  if (typeof dragRef.value?.getData === 'function') {
    return dragRef.value.getData();
  }

  return [];
};

defineExpose({
  isValid: (): boolean => {
    if (typeof dragRef.value?.isValid === 'function') {
      return dragRef.value.isValid();
    }

    return true;
  },
  getData
});

</script>

<template>
  <div class="py-5 h-full text-3 leading-5">
    <div class="flex items-center flex-nowrap whitespace-nowrap space-x-2 px-5">
      <Button
        type="default"
        size="small"
        @click="insertJDBC">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" />
          <span>{{ t('jdbcPlugin.UIConfigJdbc.insertJDBC') }}</span>
        </div>
      </Button>
      <Button
        v-if="!isTransEnd"
        type="default"
        size="small"
        @click="insertTransStart">
        <div class="flex items-center">
          <Icon icon="icon-shiwu" class="mr-1" />
          <span>{{ t('jdbcPlugin.UIConfigJdbc.insertTransStart') }}</span>
        </div>
      </Button>
      <Button
        v-else
        type="default"
        size="small"
        @click="insertTransEnd">
        <div class="flex items-center">
          <Icon icon="icon-shiwu" class="mr-1" />
          <span>{{ t('jdbcPlugin.UIConfigJdbc.insertTransEnd') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="insertTime">
        <div class="flex items-center">
          <Icon icon="icon-dengdaishijian" class="mr-1" />
          <span>{{ t('jdbcPlugin.UIConfigJdbc.insertWaitingTime') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="insertRendezvous">
        <div class="flex items-center">
          <Icon icon="icon-jihedian1" class="mr-1" />
          <span>{{ t('jdbcPlugin.UIConfigJdbc.insertRendezvous') }}</span>
        </div>
      </Button>
    </div>

    <Draggable
      :id="domId"
      ref="dragRef"
      :value="props.value"
      :loaded="props.loaded"
      style="height: calc(100% - 40px);"
      class="my-4 pl-5 pr-4 space-y-3 overflow-y-auto overflow-x-hidden scroll-smooth"
      @delete="deleteHandler"
      @renderChange="renderChange" />
  </div>
</template>
