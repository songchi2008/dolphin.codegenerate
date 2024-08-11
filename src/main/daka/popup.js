document.getElementById('save').addEventListener('click', () => {
  const clockInTime = document.getElementById('clockInTime').value;
  const clockOutTime = document.getElementById('clockOutTime').value;

  chrome.storage.sync.set({ clockInTime, clockOutTime }, () => {
    alert('设置已保存！');

    // 通知背景脚本更新定时器
    chrome.runtime.sendMessage({ action: 'updateAlarms' });
  });
});

// 加载当前设置
chrome.storage.sync.get(['clockInTime', 'clockOutTime'], (items) => {
  document.getElementById('clockInTime').value = items.clockInTime || '08:55';
  document.getElementById('clockOutTime').value = items.clockOutTime || '18:00';
});
