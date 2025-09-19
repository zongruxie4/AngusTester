import { Component, defineAsyncComponent, ref, watch, onMounted } from 'vue';

/**
 * Debug result management composable
 * Handles dynamic loading of plugin-specific debug results
 */
export function useDebugResult (props: any) {
  const Plugin = ref<Component>();

  /**
   * Load plugin-specific debug result component
   */
  onMounted(() => {
    watch(() => props.pluginType, (pluginType) => {
      if (pluginType === 'Ftp') {
        Plugin.value = defineAsyncComponent(() => import('@/plugins/test/ftp/index').then(module => module.DebugResult));
        return;
      }

      if (pluginType === 'Http') {
        Plugin.value = defineAsyncComponent(() => import('@/plugins/test/http/index').then(module => module.DebugResult));
        return;
      }

      if (pluginType === 'Jdbc') {
        Plugin.value = defineAsyncComponent(() => import('@/plugins/test/jdbc/index').then(module => module.DebugResult));
        return;
      }

      if (pluginType === 'Ldap') {
        Plugin.value = defineAsyncComponent(() => import('@/plugins/test/ldap/index').then(module => module.DebugResult));
        return;
      }

      if (pluginType === 'Mail') {
        Plugin.value = defineAsyncComponent(() => import('@/plugins/test/mail/index').then(module => module.DebugResult));
        return;
      }

      if (pluginType === 'Smtp') {
        Plugin.value = defineAsyncComponent(() => import('@/plugins/test/smtp/index').then(module => module.DebugResult));
        return;
      }

      if (pluginType === 'Tcp') {
        Plugin.value = defineAsyncComponent(() => import('@/plugins/test/tcp/index').then(module => module.DebugResult));
        return;
      }

      if (pluginType === 'WebSocket') {
        Plugin.value = defineAsyncComponent(() => import('@/plugins/test/websocket/index').then(module => module.DebugResult));
      }
    }, { immediate: true });
  });

  return {
    Plugin
  };
}
