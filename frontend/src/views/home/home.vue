<template>
  <InheritorBlogHome v-if="isInheritor" />
  <AdminNoticeHome v-else />
</template>

<script setup>
import { computed } from "vue";
import useUserStore from "@/stores/userStore";
import AdminNoticeHome from "./admin-notice-home.vue";
import InheritorBlogHome from "./inheritor-blog-home.vue";

const userStore = useUserStore();
const isInheritor = computed(() => userStore.roleFlag === "inheritor");
</script>
