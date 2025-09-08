<script lang="ts" setup>
import { inject } from 'vue';

// eslint-disable-next-line import/no-absolute-path
import Plugin from '@/plugins/ldap';

// eslint-disable-next-line import/no-absolute-path

interface Props {
    tabKey:string;
    scenarioInfo:{[key:string]:any};
    appInfo:{[key:string]:any};
    userInfo:{[key:string]:any};
    projectId:string;
}

const props = withDefaults(defineProps<Props>(), {
  tabKey: undefined,
  scenarioInfo: undefined,
  appInfo: undefined,
  userInfo: undefined,
  projectId: undefined
});

const updateRefreshNotify = inject('updateRefreshNotify');
const updateTabPane = inject('updateTabPane');
const getTabPane = inject('getTabPane');
const replaceTabPane = inject('replaceTabPane');
</script>
<template>
  <Plugin
    :tabKey="props.tabKey"
    :projectId="props.projectId"
    :scenarioInfo="props.scenarioInfo"
    :userInfo="props.userInfo"
    :appInfo="props.appInfo"
    :updateRefreshNotify="updateRefreshNotify"
    :updateTabPane="updateTabPane"
    :getTabPane="getTabPane"
    :replaceTabPane="replaceTabPane" />
</template>
