import { createRouter, createWebHistory } from 'vue-router'
import DynamicHome from '../views/DynamicHome.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import ArticleEdit from '../views/ArticleEdit.vue'
import AdminLayout from '../components/admin/AdminLayout.vue'
import SystemSettings from '../components/admin/views/SystemSettings.vue'
import UserList from '../components/admin/views/UserList.vue'
import RoleManagement from '../components/admin/views/RoleManagement.vue'

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
      { path: 'users', name: 'AdminUsers', component: UserList },
      { path: 'roles', name: 'AdminRoles', component: RoleManagement }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫：保存进入管理后台前的路由
router.beforeEach((to, from, next) => {
  // 如果即将进入管理后台，且来源不是管理后台
  if (to.path.startsWith('/admin') && !from.path.startsWith('/admin')) {
    // 保存来源路由
    localStorage.setItem('previous-route-before-admin', from.path)
  }
  
  // 如果从管理后台离开到非管理后台页面，关闭管理后台模态框
  if (from.path.startsWith('/admin') && !to.path.startsWith('/admin')) {
    // 触发自定义事件来关闭管理后台模态框
    window.dispatchEvent(new CustomEvent('close-admin-modal'))
  }
  
  next()
})

export default router
