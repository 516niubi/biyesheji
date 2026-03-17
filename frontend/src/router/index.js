import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    name: "Layout",
    redirect: "/home",
    component: () => import("../layouts/index.vue"),
    meta: {
      title: "入口",
    },
    children: [
      {
        path: "/home",
        name: "Home",
        component: () => import("@/views/home/home.vue"),
        meta: {
          title: "首页",
        },
      },
      {
        path: "/user",
        name: "User",
        component: () => import("@/views/system/user.vue"),
        meta: {
          title: "用户管理",
        },
      },
      {
        path: "/admin",
        name: "Admin",
        component: () => import("@/views/system/admin.vue"),
        meta: {
          title: "管理员管理",
        },
      },
      {
        path: "/notice",
        name: "Notice",
        component: () => import("@/views/system/notice.vue"),
        meta: {
          title: "公告管理",
        },
      },
      {
        path: "/banner",
        name: "Banner",
        component: () => import("@/views/system/banner.vue"),
        meta: {
          title: "轮播图管理",
        },
      },
      {
        path: "/person",
        name: "Person",
        component: () => import("@/views/person.vue"),
        meta: {
          title: "个人信息",
        },
      },
      {
        path: "/ichType",
        name: "IchType",
        component: () => import("@/views/system/ichType.vue"),
        meta: {
          title: "分类管理",
        },
      },
      {
        path: "/culturalHeritage",
        name: "CulturalHeritage",
        component: () => import("@/views/system/culturalHeritage.vue"),
        meta: {
          title: "非遗文物管理",
        },
      },
      {
        path: "/video",
        name: "Video",
        component: () => import("@/views/system/video.vue"),
        meta: {
          title: "宣传视频管理",
        },
      },
      {
        path: "/article",
        name: "Article",
        component: () => import("@/views/system/article.vue"),
        meta: {
          title: "资讯管理",
        },
      },
      {
        path: "/feedback",
        name: "Feedback",
        component: () => import("@/views/system/feedback.vue"),
        meta: {
          title: "反馈管理",
        },
      },
      {
        path: "/collect",
        name: "Collect",
        component: () => import("@/views/system/collect.vue"),
        meta: {
          title: "收藏管理",
        },
      },
      {
        path: "/comment",
        name: "Comment",
        component: () => import("@/views/system/comment.vue"),
        meta: {
          title: "评论管理",
        },
      },
      {
        path: "/activity",
        name: "Activity",
        component: () => import("@/views/system/activity.vue"),
        meta: {
          title: "活动管理",
        },
      },
      {
        path: "/activityApplication",
        name: "ActivityApplication",
        component: () => import("@/views/system/activityApplication.vue"),
        meta: {
          title: "活动报名管理",
        },
      },
      {
        path: "/chart",
        name: "Chart",
        component: () => import("@/views/system/chart-page.vue"),
        meta: {
          title: "数据统计",
        },
      },
    ],
  },
  {
    path: "/front",
    name: "Front",
    component: () => import("../views/front-layout/index.vue"),
    redirect: "/front/home",
    meta: {
      title: "前台",
    },
    children: [
      {
        path: "/front/home",
        name: "FrontHome",
        component: () => import("../views/front/home.vue"),
        meta: {
          title: "首页",
        },
      },
      {
        path: "/front/heritage",
        name: "FrontHeritage",
        component: () => import("../views/front/heritage.vue"),
        meta: {
          title: "非遗文物",
        },
      },
      {
        path: "/front/heritageDetail",
        name: "FrontHeritageDetail",
        component: () => import("../views/front/heritage-detail.vue"),
        meta: {
          title: "非遗文物详情",
        },
      },
      {
        path: "/front/activity",
        name: "FrontActivity",
        component: () => import("../views/front/activity.vue"),
        meta: {
          title: "活动中心",
        },
      },
      {
        path: "/front/activityDetail",
        name: "FrontActivityDetail",
        component: () => import("../views/front/activity-detail.vue"),
        meta: {
          title: "活动详情",
        },
      },
      {
        path: "/front/news",
        name: "FrontNews",
        component: () => import("../views/front/news.vue"),
        meta: {
          title: "新闻资讯",
        },
      },
      {
        path: "/front/newsDetail",
        name: "FrontNewsDetail",
        component: () => import("../views/front/news-detail.vue"),
        meta: {
          title: "新闻资讯详情",
        },
      },
      {
        path: "/front/videos",
        name: "FrontVideos",
        component: () => import("../views/front/videos.vue"),
        meta: {
          title: "非遗宣传",
        },
      },
      {
        path: "/front/videosDetail",
        name: "FrontVideosDetail",
        component: () => import("../views/front/videos-detail.vue"),
        meta: {
          title: "非遗宣传详情",
        },
      },
      {
        path: "/front/feedback",
        name: "FrontFeedback",
        component: () => import("../views/front/feedback.vue"),
        meta: {
          title: "反馈",
        },
      },
      {
        path: "/front/profile",
        name: "FrontProfile",
        component: () => import("../views/front/profile.vue"),
        meta: {
          title: "个人信息",
        },
      },
      {
        path: "/front/myApplications",
        name: "FrontMyApplications",
        component: () => import("../views/front/my-applications.vue"),
        meta: {
          title: "我的报名",
        },
      },
      {
        path: "/front/myCollect",
        name: "FrontMyCollect",
        component: () => import("../views/front/my-collect.vue"),
        meta: {
          title: "我的收藏",
        },
      },
    ],
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("../views/login.vue"),
    meta: {
      title: "登录",
    },
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("../views/register.vue"),
    meta: {
      title: "注册",
    },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("../views/notFound.vue"),
    meta: {
      title: "404",
    },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || "Vue App";
  next();
});

export default router;
