<script setup lang="ts">
import { ref } from "vue";
import config from "../../config";

// 联系信息
const contactInfo = ref({
  phone: "400-123-4567",
  email: "contact@heritage.com",
  address: "北京市朝阳区文化大街123号",
  workTime: "周一至周五 9:00-18:00",
});
</script>

<template>
  <footer class="front-footer">
    <!-- 主要内容区域 -->
    <div class="footer-main">
      <div class="footer-container">
        <div class="footer-content">
          <!-- 关于我们 -->
          <div class="footer-section">
            <h3 class="section-title">关于我们</h3>
            <div class="section-content">
              <div class="logo-section">
                <img src="/vite.svg" alt="Logo" class="footer-logo" />
                <h4 class="system-name">{{ config.frontName }}</h4>
              </div>
              <p class="description">
                {{ config.description }}
              </p>
            </div>
          </div>

          <!-- 快速导航 -->
          <div class="footer-section">
            <h3 class="section-title">快速导航</h3>
            <div class="section-content">
              <ul class="nav-links">
                <li><a href="/front">首页</a></li>
                <li><a href="/front/heritage">非遗项目</a></li>
                <li><a href="/front/activity">活动中心</a></li>
                <li><a href="/front/inheritor">传承人</a></li>
                <li><a href="/front/news">新闻资讯</a></li>
                <li><a href="/front/about">关于我们</a></li>
              </ul>
            </div>
          </div>

          <!-- 联系我们 -->
          <div class="footer-section">
            <h3 class="section-title">联系我们</h3>
            <div class="section-content">
              <div class="contact-info">
                <div class="contact-item">
                  <i class="icon-phone"></i>
                  <span>{{ contactInfo.phone }}</span>
                </div>
                <div class="contact-item">
                  <i class="icon-email"></i>
                  <span>{{ contactInfo.email }}</span>
                </div>
                <div class="contact-item">
                  <i class="icon-location"></i>
                  <span>{{ contactInfo.address }}</span>
                </div>
                <div class="contact-item">
                  <i class="icon-time"></i>
                  <span>{{ contactInfo.workTime }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.front-footer {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: #ecf0f1;
  margin-top: auto;

  .footer-main {
    padding: 60px 0 40px;

    .footer-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
    }

    .footer-content {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr 1fr;
      gap: 40px;

      .footer-section {
        .section-title {
          font-size: 18px;
          font-weight: 600;
          color: #fff;
          margin-bottom: 20px;
          position: relative;

          &::after {
            content: "";
            position: absolute;
            bottom: -8px;
            left: 0;
            width: 30px;
            height: 3px;
            background: linear-gradient(90deg, #3498db, #2980b9);
            border-radius: 2px;
          }
        }

        .section-content {
          .logo-section {
            display: flex;
            align-items: center;
            margin-bottom: 15px;

            .footer-logo {
              width: 32px;
              height: 32px;
              margin-right: 10px;
            }

            .system-name {
              font-size: 16px;
              font-weight: 600;
              color: #fff;
              margin: 0;
            }
          }

          .description {
            font-size: 14px;
            line-height: 1.6;
            color: #bdc3c7;
            margin-bottom: 20px;
          }

          .social-media {
            .social-title {
              font-size: 14px;
              color: #ecf0f1;
              margin-right: 10px;
            }

            .social-icons {
              display: inline-flex;
              gap: 10px;

              .social-link {
                display: flex;
                align-items: center;
                justify-content: center;
                width: 32px;
                height: 32px;
                background: rgba(255, 255, 255, 0.1);
                border-radius: 50%;
                color: #ecf0f1;
                text-decoration: none;
                transition: all 0.3s;

                &:hover {
                  background: #3498db;
                  transform: translateY(-2px);
                }

                i {
                  font-size: 16px;
                }
              }
            }
          }

          .nav-links {
            list-style: none;
            padding: 0;
            margin: 0;

            li {
              margin-bottom: 12px;

              a {
                color: #bdc3c7;
                text-decoration: none;
                font-size: 14px;
                transition: color 0.3s;

                &:hover {
                  color: #3498db;
                }
              }
            }
          }

          .contact-info {
            .contact-item {
              display: flex;
              align-items: center;
              margin-bottom: 12px;
              font-size: 14px;
              color: #bdc3c7;

              i {
                margin-right: 10px;
                width: 16px;
                color: #3498db;
              }
            }
          }

          .friend-links {
            list-style: none;
            padding: 0;
            margin: 0;

            li {
              margin-bottom: 12px;

              a {
                color: #bdc3c7;
                text-decoration: none;
                font-size: 14px;
                transition: color 0.3s;

                &:hover {
                  color: #3498db;
                }
              }
            }
          }
        }
      }
    }
  }

  .footer-bottom {
    background: rgba(0, 0, 0, 0.2);
    padding: 20px 0;
    border-top: 1px solid rgba(255, 255, 255, 0.1);

    .footer-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
    }

    .copyright {
      text-align: center;

      p {
        margin: 5px 0;
        font-size: 13px;
        color: #95a5a6;

        a {
          color: #95a5a6;
          text-decoration: none;
          transition: color 0.3s;

          &:hover {
            color: #3498db;
          }
        }
      }

      .icp {
        font-size: 12px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .front-footer {
    .footer-main {
      padding: 40px 0 30px;

      .footer-content {
        grid-template-columns: 1fr;
        gap: 30px;
      }
    }

    .footer-bottom {
      .copyright {
        p {
          font-size: 12px;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .front-footer {
    .footer-main {
      padding: 30px 0 20px;

      .footer-container {
        padding: 0 15px;
      }

      .footer-content {
        gap: 25px;

        .footer-section {
          .section-title {
            font-size: 16px;
          }
        }
      }
    }
  }
}

// 图标样式（可以根据实际使用的图标库调整）
.icon-phone::before { content: "📞"; }
.icon-email::before { content: "📧"; }
.icon-location::before { content: "📍"; }
.icon-time::before { content: "🕒"; }
.icon-wechat::before { content: "💬"; }
.icon-weibo::before { content: "🐦"; }
.icon-tiktok::before { content: "🎵"; }
.icon-bilibili::before { content: "📺"; }
</style>