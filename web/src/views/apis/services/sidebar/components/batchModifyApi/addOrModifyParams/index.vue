<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
// import { } from '@xcan-angus/vue-ui';
import { TabPane, Tabs } from 'ant-design-vue';

const RequestParams = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/requestParam/index.vue'));
const RequestHeader = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/requestHeader/index.vue'));
const RequestCookie = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/requestCookie/index.vue'));

const queryData = ref([]);
const headerData = ref([]);
const cookieData = ref([]);

const changeQueryData = (data) => {
  queryData.value = data.filter(i => !!i.name);
};

const changeHeaderData = (data) => {
  headerData.value = data;
};

const changeCookirData = (data) => {
  cookieData.value = data;
};

defineExpose({
  getData: () => {
    return [...queryData.value, ...headerData.value, ...cookieData.value];
  }
});

</script>
<template>
  <Tabs size="small">
    <TabPane key="query" tab="请求参数">
      <RequestParams
        :value="queryData"
        @change="changeQueryData" />
    </TabPane>
    <TabPane key="header" tab="请求头">
      <RequestHeader
        :value="headerData"
        @change="changeHeaderData" />
    </TabPane>
    <TabPane key="cookie" tab="Cookie">
      <RequestCookie
        :value="cookieData"
        @change="changeCookirData" />
    </TabPane>
  </Tabs>
</template>
