<script lang="ts" setup>
import { computed } from 'vue';
import { NoData } from '@xcan-angus/vue-ui';
// eslint-disable-next-line import/no-absolute-path
import { DebugLog as JDBCDebugLog } from '/public/plugins/Jdbc/index.mjs';
// eslint-disable-next-line import/no-absolute-path
import { DebugLog as HttpDebugLog } from '/public/plugins/Http/index.mjs';

// eslint-disable-next-line import/no-absolute-path
import { DebugLog as SmtpDebugLog } from '/public/plugins/Smtp/index.mjs';

// eslint-disable-next-line import/no-absolute-path
import { DebugLog as MailDebugLog } from '/public/plugins/Mail/index.mjs';

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
  pluginType:'Jdbc'|'Http'|'Smtp'|'Mail'|'Tcp'|'Ftp'|'Websocket'
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  pluginType: undefined
});

const schedulingResult = computed(() => {
  return props.value?.schedulingResult;
});
</script>
<template>
  <template v-if="props.pluginType==='Http'">
    <HttpDebugLog :value="schedulingResult" />
  </template>
  <template v-else-if="props.pluginType==='Jdbc'">
    <JDBCDebugLog :value="schedulingResult" />
  </template>
  <template v-else-if="props.pluginType==='Smtp'">
    <SmtpDebugLog :value="schedulingResult" />
  </template>
  <template v-else-if="props.pluginType==='Mail'">
    <MailDebugLog :value="schedulingResult" />
  </template>
  <template v-else>
    <NoData size="small" />
  </template>
</template>
