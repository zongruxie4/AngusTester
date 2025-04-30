<script lang="ts" setup>
import { Component, defineAsyncComponent, onMounted, ref, watch } from 'vue';

interface Props {
  value: {
    sampleContents: {[key:string]:any}[];
    task: {
      arguments: {
        ignoreAssertions: boolean;
      };
      pipelines: {
        name: string;
      }[];
    };
    schedulingResult: {
      console: string[];
      success: boolean;
      exitCode: string;
  }
};
  pluginType: 'Ftp'|'Http'|'Jdbc'|'Ldap'|'Mail'|'Smtp'|'Tcp'|'WebSocket';
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  pluginType: undefined
});

const Plugin = ref<Component>();

onMounted(() => {
  watch(() => props.pluginType, (pluginType) => {
    if (pluginType === 'Ftp') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('@/plugins/ftp/index').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      return;
    }

    if (pluginType === 'Http') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('@/plugins/http/index').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      return;
    }

    if (pluginType === 'Jdbc') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('@/plugins/jdbc/index').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      return;
    }

    if (pluginType === 'Ldap') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('@/plugins/ldap/index').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      return;
    }

    if (pluginType === 'Mail') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('@/plugins/mail/index').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      return;
    }

    if (pluginType === 'Smtp') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('@/plugins/smtp/index').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      return;
    }

    if (pluginType === 'Tcp') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('@/plugins/tcp/index').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      return;
    }

    if (pluginType === 'WebSocket') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('@/plugins/websocket/index').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
    }
  }, { immediate: true });
});
</script>
<template>
  <template v-if="Plugin">
    <Plugin :value="props.value" />
  </template>
</template>
