<script lang="ts" setup>
import { useDebugResult } from './composables/useDebugResult';

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

// Use debug result composable
const {
  Plugin
} = useDebugResult(props);

</script>
<template>
  <template v-if="Plugin">
    <Plugin :value="props.value" />
  </template>
</template>
