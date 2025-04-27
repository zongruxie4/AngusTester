<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';

const ApiKey = defineAsyncComponent(() => import('../GlobalSecurity/ApiKey.vue'));
const BasicAuth = defineAsyncComponent(() => import('../GlobalSecurity/BasicAuth.vue'));
const BearerAuth = defineAsyncComponent(() => import('../GlobalSecurity/BearerAuth.vue'));
const OpenID = defineAsyncComponent(() => import('../GlobalSecurity/OpenID.vue'));
const Oauth2 = defineAsyncComponent(() => import('./Oauth2.vue'));

interface Props {
  value: {
    type: 'http' | 'apiKey' | 'oauth2' | 'openIdConnect';
    scheme: 'basic' | 'bearer';
    typeName: string;
    in: string;
    name: string;
    description: string;
    flows: {
      implicit: {
        authorizationUrl: string;
        tokenUrl: string;
        refreshUrl: string;
        scopes: { [key: string]: string; };
      };
      authorizationCode: {
        authorizationUrl: string;
        tokenUrl: string;
        refreshUrl: string;
        scopes: { [key: string]: string; };
      };
      password: {
        authorizationUrl: string;
        tokenUrl: string;
        refreshUrl: string;
        scopes: { [key: string]: string; };
      };
      clientCredentials: {
        authorizationUrl: string;
        tokenUrl: string;
        refreshUrl: string;
        scopes: { [key: string]: string; };
      }
    }
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const type = computed(() => {
  return props.value?.type;
});

const scheme = computed(() => {
  return props.value?.scheme;
});
</script>
<template>
  <div>
    <ApiKey
      v-if="type === 'apiKey'"
      :value="props.value"
      class="mt-3" />
    <template v-if="type === 'http'">
      <BasicAuth
        v-if="scheme === 'basic'"
        :value="props.value"
        class="mt-3" />
      <BearerAuth
        v-if="scheme === 'bearer'"
        :value="props.value"
        class="mt-3" />
    </template>
    <OpenID
      v-if="type === 'openIdConnect'"
      :value="props.value"
      class="mt-3" />
    <Oauth2
      v-if="type === 'oauth2'"
      :value="props.value"
      class="mt-3" />
  </div>
</template>
