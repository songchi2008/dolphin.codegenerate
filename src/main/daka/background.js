function scheduleAlarms() {
  chrome.storage.sync.get(['clockInTime', 'clockOutTime'], (items) => {
    const clockInTime = items.clockInTime || '08:55';
    const clockOutTime = items.clockOutTime || '18:00';

    const now = new Date();
    
    // 设置上班时间
    const clockInAlarm = new Date();
    const [clockInHour, clockInMinute] = clockInTime.split(':').map(Number);
    clockInAlarm.setHours(clockInHour);
    clockInAlarm.setMinutes(clockInMinute);
    clockInAlarm.setSeconds(0);

    // 如果上班时间已经过去了，设置为明天
    if (clockInAlarm <= now) {
      clockInAlarm.setDate(now.getDate() + 1);
    }

    // 设置下班时间
    const clockOutAlarm = new Date();
    const [clockOutHour, clockOutMinute] = clockOutTime.split(':').map(Number);
    clockOutAlarm.setHours(clockOutHour);
    clockOutAlarm.setMinutes(clockOutMinute);
    clockOutAlarm.setSeconds(0);

    // 如果下班时间已经过去了，设置为明天
    if (clockOutAlarm <= now) {
      clockOutAlarm.setDate(now.getDate() + 1);
    }

    chrome.alarms.create('clockIn', { when: clockInAlarm.getTime(), periodInMinutes: 1440 });
    chrome.alarms.create('clockOut', { when: clockOutAlarm.getTime(), periodInMinutes: 1440 });
  });
}

chrome.alarms.onAlarm.addListener((alarm) => {
  chrome.notifications.create({
    type: 'basic',
    iconUrl: 'icon.png',
    title: '打卡提醒',
    message: `该打卡了！时间: ${alarm.name}`
  });
});

chrome.runtime.onStartup.addListener(scheduleAlarms);
chrome.runtime.onInstalled.addListener(scheduleAlarms);

chrome.runtime.onMessage.addListener((message) => {
  if (message.action === 'updateAlarms') {
    scheduleAlarms();
  }
});
