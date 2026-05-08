<script setup lang="ts">
import { computed } from 'vue'
import { ElAvatar } from 'element-plus'
import { getImageUrl } from '../utils/system'

const props = withDefaults(
  defineProps<{
    inheritorId?: number | null
    name?: string
    avatar?: string | null
    size?: number
    showCaption?: boolean
  }>(),
  {
    size: 40,
    showCaption: true,
    name: '平台'
  }
)

const px = computed(() => props.size)
const src = computed(() => (props.avatar ? getImageUrl(props.avatar) : undefined))
const displayName = computed(() => props.name || '平台')
const initial = computed(() => displayName.value.charAt(0) || '平')
</script>

<template>
  <div class="publisher-inheritor-row">
    <router-link
      v-if="inheritorId"
      :to="'/front/inheritor/' + inheritorId"
      class="avatar-link"
      :title="'查看「' + displayName + '」主页'"
      @click.stop
    >
      <el-avatar :size="px" :src="src" class="publisher-avatar">
        {{ initial }}
      </el-avatar>
    </router-link>
    <div v-else class="avatar-static" aria-hidden="true">
      <el-avatar :size="px" :src="src" class="publisher-avatar">
        {{ initial }}
      </el-avatar>
    </div>
    <div class="publisher-text" :class="{ 'no-caption': !showCaption }">
      <span v-if="showCaption" class="publisher-label">发布人</span>
      <span class="publisher-name">{{ displayName }}</span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.publisher-inheritor-row {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.avatar-link {
  flex-shrink: 0;
  line-height: 0;
  border-radius: 50%;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: scale(1.06);
    box-shadow: 0 4px 14px rgba(197, 61, 46, 0.35);
  }

  &:focus-visible {
    outline: 2px solid #c53d2e;
    outline-offset: 2px;
  }
}

.avatar-static {
  flex-shrink: 0;
  line-height: 0;
  opacity: 0.9;
}

.publisher-avatar {
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.publisher-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;

  &.no-caption {
    flex-direction: row;
    align-items: center;
    gap: 0;
  }
}

.publisher-label {
  font-size: 11px;
  font-weight: 600;
  color: #5c534c;
  letter-spacing: 0.04em;
}

.publisher-name {
  font-size: 15px;
  font-weight: 700;
  color: #1f1a17;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-caption .publisher-name {
  font-size: 14px;
  font-weight: 600;
}
</style>
