<script setup lang="ts">
import { computed } from 'vue';
import { utils } from '@xcan-angus/tools';

import GlobalSecurity from './GlobalSecurity/index.vue';
import { PathItemInfo } from './PropsType';

interface Props {
  value: PathItemInfo['security'];
  navs: {
    _id: string;
    _name: string;
    _key: string;
  }[];
  securitySchemes: {
    [key: string]: {
      type: 'http' | 'apiKey' | 'openIdConnect' | 'oauth2';
      typeName: string;
      scheme?: 'basic' | 'bearer';
      in?: 'header' | 'cookie';
      openIdConnectUrl?: string;
      flows?: {
        [key: string]: any;
      }
    }
  }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  navs: () => [],
  securitySchemes: undefined
});

const securitys = computed(() => {
  if (!props.value) {
    return [];
  }

  return props.value.map(item => {
    const entries = Object.entries(item)[0];
    return {
      _name: entries[0],
      _value: entries[1] as string[],
      _id: utils.uuid('_')
    };
  });
});

const schemes = computed(() => {
  if (!props.securitySchemes) {
    return {};
  }

  return props.securitySchemes;
});

const domId = computed(() => {
  if (!props.navs?.length) {
    return '';
  }

  return props.navs.find(item => item._key === 'security')?._id;
});
</script>
<template>
  <div class="mx-3.25">
    <div :id="domId" class="text-3.5 text-theme-title mb-2">安全需求</div>
    <div
      v-for="item in securitys"
      :key="item._id"
      class="mb-7 last:mb-0 space-y-1.5">
      <div :id="item._id" class="text-3.5 text-theme-title whitespace-pre-wrap break-all">{{ item._name }}</div>
      <GlobalSecurity :value="schemes[item._name]" :scopes="item._value" />
    </div>
  </div>
</template>
