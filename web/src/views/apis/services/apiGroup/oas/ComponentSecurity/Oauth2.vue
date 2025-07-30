<script setup lang="ts">
import { computed } from 'vue';
import { Tag } from 'ant-design-vue';
import { Colon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

type FlowsKey = 'implicit' | 'authorizationCode' | 'password' | 'clientCredentials';
type FlowsItem = {
  authorizationUrl: string;
  tokenUrl: string;
  refreshUrl: string;
  scopes: { [key: string]: string; };
}

interface Props {
  value: {
    flows: {
      [key in FlowsKey]: FlowsItem;
    }
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const keys = computed<FlowsKey[]>(() => {
  if (!props.value?.flows) {
    return [];
  }

  return Object.keys(props.value.flows) as FlowsKey[];
});

const dataMap = computed<{ [key in FlowsKey]: FlowsItem }>(() => {
  return props.value?.flows || {};
});

const scopesMap = computed<{[key in FlowsKey]:{[x:string]:string;}[]}>(() => {
  if (!props.value?.flows) {
    return {} as {[key in FlowsKey]:{[x:string]:string;}[]};
  }

  const map = Object.entries(props.value.flows).reduce((prev, [key, { scopes }]) => {
    const _scopes:{id:string;name:string;value:string;}[] = [];
    if (scopes) {
      for (const key in scopes) {
        _scopes.push({
          id: utils.uuid(),
          name: key,
          value: scopes[key]
        });
      }
    }

    prev[key] = _scopes;
    return prev;
  }, {} as {[key in FlowsKey]:{[x:string]:string;}[]});

  return map;
});

const keyMap = {
  implicit: 'Implicit OAuth Flow',
  password: 'Password OAuth Flow',
  clientCredentials: 'Client Credentials OAuth Flow',
  authorizationCode: 'Authorization Code OAuth Flow'
};
</script>
<template>
  <div class="space-y-5">
    <div v-for="item in keys" :key="item">
      <div class="text-theme-title text-3.5 flex items-center mb-2">
        <div class="w-1.5 h-1.5 rounded border-2 border-solid border-gray-400"></div>
        <div class="ml-1.5">{{ keyMap[item] || item }}</div>
      </div>
      <div class="space-y-1.5 pl-3 mb-2">
        <div v-if="dataMap[item].authorizationUrl" class="flex items-center">
          <div class="flex-shrink-0 flex items-center mr-2">
            <span class="text-theme-title">Authorize URL</span>
            <Colon />
          </div>
          <a
            target="_blank"
            style="color:#1890ff"
            class="truncate"
            :title="dataMap[item].authorizationUrl"
            :href="dataMap[item].authorizationUrl">{{ dataMap[item].authorizationUrl }}</a>
        </div>
        <div v-if="dataMap[item].tokenUrl" class="flex items-center">
          <div class="flex-shrink-0 flex items-center mr-2">
            <span class="text-theme-title">Token URL</span>
            <Colon />
          </div>
          <a
            target="_blank"
            style="color:#1890ff"
            class="truncate"
            :title="dataMap[item].tokenUrl"
            :href="dataMap[item].tokenUrl">{{ dataMap[item].tokenUrl }}</a>
        </div>
        <div v-if="dataMap[item].refreshUrl" class="flex items-center">
          <div class="flex-shrink-0 flex items-center mr-2">
            <span class="text-theme-title">Refresh URL</span>
            <Colon />
          </div>
          <a
            target="_blank"
            style="color:#1890ff"
            class="truncate"
            :title="dataMap[item].refreshUrl"
            :href="dataMap[item].refreshUrl">{{ dataMap[item].refreshUrl }}</a>
        </div>
        <div>
          <div class="flex-shrink-0 flex items-center mr-2 mb-1.5">
            <span class="text-theme-title">Scopes</span>
            <Colon />
          </div>
          <div class="pl-5 space-y-2">
            <div
              v-for="_scope in scopesMap[item]"
              :key="_scope.id"
              class="flex items-center space-x-1.5">
              <div class="flex-shrink-0 w-1 h-1 bg-black-log-bg rounded"></div>
              <Tag class="flex-shrink-0 max-w-25 truncate">{{ _scope.name }}</Tag>
              <div class="truncate" :title="_scope.value">{{ _scope.value }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.ant-tag {
  margin-right: 0;
  line-height: 18px;
}
</style>
