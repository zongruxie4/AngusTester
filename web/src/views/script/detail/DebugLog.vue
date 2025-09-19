<script lang="ts" setup>
import { NoData } from '@xcan-angus/vue-ui';
// eslint-disable-next-line import/no-absolute-path
import { DebugLog as JDBCDebugLog } from '@/plugins/test/jdbc/index';
// eslint-disable-next-line import/no-absolute-path
import { DebugLog as HttpDebugLog } from '@/plugins/test/http/index';

// eslint-disable-next-line import/no-absolute-path
import { DebugLog as SmtpDebugLog } from '@/plugins/test/smtp/index';

// eslint-disable-next-line import/no-absolute-path
import { DebugLog as MailDebugLog } from '@/plugins/test/mail/index';
import { useDebugLog } from './composables/useDebugLog';

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

// Use debug log composable
const {
  schedulingResult
} = useDebugLog(props);
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
