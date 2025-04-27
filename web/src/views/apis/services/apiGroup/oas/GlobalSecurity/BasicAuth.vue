<script setup lang="ts">
import { Tag } from 'ant-design-vue';
</script>
<template>
  <div>
    <div class="text-theme-sub-content">
      Basic authentication is a simple authentication scheme built into the HTTP
      protocol. The client sends HTTP requests with the Authorization header that contains the word Basic word followed
      by a space and a base64-encoded string <Tag>username:password</Tag>. For example, to authorize as demo / p@55w0rd
      the client would send
    </div>
    <pre
      style="background-color: rgb(251, 251, 251);"
      class="border border-solid rounded border-theme-text-box max-h-70 overflow-y-auto py-2.5 px-3.5 mt-2.5 flex items-start whitespace-pre-wrap break-all">
        <code>Authorization: Basic ZGVtbzpwQDU1dzByZA==</code>
      </pre>
  </div>
</template>
<style scoped>
.ant-tag {
  margin-right: 0;
  line-height: 18px;
}
</style>
