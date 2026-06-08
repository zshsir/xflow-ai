import { Quadrant, type QuadrantMeta as QuadrantMetaType } from '@/types/todo'

export const QuadrantMeta: Record<Quadrant, QuadrantMetaType> = {
  [Quadrant.IMPORTANT_URGENT]: {
    id: Quadrant.IMPORTANT_URGENT,
    name: '重要且紧急',
    description: '立即处理',
    color: '#F56C6C',
    icon: 'Lightning',
  },
  [Quadrant.IMPORTANT_NOT_URGENT]: {
    id: Quadrant.IMPORTANT_NOT_URGENT,
    name: '重要不紧急',
    description: '计划处理',
    color: '#409EFF',
    icon: 'Calendar',
  },
  [Quadrant.NOT_IMPORTANT_URGENT]: {
    id: Quadrant.NOT_IMPORTANT_URGENT,
    name: '紧急不重要',
    description: '快速处理',
    color: '#E6A23C',
    icon: 'Clock',
  },
  [Quadrant.NOT_IMPORTANT_NOT_URGENT]: {
    id: Quadrant.NOT_IMPORTANT_NOT_URGENT,
    name: '不紧急不重要',
    description: '可委托或删除',
    color: '#909399',
    icon: 'Delete',
  },
}

export const QUADRANT_ORDER = [
  Quadrant.IMPORTANT_URGENT,
  Quadrant.IMPORTANT_NOT_URGENT,
  Quadrant.NOT_IMPORTANT_URGENT,
  Quadrant.NOT_IMPORTANT_NOT_URGENT,
]