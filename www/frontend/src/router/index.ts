import { createRouter, createWebHistory } from 'vue-router'
import DynamicHome from '../views/DynamicHome.vue'

const routes = [
  { path: '/', name: 'Home', component: DynamicHome },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
