<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';

const MockCsv = defineAsyncComponent(() => import('./MockCsv.vue'));
const MockJson = defineAsyncComponent(() => import('./MockJson.vue'));
const MockCustom = defineAsyncComponent(() => import('./MockCustom.vue'));
const MockExcel = defineAsyncComponent(() => import('./MockExcel.vue'));
const MockSql = defineAsyncComponent(() => import('./MockSql.vue'));
const MockTab = defineAsyncComponent(() => import('./MockTab.vue'));
const MockXml = defineAsyncComponent(() => import('./MockXml.vue'));

const format = ref<'CSV'|'JSON'|'CUSTOM'|'EXCEL'|'SQL'|'TAB'|'XML'>('CSV');
const params = ref<{ [key: string]: any }>();

const formatChange = (data) => {
  params.value = data;
  format.value = data.format;
};
</script>
<template>
  <template v-if="format === 'CSV'">
    <MockCsv :params="params" @formatChange="formatChange" />
  </template>
  <template v-if="format === 'JSON'">
    <MockJson :params="params" @formatChange="formatChange" />
  </template>
  <template v-if="format === 'CUSTOM'">
    <MockCustom :params="params" @formatChange="formatChange" />
  </template>
  <template v-if="format === 'EXCEL'">
    <MockExcel :params="params" @formatChange="formatChange" />
  </template>
  <template v-if="format === 'SQL'">
    <MockSql :params="params" @formatChange="formatChange" />
  </template>
  <template v-if="format === 'TAB'">
    <MockTab :params="params" @formatChange="formatChange" />
  </template>
  <template v-if="format === 'XML'">
    <MockXml :params="params" @formatChange="formatChange" />
  </template>
</template>
