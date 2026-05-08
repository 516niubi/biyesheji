<script setup>
import { computed } from 'vue'
import { ElImage } from 'element-plus'
import { getImageUrl } from '../utils/system'
import { useRouter } from 'vue-router'
import PublisherInheritorRow from './PublisherInheritorRow.vue'

const props = defineProps({
  activity: {
    type: Object,
    required: true
  },
  size: {
    type: String,
    default: 'small',
    validator: (value) => ['large', 'small'].includes(value)
  }
})

const router = useRouter()

const handleClick = () => {
  router.push({
    path: '/front/activityDetail',
    query: {
      id: props.activity.id
    }
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const getStatusText = () => {
  const now = new Date()
  const startTime = new Date(props.activity.startTime)
  const endTime = new Date(props.activity.endTime)

  if (now < startTime) return '即将开始'
  if (now >= startTime && now <= endTime) return '进行中'
  return '已结束'
}

const getStatusClass = () => {
  const now = new Date()
  const startTime = new Date(props.activity.startTime)
  const endTime = new Date(props.activity.endTime)

  if (now < startTime) return 'upcoming'
  if (now >= startTime && now <= endTime) return 'ongoing'
  return 'ended'
}

const plainSnippet = computed(() => {
  const raw = props.activity.address || props.activity.content || ''
  const text = String(raw)
    .replace(/<[^>]*>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  if (!text) return '查看活动详情与报名方式'
  return text.length > 76 ? text.slice(0, 76) + '…' : text
})
</script>

<template>
  <!-- 大图：首页主推活动 -->
  <div
    v-if="size === 'large'"
    class="activity-card activity-card--large"
    @click="handleClick"
  >
    <div class="card-image-wrapper">
      <el-image
        :src="getImageUrl(activity.coverImage)"
        :alt="activity.title"
        type="card"
        class="activity-image"
        fit="cover"
      >
        <template #error>
          <div class="image-error">
            <span>暂无封面</span>
          </div>
        </template>
      </el-image>
      <div class="card-overlay">
        <div class="activity-status" :class="getStatusClass()">
          {{ getStatusText() }}
        </div>
        <div class="activity-meta">
          <div v-if="activity.startTime" class="activity-date">
            {{ formatDate(activity.startTime) }}
          </div>
          <div class="view-count">{{ activity.viewCount || 0 }} 浏览</div>
        </div>
      </div>
    </div>
    <div class="card-content">
      <h3 class="activity-title">{{ activity.title }}</h3>
      <p v-if="activity.content || activity.address" class="activity-description">
        {{ plainSnippet }}
      </p>
      <div v-if="activity.address" class="activity-location">
        {{ activity.address }}
      </div>
      <div class="card-divider" />
      <div class="publisher-wrap" @click.stop>
        <PublisherInheritorRow
          :inheritor-id="activity.creatorId"
          :name="activity.publisherName || '平台'"
          :avatar="activity.publisherAvatar"
          :size="36"
          :show-caption="false"
        />
      </div>
    </div>
  </div>

  <!-- 小图：首页侧栏，横向图文 + 发布人 -->
  <div v-else class="activity-card activity-card--compact" @click="handleClick">
    <div class="compact-thumb">
      <el-image
        :src="getImageUrl(activity.coverImage)"
        :alt="activity.title"
        class="compact-img"
        fit="cover"
      >
        <template #error>
          <div class="compact-img-error">暂无图</div>
        </template>
      </el-image>
      <span class="compact-status" :class="getStatusClass()">{{ getStatusText() }}</span>
    </div>
    <div class="compact-body">
      <h3 class="compact-title">{{ activity.title }}</h3>
      <p class="compact-snippet">{{ plainSnippet }}</p>
      <div class="compact-meta">
        <span v-if="activity.startTime">{{ formatDate(activity.startTime) }}</span>
        <span>{{ activity.viewCount || 0 }} 浏览</span>
      </div>
      <div class="compact-publisher" @click.stop>
        <PublisherInheritorRow
          :inheritor-id="activity.creatorId"
          :name="activity.publisherName || '平台'"
          :avatar="activity.publisherAvatar"
          :size="28"
          :show-caption="false"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.activity-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
  box-shadow: 0 4px 18px rgba(31, 26, 23, 0.08);
  border: 1px solid rgba(31, 26, 23, 0.06);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 14px 36px rgba(31, 26, 23, 0.12);
    border-color: rgba(197, 61, 46, 0.25);
  }
}

.activity-card--large {
  height: 100%;

  .card-image-wrapper {
    position: relative;
    height: 280px;
    overflow: hidden;
    background: linear-gradient(145deg, #efe6dc 0%, #e0d4c8 100%);

    .activity-image {
      width: 100%;
      height: 100%;
      transition: transform 0.35s ease;
    }

    .card-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(to bottom, rgba(0, 0, 0, 0.15) 0%, transparent 45%, rgba(0, 0, 0, 0.55) 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 12px;
      pointer-events: none;
    }

    &:hover .card-overlay {
      opacity: 1;
    }

    &:hover .activity-image {
      transform: scale(1.04);
    }
  }

  .activity-status {
    align-self: flex-end;
    padding: 5px 12px;
    border-radius: 999px;
    font-size: 11px;
    font-weight: 700;
    color: #fff;

    &.upcoming {
      background: rgba(41, 98, 255, 0.92);
    }
    &.ongoing {
      background: rgba(46, 160, 67, 0.92);
    }
    &.ended {
      background: rgba(90, 90, 90, 0.88);
    }
  }

  .activity-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
  }

  .activity-date,
  .view-count {
    background: rgba(255, 255, 255, 0.95);
    color: #444;
    padding: 5px 10px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 600;
  }

  .card-content {
    padding: 18px 20px 16px;
  }

  .activity-title {
    font-size: 18px;
    font-weight: 800;
    color: #1f1a17;
    margin: 0 0 10px;
    line-height: 1.35;
  }

  .activity-description {
    font-size: 13px;
    color: #5c534c;
    line-height: 1.55;
    margin: 0 0 10px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .activity-location {
    font-size: 12px;
    color: #a82c28;
    font-weight: 600;
    margin-bottom: 4px;
  }

  .card-divider {
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(31, 26, 23, 0.08), transparent);
    margin: 14px 0 10px;
  }

  .publisher-wrap :deep(.publisher-name) {
    font-size: 13px;
  }
}

.activity-card--compact {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  min-height: 112px;
  text-align: left;

  &:hover {
    transform: translateY(-2px);
  }

  .compact-thumb {
    position: relative;
    width: 118px;
    flex-shrink: 0;
    background: #f0ebe6;
  }

  .compact-img {
    width: 100%;
    height: 100%;
    min-height: 112px;
    display: block;
  }

  .compact-img-error {
    min-height: 112px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: #8a8279;
    background: #ebe4de;
  }

  .compact-status {
    position: absolute;
    bottom: 8px;
    left: 8px;
    right: 8px;
    text-align: center;
    padding: 3px 6px;
    font-size: 10px;
    font-weight: 700;
    color: #fff;
    border-radius: 8px;
    line-height: 1.2;

    &.upcoming {
      background: rgba(41, 98, 255, 0.9);
    }
    &.ongoing {
      background: rgba(46, 160, 67, 0.9);
    }
    &.ended {
      background: rgba(70, 70, 70, 0.88);
    }
  }

  .compact-body {
    flex: 1;
    min-width: 0;
    padding: 12px 14px 10px 12px;
    display: flex;
    flex-direction: column;
  }

  .compact-title {
    margin: 0 0 6px;
    font-size: 15px;
    font-weight: 800;
    color: #1f1a17;
    line-height: 1.35;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .compact-snippet {
    margin: 0 0 8px;
    font-size: 12px;
    color: #6b645c;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    flex: 1;
  }

  .compact-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    font-size: 11px;
    color: #8a8279;
    font-weight: 600;
    margin-bottom: 6px;
  }

  .compact-publisher {
    margin-top: auto;
    padding-top: 8px;
    border-top: 1px solid rgba(31, 26, 23, 0.07);
  }

  .compact-publisher :deep(.publisher-name) {
    font-size: 12px;
  }
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  min-height: 200px;
  background: #ebe4de;
  color: #7a726a;
  font-size: 13px;
}

@media (max-width: 768px) {
  .activity-card--large .card-image-wrapper {
    height: 220px;
  }
}
</style>
