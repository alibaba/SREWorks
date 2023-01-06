// @extract组件名:CarouselCompFour 
import CarouselCompTest from './TemplateComponent/index.js';

// @extract组件meta:CarouselCompTestMeta 
import CarouselCompTestMeta from './TemplateComponent/meta';

// @extract导出命名格式 “组件名”+“meta”，便于挂载底座window后识别
export { CarouselCompTest, CarouselCompTestMeta }