<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import { SchemaType, ParameterIn } from '@xcan-angus/infra';
import { API_EXTENSION_KEYS } from '@/types/openapi-types';
import { HTTP_HEADERS } from '@/utils/constant';

import ApiCookie from '@/components/ApiCookie/index.vue';
import ApiParameter from '@/components/ApiParameter/index.vue';
import ApiHeader from '@/components/ApiHeader/index.vue';

interface Props {
    dataSource: {[key: string]: any}[]
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([])
});

const parameters = ref<{[key: string]: any}>([]);

const header = ref<{[key: string]: any}>([]);

const cookie = ref<{[key: string]: any}>([]);

const contentType = ref();

const changeParameters = (data) => {
  parameters.value = data;
};

const changeHeader = (data) => {
  header.value = data;
};

const changeCookie = (data) => {
  cookie.value = data;
};

const getData = () => {
  const contentTypeParams = [];
  if (contentType.value) {
    contentTypeParams.push(
      {
        name: HTTP_HEADERS.CONTENT_TYPE,
        in: ParameterIn.header,
        [API_EXTENSION_KEYS.valueKey]: contentType.value,
        schema: { type: SchemaType }
      }
    );
  }
  return {
    parameters: [
      ...parameters.value,
      ...header.value,
      ...cookie.value,
      ...contentTypeParams
    ]
  };
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    parameters.value = newValue.filter(i => i.in === ParameterIn.query || i.in === ParameterIn.path);
    header.value = newValue.filter(i => i.in === ParameterIn.header);
    cookie.value = newValue.filter(i => i.in === ParameterIn.cookie);
    const contentTyIdx = header.value.findIndex(i => i.name === HTTP_HEADERS.CONTENT_TYPE);
    if (contentTyIdx > -1) {
      contentType.value = header.value[contentTyIdx][API_EXTENSION_KEYS.valueKey];
      header.value.splice(contentTyIdx, 1);
    }
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  getData
});
</script>
<template>
  <div class="space-y-4">
    <div>
      <div class="font-medium text-4 border-b pb-1 mb-2">
        <Icon icon="icon-dangqianweizhi" class="text-5" /> Parameters
      </div>
      <ApiParameter
        :value="parameters"
        @change="changeParameters" />
    </div>
    <div>
      <div class="font-medium text-4 border-b pb-1 mb-2">
        <Icon icon="icon-dangqianweizhi" class="text-5" /> Header
      </div>
      <ApiHeader
        :value="header"
        :contentType="contentType"
        @change="changeHeader" />
    </div>
    <div>
      <div class="font-medium text-4 border-b pb-1 mb-2">
        <Icon icon="icon-dangqianweizhi" class="text-5" /> Cookie
      </div>
      <ApiCookie
        :value="cookie"
        @change="changeCookie" />
    </div>
  </div>
</template>
