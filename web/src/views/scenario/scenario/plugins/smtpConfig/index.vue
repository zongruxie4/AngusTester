<script lang="ts" setup>
import { inject } from 'vue';
import '@angus/exec/style.css';

// eslint-disable-next-line import/no-absolute-path
import Plugin from '/public/plugins/Smtp/index.mjs';
// eslint-disable-next-line import/no-absolute-path
import '/public/plugins/Smtp/style.css';

interface Props {
    tabKey:string;
    sceneInfo:{[key:string]:any};
    appInfo:{[key:string]:any};
    userInfo:{[key:string]:any};
    projectId:string;
}

const props = withDefaults(defineProps<Props>(), {
  tabKey: undefined,
  sceneInfo: undefined,
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
    :sceneInfo="props.sceneInfo"
    :userInfo="props.userInfo"
    :appInfo="props.appInfo"
    :updateRefreshNotify="updateRefreshNotify"
    :updateTabPane="updateTabPane"
    :getTabPane="getTabPane"
    :replaceTabPane="replaceTabPane" />
</template>
