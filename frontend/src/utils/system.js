import Config from "@/config";
import { menuList } from "./menu";
import useUserStore from "@/stores/userStore";

/** 获取图片的全路径 */
export const getImageUrl = (url) => {
  return Config.baseUrl + url;
};

/**
 * 递归搜索菜单项
 * @param {Array} menus - 菜单数组
 * @param {string} tableName - 要查找的表名
 * @returns {Object|null} - 找到的菜单项或null
 */
const findMenuRecursive = (menus, tableName) => {
  for (const menu of menus) {
    // 直接匹配当前菜单
    if (menu.tableName === tableName) {
      return menu;
    }
    // 如果有子菜单，递归搜索
    if (menu.children?.length) {
      const found = findMenuRecursive(menu.children, tableName);
      if (found) return found;
    }
  }
  return null;
};

/**
 * 检查当前角色是否具有指定菜单的操作权限
 * @param {string} tableName - 菜单表名
 * @param {string} value - 操作权限值
 * @returns {boolean} - 是否具有权限
 */
export const hasPermission = (tableName, value) => {
  // 获取当前角色标识
  const roleFlag = useUserStore().roleFlag;

  // 未登录或无角色标识，无权限
  if (!roleFlag) {
    return false;
  }

  try {
    // 获取当前角色的所有后台菜单权限
    const currentRoleMenus =
      menuList.find((menu) => menu.roleFlag === roleFlag)?.backMenu || [];

    // 递归查找指定菜单
    const targetMenu = findMenuRecursive(currentRoleMenus, tableName);

    // 菜单不存在，无权限
    if (!targetMenu) {
      return false;
    }

    // 检查是否包含指定操作权限
    return targetMenu.buttons?.includes(value) || false;
  } catch (error) {
    console.error("权限检查出错:", error);
    return false;
  }
};
