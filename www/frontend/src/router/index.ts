import { createRouter, createWebHistory } from 'vue-router'
import DynamicHome from '../views/DynamicHome.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import ArticleEdit from '../views/ArticleEdit.vue'
import AdminLayout from '../components/admin/AdminLayout.vue'
import SystemSettings from '../components/admin/views/SystemSettings.vue'
import UserList from '../components/admin/views/UserList.vue'

const routes = [
  { path: '/', name: 'Home', component: DynamicHome },
  { path: '/article/:id', name: 'ArticleDetail', component: ArticleDetail },
  { path: '/edit/:id', name: 'ArticleEdit', component: ArticleEdit },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/'
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/settings', // 默认重定向到系统设置
    children: [
      { path: 'settings', name: 'AdminSettings', component: SystemSettings },
      { path: 'users', name: 'AdminUsers', component: UserList }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
