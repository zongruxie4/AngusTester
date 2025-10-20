import { Component, defineAsyncComponent } from 'vue';

import ApiAssertion from '@/components/apis/assertion/index.vue';
import ResponseAssert from '@/components/apis/response/ResponseAssert.vue';
import ApiAuthorization from '@/views/apis/services/protocol/http/Authorization.vue';

export const RequestParams: Component = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/RequestParameter.vue'));
export const RequestBody: Component = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/requestBody/index.vue'));
export const RequestHeader: Component = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/RequestHeader.vue'));
export const RequestCookie: Component = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/RequestCookie.vue'));
export const Authorization: Component = ApiAuthorization;
export const AssertForm: Component = ApiAssertion;

export const ApiRequest: Component = defineAsyncComponent(() => import('@/views/apis/services/components/Request.vue'));
export const ApiResponse: Component = defineAsyncComponent(() => import('@/views/apis/services/components/Response.vue'));
export const ApiTimeline: Component = defineAsyncComponent(() => import('@/views/apis/services/components/Timeline.vue'));
export const ApiCookie: Component = defineAsyncComponent(() => import('@/views/apis/services/components/Cookie.vue'));
export const ApiAssert: Component = ResponseAssert;
