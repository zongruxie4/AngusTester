import { RouteRecordRaw } from 'vue-router';

import Layout from '@/layout/default.vue';

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
        path: '/task',
        component: () => import('@/views/task/index.vue'),
        meta: {
          caches: ['/task/:id']
        }
      },
      {
        path: '/function',
        component: () => import('@/views/function/index.vue')
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
        component: () => import('@/views/script/homepage/index.vue')
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
              name: '文件数据',
              path: '/data',
              query: []
            },
            { name: '生成数据' }
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
        component: () => import('@/views/execution/add/index.vue'),
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
        component: () => import('@/views/execution/add/index.vue'),
        meta: {
          breadcrumb: [
            {
              name: 'execute.execute',
              path: '/execution'
            },
            { name: '体验执行' }
          ],
          className: 'my-tabs'
        }
      },
      {
        path: '/execution/edit/:id',
        component: () => import('@/views/execution/add/index.vue'),
        meta: {
          breadcrumb: [
            {
              name: 'execute.execute',
              path: '/execution'
            },
            { name: '修改执行配置' }
          ],
          className: 'my-tabs'
        }
      },
      {
        path: '/execution/info/:id',
        component: () => import('@/views/execution/info/index.vue')
      },
      {
        path: '/mockservice',
        component: () => import('@/views/mock/index.vue')
      },
      {
        path: '/mockservice/add',
        component: () => import('@/views/mock/add/index.vue'),
        meta: {
          breadcrumb: [
            {
              name: 'Mock',
              path: '/mockservice'
            },
            { name: '添加' }
          ]
        }
      },
      {
        path: '/mockservice',
        component: () => import('@/views/mock/detail/index.vue'),
        children: [
          {
            path: '/mockservice/:id/apis',
            component: () => import('@/views/mock/detail/mockApis/index.vue')
          },
          {
            path: '/mockservice/:id/request',
            component: () => import('@/views/mock/detail/requestRecord/index.vue')
          },
          {
            path: '/mockservice/:id/log',
            component: () => import('@/views/mock/detail/log/index.vue')
          },
          {
            path: '/mockservice/:id/activity',
            component: () => import('@/views/mock/detail/activity/index.vue')
          },
          {
            path: '/mockservice/:id/monitor',
            component: () => import('@/views/mock/detail/control.vue')
          },
          {
            path: '/mockservice/:id/setting',
            component: () => import('@/views/mock/detail/mockSet.vue')
          }
        ]
      },
      {
        path: '/node/detail/:id',
        component: () => import('@/views/config/node/detail/index.vue'),
        meta: {
          breadcrumb: [
            {
              name: '节点',
              path: '/config#node'
            },
            { name: '详情' }
          ]
        }
      },
      {
        path: '/report',
        component: () => import('@/views/report/homepage/index.vue')
      }
    ]
  },
  {
    path: '/report/:id/preview',
    component: () => import('@/views/report/preview/index.vue')
  },
  {
    path: '/apis/share',
    component: () => import('@/views/apis/apisShare/index.vue')
  }
];

export default routes;
