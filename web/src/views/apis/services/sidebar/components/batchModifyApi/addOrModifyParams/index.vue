<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
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
    <TabPane key="query" :tab="t('protocol.requestParameter')">
      <RequestParams
        :value="queryData"
        @change="changeQueryData" />
    </TabPane>
    <TabPane key="header" :tab="t('protocol.requestHeader')">
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
