import { boot } from 'quasar/wrappers'
import axios from 'axios'

const api = axios.create({ baseURL: '/' })

export default boot(({ app }) => {
  app.config.globalProperties.$api = api
  // ^ ^ ^ this will allow you to use this.$api (for Vue Options API form)
  //       so you can easily perform requests against your app's API
})

export { axios, api }
