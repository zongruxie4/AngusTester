import { RouteRecordRaw } from 'vue-router';
import Layout from '@/layout/Default.vue';
import { i18n } from '@xcan-angus/infra';
import { ApiMenuKey } from '@/views/apis/types';

const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string): string => value);

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'angustester',
    component: Layout,
    children: [
      {
        path: '/project',
        component: () => import('@/views/project/index.vue')
      },
      {
        path: 'config',
        component: () => import('@/views/config/index.vue')
      },
      {
        path: '/kanban',
        component: () => import('@/views/kanban/index.vue')
      },
      {
        path: '/issue',
        component: () => import('@/views/issue/index.vue'),
        meta: {
          caches: ['/issue/:id']
        }
      },
      {
        path: '/test',
        component: () => import('@/views/test/index.vue')
      },
      {
        path: '/apis',
        component: () => import('@/views/apis/index.vue')
      },
      {
        path: '/scenario',
        component: () => import('@/views/scenario/index.vue')
      },
      {
        path: '/script',
        component: () => import('@/views/script/home/index.vue')
      },
      {
        path: '/script/edit',
        component: () => import('@/views/script/detail/index.vue')
      },
      {
        path: '/script/edit/:id',
        component: () => import('@/views/script/detail/index.vue')
      },
      {
        path: '/script/detail/:id',
        component: () => import('@/views/script/detail/index.vue')
      },
      {
        path: '/data',
        component: () => import('@/views/data/index.vue')
      },
      {
        path: '/data/file/:id',
        component: () => import('@/views/data/file/File.vue')
      },
      {
        path: '/data/generate',
        component: () => import('@/views/data/file/generate/index.vue'),
        meta: {
          breadcrumb: [
            {
              name: t('routes.fileData'),
              path: '/data',
              query: []
            },
            { name: t('routes.generateData') }
          ],
          flexCol: true
        }
      },
      {
        path: '/execution',
        component: () => import('@/views/execution/index.vue')
      },
      {
        path: '/execution/add',
        component: () => import('@/views/execution/Add.vue'),
        meta: {
          breadcrumb: [
            {
              name: 'execute.execute',
              path: '/execution'
            },
            { name: 'execute.createExecution' }
          ],
          className: 'my-tabs'
        }
      },
      {
        path: '/execution/experience',
        component: () => import('@/views/execution/Add.vue'),
        meta: {
          breadcrumb: [
            {
              name: 'execute.execute',
              path: '/execution'
            },
            { name: t('routes.experienceExecution') }
          ],
          className: 'my-tabs'
        }
      },
      {
        path: '/execution/edit/:id',
        component: () => import('@/views/execution/Add.vue'),
        meta: {
          breadcrumb: [
            {
              name: 'execute.execute',
              path: '/execution'
            },
            { name: t('routes.modifyExecutionConfig') }
          ],
          className: 'my-tabs'
        }
      },
      {
        path: '/execution/info/:id',
        component: () => import('@/views/execution/detail/index.vue')
      },
      {
        path: `/apis#${ApiMenuKey.MOCK}`,
        component: () => import('@/views/apis/mock/index.vue')
      },
      {
        path: '/mockservice/add',
        component: () => import('@/views/apis/mock/add/index.vue'),
        meta: {
          breadcrumb: [
            {
              name: t('routes.mock'),
              path: `/apis#${ApiMenuKey.MOCK}`
            },
            { name: t('common.add') }
          ]
        }
      },
      {
        path: `/apis#${ApiMenuKey.MOCK}`,
        component: () => import('@/views/apis/mock/detail/index.vue'),
        children: [
          {
            path: '/mockservice/:id/apis',
            component: () => import('@/views/apis/mock/detail/apis/index.vue')
          },
          {
            path: '/mockservice/:id/request',
            component: () => import('@/views/apis/mock/detail/RequestRecord.vue')
          },
          {
            path: '/mockservice/:id/log',
            component: () => import('@/views/apis/mock/detail/Log.vue')
          },
          {
            path: '/mockservice/:id/activity',
            component: () => import('@/views/apis/mock/detail/Activity.vue')
          },
          {
            path: '/mockservice/:id/monitor',
            component: () => import('@/views/apis/mock/detail/Monitor.vue')
          },
          {
            path: '/mockservice/:id/setting',
            component: () => import('@/views/apis/mock/detail/Setting.vue')
          }
        ]
      },
      {
        path: '/node/detail/:id',
        component: () => import('@/views/config/node/detail/index.vue'),
        meta: {
          breadcrumb: [
            {
              name: t('routes.node'),
              path: '/config#node'
            },
            { name: t('common.detail') }
          ]
        }
      },
      {
        path: '/report',
        component: () => import('@/views/report/home/index.vue')
      }
    ]
  },
  {
    path: '/report/:id/preview',
    component: () => import('@/views/report/preview/index.vue')
  },
  {
    path: '/apis/share',
    component: () => import('@/views/apis/share/shared/index.vue')
  }
];

export default routes;
