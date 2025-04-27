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
      Plugin.value = defineAsyncComponent(() => import('/public/plugins/Ftp/index.mjs').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      import('/public/plugins/Ftp/style.css');
      return;
    }

    if (pluginType === 'Http') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('/public/plugins/Http/index.mjs').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      import('/public/plugins/Http/style.css');
      return;
    }

    if (pluginType === 'Jdbc') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('/public/plugins/Jdbc/index.mjs').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      import('/public/plugins/Jdbc/style.css');
      return;
    }

    if (pluginType === 'Ldap') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('/public/plugins/Ldap/index.mjs').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      import('/public/plugins/Ldap/style.css');
      return;
    }

    if (pluginType === 'Mail') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('/public/plugins/Mail/index.mjs').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      import('/public/plugins/Mail/style.css');
      return;
    }

    if (pluginType === 'Smtp') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('/public/plugins/Smtp/index.mjs').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      import('/public/plugins/Smtp/style.css');
      return;
    }

    if (pluginType === 'Tcp') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('/public/plugins/Tcp/index.mjs').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      import('/public/plugins/Tcp/style.css');
      return;
    }

    if (pluginType === 'WebSocket') {
      // eslint-disable-next-line import/no-absolute-path
      Plugin.value = defineAsyncComponent(() => import('/public/plugins/WebSocket/index.mjs').then(module => module.DebugResult));
      // eslint-disable-next-line import/no-absolute-path
      import('/public/plugins/WebSocket/style.css');
    }
  }, { immediate: true });
});
</script>
<template>
  <template v-if="Plugin">
    <Plugin :value="props.value" />
  </template>
</template>
