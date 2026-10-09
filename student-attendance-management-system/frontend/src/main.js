import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue'
import router from './router'
import { isAxiosError } from 'axios'

const app = createApp(App)
// Stores display API failures beside the relevant action. Vue also receives
// rejected event/lifecycle promises; avoid reporting those handled failures twice.
app.config.errorHandler = (error) => {
  if (!isAxiosError(error)) console.error(error)
}
app.use(createPinia()).use(router).mount('#app')
