import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { installConsoleGuard } from './utils/consoleGuard'
import './styles/base.css'

installConsoleGuard()

createApp(App)
  .use(createPinia())
  .use(router)
  .mount('#app')
