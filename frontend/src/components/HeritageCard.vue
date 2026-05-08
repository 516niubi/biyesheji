<template>
  <div class="heritage-card" @click="handleClick">
    <div class="card-image-wrapper">
      <el-image
        :src="heritage.coverImage"
        :alt="heritage.name"
        class="heritage-image"
        fit="cover"
      >
        <template #error>
          <div class="image-error">
            <span>暂无图片</span>
          </div>
        </template>
      </el-image>
      <div class="image-shade" />
      <span v-if="heritage.categoryName" class="category-pill">{{ heritage.categoryName }}</span>
      <div class="view-badge">{{ heritage.viewCount ?? 0 }} 浏览</div>
    </div>
    <div class="card-body">
      <h3 class="heritage-name">{{ heritage.name }}</h3>
      <p v-if="heritage.intro" class="heritage-intro">{{ heritage.intro }}</p>
      <div class="card-divider" />
      <div class="publisher-wrap" @click.stop>
        <PublisherInheritorRow
          :inheritor-id="heritage.creatorId"
          :name="heritage.publisherName || '平台'"
          :avatar="heritage.publisherAvatar"
          :size="36"
          :show-caption="false"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElImage } from 'element-plus'
import { useRouter } from 'vue-router'
import PublisherInheritorRow from './PublisherInheritorRow.vue'

const props = defineProps({
  heritage: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const handleClick = () => {
  router.push({
    path: '/front/heritageDetail',
    query: {
      id: props.heritage.id
    }
  })
}
</script>

<style lang="scss" scoped>
.heritage-card {
  position: relative;
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.28s ease, box-shadow 0.28s ease;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(31, 26, 23, 0.06);

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.14);
    border-color: rgba(197, 61, 46, 0.25);

    .heritage-image {
      transform: scale(1.04);
    }
  }
}

.card-image-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(145deg, #e8e0d8 0%, #c9bfb5 100%);
}

.heritage-image {
  width: 100%;
  height: 100%;
  transition: transform 0.45s ease;
  display: block;
}

.image-shade {
  pointer-events: none;
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(15, 12, 10, 0.55) 0%, transparent 45%);
}

.category-pill {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  padding: 5px 11px;
  font-size: 12px;
  font-weight: 600;
  color: #1f1a17;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 999px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  max-width: calc(100% - 100px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.view-badge {
  position: absolute;
  bottom: 12px;
  right: 12px;
  z-index: 2;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 999px;
  backdrop-filter: blur(8px);
}

.card-body {
  padding: 18px 18px 16px;
}

.heritage-name {
  font-size: 17px;
  font-weight: 800;
  color: #1f1a17;
  margin: 0 0 8px;
  line-height: 1.35;
  letter-spacing: -0.02em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.heritage-intro {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: #5c534c;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.4em;
}

.card-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(31, 26, 23, 0.1), transparent);
  margin: 14px 0 12px;
}

.publisher-wrap {
  padding-top: 2px;

  :deep(.publisher-inheritor-row) {
    align-items: center;
  }

  :deep(.publisher-name) {
    font-size: 13px;
    font-weight: 600;
    color: #3d3833;
  }

  :deep(.publisher-avatar) {
    width: 36px !important;
    height: 36px !important;
  }
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  min-height: 160px;
  color: #7a726a;
  font-size: 13px;
}

@media (max-width: 768px) {
  .card-image-wrapper {
    height: 168px;
  }

  .heritage-name {
    font-size: 16px;
  }
}
</style>
