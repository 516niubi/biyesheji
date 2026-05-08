export const roles = [
  {
    label: "管理员",
    value: "admin",
  },
  {
    label: "用户",
    value: "user",
  },
  {
    label: "非遗传承人",
    value: "inheritor",
  },
];

export const menuList = [
  {
    roleFlag: "admin",
    name: "管理员",
    backMenu: [
      {
        name: "首页",
        path: "/home",
        icon: "HomeFilled",
        tableName: "",
        buttons: [],
      },
      {
        name: "数据统计",
        path: "/chart",
        icon: "Histogram",
      },
      {
        name: "系统管理",
        path: "/system",
        icon: "Setting",
        children: [
          {
            name: "轮播图管理",
            path: "/banner",
            tableName: "banner",
            buttons: ["新增", "编辑", "删除"],
          },
          {
            name: "公告管理",
            path: "/notice",
            tableName: "notice",
            buttons: ["新增", "编辑", "删除"],
          },
        ],
      },
      {
        name: "信息管理",
        path: "/info",
        icon: "Document",
        children: [
          {
            name: "分类管理",
            path: "/ichType",
            tableName: "ichType",
            buttons: ["新增", "编辑", "删除"],
          },
          {
            name: "反馈管理",
            path: "/feedback",
            tableName: "feedback",
            buttons: ["删除"],
          },
          {
            name: "传承人私信",
            path: "/privateMessage",
            tableName: "",
            buttons: [],
          },
          {
            name: "收藏管理",
            path: "/collect",
            tableName: "collect",
            buttons: ["删除"],
          },
        ],
      },
      {
        name: "账号管理",
        path: "/account",
        icon: "User",
        children: [
          {
            name: "个人信息",
            path: "/person",
            icon: "",
            tableName: "person",
            buttons: [],
          },
          {
            name: "用户管理",
            path: "/user",
            tableName: "user",
            buttons: ["新增", "编辑", "删除"],
          },
          {
            name: "管理员管理",
            path: "/admin",
            tableName: "admin",
            buttons: ["新增", "编辑", "删除"],
          },
          {
            name: "传承人审核",
            path: "/inheritorAudit",
            tableName: "inheritorAudit",
            buttons: ["审核"],
          },
          {
            name: "传承人管理",
            path: "/inheritorCertified",
            tableName: "inheritorCertified",
            buttons: ["编辑", "删除"],
          },
        ],
      },
    ],
  },
  {
    roleFlag: "inheritor",
    name: "传承人工作台",
    backMenu: [
      {
        name: "首页",
        path: "/home",
        icon: "HomeFilled",
        tableName: "",
        buttons: [],
      },
      {
        name: "内容工作室",
        path: "/studio",
        icon: "Document",
        children: [
          {
            name: "非遗文物",
            path: "/studio/heritage",
            tableName: "culturalHeritage",
            buttons: ["新增", "编辑", "删除"],
          },
          {
            name: "宣传视频",
            path: "/studio/video",
            tableName: "video",
            buttons: ["新增", "编辑", "删除"],
          },
          {
            name: "资讯",
            path: "/studio/article",
            tableName: "article",
            buttons: ["新增", "编辑", "删除"],
          },
          {
            name: "评论",
            path: "/studio/comment",
            tableName: "comment",
            buttons: ["编辑", "删除"],
          },
          {
            name: "活动",
            path: "/studio/activity",
            tableName: "activity",
            buttons: ["新增", "编辑", "删除"],
          },
          {
            name: "活动报名",
            path: "/studio/application",
            tableName: "activityApplication",
            buttons: ["删除", "审核"],
          },
          {
            name: "私信",
            path: "/studio/privateMessage",
            tableName: "",
            buttons: [],
          },
        ],
      },
      {
        name: "账号管理",
        path: "/account",
        icon: "User",
        children: [
          {
            name: "个人信息",
            path: "/person",
            icon: "",
            tableName: "person",
            buttons: [],
          },
        ],
      },
    ],
  },
];
