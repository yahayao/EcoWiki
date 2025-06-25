import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import SimpleHome from '../views/SimpleHome.vue'

const homeStyle = () => localStorage.getItem('homeStyle') || 'classic'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: homeStyle() === 'simple' ? SimpleHome : Home
  },
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
