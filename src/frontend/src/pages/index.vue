<template>
  <div class="wrapper">
    <nav class="main-header navbar navbar-expand-md navbar-light navbar-white">
      <div class="container">
        <a href="javascript:;" class="navbar-brand">
          <img
            alt="Mybatis-Plus Code Generator"
            src="~/assets/img/mybatis-logo.png"
            class="brand-image img-circle elevation-3"
            style="opacity: .8"
          />
          <span class="brand-text font-weight-light">Mybatis-Plus 코드 생성기</span>
        </a>
        <div class="collapse navbar-collapse order-3" id="navbarCollapse">
          <!-- Left navbar links -->
          <ul class="navbar-nav">
            <li class="nav-item dropdown">
              <a
                id="tableMenuDropdown"
                href="#"
                data-toggle="dropdown"
                aria-haspopup="true"
                aria-expanded="false"
                class="nav-link dropdown-toggle"
              >기본 코드 생성</a>
              <ul aria-labelledby="tableMenuDropdown" class="dropdown-menu border-0 shadow">
                <li>
                  <router-link to="/table" class="dropdown-item">테이블</router-link>
                </li>
                <li>
                  <router-link to="/config" class="dropdown-item">설정</router-link>
                </li>
              </ul>
            </li>
            <li class="nav-item">
              <router-link to="/sql" class="nav-link">SQL 코드 생성</router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper" v-cloak>
      <nuxt-child :key="viewKey" />
    </div>
  </div>
</template>
<script>
import axios from "axios";

export default {
  props: [],
  beforeDestroy: function() {},
  computed: {
    viewKey() {
      return this.$route.name !== undefined
        ? this.$route.name + +new Date()
        : this.$route + +new Date();
    }
  },
  data() {
    return {};
  },
  mounted: function() {},
  methods: {}
};
</script>
<style>
</style>


