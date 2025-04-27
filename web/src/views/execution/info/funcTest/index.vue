<script setup lang="ts">
import { defineAsyncComponent, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { exec } from '@/api/alctrl';

interface Props {
  loading:boolean;
  execInfo:{
    id:string;
    reportInterval:string;
    status:{
      message:string;
      value:'PENDING'|'RUNNING'|'COMPLETED';
    };
    plugin:'Http'|'Jdbc';
  };
  exception?: { codeName: string; messageName: string; code: string; message: string;};
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  execInfo: undefined,
  exception: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'loaded', data: {[key:string]:any}): void;
  (e: 'update:loading', value:boolean): void;
}>();

const Http = defineAsyncComponent(() => import('./http.vue'));
const Jdbc = defineAsyncComponent(() => import('./jdbc.vue'));
const Mail = defineAsyncComponent(() => import('./mail.vue'));
const Smtp = defineAsyncComponent(() => import('./smtp.vue'));
const Tcp = defineAsyncComponent(() => import('./tcp.vue'));
const Ftp = defineAsyncComponent(() => import('./ftp.vue'));
const Ldap = defineAsyncComponent(() => import('./ldap.vue'));
const WebSocket = defineAsyncComponent(() => import('./websocket.vue'));

let timer:NodeJS.Timeout | null = null;
let delayInSeconds = 0;
const execContent = ref<{[key:string]:any}[]>([]);

const loadData = async () => {
  const [error, { data }] = await exec.getInfo(props.execInfo.id);
  if (error) {
    emit('update:loading', false);
    return;
  }

  emit('loaded', data);
  await loadExecContent();
  if (['PENDING', 'RUNNING'].includes(data.status?.value)) {
    timedLoadData();
  }
};

const loadExecContent = async () => {
  let total = 0;
  const pageSize = 50;
  const tempList: any[] = [];
  const load = async (pageNo = 1) => {
    const [error, res] = await exec.loadExtensionContent(props.execInfo.id, { pageNo, pageSize, extField: 'SampleResultContent' });
    if (error) {
      emit('update:loading', false);
      return;
    }

    total = +res.data?.total;
    if (res.data?.list?.length) {
      tempList.push(...res.data.list);
    }
  };

  await load();
  if (total <= pageSize) {
    execContent.value = tempList;
    return;
  }

  const totalSize = Math.ceil(total / pageSize);
  for (let i = 2; i <= totalSize; i++) {
    await load(i);
  }

  execContent.value = tempList;
};

const timedLoadData = async () => {
  if (timer) {
    clearTimeout(timer);
  }

  timer = setTimeout(async () => {
    loadData();
  }, delayInSeconds);
};

onMounted(() => {
  watch(() => props.execInfo, async (newValue) => {
    const status = newValue?.status?.value;
    if (!status) {
      return;
    }

    const reportInterval = +props.execInfo?.reportInterval?.replace(/[^\d]/g, '');
    if (reportInterval < 3) {
      delayInSeconds = 3 * 1000;
    } else {
      delayInSeconds = reportInterval * 1000;
    }

    if (['PENDING', 'RUNNING'].includes(status)) {
      await loadData();
    } else {
      if (timer) {
        clearTimeout(timer);
      }

      await loadExecContent();
    }
    emit('update:loading', false);
  }, { immediate: true });
});

onBeforeUnmount(() => {
  if (timer) {
    clearTimeout(timer);
  }
});
</script>
<template>
  <template v-if="props.execInfo?.plugin==='Http'">
    <Http
      :execInfo="props.execInfo"
      :execContent="execContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin==='Jdbc'">
    <Jdbc
      :execInfo="props.execInfo"
      :execContent="execContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Mail'">
    <Mail
      :execInfo="props.execInfo"
      :execContent="execContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Smtp'">
    <Smtp
      :execInfo="props.execInfo"
      :execContent="execContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Tcp'">
    <Tcp
      :execInfo="props.execInfo"
      :execContent="execContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Ftp'">
    <Ftp
      :execInfo="props.execInfo"
      :execContent="execContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Ldap'">
    <Ldap
      :execInfo="props.execInfo"
      :execContent="execContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'WebSocket'">
    <WebSocket
      :execInfo="props.execInfo"
      :execContent="execContent"
      :exception="props.exception" />
  </template>
</template>
