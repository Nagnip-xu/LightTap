const stage = document.querySelector('#demo-stage');
const targetsHost = document.querySelector('#targets');
const toolbar = document.querySelector('#toolbar');
const play = document.querySelector('#play-demo');
const add = document.querySelector('#add-target');
const remove = document.querySelector('#remove-target');
const speed = document.querySelector('#demo-speed');
const counter = document.querySelector('#tap-count');
const status = document.querySelector('#demo-status');
const instruction = document.querySelector('#demo-instruction');
const cursor = document.querySelector('#demo-cursor');
const connection = document.querySelector('#connection');
const modes = [...document.querySelectorAll('[data-mode]')];
const initialPoints = [{x: .26, y: .65}, {x: .5, y: .78}, {x: .76, y: .59}];
let points = initialPoints.map(point => ({...point}));
let mode = 'multi';
let timer = null;
let running = false;
let total = 0;
let current = 0;
let toolbarPosition = {x: .5, y: .3};
let activeDrag = null;
const roundLimit = 5;
const clamp = (value, min, max) => Math.max(min, Math.min(max, value));
const visibleCount = () => mode === 'single' ? 1 : points.length;

function pointPosition(index) {
  return mode === 'single' ? {...points[0], x: points[0].x === initialPoints[0].x ? .5 : points[0].x} : points[index];
}

function updatePath() {
  const {width, height} = stage.getBoundingClientRect();
  const d = points.slice(0, visibleCount()).map((_, index) => {
    const point = pointPosition(index);
    return `${index ? 'L' : 'M'}${point.x * width},${point.y * height}`;
  }).join(' ');
  connection.setAttribute('d', d);
}

function positionToolbar() {
  const {width, height} = stage.getBoundingClientRect();
  const half = toolbar.offsetWidth / 2 + 8;
  toolbarPosition.x = clamp(toolbarPosition.x, half / width, 1 - half / width);
  toolbarPosition.y = clamp(toolbarPosition.y, 44 / height, 1 - 60 / height);
  toolbar.style.left = `${toolbarPosition.x * 100}%`;
  toolbar.style.top = `${toolbarPosition.y * 100}%`;
}

function renderTargets() {
  targetsHost.replaceChildren();
  points.slice(0, visibleCount()).forEach((_, index) => {
    const point = pointPosition(index);
    const button = document.createElement('button');
    button.className = 'tap-target';
    button.textContent = String(index + 1);
    button.style.left = `${point.x * 100}%`;
    button.style.top = `${point.y * 100}%`;
    button.dataset.index = String(index);
    button.disabled = running;
    button.setAttribute('aria-label', `点击位置 ${index + 1}，拖动或使用方向键移动`);
    button.title = `拖动位置 ${index + 1}`;
    button.addEventListener('pointerdown', event => beginDrag(event, 'point', index));
    button.addEventListener('keydown', event => moveWithKeyboard(event, 'point', index));
    targetsHost.append(button);
  });
  updatePath();
  updateControls();
}

function updateControls() {
  add.disabled = running || mode === 'single' || points.length >= 10;
  remove.disabled = running || mode === 'single' || points.length <= 1;
  speed.disabled = running;
  modes.forEach(button => {
    button.disabled = running;
    button.setAttribute('aria-pressed', String(button.dataset.mode === mode));
  });
  play.setAttribute('aria-label', running ? '停止演示' : '开始演示');
  play.title = running ? '停止演示' : '开始演示';
  play.querySelector('use').setAttribute('href', running ? '#i-stop' : '#i-play');
  stage.classList.toggle('running', running);
}

function finish(message) {
  clearTimeout(timer);
  timer = null;
  running = false;
  targetsHost.querySelectorAll('.tap-target').forEach(button => {
    button.disabled = false;
    button.classList.remove('active');
  });
  instruction.textContent = total ? '可以调整位置，再试一次' : '拖动编号调整位置，再点播放';
  status.textContent = message;
  updateControls();
}

function tick() {
  if (!running) return;
  const count = visibleCount();
  const index = current % count;
  const point = pointPosition(index);
  const buttons = [...targetsHost.querySelectorAll('.tap-target')];
  buttons.forEach(button => button.classList.remove('active'));
  const target = buttons[index];
  // Restart the finite click pulse when a single target repeats.
  void target.offsetWidth;
  target.classList.add('active');
  cursor.style.left = `${point.x * 100}%`;
  cursor.style.top = `${point.y * 100}%`;
  total += 1;
  current += 1;
  counter.textContent = String(total);
  instruction.textContent = `正在点击位置 ${index + 1} · 第 ${Math.ceil(current / count)} / ${roundLimit} 轮`;
  if (current >= count * roundLimit) {
    timer = setTimeout(() => finish(`已完成 ${roundLimit} 轮，共 ${total} 次点击。再试试不同的位置和节奏。`), 300);
  } else timer = setTimeout(tick, Number(speed.value));
}

play.addEventListener('click', () => {
  if (running) {
    finish(`已停止，共点击 ${total} 次。点击播放可以重新开始。`);
    return;
  }
  total = 0;
  current = 0;
  counter.textContent = '0';
  running = true;
  status.textContent = '正在网页内演示，点击方形按钮可随时停止。';
  renderTargets();
  updateControls();
  tick();
});

add.addEventListener('click', () => {
  if (add.disabled) return;
  const n = points.length;
  points.push({x: .16 + (n % 5) * .17, y: .58 + Math.floor(n / 5) * .2});
  renderTargets();
  status.textContent = `已添加位置 ${points.length}，可以拖动调整。`;
});

remove.addEventListener('click', () => {
  if (remove.disabled) return;
  points.pop();
  renderTargets();
  status.textContent = `还剩 ${points.length} 个点击位置。`;
});

document.querySelector('#reset-demo').addEventListener('click', () => {
  finish('演示已重置。拖动编号定位，再点击播放。');
  points = initialPoints.map(point => ({...point}));
  mode = 'multi';
  total = 0;
  current = 0;
  counter.textContent = '0';
  toolbarPosition = {x: .5, y: .3};
  speed.value = '600';
  document.querySelector('#speed-value').textContent = '600 毫秒';
  positionToolbar();
  renderTargets();
  instruction.textContent = '拖动编号调整位置，再点播放';
});

modes.forEach(button => button.addEventListener('click', () => {
  if (running) return;
  mode = button.dataset.mode;
  total = 0;
  counter.textContent = '0';
  status.textContent = mode === 'single' ? '单点演示：同一位置点击 5 次。' : `多点演示：按编号依次点击，共 ${roundLimit} 轮。`;
  instruction.textContent = '拖动编号调整位置，再点播放';
  renderTargets();
}));

speed.addEventListener('input', () => {
  document.querySelector('#speed-value').textContent = `${speed.value} 毫秒`;
});

function beginDrag(event, kind, index = 0) {
  if (event.button !== 0 || (running && kind === 'point')) return;
  const point = kind === 'toolbar' ? toolbarPosition : pointPosition(index);
  const bounds = stage.getBoundingClientRect();
  activeDrag = {
    kind, index, pointerId: event.pointerId, element: event.currentTarget,
    offsetX: event.clientX - (bounds.left + point.x * bounds.width),
    offsetY: event.clientY - (bounds.top + point.y * bounds.height)
  };
  event.currentTarget.setPointerCapture(event.pointerId);
  event.preventDefault();
}

function moveDrag(event) {
  if (!activeDrag || activeDrag.pointerId !== event.pointerId) return;
  const bounds = stage.getBoundingClientRect();
  const x = (event.clientX - bounds.left - activeDrag.offsetX) / bounds.width;
  const y = (event.clientY - bounds.top - activeDrag.offsetY) / bounds.height;
  if (activeDrag.kind === 'toolbar') {
    toolbarPosition = {x, y};
    positionToolbar();
  } else {
    const point = {x: clamp(x, 32 / bounds.width, 1 - 32 / bounds.width), y: clamp(y, 165 / bounds.height, 1 - 65 / bounds.height)};
    points[activeDrag.index] = point;
    activeDrag.element.style.left = `${point.x * 100}%`;
    activeDrag.element.style.top = `${point.y * 100}%`;
    updatePath();
  }
}

function endDrag(event) {
  if (activeDrag?.pointerId === event.pointerId) activeDrag = null;
}

function moveWithKeyboard(event, kind, index = 0) {
  const arrows = {ArrowLeft: [-1, 0], ArrowRight: [1, 0], ArrowUp: [0, -1], ArrowDown: [0, 1]};
  if (!arrows[event.key] || (running && kind === 'point')) return;
  event.preventDefault();
  const bounds = stage.getBoundingClientRect();
  const point = kind === 'toolbar' ? toolbarPosition : pointPosition(index);
  const delta = arrows[event.key];
  const x = point.x + delta[0] * 12 / bounds.width;
  const y = point.y + delta[1] * 12 / bounds.height;
  if (kind === 'toolbar') {
    toolbarPosition = {x, y};
    positionToolbar();
  } else {
    points[index] = {x: clamp(x, 32 / bounds.width, 1 - 32 / bounds.width), y: clamp(y, 165 / bounds.height, 1 - 65 / bounds.height)};
    event.currentTarget.style.left = `${points[index].x * 100}%`;
    event.currentTarget.style.top = `${points[index].y * 100}%`;
    updatePath();
  }
}

const handle = document.querySelector('#drag-toolbar');
handle.addEventListener('pointerdown', event => beginDrag(event, 'toolbar'));
handle.addEventListener('keydown', event => moveWithKeyboard(event, 'toolbar'));
stage.addEventListener('pointermove', moveDrag);
stage.addEventListener('pointerup', endDrag);
stage.addEventListener('pointercancel', endDrag);
stage.addEventListener('lostpointercapture', endDrag);

new ResizeObserver(() => { positionToolbar(); updatePath(); }).observe(stage);
new IntersectionObserver(entries => {
  if (running && !entries[0].isIntersecting) finish(`演示已停止，共点击 ${total} 次。回到这里后可以重新开始。`);
}, {threshold: 0}).observe(stage);
document.addEventListener('visibilitychange', () => {
  if (document.hidden && running) finish(`演示已停止，共点击 ${total} 次。`);
});

document.querySelectorAll('[data-screen]').forEach(button => button.addEventListener('click', () => {
  const dark = button.dataset.screen === 'dark';
  const image = document.querySelector('#app-screen');
  image.src = dark ? './assets/app-dark.webp' : './assets/app-light.webp';
  image.alt = dark ? '轻点深色设置界面：圆角结束条件菜单，当前选择运行指定时长' : '轻点浅色主界面：权限状态、点击模式和点击节奏设置';
  document.querySelector('#screen-label').textContent = dark ? '深色设置 · 实际截图' : '应用实际界面';
  document.querySelectorAll('[data-screen]').forEach(option => option.setAttribute('aria-pressed', String(option === button)));
}));

renderTargets();
positionToolbar();
