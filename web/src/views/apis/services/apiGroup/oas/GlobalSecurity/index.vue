<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { Tag, TypographyParagraph } from 'ant-design-vue';

const ApiKey = defineAsyncComponent(() => import('./ApiKey.vue'));
const BasicAuth = defineAsyncComponent(() => import('./BasicAuth.vue'));
const BearerAuth = defineAsyncComponent(() => import('./BearerAuth.vue'));
const OpenID = defineAsyncComponent(() => import('./OpenID.vue'));
const Oauth2 = defineAsyncComponent(() => import('./Oauth2.vue'));

interface Props {
  scopes: { _id: string; _name: string, _value: string[] };
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
  scopes: undefined,
  value: undefined
});

const type = computed(() => {
  return props.value?.type;
});

const typeName = computed(() => {
  return props.value?.typeName;
});

const scheme = computed(() => {
  return props.value?.scheme;
});

const description = computed(() => {
  return props.value?.description;
});
</script>
<template>
  <div>
    <Tag class="flex-shrink-0" color="rgba(0, 213, 158, 1)">{{ typeName }}</Tag>
    <TypographyParagraph
      v-if="description"
      class="text-theme-sub-content mt-1.5 break-all"
      :content="description"
      :ellipsis="{ rows: 3, expandable: true, symbol: '更多' }" />
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
      :scopes="props.scopes"
      class="mt-3" />
  </div>
</template>

<style scoped>
.ant-tag {
  margin-right: 0;
  line-height: 18px;
}
</style>
