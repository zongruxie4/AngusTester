/// <reference types="vite/client" />

declare module 'angus-design/authUtils';
declare module 'angus-design/utils';
declare module 'angus-design/assertUtils';
declare module '*.vue' {
  import { DefineComponent } from 'vue';
  // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
  const component: DefineComponent<{}, {}, any>;
  export default component;
}
