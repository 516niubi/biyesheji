<template>
  <div class="not-found">
    <h1>404</h1>
    <p>抱歉，页面未找到</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<script setup></script>

<style scoped>
.not-found {
  padding: 20px;
  text-align: center;
}

a {
  color: #42b983;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
</style>
