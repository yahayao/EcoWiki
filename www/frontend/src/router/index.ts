/**
 * Vue Router路由配置文件
 * 
 * 定义了EcoWiki应用程序的所有路由规则和导航守卫。
 * 使用Vue Router 4.x，支持现代的路由功能和组合式API。
 * 
 * 主要功能：
 * - 定义页面路由映射
 * - 配置嵌套路由（管理后台）
 * - 设置路由守卫和拦截器
 * - 处理404页面重定向
 * - 管理后台访问控制
 * 
 * 路由结构：
 * - / : 动态首页（根据用户偏好显示不同风格）
 * - /wiki/:title : 文章详情页（通过标题访问）
 * - /edit/:title : 文章编辑页（编辑现有文章或新建）
 * - /admin : 管理后台布局
 *   - /admin/settings : 系统设置
 *   - /admin/users : 用户管理
 *   - /admin/roles : 角色权限管理
 * 
 * 特殊功能：
 * - 路由守卫保存进入管理后台前的路由
 * - 离开管理后台时触发关闭事件
 * - 404页面自动重定向到首页
 * 
 * @author EcoWiki Team
 * @version 2.0 (增加管理后台嵌套路由)
 * @since 2025-06-30
 */

import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件
import DynamicHome from '../views/DynamicHome.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import ArticleEdit from '../views/ArticleEdit.vue'
import ArticleHistory from '../views/ArticleHistory.vue'

// 导入用户个人资料组件
import UserProfile from '../components/userhome/UserProfile.vue'
import UserPage from '../components/userhome/view/UserPage.vue'
import UserInformation from '../components/userhome/view/UserInformation.vue'
import UserContribute from '../components/userhome/view/UserContribute.vue'
import UserSecure from '../components/userhome/view/UserSecure.vue'
import UserArticle from '@/components/userhome/view/UserArticle.vue'

// 导入管理后台组件
import AdminLayout from '../components/admin/AdminLayout.vue'
import SystemSettings from '../components/admin/views/SystemSettings.vue'
import UserList from '../components/admin/views/UserList.vue'
import RoleManagement from '../components/admin/views/RoleManagement.vue'
import PermissionManagement from '../components/admin/views/PermissionManagement.vue'
import RolePermissionAssignment from '../components/admin/views/RolePermissionAssignment.vue'
import ArticleManagement from '../components/admin/views/ArticleManagement.vue'

//import ForgotPassword from '../components/auth/Forgotpanel.vue'
/**
 * 路由配置数组
 * 定义了应用程序的所有路由规则
 */
const routes = [
  { path: '/', name: 'Home', component: DynamicHome },
  { path: '/wiki/:title', name: 'ArticleDetail', component: ArticleDetail },
  { path: '/wiki/:title/history', name: 'ArticleHistory', component: ArticleHistory },
  { path: '/edit/:title', name: 'ArticleEdit', component: ArticleEdit },
  { path: '/UserProfile', 
    component: UserProfile,
    redirect: '/UserProfile/UserPage',
    children: [
      { path: 'UserPage', name: 'UserPage', component: UserPage },
      { path: 'UserInformation', name: 'UserInformation', component: UserInformation },
      { path: 'UserContribute', name: 'UserContribute', component: UserContribute },
      { path: 'UserSecure', name: 'UserSecure', component: UserSecure },
      { path: 'UserArticle', name: 'UserArticle', component: UserArticle }
    ]
   },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/settings', // 默认重定向到系统设置
    children: [
      // 系统设置页面
      { 
        path: 'settings', 
        name: 'AdminSettings', 
        component: SystemSettings 
      },
      // 用户管理页面
      { 
        path: 'users', 
        name: 'AdminUsers', 
        component: UserList 
      },
      // 文章管理页面
      { 
        path: 'articles', 
        name: 'AdminArticles', 
        component: ArticleManagement 
      },
      // 角色权限管理页面
      { 
        path: 'roles', 
        name: 'AdminRoles', 
        component: RoleManagement 
      },
      // 权限管理页面
      { 
        path: 'permissions', 
        name: 'AdminPermissions', 
        component: PermissionManagement 
      },
      // 角色权限分配页面
      { 
        path: 'role-permissions', 
        name: 'AdminRolePermissions', 
        component: RolePermissionAssignment 
      }
    ]
  },
  
  // 404 Not Found处理
  // 匹配所有未定义的路由，重定向到首页
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/'
  }
]

/**
 * 创建路由器实例
 * 使用HTML5 History模式，支持干净的URL
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置导航守卫
 * 
 * 主要功能：
 * 1. 保存进入管理后台前的路由，便于返回
 * 2. 检测离开管理后台的导航，触发关闭事件
 * 
 * @param to 即将进入的路由对象
 * @param from 当前导航正要离开的路由对象
 * @param next 确认导航的回调函数
 */
router.beforeEach((to, from, next) => {
  // 如果即将进入管理后台，且来源不是管理后台
  if (to.path.startsWith('/admin') && !from.path.startsWith('/admin')) {
    // 保存来源路由到localStorage，用于返回按钮功能
    localStorage.setItem('previous-route-before-admin', from.path)
  }
  
  // 如果从管理后台离开到非管理后台页面，关闭管理后台模态框
  if (from.path.startsWith('/admin') && !to.path.startsWith('/admin')) {
    // 触发自定义事件来关闭管理后台模态框
    // 这个事件会被App.vue监听并处理
    window.dispatchEvent(new CustomEvent('close-admin-modal'))
  }
  
  // 确认导航
  next()
})

export default router
