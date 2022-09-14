import axios from 'axios'
import Vue from 'vue'
import _ from 'lodash'
import {
  store
} from '@/assets/js/vuex-store'
import {
  Message
} from "element-ui"

export default ({
  app
}) => {

  axios.defaults.baseURL = process.env.contextPath;

  axios.interceptors.request.use(function (config) {
    if (config.method === 'get') {
      config.url = encodeURI(config.url);
    }
    //Add permission token
    if (store.state.authToken) {
      config.headers["jwt-token"] = store.state.authToken;
    }
    return config;
  }, function (error) {
    Message.error("request unavailable");
    return Promise.reject(error);
  });

  axios.interceptors.response.use(function (res) {
    if (res.headers["jwt-token"]) {
      //Refresh tokens that are about to expire
      store.commit('refreshToken', res.headers["jwt-token"]);
    }
    var result = res.data;
    if (!result.code) {
      return result;
    }
    switch (result.code) {
      case (200):
        return result.data;
      case (401):
        Message.error("You are not logged in or your login information has expired");
        return Promise.reject(result.code);
      case (403):
        Message.error("You do not have permission to access this feature");
        return Promise.reject(result.code);
      default:
        Vue.prototype.$loading().close();
        Message.error(result.message);
        return Promise.reject(result.code);
    }
  }, function (error) {
    Message.error("Unable to reach the server, please check the network");
    return Promise.reject(error);
  });
}