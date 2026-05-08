/*
 Navicat Premium Data Transfer

 Source Server         : mysql8
 Source Server Type    : MySQL
 Source Server Version : 80012
 Source Host           : localhost:3306
 Source Schema         : db_ich

 Target Server Type    : MySQL
 Target Server Version : 80012
 File Encoding         : 65001

 Date: 08/09/2025 22:33:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动详情',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动地点',
  `max_people` int(11) NULL DEFAULT NULL COMMENT '人数限制',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态（0-未开始，1-进行中，2-已结束）',
  `view_count` int(11) NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------
INSERT INTO `activity` VALUES (1, '2025新春非遗文化庙会', '/file/files/4f1433dae36e4604b0d55a9600d9b019.png', '喜迎新春，举办大型非遗文化庙会。活动内容：\n1. 各地非遗项目展演展示\n2. 传统手工艺现场制作体验\n3. 民间艺术表演（舞龙舞狮、高跷、杂技等）\n4. 传统美食制作品尝\n5. 非遗文创产品展销\n让市民在欢乐祥和的氛围中感受传统文化的魅力。', '2025-02-10 09:00:00', '2025-02-16 21:00:00', '市文化广场', 5000, 0, 1234, '2024-12-15 10:30:00');
INSERT INTO `activity` VALUES (2, '昆曲艺术春季大师班', '/file/files/d04fa420be6a4bc2a7ebf4b57d5158c5.png', '由国家级昆曲传承人亲自授课，为期五天的昆曲艺术深度体验课程。内容包括：\n- 昆曲发展历史与艺术特色讲座\n- 经典唱腔教学与练习\n- 身段动作规范化训练\n- 《牡丹亭》经典片段排练\n- 传统戏曲化妆与服饰体验\n结业时将进行汇报演出。', '2025-03-15 09:00:00', '2025-03-19 17:00:00', '市戏曲艺术中心', 30, 0, 876, '2024-12-20 14:25:00');
INSERT INTO `activity` VALUES (3, '传统手工艺创新设计大赛', '/file/files/f8305954928f40e2999be36f3f52307c.png', '为推动传统手工艺与现代设计融合，举办创新设计大赛。\n参赛类别：\n1. 陶瓷艺术创新设计\n2. 织绣技艺现代应用\n3. 木雕竹编创意作品\n4. 金属工艺当代设计\n获奖作品将获得资金扶持和市场推广支持。', '2025-04-01 00:00:00', '2025-06-30 23:59:00', '线上征集+线下展览', 200, 0, 1567, '2024-11-10 09:15:00');
INSERT INTO `activity` VALUES (4, '二十四节气传统文化体验营', '/file/files/ed5bb4fbb9524a76a4f1d5c4ecb1f914.png', '按照二十四节气时间节点，开展系列传统文化体验活动：\n立春：春饼制作、迎春祈福\n清明：青团制作、踏青游园\n端午：包粽子、赛龙舟体验\n中秋：月饼制作、赏月诗会\n冬至：饺子制作、数九消寒\n每期活动限额报名。', '2025-02-04 08:00:00', '2025-12-22 17:00:00', '传统文化体验馆', 50, 0, 2345, '2024-10-20 13:40:00');
INSERT INTO `activity` VALUES (5, '非遗数字化保护研讨会', '/file/files/81021f71f8974a56b61a17fd22b01ded.png', '邀请国内外专家学者，共同探讨非遗数字化保护技术：\n议题包括：\n- 3D扫描技术在非遗保护中的应用\n- 虚拟现实非遗体验平台建设\n- 区块链技术在非遗传承人认证中的作用\n- 人工智能辅助非遗技艺教学\n- 数字化档案建设标准研讨', '2025-05-20 09:00:00', '2025-05-22 17:00:00', '国际会议中心', 300, 0, 987, '2024-11-30 16:50:00');
INSERT INTO `activity` VALUES (6, '传统中医药文化节', '/file/files/6a34f3740ebf49638a5bbbf7ea783455.png', '弘扬传统中医药文化，举办系列健康主题活动：\n1. 名中医义诊活动\n2. 中药辨识与炮制展示\n3. 养生功法教学（太极拳、八段锦）\n4. 药膳制作体验\n5. 中医药文化讲座\n6. 中药材种植基地参观', '2025-07-12 08:30:00', '2025-07-14 20:00:00', '中医药大学附属医院', 1000, 0, 1789, '2024-12-05 11:20:00');
INSERT INTO `activity` VALUES (7, '青年非遗传承人创业培训', '/file/files/915d284271834260acf90160e9ce57ce.png', '针对青年非遗传承人的创业能力提升计划：\n培训内容：\n- 非遗产品市场定位与营销\n- 电商平台运营实操\n- 文创产品设计开发\n- 知识产权保护\n- 项目融资与商业模式\n优秀项目可获得创业基金支持。', '2025-08-08 09:00:00', '2025-08-10 17:00:00', '创业孵化中心', 80, 0, 1345, '2024-12-28 15:30:00');
INSERT INTO `activity` VALUES (8, '传统节日文化体验系列活动', '/file/files/98271dd35f584b0eb4217afad74bf322.png', '全年传统节日文化深度体验：\n元宵节：灯谜会、汤圆制作\n清明节：踏青、放风筝、青团制作\n端午节：香包制作、龙舟体验\n七夕节：乞巧手工艺、传统服饰体验\n重阳节：登高、敬老活动\n中秋节：月饼制作、赏月晚会', '2025-02-05 09:00:00', '2025-10-29 21:00:00', '各社区文化中心', 100, 0, 2680, '2024-11-15 10:05:00');
INSERT INTO `activity` VALUES (9, '非遗进校园工程', '/file/files/11cf7252e4fc416182c4d565b7f07c15.png', '推动非遗文化在青少年中的传播：\n1. 非遗大师校园讲座\n2. 手工艺社团建设\n3. 非遗课程开发\n4. 校园非遗文化节\n5. 师生非遗作品展\n6. 非遗研学实践活动', '2025-03-01 08:00:00', '2025-12-20 17:00:00', '全市中小学校', 5000, 0, 3127, '2024-10-10 14:35:00');
INSERT INTO `activity` VALUES (10, '国际非遗文化交流展', '/file/files/220729df36134df3b570e0d035719ef6.png', '邀请一带一路沿线国家非遗代表参加：\n展示内容：\n- 各国传统表演艺术\n- 手工艺制作展示\n- 非遗保护经验交流\n- 国际合作项目洽谈\n- 文化创意产品展销\n推动中华文化走出去，促进文明交流互鉴。', '2025-08-01 10:00:00', '2025-09-25 18:00:00', '国际展览中心', 2000, 1, 1903, '2024-12-10 09:20:00');
INSERT INTO `activity` VALUES (11, '国家文化交流', '/file/files/b9754551b69345aa9a57da977c3da85b.png', '国家文化交流', '2025-09-08 00:00:00', '2025-09-30 00:00:00', '北京', 200, 1, 4, '2025-09-08 22:17:01');

-- ----------------------------
-- Table structure for activity_application
-- ----------------------------
DROP TABLE IF EXISTS `activity_application`;
CREATE TABLE `activity_application`  (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(20) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态（0-待审核，1-通过，2-拒绝）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动报名表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_application
-- ----------------------------
INSERT INTO `activity_application` VALUES (1, 1, 1, '张三', '13800010001', '期待参加新春庙会，特别喜欢传统手工艺', 1, '2024-12-20 10:30:25', '2024-12-21 14:22:18');
INSERT INTO `activity_application` VALUES (2, 2, 2, '李四', '13800020002', '作为昆曲传承人，希望深入学习交流', 1, '2024-12-25 14:16:33', '2024-12-26 09:45:12');
INSERT INTO `activity_application` VALUES (3, 3, 3, '王五', '13800030003', '陶瓷手工艺人，希望参加创新设计大赛', 1, '2024-11-15 08:45:19', '2025-09-08 21:55:58');
INSERT INTO `activity_application` VALUES (4, 4, 4, '小红', '13800040004', '大学生，想系统学习二十四节气文化', 1, '2024-12-01 13:25:40', '2024-12-02 11:20:33');
INSERT INTO `activity_application` VALUES (5, 5, 5, '明明', '13800050005', '非遗保护工作者，希望参与数字化研讨', 1, '2024-12-10 16:50:22', '2024-12-11 15:40:28');
INSERT INTO `activity_application` VALUES (6, 6, 6, '美美', '13800060006', '舞蹈老师，对中医药养生很感兴趣', 2, '2024-12-28 11:35:17', '2024-12-29 10:15:42');
INSERT INTO `activity_application` VALUES (7, 7, 7, '陈老师', '13800070007', '大学教授，希望为青年传承人提供指导', 1, '2024-12-30 09:15:30', '2024-12-31 14:35:20');
INSERT INTO `activity_application` VALUES (8, 8, 8, '莉莉', '13800080008', '文创设计师，想深入了解传统节日文化', 0, '2024-11-20 15:40:55', '2024-11-20 08:25:37');
INSERT INTO `activity_application` VALUES (9, 9, 9, '汤姆', '13800090009', '摄影师，希望记录非遗进校园的过程', 1, '2024-12-05 12:20:43', '2024-12-06 16:50:12');
INSERT INTO `activity_application` VALUES (10, 10, 10, '苏菲', '13800100010', '茶艺师，期待国际文化交流展', 1, '2024-12-15 14:30:18', '2024-12-16 09:45:28');
INSERT INTO `activity_application` VALUES (11, 1, 4, '张三', '13800010001', '对二十四节气活动也很感兴趣', 1, '2024-12-22 11:30:25', '2024-12-23 10:22:18');
INSERT INTO `activity_application` VALUES (12, 4, 7, '小红', '13800040004', '想参加青年创业培训，为未来做准备', 1, '2024-12-28 15:25:40', '2025-09-08 21:55:54');
INSERT INTO `activity_application` VALUES (13, 5, 10, '明明', '13800050005', '希望在国际交流展上分享保护经验', 1, '2024-12-18 10:50:22', '2024-12-19 11:40:28');
INSERT INTO `activity_application` VALUES (14, 7, 5, '陈老师', '13800070007', '数字化保护是我研究的重点领域', 1, '2024-12-12 14:15:30', '2024-12-13 09:35:20');
INSERT INTO `activity_application` VALUES (15, 2, 1, '李四', '13800020002', '带学生一起参加新春庙会', 1, '2024-12-19 16:16:33', '2024-12-20 08:45:12');
INSERT INTO `activity_application` VALUES (16, 6, 1, '美美', '13800060006', '和家人一起参加庙会活动', 1, '2024-12-18 13:35:17', '2024-12-19 10:15:42');
INSERT INTO `activity_application` VALUES (17, 8, 1, '莉莉', '13800080008', '寻找设计灵感，参加庙会', 1, '2024-12-17 11:40:55', '2024-12-18 08:25:37');
INSERT INTO `activity_application` VALUES (18, 3, 3, '王五的朋友', '13800030013', '王五推荐参加设计大赛', 1, '2024-11-18 09:45:19', '2025-09-08 21:56:10');
INSERT INTO `activity_application` VALUES (19, 9, 3, '汤姆的同事', '13800090019', '共同参加设计大赛', 0, '2024-11-20 10:20:43', '2024-11-20 10:20:43');
INSERT INTO `activity_application` VALUES (20, 10, 6, '苏菲', '13800100010', '对中医药养生很感兴趣', 1, '2024-12-30 10:30:18', '2024-12-31 14:45:28');
INSERT INTO `activity_application` VALUES (22, 3, 11, '张三', '19876510982', '及时参与', 1, '2025-09-08 22:18:04', '2025-09-08 22:18:27');
INSERT INTO `activity_application` VALUES (23, 4, 11, '张三', '18909718761', '', 1, '2025-09-08 22:28:12', '2025-09-08 22:28:29');

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `nick_name` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `age` int(11) NULL DEFAULT 18 COMMENT '年龄',
  `gender` int(11) NULL DEFAULT 2 COMMENT '性别（0-男，1-女，2-保密）',
  `avatar` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人介绍',
  `status` int(11) NULL DEFAULT 1 COMMENT '账号状态（0-封禁，1-正常）',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '123', '管理员', 18, 2, '/file/files/fac946a2292e4d6c91e056a3818504a1.jpeg', '简介很简洁', 1, 'admin', '2024-08-09 13:52:30', '2025-09-08 20:53:51');
INSERT INTO `admin` VALUES (2, 'caiya', '123', '菜牙', 18, 2, '/file/files/2e8cbab2459947c1be21e4f9461dd904.jpg', '简介很简洁', 1, 'admin', '2024-08-09 13:53:38', '2025-09-08 22:31:07');
INSERT INTO `admin` VALUES (3, 'test', '123', '测试22', 18, 2, '/file/files/2e8cbab2459947c1be21e4f9461dd904.jpg', '我是测试人员', 1, 'admin', '2024-10-20 20:15:44', '2024-10-20 20:18:05');
INSERT INTO `admin` VALUES (4, 'abb', '123', 'abb', 18, 0, '/file/files/7d6d816061e44e4dbbbd8c0a167d4443.png', 'abb', 1, 'admin', '2025-09-08 22:13:20', '2025-09-08 22:13:20');

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `intro` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '简介',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `cover_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '浏览次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '资讯表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article
-- ----------------------------
INSERT INTO `article` VALUES (1, '昆曲艺术的传承与创新', '探讨传统昆曲在现代社会中的发展路径', '昆曲作为中国最古老的剧种之一，有着600多年的历史。近年来，随着传统文化复兴浪潮的兴起，昆曲艺术重新受到关注。本文从人才培养、剧目创新、市场推广等方面分析昆曲的现代传承之路，并探讨如何让这一古老艺术形式更好地适应现代观众的审美需求。', '/file/files/312902b3bac94e948b717c251e1f6a9b.jpg', 3247, '2023-06-10 09:30:00', '2025-09-08 22:27:01');
INSERT INTO `article` VALUES (2, '二十四节气的现代意义', '传统智慧在当代生活中的应用价值', '<p>二十四节气不仅是古代农耕文明的智慧结晶，在现代社会依然具有重要的指导意义。本文详细分析每个节气的特点及其对健康养生、农业生产、文化教育的启示，探讨如何将这一传统文化遗产融入现代生活，发挥其应有的价值。</p>', '/file/files/a704a1dc027d4182974b3d2ef5dd5b2e.png', 1876, '2023-05-22 14:15:00', '2025-08-30 21:29:03');
INSERT INTO `article` VALUES (3, '景德镇瓷器制作工艺探秘', '千年瓷都的传统技艺与现代发展', '<p>景德镇瓷器以其精湛的工艺和独特的艺术价值享誉世界。本文深入介绍景德镇传统制瓷的72道工序，包括选料、拉坯、画坯、施釉、烧窑等关键环节，同时探讨现代科技与传统工艺的结合，以及景德镇瓷器在当代艺术市场中的地位。</p>', '/file/files/fa7ac8c853f046e2989f1b83a37edce5.png', 2453, '2023-07-05 11:20:00', '2025-08-30 21:14:52');
INSERT INTO `article` VALUES (4, '中医针灸的国际传播', '中国传统医学走向世界的历程与挑战', '<p>针灸作为中医的重要组成部分，已经在全球100多个国家得到应用。本文回顾针灸国际传播的历史进程，分析在不同文化背景下面临的挑战，探讨标准化、本土化等关键问题，并对中医针灸的未来发展前景进行展望。</p>', '/file/files/d76a5131dab040bc9eb24708f3678998.png', 3124, '2023-06-28 13:40:00', '2025-09-08 22:25:59');
INSERT INTO `article` VALUES (5, '皮影戏的数字化保护', '科技手段助力非物质文化遗产传承', '<p>面对皮影戏传承人老龄化、观众减少等问题，数字化技术为其保护提供了新思路。本文介绍皮影戏数字化采集、存储、展示的技术方法，分析数字博物馆、虚拟现实等技术在皮影戏保护中的应用，探讨传统艺术与现代科技融合的创新模式。</p>', '/file/files/bbca22557c08485dab294fcfdccfd34f.png', 1567, '2023-07-12 16:50:00', '2025-08-30 21:30:09');
INSERT INTO `article` VALUES (6, '中国传统节日的文化内涵', '从端午节看中华文化的传承与发展', '<p>中国传统节日承载着丰富的文化内涵和民族精神。本文以端午节为例，深入分析其历史渊源、民俗活动和文化象征，探讨传统节日在现代社会的传承方式，以及如何通过节日文化增强民族认同感和文化自信。</p>', '/file/files/ed2dbac740e5403690cd2426ed9ba391.jpg', 2789, '2023-05-30 10:05:00', '2025-08-30 21:14:07');
INSERT INTO `article` VALUES (7, '中国书法的美学价值', '笔墨之间的哲学思考与艺术表达', '<p>书法不仅是文字书写的艺术，更是中国人哲学思想和审美情趣的集中体现。本文从笔法、结体、章法等角度分析书法的美学特征，探讨书法艺术中蕴含的阴阳平衡、虚实相生等哲学思想，以及书法教育在传统文化传承中的重要作用。</p>', '/file/files/b70c19387a4e40ecbd270ae67cae8012.png', 1987, '2023-07-18 08:45:00', '2025-08-30 21:31:02');
INSERT INTO `article` VALUES (9, '传统工艺的现代转型', '手工技艺在工业化时代的生存之道', '<p>在工业化大规模生产的背景下，许多传统工艺面临生存危机。本文以宣纸制作、刺绣、漆器等为例，分析传统工艺现代转型的成功案例，探讨如何在保持传统特色的同时实现创新发展，找到传统与现代的平衡点。</p>', '/file/files/0e8193a2f7d24fe4a024ffc7436660c5.png', 1654, '2023-07-22 12:10:00', '2025-08-30 21:31:51');
INSERT INTO `article` VALUES (10, '青年与非遗传承', '新时代年轻人的文化使命与创新实践', '随着文化自信的增强，越来越多的年轻人投身非遗传承事业。本文通过多个典型案例，展示青年人在非遗传承中的创新实践，分析他们为传统文化注入的新活力，探讨如何更好地吸引和培养青年传承人，确保非遗项目的可持续发展。', '/file/files/5a8858bebc9a4ee0b6606715c82de8e8.png', 2894, '2023-08-01 09:20:00', '2025-09-08 22:30:47');

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (6, '《守拙》泰山福香烧陶艺', '/file/files/968e3468121c47c788681fce57334af7.jpg', '2025-08-30 20:02:25', '2025-08-30 20:02:25');
INSERT INTO `banner` VALUES (7, '让非遗再燃一次', '/file/files/a0a84ac8bc1a4a9797a3d6ef925a743d.jpg', '2025-08-30 20:08:06', '2025-08-30 20:08:06');
INSERT INTO `banner` VALUES (8, '伞', '/file/files/a02f69d2198a471f9434cb12ca93df3b.jpg', '2025-08-30 20:08:26', '2025-08-30 20:08:26');
INSERT INTO `banner` VALUES (9, '传统文化体验', '/file/files/60a820ffd91b4de4a5dd3b53a8819649.png', '2025-09-08 20:04:00', '2025-09-08 22:30:07');

-- ----------------------------
-- Table structure for collect
-- ----------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `heritage_id` int(11) NOT NULL COMMENT '文物ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Fixed;

-- ----------------------------
-- Records of collect
-- ----------------------------
INSERT INTO `collect` VALUES (3, 3, 8, '2025-09-08 21:54:56', '2025-09-08 21:54:56');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `heritage_id` int(11) NOT NULL COMMENT '文物ID',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '评论内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 3, 7, '你好', '2025-08-31 13:00:18', '2025-08-31 13:00:18');
INSERT INTO `comment` VALUES (2, 3, 6, '出色的传统工艺', '2025-08-31 15:11:35', '2025-08-31 15:11:35');
INSERT INTO `comment` VALUES (3, 3, 7, 'hello!!!', '2025-09-08 22:25:37', '2025-09-08 22:25:37');
INSERT INTO `comment` VALUES (4, 3, 7, '1231223', '2025-09-08 22:26:33', '2025-09-08 22:26:33');

-- ----------------------------
-- Table structure for cultural_heritage
-- ----------------------------
DROP TABLE IF EXISTS `cultural_heritage`;
CREATE TABLE `cultural_heritage`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `category_id` int(11) NULL DEFAULT NULL COMMENT '分类ID',
  `cover_image` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
  `intro` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '简介',
  `description` longtext CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL COMMENT '详细描述',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '浏览次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '非遗文物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cultural_heritage
-- ----------------------------
INSERT INTO `cultural_heritage` VALUES (1, '昆曲', 3, '/file/files/87ab741e0c5145ee9d4e6f5205c71323.jpg', '中国最古老的剧种之一，被誉为“百戏之祖”', '<p>昆曲起源于元末明初的苏州昆山一带，以其婉转的唱腔、细腻的表演和丰富的剧目闻名。2001年被联合国教科文组织列为“人类口述和非物质遗产代表作”。代表剧目有《牡丹亭》、《长生殿》等。</p>', 1250, '2023-01-15 10:30:00', '2025-08-30 21:47:22');
INSERT INTO `cultural_heritage` VALUES (2, '端午节', 2, '/file/files/d8818ccf5fd646f1b6430f1f2c7ccca9.png', '中国传统节日，纪念屈原的重要节日', '<p>端午节又称端阳节，有赛龙舟、吃粽子、挂艾草等习俗。2009年入选世界非物质文化遗产名录。各地庆祝方式各异，但都体现了中国人对传统文化的传承。</p>', 980, '2023-02-10 09:15:00', '2025-08-30 21:46:13');
INSERT INTO `cultural_heritage` VALUES (3, '景德镇瓷器', 3, '/file/files/4d59e73f398a4337911ce998e2f46830.png', '千年瓷都景德镇的传统制瓷技艺', '<p>景德镇制瓷历史长达1700多年，以其“白如玉、明如镜、薄如纸、声如磬”的独特风格享誉世界。传统制瓷工艺包括拉坯、利坯、画坯、施釉、烧窑等72道工序。</p>', 880, '2023-01-20 14:20:00', '2025-09-08 22:15:40');
INSERT INTO `cultural_heritage` VALUES (4, '中医针灸', 4, '/file/files/4fc0b14d11f54cadb549be53c9b19d9f.png', '中国传统医学的重要组成部分', '<p>针灸是通过刺激人体特定穴位来治疗疾病的方法，包括针法和灸法。2010年被列入人类非物质文化遗产代表作名录。其理论基础是中医的经络学说。</p>', 1532, '2023-03-05 11:40:00', '2025-08-30 21:43:46');
INSERT INTO `cultural_heritage` VALUES (5, '皮影戏', 3, '/file/files/e5dfc5aadfc6442c907b3d23f31c756e.png', '古老的民间戏剧形式', '<p>表演者幕后操纵皮质人偶，通过灯光将影子投射到白色幕布上，配以音乐和唱腔讲故事。2011年被列入人类非物质文化遗产名录。各地皮影戏风格各异，具有浓厚的地方特色。</p>', 654, '2023-02-28 16:45:00', '2025-08-30 21:45:12');
INSERT INTO `cultural_heritage` VALUES (6, '宣纸制作技艺', 5, '/file/files/d998ba832e8345e2bf945ebccde94668.jpg', '中国传统造纸工艺的杰出代表', '<p>宣纸因产于安徽宣城而得名，有“纸寿千年”之誉。传统制作工艺包括选料、浸泡、发酵、打浆、抄纸、晒纸等108道工序。2009年入选人类非物质文化遗产代表作名录。</p><p><img src=\"http://localhost:8088/api/file/files/90d9356e84a342f2b7034e5018acf9c7.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 437, '2023-03-15 08:50:00', '2025-09-08 20:36:40');
INSERT INTO `cultural_heritage` VALUES (7, '二十四节气', 4, '/file/files/e62b183924084682ab0311dcaa907e7f.png', '中国人通过观察太阳周年运动而形成的时间知识体系', '<p>二十四节气指导着传统农业生产和日常生活，是中国历法的独特创造。2016年被列入人类非物质文化遗产代表作名录。每个节气都有相应的气候特征和民俗活动。</p>', 2120, '2023-01-08 13:25:00', '2025-09-08 22:26:39');
INSERT INTO `cultural_heritage` VALUES (8, '中国书法', 5, '/file/files/2f520c7f79a442b0af40d0da14dfa6fe.png', '以汉字为载体的独特艺术形式', '<p>书法通过笔墨、结体、章法等造型美，表现作者的情感与审美追求。2009年被列入人类非物质文化遗产代表作名录。主要书体有篆、隶、楷、行、草五种。</p>', 1877, '2023-02-14 10:10:00', '2025-09-08 21:54:55');
INSERT INTO `cultural_heritage` VALUES (9, '京剧', 3, '/file/files/da3766ec4edd40fea01e45dc3b51b53a.png', '中国最具影响力的戏曲剧种之一', '<p>京剧形成于北京，融合了徽剧、汉调等多种地方戏曲精华。以程式化的表演、华丽的脸谱和独特的唱腔著称。2010年被列入人类非物质文化遗产代表作名录。</p>', 1453, '2023-03-01 15:30:00', '2025-08-30 21:45:02');
INSERT INTO `cultural_heritage` VALUES (10, '中国剪纸', 5, '/file/files/afcb1f69f8ff44d3b43afb86464c997c.png', '流行于中国民间的传统装饰艺术', '<p>剪纸用剪刀或刻刀在纸上剪刻花纹，用于装点生活或配合民俗活动。2009年入选人类非物质文化遗产代表作名录。各地剪纸风格迥异，反映不同的地域文化特征。</p>', 765, '2023-02-05 09:45:00', '2025-08-30 21:46:59');

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '反馈标题',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '反馈内容',
  `img` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '反馈图片',
  `reply` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '回复内容',
  `status` int(2) NULL DEFAULT 0 COMMENT '状态（0-未回复，1-已回复）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '反馈表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of feedback
-- ----------------------------
INSERT INTO `feedback` VALUES (1, 2, '首页不错', '首页不错', '/file/files/03a72290b2534144bfa1cd8035f60474.png', '谢谢', 1, '2025-08-31 09:23:12', '2025-08-31 09:24:44');
INSERT INTO `feedback` VALUES (2, 4, '优化首页体验', '首页展示有点快，可以速度变慢点', '/file/files/985ed0ef132946b1b194ae864eb1a82c.jpg', '好的，谢谢您的建议', 1, '2025-09-08 20:08:31', '2025-09-08 20:09:02');
INSERT INTO `feedback` VALUES (4, 3, '你好你好', '你好你好', '', 'hello', 1, '2025-09-08 22:19:43', '2025-09-08 22:20:01');
INSERT INTO `feedback` VALUES (5, 4, '测试反馈', '1224345235', '', '这是一个回复', 1, '2025-09-08 22:29:03', '2025-09-08 22:29:17');

-- ----------------------------
-- Table structure for ich_type
-- ----------------------------
DROP TABLE IF EXISTS `ich_type`;
CREATE TABLE `ich_type`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '分类名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '非遗文物分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ich_type
-- ----------------------------
INSERT INTO `ich_type` VALUES (2, '传统口头文学', '2025-08-29 23:04:52', '2025-08-29 23:04:52');
INSERT INTO `ich_type` VALUES (3, '传统表演艺术', '2025-08-29 23:04:52', '2025-08-29 23:04:52');
INSERT INTO `ich_type` VALUES (4, '传统节庆习俗', '2025-08-29 23:04:52', '2025-08-29 23:04:52');
INSERT INTO `ich_type` VALUES (5, '传统手工技艺', '2025-08-29 23:04:52', '2025-08-29 23:04:52');
INSERT INTO `ich_type` VALUES (6, '传统医药知识', '2025-08-29 23:04:52', '2025-08-29 23:04:52');
INSERT INTO `ich_type` VALUES (7, '传统体育游艺', '2025-08-29 23:04:52', '2025-08-29 23:04:52');
INSERT INTO `ich_type` VALUES (8, '传统饮食文化', '2025-08-29 23:04:52', '2025-08-29 23:04:52');
INSERT INTO `ich_type` VALUES (9, '传统美术工艺', '2025-08-29 23:04:52', '2025-08-29 23:04:52');
INSERT INTO `ich_type` VALUES (10, '传统建筑技艺', '2025-08-29 23:04:52', '2025-08-29 23:04:52');
INSERT INTO `ich_type` VALUES (11, '传统生态知识', '2025-08-29 23:04:52', '2025-09-08 20:09:28');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公告图片',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (1, '第一条公告', '欢迎来到系统', NULL, 1, '2025-05-04 17:28:53', '2025-05-04 17:28:53');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `nick_name` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `age` int(11) NULL DEFAULT 18 COMMENT '年龄',
  `gender` int(11) NULL DEFAULT 2 COMMENT '性别（0-男，1-女，2-保密）',
  `avatar` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人介绍',
  `status` int(11) NULL DEFAULT 1 COMMENT '账号状态（0-封禁，1-正常）',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (3, 'user', '123', '长伞', 18, 2, '/file/files/2e8cbab2459947c1be21e4f9461dd904.jpg', '长伞', 1, 'user', '2024-10-20 20:15:44', '2025-09-08 20:53:24');
INSERT INTO `user` VALUES (4, 'zhangsan', '123', '张三', 25, 0, '/file/files/521f7867dfc64dc388fec1e1cb6367b1.jpg', '非遗文化爱好者，擅长传统书法和绘画', 1, 'user', '2024-03-15 10:30:25', '2025-09-08 20:53:34');
INSERT INTO `user` VALUES (5, 'lisi_2024', '123', '李四', 32, 1, '/avatar/2.jpg', '昆曲传承人，致力于传统戏曲的推广', 1, 'user', '2024-05-22 14:16:33', '2025-02-15 09:45:12');
INSERT INTO `user` VALUES (6, 'wangwu', '123', '王五', 28, 0, '/avatar/3.jpg', '手工艺人，专注于陶瓷艺术创作', 1, 'user', '2024-07-10 08:45:19', '2024-12-30 16:30:45');
INSERT INTO `user` VALUES (7, 'xiaohong', '123', '小红', 22, 1, '/avatar/4.jpg', '大学生，热爱传统文化，参加过多项非遗活动', 1, 'user', '2024-02-18 13:25:40', '2025-03-10 11:20:33');
INSERT INTO `user` VALUES (8, 'mingming', '123', '明明', 35, 0, '/avatar/5.jpg', '非遗保护工作者，从事文化遗产研究10年', 1, 'user', '2024-09-05 16:50:22', '2025-01-08 15:40:28');
INSERT INTO `user` VALUES (9, 'meimei', '123', '美美', 26, 1, '/avatar/6.jpg', '舞蹈老师，擅长传统民族舞蹈教学', 1, 'user', '2024-04-12 11:35:17', '2024-11-25 10:15:42');
INSERT INTO `user` VALUES (10, 'david_chen', '123', '陈老师', 45, 0, '/avatar/7.jpg', '大学教授，研究方向为中国传统文化保护', 1, 'user', '2024-08-20 09:15:30', '2025-02-28 14:35:20');
INSERT INTO `user` VALUES (11, 'lily_wang', '123', '莉莉', 29, 1, '/avatar/8.jpg', '文创设计师，将传统元素融入现代设计', 1, 'user', '2024-06-08 15:40:55', '2024-10-18 08:25:37');
INSERT INTO `user` VALUES (12, 'tom_li', '123', '汤姆', 31, 0, '/avatar/9.jpg', '摄影师，专注于记录非遗技艺传承过程', 1, 'user', '2024-01-25 12:20:43', '2025-03-05 16:50:12');
INSERT INTO `user` VALUES (13, 'sophia_zhao', '123', '苏菲', 27, 1, '/avatar/10.jpg', '茶艺师，精通中国传统茶文化', 1, 'user', '2024-10-12 14:30:18', '2024-12-15 09:45:28');
INSERT INTO `user` VALUES (14, 'user2', '123', '小小', 18, 2, '/file/files/79de3967ddd54212a7d843a9b0eea640.png', NULL, 1, 'user', '2025-08-31 13:41:19', '2025-08-31 13:42:09');
INSERT INTO `user` VALUES (15, 'xiao', '123', NULL, 18, 2, NULL, NULL, 1, 'user', '2025-09-08 22:04:29', '2025-09-08 22:04:29');
INSERT INTO `user` VALUES (16, 'zz', '123', 'zz', 18, 2, NULL, NULL, 1, 'user', '2025-09-08 22:06:11', '2025-09-08 22:06:11');
INSERT INTO `user` VALUES (18, 'zyu', '123', 'zyu', 18, 0, '/file/files/b0327ced5d254b46b3f00f287cc02634.png', 'zyu', 1, 'user', '2025-09-08 22:12:49', '2025-09-08 22:12:49');

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '视频标题',
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '视频地址',
  `cover_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '封面图',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '浏览次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '宣传视频表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video
-- ----------------------------
INSERT INTO `video` VALUES (1, '《守拙》泰山福香烧陶艺', '/file/files/d7442a8015fe4a458ea2f8dd026a13da.mp4', '/file/files/95a2bc54a9934a73a8577754d5c54829.jpg', 3, '2025-08-30 09:36:28', '2025-08-31 15:13:27');
INSERT INTO `video` VALUES (2, '年味', '/file/files/87af769d7c5343b49d9a69651b520fa5.mp4', '/file/files/e3322245b6ba47e9bfa1eb178c165138.jpg', 293, '2025-09-08 20:20:29', '2025-09-08 22:28:43');

SET FOREIGN_KEY_CHECKS = 1;
