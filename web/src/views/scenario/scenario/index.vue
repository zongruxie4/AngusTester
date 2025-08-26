<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils, appContext } from '@xcan-angus/infra';

import { IPane } from './PropsType';

const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const Auth = defineAsyncComponent(() => import('@/views/scenario/scenario/auth/index.vue'));
const List = defineAsyncComponent(() => import('@/views/scenario/scenario/list/index.vue'));
const HttpConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/httpConfig/index.vue'));
const WebSocketConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/webSocketConfig/index.vue'));
const JdbcConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/jdbcConfig/index.vue'));
const FtpConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/ftpConfig/index.vue'));
const LdapConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/ldapConfig/index.vue'));
const MailConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/mailConfig/index.vue'));
const SmtpConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/smtpConfig/index.vue'));
const TcpConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/tcpConfig/index.vue'));
const Detail = defineAsyncComponent(() => import('@/views/scenario/scenario/detail/index.vue'));

const route = useRoute();
const router = useRouter();
const browserTabRef = ref();

const refreshNotify = ref<string>();

const updateRefreshNotify = () => {
  refreshNotify.value = utils.uuid();
};

const addTabPane = (data: IPane) => {
  browserTabRef.value.add(() => {
    return data;
  });
};

const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

const updateTabPane = (data: IPane) => {
  browserTabRef.value.update(data);
};

const replaceTabPane = (key: string, data: { key: string }) => {
  browserTabRef.value.replace(key, data);
};

const initialize = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((ids: string[]) => {
      if (!ids.includes('sceneList')) {
        return {
          _id: 'sceneList',
          value: 'sceneList',
          name: t('scenario.title'),
          closable: false // 是否允许关闭，true - 允许关闭，false - 禁止关闭
        };
      }
    });
  }

  hashChange(route.hash);
};

const hashChange = (hash:string) => {
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return;
  }

  const queryParameters = queryString.split('&').reduce((prev, cur) => {
    const [key, value] = cur.split('=');
    prev[key] = value;
    return prev;
  }, {} as { [key: string]: string });

  const { id, name, plugin, type } = queryParameters;
  if (id && name && plugin && type === 'detail') {
    browserTabRef.value.add(() => {
      return {
        _id: id + 'detail',
        name,
        value: 'detail',
        data: { scenarioId: id, name }
      };
    });
  } else if (id && name && plugin) {
    browserTabRef.value.add(() => {
      return {
        _id: id,
        name,
        value: plugin,
        sceneInfo: { id, name }
      };
    });
  }

  router.replace('/scenario#scenario');
};

const storageKeyChange = () => {
  initialize();
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#scenario')) {
      return;
    }

    hashChange(route.hash);
  }, {
    immediate: true
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }

  return `scene${props.projectId}`;
});

// 更新指定的tabPane
provide('updateRefreshNotify', updateRefreshNotify);

// 添加指定的tabPane
provide('addTabPane', addTabPane);

// 获取tabPane
provide('getTabPane', getTabPane);

// 删除指定的tabPane
provide('deleteTabPane', deleteTabPane);

// 更新指定的tabPane
provide('updateTabPane', updateTabPane);

// 替换指定tabPane
provide('replaceTabPane', replaceTabPane);
</script>
<template>
  <BrowserTab
    ref="browserTabRef"
    hideAdd
    class="h-full"
    :userId="props.userInfo.id"
    :storageKey="storageKey"
    @storageKeyChange="storageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'sceneList'">
        <List
          :notify="refreshNotify"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Http'">
        <HttpConfig
          :tabKey="record._id"
          :sceneInfo="record.sceneInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'WebSocket'">
        <WebSocketConfig
          :tabKey="record._id"
          :sceneInfo="record.sceneInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Jdbc'">
        <JdbcConfig
          :tabKey="record._id"
          :sceneInfo="record.sceneInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Ftp'">
        <FtpConfig
          :tabKey="record._id"
          :sceneInfo="record.sceneInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Ldap'">
        <LdapConfig
          :tabKey="record._id"
          :sceneInfo="record.sceneInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Mail'">
        <MailConfig
          :tabKey="record._id"
          :sceneInfo="record.sceneInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Smtp'">
        <SmtpConfig
          :tabKey="record._id"
          :sceneInfo="record.sceneInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Tcp'">
        <TcpConfig
          :tabKey="record._id"
          :sceneInfo="record.sceneInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'authorization'">
        <Auth :appId="appInfo?.id" :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'detail'">
        <Detail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
