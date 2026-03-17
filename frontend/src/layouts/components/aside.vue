<script setup>
import Config from "@/config";
import { menuList } from "@/utils/menu";
import * as ElementPlusIcons from "@element-plus/icons-vue";
import { useRoute } from "vue-router";
import useUserStore from "@/stores/userStore";
import { ref, watch } from "vue";

const route = useRoute();

defineProps({
  isCollapse: {
    type: Boolean,
    required: true,
  },
});

const userStore = useUserStore();

// 当前打开的子菜单索引
const openedSubMenu = ref('');

// 获取图标组件
const getIcon = (iconName) => {
  return ElementPlusIcons[iconName];
};

// 获取菜单列表
const getMenuList = () => {
  return menuList
    .filter((menu) => menu.roleFlag === userStore.roleFlag)
    .flatMap((menu) => menu.backMenu);
};

// 处理子菜单打开事件
const handleSubMenuOpen = (index) => {
  openedSubMenu.value = index;
};

// 处理子菜单关闭事件
const handleSubMenuClose = (index) => {
  if (openedSubMenu.value === index) {
    openedSubMenu.value = '';
  }
};

// 监听路由变化，设置默认打开的子菜单
watch(
  () => route.path,
  (newPath) => {
    const menuList = getMenuList();
    for (const menu of menuList) {
      if (menu.children) {
        const hasActiveChild = menu.children.some(child => child.path === newPath);
        if (hasActiveChild) {
          openedSubMenu.value = menu.path;
          break;
        }
      }
    }
  },
  { immediate: true }
);
</script>

<template>
  <div class="logo-container">
    <img src="../../assets/logo.png" alt="logo" class="logo-icon" />
    <span class="system-name" v-if="!isCollapse">{{ Config.systemName }}</span>
  </div>
  <el-menu
    :collapse="isCollapse"
    :default-active="route.path"
    :default-openeds="[openedSubMenu]"
    class="el-menu-custom"
    router
    @sub-menu-open="handleSubMenuOpen"
    @sub-menu-close="handleSubMenuClose"
    unique-opened
  >
    <template v-for="menu in getMenuList()" :key="menu.path">
      <!-- 有子菜单的情况 -->
      <el-sub-menu v-if="menu.children" :index="menu.path">
        <template #title>
          <el-icon v-if="menu.icon">
            <component :is="getIcon(menu.icon)" />
          </el-icon>
          <span>{{ menu.name }}</span>
        </template>
        <el-menu-item
          v-for="subMenu in menu.children"
          :key="subMenu.path"
          :index="subMenu.path"
        >
          {{ subMenu.name }}
        </el-menu-item>
      </el-sub-menu>
      <!-- 没有子菜单的情况 -->
      <el-menu-item v-else :index="menu.path">
        <el-icon v-if="menu.icon">
          <component :is="getIcon(menu.icon)" />
        </el-icon>
        <template #title>{{ menu.name }}</template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<style lang="scss" scoped>
.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background-color: var(--el-menu-bg-color);
  overflow: hidden;
  transition: all 0.3s;

  .logo-icon {
    width: 28px;
    height: 28px;
    margin-right: 8px;
  }

  .system-name {
    color: var(--el-text-color-primary);
    font-size: 16px;
    font-weight: 600;
    margin: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: opacity 0.3s;
  }
}

.el-menu-custom {
  height: calc(100% - 60px);
  border-right: none;
}

:deep(.el-menu) {
  border-right: none;
}

// 所有菜单项的基础样式
:deep(.el-menu-item),
:deep(.el-sub-menu .el-sub-menu__title) {
  margin: 0 10px;
  width: calc(100% - 20px);
  border-radius: 4px;
  padding-left: 20px !important;

  .el-icon {
    margin-right: 8px;
    width: 24px;
    text-align: center;
  }
}

:deep(.el-menu-item) {
  &.is-active {
    background-color: var(--el-color-primary-light-9) !important;

    &::before {
      position: absolute;
      left: -10px;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 16px;
      background-color: var(--el-color-primary);
      border-radius: 0 2px 2px 0;
      content: "";
    }
  }
}

// 子菜单样式
:deep(.el-sub-menu) {
  .el-menu--inline {
    .el-menu-item {
      padding-left: 48px !important;
      margin-left: 10px;
      width: calc(100% - 30px);

      &.is-active::before {
        left: 0px;
      }
    }
  }
}

// 折叠时的样式
:deep(.el-menu--collapse) {
  .el-menu-item,
  .el-sub-menu .el-sub-menu__title {
    margin: 0 8px;
    width: calc(100% - 16px);
    padding-left: 20px !important;
  }

  .el-sub-menu__title {
    .el-sub-menu__icon-arrow {
      display: none;
    }
  }
}
</style>
