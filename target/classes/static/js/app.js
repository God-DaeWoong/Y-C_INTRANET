// Global variables
let calendar;
let currentMembers = [];
let currentEvents = [];
let selectedEvent = null;
let currentDepartments = [];
let selectedParentDeptId = null;
let selectedChildDeptId = null;
let currentView = 'calendar'; // 'calendar' or 'team'
let pendingApprovals = [];

// Korean public holidays (2025-2026)
const koreanHolidays = {
    '2025-01-01': '신정',
    '2025-01-28': '설날',
    '2025-01-29': '설날',
    '2025-01-30': '설날',
    '2025-03-01': '삼일절',
    '2025-05-05': '어린이날',
    '2025-05-06': '대체공휴일',
    '2025-06-06': '현충일',
    '2025-08-15': '광복절',
    '2025-10-03': '개천절',
    '2025-10-05': '추석',
    '2025-10-06': '추석',
    '2025-10-07': '추석',
    '2025-10-08': '대체공휴일',
    '2025-10-09': '한글날',
    '2025-12-25': '크리스마스',
    '2026-01-01': '신정',
    '2026-02-16': '설날',
    '2026-02-17': '설날',
    '2026-02-18': '설날',
    '2026-03-01': '삼일절',
    '2026-05-05': '어린이날',
    '2026-05-25': '부처님오신날',
    '2026-06-06': '현충일',
    '2026-08-15': '광복절',
    '2026-09-24': '추석',
    '2026-09-25': '추석',
    '2026-09-26': '추석',
    '2026-10-03': '개천절',
    '2026-10-09': '한글날',
    '2026-12-25': '크리스마스'
};

// Initialize app
document.addEventListener('DOMContentLoaded', async function() {
    await loadDepartments();
    await loadMembers();
    initCalendar();
    initEventHandlers();
    await loadEvents();
    await loadPendingApprovals();
    initMyScheduleView();
});

// Load departments
async function loadDepartments() {
    try {
        currentDepartments = await API.getDepartments();
        // populateParentDeptSelect() removed - using mySchedule selects instead
    } catch (error) {
        console.error('Failed to load departments:', error);
        alert('부서 목록을 불러오는데 실패했습니다.');
    }
}

// Load members
async function loadMembers() {
    try {
        currentMembers = await API.getMembers(true);
        // populateMemberSelects() removed - using mySchedule selects instead
    } catch (error) {
        console.error('Failed to load members:', error);
        alert('멤버 목록을 불러오는데 실패했습니다.');
    }
}

// Populate parent department select
function populateParentDeptSelect() {
    const select = document.getElementById('parentDeptSelect');
    select.innerHTML = '<option value="">전체 본부</option>';

    const topLevelDepts = currentDepartments.filter(d => d.parentId === null || d.depth === 0);
    topLevelDepts.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.id;
        option.textContent = dept.name;
        select.appendChild(option);
    });
}

// Populate child department select
function populateChildDeptSelect(parentId) {
    const select = document.getElementById('childDeptSelect');
    select.innerHTML = '<option value="">전체 팀</option>';

    if (!parentId) {
        return;
    }

    const childDepts = currentDepartments.filter(d => d.parentId == parentId);
    childDepts.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.id;
        option.textContent = dept.name;
        select.appendChild(option);
    });
}

// Populate member select dropdowns
function populateMemberSelects() {
    // 헤더의 멤버 선택
    const memberSelect = document.getElementById('memberSelect');
    const currentValue = memberSelect.value;

    memberSelect.innerHTML = '<option value="">전체 멤버</option>';

    let filteredMembers = currentMembers;

    // 하위 부서가 선택되었으면 해당 부서 멤버만
    if (selectedChildDeptId) {
        filteredMembers = currentMembers.filter(m => m.departmentId == selectedChildDeptId);
    }
    // 상위 부서만 선택되었으면 해당 본부 전체 멤버
    else if (selectedParentDeptId) {
        const childDeptIds = currentDepartments
            .filter(d => d.parentId == selectedParentDeptId)
            .map(d => d.id);
        filteredMembers = currentMembers.filter(m => childDeptIds.includes(m.departmentId));
    }

    filteredMembers.forEach(member => {
        const option = document.createElement('option');
        option.value = member.id;
        option.textContent = `${member.name} (${member.department})`;
        memberSelect.appendChild(option);
    });

    if (currentValue && filteredMembers.find(m => m.id == currentValue)) {
        memberSelect.value = currentValue;
    }
}

// Populate event member select (for event modal)
function populateEventMemberSelect(selectedParentId = null, selectedChildId = null) {
    const select = document.getElementById('eventMember');
    select.innerHTML = '<option value="">선택하세요</option>';

    let filteredMembers = currentMembers;

    // 하위 부서가 선택되었으면 해당 부서 멤버만
    if (selectedChildId) {
        filteredMembers = currentMembers.filter(m => m.departmentId == selectedChildId);
    }
    // 상위 부서만 선택되었으면 해당 본부 전체 멤버
    else if (selectedParentId) {
        const childDeptIds = currentDepartments
            .filter(d => d.parentId == selectedParentId)
            .map(d => d.id);
        filteredMembers = currentMembers.filter(m => childDeptIds.includes(m.departmentId));
    }

    if (filteredMembers.length === 0) {
        select.innerHTML = '<option value="">해당 부서에 멤버가 없습니다</option>';
        return;
    }

    filteredMembers.forEach(member => {
        const option = document.createElement('option');
        option.value = member.id;
        option.textContent = `${member.name} (${member.department || '부서 없음'})`;
        select.appendChild(option);
    });
}

// Initialize FullCalendar
function initCalendar() {
    const calendarEl = document.getElementById('calendar');

    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'ko',
        height: 880,
        selectable: true,
        selectMirror: true,
        slotMinTime: '08:00:00',
        slotMaxTime: '20:00:00',
        slotDuration: '00:30:00',
        displayEventTime: true,
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,listMonth'
        },
        buttonText: {
            today: '오늘',
            month: '월',
            week: '주',
            list: '목록'
        },
        events: [],
        eventClick: function(info) {
            handleEventClick(info.event);
        },
        dateClick: function(info) {
            // Check if this is a time slot click (week view) or date click (month view)
            const clickedDate = new Date(info.date);
            let startStr, endStr, startTime, endTime;

            // Check if the dateStr contains time information (e.g., "2025-12-31T13:00:00+09:00")
            if (info.dateStr.includes('T')) {
                // Time slot click from week view
                const year = clickedDate.getFullYear();
                const month = String(clickedDate.getMonth() + 1).padStart(2, '0');
                const day = String(clickedDate.getDate()).padStart(2, '0');
                startStr = `${year}-${month}-${day}`;
                endStr = startStr;

                const startHours = String(clickedDate.getHours()).padStart(2, '0');
                const startMinutes = String(clickedDate.getMinutes()).padStart(2, '0');
                startTime = `${startHours}:${startMinutes}`;

                // Default to 30 minutes duration
                const endDate = new Date(clickedDate.getTime() + 30 * 60000);
                const endHours = String(endDate.getHours()).padStart(2, '0');
                const endMinutes = String(endDate.getMinutes()).padStart(2, '0');
                endTime = `${endHours}:${endMinutes}`;
            } else {
                // Date click from month view
                startStr = info.dateStr;
                endStr = info.dateStr;
                startTime = null;
                endTime = null;
            }

            openEventModal(null, startStr, endStr, startTime, endTime);
        },
        select: function(info) {
            // Check if this is a time selection (has time component)
            const hasTime = !info.allDay;

            let startStr, endStr, startTime, endTime;

            if (hasTime) {
                // Time selection from week view - extract date and time separately
                const start = new Date(info.start);
                const end = new Date(info.end);

                // Format date as yyyy-MM-dd
                const year = start.getFullYear();
                const month = String(start.getMonth() + 1).padStart(2, '0');
                const day = String(start.getDate()).padStart(2, '0');
                startStr = `${year}-${month}-${day}`;
                endStr = startStr; // Same day for time selections

                // Format time as HH:MM
                const startHours = String(start.getHours()).padStart(2, '0');
                const startMinutes = String(start.getMinutes()).padStart(2, '0');
                const endHours = String(end.getHours()).padStart(2, '0');
                const endMinutes = String(end.getMinutes()).padStart(2, '0');
                startTime = `${startHours}:${startMinutes}`;
                endTime = `${endHours}:${endMinutes}`;
            } else {
                // Date selection from month view
                const start = new Date(info.start);
                const end = new Date(info.end);

                // FullCalendar provides exclusive end date, so subtract 1 day
                end.setDate(end.getDate() - 1);

                // Format dates as yyyy-MM-dd in local timezone
                const startYear = start.getFullYear();
                const startMonth = String(start.getMonth() + 1).padStart(2, '0');
                const startDay = String(start.getDate()).padStart(2, '0');
                startStr = `${startYear}-${startMonth}-${startDay}`;

                const endYear = end.getFullYear();
                const endMonth = String(end.getMonth() + 1).padStart(2, '0');
                const endDay = String(end.getDate()).padStart(2, '0');
                endStr = `${endYear}-${endMonth}-${endDay}`;

                startTime = null;
                endTime = null;
            }

            openEventModal(null, startStr, endStr, startTime, endTime);
            calendar.unselect();
        },
        dayCellDidMount: function(info) {
            // Format date in local timezone (not UTC)
            const year = info.date.getFullYear();
            const month = String(info.date.getMonth() + 1).padStart(2, '0');
            const day = String(info.date.getDate()).padStart(2, '0');
            const dateStr = `${year}-${month}-${day}`;
            const dayOfWeek = info.date.getDay();

            // Add holiday class and name if it's a Korean holiday
            if (koreanHolidays[dateStr]) {
                info.el.classList.add('holiday');
                const dayTop = info.el.querySelector('.fc-daygrid-day-top');
                if (dayTop) {
                    const holidaySpan = document.createElement('span');
                    holidaySpan.className = 'holiday-name';
                    holidaySpan.style.fontSize = '0.8em';
                    holidaySpan.style.textAlign = 'center';
                    holidaySpan.style.lineHeight = '2.5em';
                    holidaySpan.style.paddingRight = '4.5px';
                    holidaySpan.style.color = '#d32f2f';
                    holidaySpan.style.fontWeight = 'bold';
                    holidaySpan.style.display = 'block';
                    holidaySpan.textContent = `(${koreanHolidays[dateStr]})`;
                    dayTop.insertBefore(holidaySpan, dayTop.firstChild);
                }

                // Apply red color to holiday date number
                const dayNumber = info.el.querySelector('a.fc-daygrid-day-number');
                if (dayNumber) {
                    dayNumber.style.setProperty('color', '#d32f2f', 'important');
                    dayNumber.style.setProperty('font-weight', '700', 'important');
                }
            }

            // Add weekend classes and apply colors
            if (dayOfWeek === 0) { // Sunday
                info.el.classList.add('fc-day-sun');
                const dayNumber = info.el.querySelector('a.fc-daygrid-day-number');
                if (dayNumber) {
                    dayNumber.style.setProperty('color', '#d32f2f', 'important');
                    dayNumber.style.setProperty('font-weight', '600', 'important');
                }
            } else if (dayOfWeek === 6) { // Saturday
                info.el.classList.add('fc-day-sat');
                const dayNumber = info.el.querySelector('a.fc-daygrid-day-number');
                if (dayNumber) {
                    dayNumber.style.setProperty('color', '#1976d2', 'important');
                    dayNumber.style.setProperty('font-weight', '600', 'important');
                }
            }
        },
        eventDidMount: function(info) {
            // Add tooltip to show full event title
            const eventTitle = info.event.title;
            info.el.setAttribute('title', eventTitle);

            // Make event title fully visible on hover
            info.el.style.position = 'relative';

            // Get event status and type
            const eventType = info.event.extendedProps.eventType;
            const eventStatus = info.event.extendedProps.status;

            // Determine event color based on status and type
            let eventColor = '';
            let eventClass = '';

            // Apply background color for REJECTED or CANCELED status first
            if (eventStatus === 'REJECTED' || eventStatus === 'CANCELED') {
                eventColor = 'rgba(76, 91, 106, 0.5)';
                eventClass = 'event-rejected';
                info.el.style.backgroundColor = eventColor;
                info.el.style.color = 'white';
                info.el.style.borderLeft = '4px solid rgb(231, 76, 60)';
            } else {
                // Apply background color and text color based on event type and subtype
                if (eventType === 'LEAVE') {
                    const eventSubtype = info.event.extendedProps.eventSubtype;
                    if (eventSubtype === '반차') {
                        eventColor = 'rgba(76, 91, 106, 0.5)';
                        eventClass = 'event-half-day';
                        info.el.style.backgroundColor = eventColor;
                        info.el.style.color = 'white';
                        info.el.style.borderColor = eventColor;
                    } else {
                        // 연차 및 기타 휴가
                        eventColor = 'rgb(236, 72, 153)';
                        eventClass = 'event-full-day';
                        info.el.style.backgroundColor = eventColor;
                        info.el.style.color = 'white';
                        info.el.style.borderColor = eventColor;
                    }
                } else if (eventType === 'SCHEDULE') {
                    // 회의
                    eventColor = 'rgb(59, 130, 246)';
                    eventClass = 'event-meeting';
                    info.el.style.backgroundColor = eventColor;
                    info.el.style.color = 'white';
                    info.el.style.borderColor = eventColor;
                }
            }

            // Add event class
            if (eventClass) {
                info.el.classList.add(eventClass);
            }

            // Apply color to list event dot and remove background for list view
            const dot = info.el.querySelector('.fc-list-event-dot');
            if (dot && eventColor) {
                dot.style.backgroundColor = eventColor + ' !important';
                dot.style.borderColor = eventColor + ' !important';
                // Remove background color for list events
                info.el.style.backgroundColor = 'transparent';
            }
        },
        eventContent: function(arg) {
            // Custom event rendering for week view to show member name
            const view = arg.view.type;
            const event = arg.event;
            const eventType = event.extendedProps.eventType;
            const memberName = event.extendedProps.memberName;

            // Only customize for SCHEDULE (회의) events in week view
            if (view === 'timeGridWeek' && eventType === 'SCHEDULE') {
                const timeText = arg.timeText;
                const title = event.title;

                return {
                    html: `
                        <div style="padding: 2px 4px; overflow: hidden;">
                            <div style="font-size: 11px; font-weight: 600;">${timeText}</div>
                            <div style="font-size: 10px; opacity: 0.9;">${memberName || ''}</div>
                            <div style="font-size: 11px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${title}</div>
                        </div>
                    `
                };
            }
            // Default rendering for other cases
            return true;
        },
        dayHeaderDidMount: function(info) {
            // Apply color to week header (일, 월, 화, ...)
            const dayOfWeek = info.date.getDay();
            const headerLink = info.el.querySelector('a.fc-col-header-cell-cushion');

            if (dayOfWeek === 0) { // Sunday
                if (headerLink) {
                    headerLink.style.setProperty('color', '#d32f2f', 'important');
                    headerLink.style.setProperty('font-weight', '600', 'important');
                }
            } else if (dayOfWeek === 6) { // Saturday
                if (headerLink) {
                    headerLink.style.setProperty('color', '#1976d2', 'important');
                    headerLink.style.setProperty('font-weight', '600', 'important');
                }
            }
        }
    });

    calendar.render();


    // Force white color on active buttons, reset inactive buttons
    const applyButtonColors = () => {
        // First, reset all button group buttons to default color
        const allButtons = document.querySelectorAll('.fc-button-group .fc-button, .fc-button-primary');
        allButtons.forEach(btn => {
            if (!btn.classList.contains('fc-button-active')) {
                btn.style.color = ''; // Reset to default
            }
        });

        // Then apply white color only to active buttons
        const activeButtons = document.querySelectorAll('.fc-button-group .fc-button.fc-button-active, .fc-button-primary:not(:disabled).fc-button-active');
        activeButtons.forEach(btn => {
            btn.style.color = '#ffffff';
        });
    };

    // Apply immediately after render using requestAnimationFrame to ensure DOM is ready
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            applyButtonColors();
        });
    });

    // Watch for class changes on all buttons
    setTimeout(() => {
        const buttons = document.querySelectorAll('.fc-button');
        buttons.forEach(button => {
            const observer = new MutationObserver(() => {
                applyButtonColors();
            });
            observer.observe(button, { attributes: true, attributeFilter: ['class'] });

            // Also apply on click
            button.addEventListener('click', () => {
                setTimeout(applyButtonColors, 0);
                setTimeout(applyButtonColors, 50);
            });
        });
    }, 0);

}

// Load events
async function loadEvents() {
    try {
        currentEvents = await API.getEvents();
        displayEvents();

        // Refresh my schedule list if a member is selected
        const selectedMemberId = sessionStorage.getItem('myScheduleMember');
        if (selectedMemberId) {
            loadMyScheduleList(selectedMemberId);
        }
    } catch (error) {
        console.error('Failed to load events:', error);
        alert('일정을 불러오는데 실패했습니다.');
    }
}

// Display events on calendar
function displayEvents() {
    console.log('==== displayEvents 함수 호출됨 ====');
    if (currentView === 'team') {
        displayTeamView();
        return;
    }

    // Get filter values from "내 일정보기"
    const selectedMemberId = sessionStorage.getItem('myScheduleMember');
    const myScheduleChildDept = sessionStorage.getItem('myScheduleChildDept');
    const myScheduleParentDept = sessionStorage.getItem('myScheduleParentDept');

    console.log('선택된 멤버 ID:', selectedMemberId);

    // Update global filter variables
    selectedParentDeptId = myScheduleParentDept || null;
    selectedChildDeptId = myScheduleChildDept || null;

    // Reload pending approvals when member changes
    loadPendingApprovals();

    console.log('전체 이벤트 수:', currentEvents.length);
    console.log('선택된 하위 부서 ID:', selectedChildDeptId);
    console.log('선택된 상위 본부 ID:', selectedParentDeptId);

    let filteredEvents = currentEvents;
    if (selectedMemberId) {
        filteredEvents = currentEvents.filter(e => e.memberId == selectedMemberId);
        console.log('멤버 필터 후 이벤트 수:', filteredEvents.length);
    } else if (selectedChildDeptId) {
        const memberIds = currentMembers
            .filter(m => m.departmentId == selectedChildDeptId)
            .map(m => m.id);
        filteredEvents = currentEvents.filter(e => memberIds.includes(e.memberId));
        console.log('하위 부서 필터 후 이벤트 수:', filteredEvents.length);
    } else if (selectedParentDeptId) {
        const childDeptIds = currentDepartments
            .filter(d => d.parentId == selectedParentDeptId)
            .map(d => d.id);
        const memberIds = currentMembers
            .filter(m => childDeptIds.includes(m.departmentId))
            .map(m => m.id);
        filteredEvents = currentEvents.filter(e => memberIds.includes(e.memberId));
        console.log('상위 본부 필터 후 이벤트 수:', filteredEvents.length);
    } else {
        console.log('전체 본부 - 필터링 없음, 이벤트 수:', filteredEvents.length);
    }

    const calendarEvents = filteredEvents.map(event => {
        // 이벤트 타입 및 상태 아이콘
        let icon = '';
        if (event.eventType === 'SCHEDULE') {
            // 회의/일정은 서류 아이콘
            icon = '📄';
        } else if (event.eventType === 'LEAVE') {
            // 연차는 상태별 아이콘
            if (event.status === 'DRAFT') {
                icon = '📝';
            } else if (event.status === 'SUBMITTED') {
                icon = '⏳';
            } else if (event.status === 'APPROVED') {
                icon = '✅';
            } else if (event.status === 'REJECTED') {
                icon = '❌';
            } else if (event.status === 'CANCELED') {
                icon = '🚫';
            }
        }

        // 시간 정보 추가
        let startDateTime = event.startDate;
        let endDateTime = event.endDate;
        let allDay = true;

        // LEAVE 타입은 항상 all-day로 표시, SCHEDULE 타입만 시간 표시
        if (event.eventType === 'SCHEDULE' && event.startTime && event.endTime) {
            startDateTime = `${event.startDate}T${event.startTime}`;
            endDateTime = `${event.endDate}T${event.endTime}`;
            allDay = false;
        } else {
            // For all-day events, FullCalendar end is exclusive, so add 1 day
            const endDate = new Date(event.endDate);
            endDate.setDate(endDate.getDate() + 1);
            const year = endDate.getFullYear();
            const month = String(endDate.getMonth() + 1).padStart(2, '0');
            const day = String(endDate.getDate()).padStart(2, '0');
            endDateTime = `${year}-${month}-${day}`;
        }

        return {
            id: event.id,
            title: `${icon} ${event.memberName || ''} - ${event.title}`,
            start: startDateTime,
            end: endDateTime,
            allDay: allDay,
            extendedProps: event,
            className: `event-${event.eventType} event-${event.status}`
        };
    });

    calendar.removeAllEvents();
    calendar.addEventSource(calendarEvents);
}

// Display team view
function displayTeamView() {
    const teamViewContainer = document.getElementById('teamView');

    // Get filtered members based on department selection
    let filteredMembers = currentMembers;
    let departmentsToShow = [];

    if (selectedChildDeptId) {
        // Show only selected child department
        const childDept = currentDepartments.find(d => d.id == selectedChildDeptId);
        if (childDept) {
            departmentsToShow = [childDept];
            filteredMembers = currentMembers.filter(m => m.departmentId == selectedChildDeptId);
        }
    } else if (selectedParentDeptId) {
        // Show all child departments of selected parent
        departmentsToShow = currentDepartments.filter(d => d.parentId == selectedParentDeptId);
        const childDeptIds = departmentsToShow.map(d => d.id);
        filteredMembers = currentMembers.filter(m => childDeptIds.includes(m.departmentId));
    } else {
        // Show all child departments (departments with parentId)
        departmentsToShow = currentDepartments.filter(d => d.parentId !== null && d.parentId !== undefined);
        // If no departments have parentId, show all departments
        if (departmentsToShow.length === 0) {
            departmentsToShow = currentDepartments;
        }
    }

    if (departmentsToShow.length === 0) {
        teamViewContainer.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🏢</div>
                <p>표시할 팀이 없습니다.</p>
            </div>
        `;
        return;
    }

    // Group members by department
    const html = departmentsToShow.map(dept => {
        const deptMembers = filteredMembers.filter(m => m.departmentId == dept.id);
        const parentDept = currentDepartments.find(d => d.id == dept.parentId);

        return `
            <div class="team-section">
                <div class="team-header" onclick="toggleTeamSection(this)">
                    <h3>
                        ${dept.name}
                        ${parentDept ? `<span style="font-weight:400;font-size:14px;opacity:0.8">(${parentDept.name})</span>` : ''}
                    </h3>
                    <span class="team-badge">${deptMembers.length}명</span>
                </div>
                <div class="team-content">
                    ${deptMembers.length > 0 ? deptMembers.map(member => {
                        const memberEvents = currentEvents.filter(e => e.memberId == member.id);
                        return `
                            <div class="member-card">
                                <div class="member-card-header">
                                    <div class="member-avatar">${member.name.charAt(0)}</div>
                                    <div class="member-info">
                                        <h4>${member.name}</h4>
                                        <p>${member.position}</p>
                                    </div>
                                </div>
                                <div class="event-list">
                                    ${memberEvents.length > 0 ? memberEvents.map(event => `
                                        <div class="event-item ${event.eventType === 'LEAVE' ? 'leave' : 'schedule'}"
                                             onclick="handleEventClick({extendedProps: ${JSON.stringify(event).replace(/"/g, '&quot;')}})">
                                            <p class="event-title">${event.title}</p>
                                            <p class="event-date">
                                                ${formatDate(event.startDate)} ~ ${formatDate(event.endDate)}
                                                <span style="margin-left:8px;color:${getStatusColor(event.status)};font-weight:800;">
                                                    ${getStatusText(event.status)}
                                                </span>
                                            </p>
                                        </div>
                                    `).join('') : '<p style="text-align:center;color:var(--text-light);font-size:12px;padding:20px 0;">일정이 없습니다</p>'}
                                </div>
                            </div>
                        `;
                    }).join('') : '<p style="text-align:center;color:var(--text-light);padding:20px;">멤버가 없습니다</p>'}
                </div>
            </div>
        `;
    }).join('');

    teamViewContainer.innerHTML = html;
}

// Toggle team section collapse
function toggleTeamSection(header) {
    const content = header.nextElementSibling;
    content.classList.toggle('collapsed');
}

// Format date for display
function formatDate(dateStr) {
    const date = new Date(dateStr);
    return `${date.getMonth() + 1}/${date.getDate()}`;
}

// Get status color
function getStatusColor(status) {
    const colors = {
        'DRAFT': '#64748b',
        'SUBMITTED': '#0369a1',
        'APPROVED': '#065f46',
        'REJECTED': '#991b1b'
    };
    return colors[status] || '#64748b';
}

// Get status text
function getStatusText(status) {
    const texts = {
        'DRAFT': '초안',
        'SUBMITTED': '제출',
        'APPROVED': '승인',
        'REJECTED': '반려'
    };
    return texts[status] || status;
}

// Switch between calendar and team view
function switchView(view) {
    currentView = view;

    const calendarView = document.getElementById('calendarView');
    const teamView = document.getElementById('teamView');
    const calendarBtn = document.getElementById('btnCalendarView');
    const teamBtn = document.getElementById('btnTeamView');

    if (view === 'calendar') {
        calendarView.style.display = 'block';
        teamView.style.display = 'none';
        calendarBtn.classList.add('active');
        teamBtn.classList.remove('active');
        displayEvents();
    } else {
        calendarView.style.display = 'none';
        teamView.style.display = 'block';
        calendarBtn.classList.remove('active');
        teamBtn.classList.add('active');
        displayTeamView();
    }
}

// Handle event click
function handleEventClick(calendarEvent) {
    const event = calendarEvent.extendedProps;
    openEventModal(event);
}

// Open event modal
function openEventModal(event = null, startDateStr = null, endDateStr = null, startTimeStr = null, endTimeStr = null) {
    const modal = document.getElementById('eventModal');
    const modalTitle = document.getElementById('modalTitle');
    const form = document.getElementById('eventForm');

    form.reset();
    selectedEvent = event;

    // Populate event department selects
    populateEventDeptSelects();

    if (event) {
        modalTitle.textContent = '일정 수정';
        document.getElementById('eventId').value = event.id;

        // Change submit button text to "수정"
        const saveButton = document.querySelector('#eventForm button[type="submit"]');
        if (saveButton) {
            saveButton.textContent = '수정';
        }

        // Find member's department
        const member = currentMembers.find(m => m.id == event.memberId);
        if (member) {
            const childDept = currentDepartments.find(d => d.id == member.departmentId);
            const parentId = childDept ? childDept.parentId : null;

            if (parentId) {
                document.getElementById('eventParentDept').value = parentId;
                populateEventChildDepts(parentId);
            }
            if (member.departmentId) {
                document.getElementById('eventChildDept').value = member.departmentId;
                populateEventMemberSelect(parentId, member.departmentId);
            }
        }

        document.getElementById('eventMember').value = event.memberId;
        document.getElementById('eventType').value = event.eventType;
        document.getElementById('eventSubtype').value = event.eventSubtype;
        document.getElementById('eventTitle').value = event.title;
        document.getElementById('eventStartDate').value = event.startDate;
        document.getElementById('eventEndDate').value = event.endDate;
        document.getElementById('eventStartTime').value = event.startTime || '';
        document.getElementById('eventEndTime').value = event.endTime || '';
        document.getElementById('eventLeaveAmount').value = event.leaveAmount || 0;
        document.getElementById('eventDescription').value = event.description || '';

        toggleLeaveAmountField();

        // Control form editability and buttons based on status
        const isDraft = event.status === 'DRAFT';
        const isRejected = event.status === 'REJECTED';
        const isApproved = event.status === 'APPROVED';
        const isCanceled = event.status === 'CANCELED';
        const isLeave = event.eventType === 'LEAVE';
        const isSchedule = event.eventType === 'SCHEDULE';
        // SCHEDULE 타입은 APPROVED 상태여도 수정 가능
        const canEdit = isDraft || isRejected || (isApproved && isSchedule);
        const formElements = document.querySelectorAll('#eventForm input, #eventForm select, #eventForm textarea, #eventForm button[type="submit"]');

        formElements.forEach(el => {
            if (!canEdit) {
                el.disabled = true;
                if (el.tagName === 'SELECT') {
                    el.style.opacity = '0.6';
                }
            } else {
                el.disabled = false;
                el.style.opacity = '1';
            }
        });

        // Show/hide buttons based on status
        const btnRequestCancellation = document.getElementById('btnRequestCancellation');

        if (canEdit) {
            // SCHEDULE 타입은 결재 요청 버튼 숨김
            document.getElementById('btnSubmitEvent').style.display = isSchedule ? 'none' : 'inline-block';
            document.getElementById('btnDeleteEvent').style.display = 'inline-block';
            if (saveButton) saveButton.style.display = 'inline-block';
            if (btnRequestCancellation) btnRequestCancellation.style.display = 'none';
        } else if (isApproved && isLeave) {
            // 승인된 연차는 취소 신청 가능
            document.getElementById('btnSubmitEvent').style.display = 'none';
            document.getElementById('btnDeleteEvent').style.display = 'none';
            if (saveButton) saveButton.style.display = 'none';
            if (btnRequestCancellation) btnRequestCancellation.style.display = 'inline-block';
        } else if (isCanceled) {
            // 취소된 일정은 삭제 가능
            document.getElementById('btnSubmitEvent').style.display = 'none';
            document.getElementById('btnDeleteEvent').style.display = 'inline-block';
            if (saveButton) saveButton.style.display = 'none';
            if (btnRequestCancellation) btnRequestCancellation.style.display = 'none';
        } else {
            document.getElementById('btnSubmitEvent').style.display = 'none';
            document.getElementById('btnDeleteEvent').style.display = 'none';
            if (saveButton) saveButton.style.display = 'none';
            if (btnRequestCancellation) btnRequestCancellation.style.display = 'none';
        }

        // Load approval information
        loadApprovalInfo(event.id);
    } else {
        modalTitle.textContent = '일정 추가';
        document.getElementById('eventId').value = '';  // 새 일정이므로 eventId를 빈 문자열로 설정

        // Reset submit button text to "저장"
        const saveButton = document.querySelector('#eventForm button[type="submit"]');
        if (saveButton) {
            saveButton.textContent = '저장';
        }

        // "내 일정보기"에서 현재 선택된 본부/팀/멤버 가져오기
        const myScheduleParentDept = document.getElementById('myScheduleParentDept')?.value;
        const myScheduleChildDept = document.getElementById('myScheduleChildDept')?.value;
        const myScheduleMember = document.getElementById('myScheduleMember')?.value;

        // 내 일정보기 선택값이 있으면 우선 사용, 없으면 마지막 사용값 사용
        const lastParentDept = myScheduleParentDept || sessionStorage.getItem('lastEventParentDept');
        const lastChildDept = myScheduleChildDept || sessionStorage.getItem('lastEventChildDept');
        const lastMember = myScheduleMember || sessionStorage.getItem('lastEventMember');

        if (lastParentDept) {
            document.getElementById('eventParentDept').value = lastParentDept;
            populateEventChildDepts(lastParentDept);

            if (lastChildDept) {
                // Need to wait for child depts to populate
                setTimeout(() => {
                    document.getElementById('eventChildDept').value = lastChildDept;
                    populateEventMemberSelect(lastParentDept, lastChildDept);

                    if (lastMember) {
                        setTimeout(() => {
                            document.getElementById('eventMember').value = lastMember;
                        }, 50);
                    }
                }, 50);
            }
        }

        // Set time if provided (from week view time selection)
        if (startTimeStr) {
            // Time selection implies SCHEDULE type - set this BEFORE toggleLeaveAmountField
            document.getElementById('eventType').value = 'SCHEDULE';
            document.getElementById('eventSubtype').value = '회의';
            document.getElementById('eventStartDate').value = startDateStr;
            document.getElementById('eventEndDate').value = endDateStr || startDateStr;
            document.getElementById('eventStartTime').value = startTimeStr;
            document.getElementById('eventEndTime').value = endTimeStr || startTimeStr;
        } else if (startDateStr) {
            // Date selection from month view
            document.getElementById('eventStartDate').value = startDateStr;
            document.getElementById('eventEndDate').value = endDateStr || startDateStr;

            // Auto-calculate leave amount if multiple days selected (excluding weekends and holidays)
            if (endDateStr && startDateStr !== endDateStr) {
                const workDays = calculateWorkDays(startDateStr, endDateStr);
                document.getElementById('eventLeaveAmount').value = workDays;
            } else {
                // Single day selection
                const date = new Date(startDateStr);
                const dayOfWeek = date.getDay();

                // Format date to check if it's a holiday
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                const dateStr = `${year}-${month}-${day}`;

                const isWeekend = (dayOfWeek === 0 || dayOfWeek === 6);
                const isHoliday = koreanHolidays[dateStr] !== undefined;

                // If it's weekend or holiday, set to 0, otherwise 1
                document.getElementById('eventLeaveAmount').value = (isWeekend || isHoliday) ? 0 : 1;
            }
        }

        document.getElementById('btnSubmitEvent').style.display = 'none';
        document.getElementById('btnDeleteEvent').style.display = 'none';

        // Show save button for new events
        if (saveButton) {
            saveButton.style.display = 'inline-block';
        }

        toggleLeaveAmountField();

        // Hide approval section for new events
        document.getElementById('approvalSection').style.display = 'none';
    }

    modal.style.display = 'block';

    // Scroll modal to top
    const modalContent = modal.querySelector('.modal-content');
    if (modalContent) {
        modalContent.scrollTop = 0;
    }
}

// Populate event department selects
function populateEventDeptSelects() {
    const parentSelect = document.getElementById('eventParentDept');
    parentSelect.innerHTML = '<option value="">선택하세요</option>';

    const topLevelDepts = currentDepartments.filter(d => d.parentId === null || d.depth === 0);
    topLevelDepts.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.id;
        option.textContent = dept.name;
        parentSelect.appendChild(option);
    });

    const childSelect = document.getElementById('eventChildDept');
    childSelect.innerHTML = '<option value="">상위 부서를 먼저 선택하세요</option>';

    const memberSelect = document.getElementById('eventMember');
    memberSelect.innerHTML = '<option value="">부서를 먼저 선택하세요</option>';
}

// Populate event child departments
function populateEventChildDepts(parentId) {
    const childSelect = document.getElementById('eventChildDept');
    childSelect.innerHTML = '<option value="">선택하세요</option>';

    const memberSelect = document.getElementById('eventMember');
    memberSelect.innerHTML = '<option value="">하위 부서를 먼저 선택하세요</option>';

    if (!parentId) {
        return;
    }

    const childDepts = currentDepartments.filter(d => d.parentId == parentId);
    childDepts.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.id;
        option.textContent = dept.name;
        childSelect.appendChild(option);
    });
}

// Close event modal
function closeEventModal() {
    document.getElementById('eventModal').style.display = 'none';
    selectedEvent = null;
}

// Toggle leave amount field based on event type
function toggleLeaveAmountField() {
    const eventType = document.getElementById('eventType').value;
    const leaveAmountGroup = document.getElementById('leaveAmountGroup');
    const timeGroup = document.getElementById('timeGroup');
    const expenseFieldsGroup = document.getElementById('expenseFieldsGroup');
    const titleGroup = document.getElementById('titleGroup');
    const descriptionGroup = document.getElementById('descriptionGroup');
    const subtypeGroup = document.querySelector('#eventSubtype')?.closest('.form-group');
    const startDateGroup = document.getElementById('eventStartDate')?.closest('div')?.parentElement;

    // 필수 필드 요소들
    const eventTitle = document.getElementById('eventTitle');
    const eventSubtype = document.getElementById('eventSubtype');
    const eventStartDate = document.getElementById('eventStartDate');
    const eventEndDate = document.getElementById('eventEndDate');

    if (eventType === 'EXPENSE') {
        // 지출보고서 관련 필드 표시
        leaveAmountGroup.style.display = 'none';
        timeGroup.style.display = 'none';
        expenseFieldsGroup.style.display = 'block';
        titleGroup.style.display = 'none';
        descriptionGroup.style.display = 'none';
        if (subtypeGroup) subtypeGroup.style.display = 'none';
        if (startDateGroup) startDateGroup.style.display = 'none';

        // 일정/휴가 필드들의 required 제거
        if (eventTitle) eventTitle.removeAttribute('required');
        if (eventSubtype) eventSubtype.removeAttribute('required');
        if (eventStartDate) eventStartDate.removeAttribute('required');
        if (eventEndDate) eventEndDate.removeAttribute('required');

        // 날짜 설정: 일정 시작일이 있으면 사용, 없으면 오늘
        let usageDate = document.getElementById('eventStartDate').value;
        if (!usageDate) {
            const today = new Date();
            usageDate = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`;
        }
        document.getElementById('expenseUsageDate').value = usageDate;
        document.getElementById('expenseProjectCode').value = 'PJ00000000';

        // sessionStorage에서 마지막 선택값 복원 (없으면 기본값)
        const savedAccount = sessionStorage.getItem('lastExpenseAccount') || '복리후생비';
        const savedCostCode = sessionStorage.getItem('lastExpenseCostCode') || '개인(개인)';
        document.getElementById('expenseAccount').value = savedAccount;
        document.getElementById('expenseCostCode').value = savedCostCode;
    } else if (eventType === 'LEAVE') {
        leaveAmountGroup.style.display = 'block';
        timeGroup.style.display = 'none';
        expenseFieldsGroup.style.display = 'none';
        titleGroup.style.display = 'block';
        descriptionGroup.style.display = 'block';
        if (subtypeGroup) subtypeGroup.style.display = 'block';
        if (startDateGroup) startDateGroup.style.display = 'flex';

        // 일정/휴가 필드들의 required 복원
        if (eventTitle) eventTitle.setAttribute('required', 'required');
        if (eventSubtype) eventSubtype.setAttribute('required', 'required');
        if (eventStartDate) eventStartDate.setAttribute('required', 'required');
        if (eventEndDate) eventEndDate.setAttribute('required', 'required');

        // 연차는 09:00~18:00 고정
        document.getElementById('eventStartTime').value = '09:00';
        document.getElementById('eventEndTime').value = '18:00';

        // Only update leave amount if it's not already set from date calculation
        const currentAmount = document.getElementById('eventLeaveAmount').value;
        if (!currentAmount || currentAmount == 0) {
            updateLeaveAmountBySubtype();
        }
    } else {
        leaveAmountGroup.style.display = 'none';
        timeGroup.style.display = 'flex';
        expenseFieldsGroup.style.display = 'none';
        titleGroup.style.display = 'block';
        descriptionGroup.style.display = 'block';
        if (subtypeGroup) subtypeGroup.style.display = 'block';
        if (startDateGroup) startDateGroup.style.display = 'flex';

        // 일정/휴가 필드들의 required 복원
        if (eventTitle) eventTitle.setAttribute('required', 'required');
        if (eventSubtype) eventSubtype.setAttribute('required', 'required');
        if (eventStartDate) eventStartDate.setAttribute('required', 'required');
        if (eventEndDate) eventEndDate.setAttribute('required', 'required');

        document.getElementById('eventLeaveAmount').value = 0;
    }
}

// Update leave amount based on subtype
function updateLeaveAmountBySubtype() {
    const subtype = document.getElementById('eventSubtype').value;
    const leaveAmountInput = document.getElementById('eventLeaveAmount');

    // Check if there's already a calculated value from date range
    const startDate = document.getElementById('eventStartDate').value;
    const endDate = document.getElementById('eventEndDate').value;

    // If date range is set and different, calculate from dates
    if (startDate && endDate && startDate !== endDate) {
        const workDays = calculateWorkDays(startDate, endDate);
        leaveAmountInput.value = workDays;
        return;
    }

    // Otherwise, use subtype default
    if (subtype === '연차' || subtype === '병가') {
        leaveAmountInput.value = 1.0;
    } else if (subtype.includes('반차')) {
        leaveAmountInput.value = 0.5;
    }
}

// Calculate work days (excluding weekends and holidays)
function calculateWorkDays(startDateStr, endDateStr) {
    const start = new Date(startDateStr);
    const end = new Date(endDateStr);

    console.log('=== 근무일수 계산 ===');
    console.log('시작일:', startDateStr);
    console.log('종료일:', endDateStr);

    let workDays = 0;
    let current = new Date(start);
    let dateList = [];

    while (current <= end) {
        const dayOfWeek = current.getDay();

        // Format date to check if it's a holiday
        const year = current.getFullYear();
        const month = String(current.getMonth() + 1).padStart(2, '0');
        const day = String(current.getDate()).padStart(2, '0');
        const dateStr = `${year}-${month}-${day}`;

        const isWeekend = (dayOfWeek === 0 || dayOfWeek === 6);
        const isHoliday = koreanHolidays[dateStr] !== undefined;

        // Count only if it's not weekend and not holiday
        if (!isWeekend && !isHoliday) {
            workDays++;
            dateList.push(dateStr);
        }

        current.setDate(current.getDate() + 1);
    }

    console.log('근무일 목록:', dateList);
    console.log('총 근무일수:', workDays);

    return workDays;
}

// Auto-calculate leave amount based on date range
function autoCalculateLeaveAmount() {
    const eventType = document.getElementById('eventType').value;
    if (eventType !== 'LEAVE') return;

    const startDate = document.getElementById('eventStartDate').value;
    const endDate = document.getElementById('eventEndDate').value;

    if (startDate && endDate) {
        const workDays = calculateWorkDays(startDate, endDate);
        if (workDays > 0) {
            document.getElementById('eventLeaveAmount').value = workDays;
        }
    }
}

// Save event
async function saveEvent(e) {
    e.preventDefault();

    const eventType = document.getElementById('eventType').value;
    const parentDeptId = document.getElementById('eventParentDept').value;
    const childDeptId = document.getElementById('eventChildDept').value;
    const memberId = parseInt(document.getElementById('eventMember').value);

    // 지출보고서인 경우 expense API로 저장
    if (eventType === 'EXPENSE') {
        const expenseData = {
            memberId: memberId,
            usageDate: document.getElementById('expenseUsageDate').value,
            description: document.getElementById('expenseDescription').value,
            account: document.getElementById('expenseAccount').value,
            amount: parseInt(document.getElementById('expenseAmount').value) || 0,
            vendor: document.getElementById('expenseVendor').value || null,
            costCode: document.getElementById('expenseCostCode').value || null,
            projectCode: document.getElementById('expenseProjectCode').value || null,
            note: document.getElementById('expenseNote').value || null
        };

        // 필수 필드 검증
        if (!expenseData.usageDate || !expenseData.description || !expenseData.account || !expenseData.amount) {
            alert('필수 항목을 모두 입력해주세요.');
            return;
        }

        try {
            const response = await fetch('/api/expense-items', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(expenseData)
            });

            if (!response.ok) {
                throw new Error('지출 내역 저장 실패');
            }

            // sessionStorage에 마지막 선택값 저장
            sessionStorage.setItem('lastExpenseAccount', expenseData.account);
            if (expenseData.costCode) {
                sessionStorage.setItem('lastExpenseCostCode', expenseData.costCode);
            }

            alert('지출 내역이 추가되었습니다.');
            closeEventModal();
            await loadEvents();
        } catch (error) {
            console.error('Failed to save expense:', error);
            alert('지출 내역 저장에 실패했습니다: ' + error.message);
        }
        return;
    }

    // 일반 일정/휴가인 경우
    // Get department, position, division from "내 일정 보기" selection
    const myScheduleParentDeptId = document.getElementById('myScheduleParentDept')?.value;
    const myScheduleChildDeptId = document.getElementById('myScheduleChildDept')?.value;

    let memberDepartment = null;
    let memberDivision = null;
    let memberPosition = null;

    // Get division (parent department) and department (child department) from "내 일정 보기"
    if (myScheduleParentDeptId) {
        const parentDept = currentDepartments.find(d => d.id == myScheduleParentDeptId);
        if (parentDept) {
            memberDivision = parentDept.name;
        }
    }

    if (myScheduleChildDeptId) {
        const childDept = currentDepartments.find(d => d.id == myScheduleChildDeptId);
        if (childDept) {
            memberDepartment = childDept.name;
        }
    }

    // Get position from selected member if available
    const selectedMember = currentMembers.find(m => m.id === memberId);
    if (selectedMember && selectedMember.position) {
        memberPosition = selectedMember.position;
    }

    const eventData = {
        memberId: memberId,
        eventType: eventType,
        eventSubtype: document.getElementById('eventSubtype').value,
        title: document.getElementById('eventTitle').value,
        startDate: document.getElementById('eventStartDate').value,
        endDate: document.getElementById('eventEndDate').value,
        startTime: document.getElementById('eventStartTime').value || null,
        endTime: document.getElementById('eventEndTime').value || null,
        leaveAmount: parseFloat(document.getElementById('eventLeaveAmount').value) || 0,
        description: document.getElementById('eventDescription').value,
        department: memberDepartment,
        position: memberPosition,
        division: memberDivision
    };

    console.log('=== Event Save Debug ===');
    console.log('myScheduleParentDeptId:', myScheduleParentDeptId, 'myScheduleChildDeptId:', myScheduleChildDeptId);
    console.log('selectedMember:', selectedMember);
    console.log('memberDepartment:', memberDepartment, 'memberDivision:', memberDivision, 'memberPosition:', memberPosition);
    console.log('eventData:', JSON.stringify(eventData, null, 2));

    try {
        const eventId = document.getElementById('eventId').value;

        if (eventId) {
            await API.updateEvent(parseInt(eventId), eventData);
            alert('일정이 수정되었습니다.');
        } else {
            await API.createEvent(eventData);
            alert('일정이 추가되었습니다.');
        }

        // Save to SessionStorage for next time
        sessionStorage.setItem('lastEventParentDept', parentDeptId);
        sessionStorage.setItem('lastEventChildDept', childDeptId);
        sessionStorage.setItem('lastEventMember', memberId);

        closeEventModal();
        await loadEvents();

        // Refresh leave balance if modal is open
        const leaveBalanceModal = document.getElementById('leaveBalanceModal');
        if (leaveBalanceModal && leaveBalanceModal.style.display === 'block') {
            const year = document.getElementById('balanceYear').value;
            await loadLeaveBalances(year);
        }
    } catch (error) {
        console.error('Failed to save event:', error);
        alert('일정 저장에 실패했습니다: ' + error.message);
    }
}

// Submit event for approval
async function submitEvent() {
    if (!selectedEvent) return;

    // Create approval selector modal
    const approvers = currentMembers.filter(m => m.isActive);

    if (approvers.length === 0) {
        alert('승인 가능한 멤버가 없습니다.');
        return;
    }

    // Show approver selection using a simple select dialog
    const approverOptions = approvers.map(m => `<option value="${m.id}">${m.name} (${m.department || '부서 없음'} - ${m.position})</option>`).join('');

    const approverSelectHtml = `
        <div style="position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); background:white; padding:24px; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.2); z-index:10001; min-width:400px;">
            <h3 style="margin:0 0 16px 0; font-size:18px;">승인자 선택</h3>
            <select id="approverSelect" style="width:100%; padding:12px; border:2px solid #e5e7eb; border-radius:8px; font-size:14px; margin-bottom:16px;">
                <option value="">승인자를 선택하세요</option>
                ${approverOptions}
            </select>
            <div style="display:flex; gap:8px; justify-content:flex-end;">
                <button id="btnCancelApprover" style="padding:10px 20px; border:2px solid #e5e7eb; background:white; border-radius:8px; cursor:pointer; font-weight:700;">취소</button>
                <button id="btnConfirmApprover" style="padding:10px 20px; border:none; background:linear-gradient(135deg,#10b981,#059669); color:white; border-radius:8px; cursor:pointer; font-weight:700;">확인</button>
            </div>
        </div>
        <div id="approverOverlay" style="position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.5); z-index:10000;"></div>
    `;

    const container = document.createElement('div');
    container.innerHTML = approverSelectHtml;
    document.body.appendChild(container);

    // Handle approver selection
    document.getElementById('btnConfirmApprover').addEventListener('click', async () => {
        const approverId = document.getElementById('approverSelect').value;
        if (!approverId) {
            alert('승인자를 선택해주세요.');
            return;
        }

        try {
            await API.submitEvent(selectedEvent.id, parseInt(approverId));
            alert('승인 요청이 제출되었습니다.');
            container.remove();
            closeEventModal();
            await loadEvents();
            await loadPendingApprovals();
        } catch (error) {
            console.error('Failed to submit event:', error);
            alert('승인 요청 제출에 실패했습니다: ' + error.message);
        }
    });

    document.getElementById('btnCancelApprover').addEventListener('click', () => {
        container.remove();
    });

    document.getElementById('approverOverlay').addEventListener('click', () => {
        container.remove();
    });
}

// Delete event
async function deleteEvent() {
    if (!selectedEvent) return;

    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        await API.deleteEvent(selectedEvent.id);
        alert('일정이 삭제되었습니다.');
        closeEventModal();
        await loadEvents();

        // Refresh leave balance if modal is open
        const leaveBalanceModal = document.getElementById('leaveBalanceModal');
        if (leaveBalanceModal && leaveBalanceModal.style.display === 'block') {
            const year = document.getElementById('balanceYear').value;
            await loadLeaveBalances(year);
        }
    } catch (error) {
        console.error('Failed to delete event:', error);

        // Check if it's a "not found" error
        if (error.message && error.message.includes('Event not found')) {
            alert('이미 삭제된 일정입니다. 화면을 새로고침합니다.');
            closeEventModal();
            await loadEvents();
        } else {
            alert('일정 삭제에 실패했습니다: ' + error.message);
        }
    }
}

// Request cancellation of approved event
async function requestCancellation() {
    if (!selectedEvent) return;

    // Create approval selector modal
    const approvers = currentMembers.filter(m => m.isActive);

    if (approvers.length === 0) {
        alert('승인 가능한 멤버가 없습니다.');
        return;
    }

    // Show approver selection using a simple select dialog
    const approverOptions = approvers.map(m => `<option value="${m.id}">${m.name} (${m.department || '부서 없음'} - ${m.position})</option>`).join('');

    const approverSelectHtml = `
        <div style="position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); background:white; padding:24px; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.2); z-index:10001; min-width:400px;">
            <h3 style="margin:0 0 16px 0; font-size:18px;">취소 승인자 선택</h3>
            <select id="approverSelect" style="width:100%; padding:12px; border:2px solid #e5e7eb; border-radius:8px; font-size:14px; margin-bottom:16px;">
                <option value="">승인자를 선택하세요</option>
                ${approverOptions}
            </select>
            <div style="display:flex; gap:8px; justify-content:flex-end;">
                <button id="btnCancelApprover" style="padding:10px 20px; border:2px solid #e5e7eb; background:white; border-radius:8px; cursor:pointer; font-weight:700;">취소</button>
                <button id="btnConfirmApprover" style="padding:10px 20px; border:none; background:linear-gradient(135deg,#ef4444,#dc2626); color:white; border-radius:8px; cursor:pointer; font-weight:700;">확인</button>
            </div>
        </div>
        <div id="approverOverlay" style="position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.5); z-index:10000;"></div>
    `;

    const container = document.createElement('div');
    container.innerHTML = approverSelectHtml;
    document.body.appendChild(container);

    // Handle approver selection
    document.getElementById('btnConfirmApprover').addEventListener('click', async () => {
        const approverId = document.getElementById('approverSelect').value;
        if (!approverId) {
            alert('승인자를 선택해주세요.');
            return;
        }

        try {
            await API.requestCancellation(selectedEvent.id, parseInt(approverId));
            alert('취소 요청이 제출되었습니다.');
            container.remove();
            closeEventModal();
            await loadEvents();
            await loadPendingApprovals();
        } catch (error) {
            console.error('Failed to request cancellation:', error);
            alert('취소 요청 제출에 실패했습니다: ' + error.message);
        }
    });

    document.getElementById('btnCancelApprover').addEventListener('click', () => {
        container.remove();
    });

    document.getElementById('approverOverlay').addEventListener('click', () => {
        container.remove();
    });
}

// Wrapper function to submit event for approval by ID (for use in schedule list)
async function submitEventForApproval(eventId) {
    const event = currentEvents.find(e => e.id === eventId);
    if (!event) {
        alert('일정을 찾을 수 없습니다.');
        return;
    }
    selectedEvent = event;
    await submitEvent();
}

// Wrapper function to request cancellation by ID (for use in schedule list)
async function requestCancellationById(eventId) {
    const event = currentEvents.find(e => e.id === eventId);
    if (!event) {
        alert('일정을 찾을 수 없습니다.');
        return;
    }
    selectedEvent = event;
    await requestCancellation();
}

// Open member management modal
async function openMemberModal() {
    const modal = document.getElementById('memberModal');
    await loadMemberList();
    modal.style.display = 'block';
}

// Close member modal
function closeMemberModal() {
    document.getElementById('memberModal').style.display = 'none';
}

// Load member list
async function loadMemberList() {
    try {
        const members = await API.getMembers();
        const memberList = document.getElementById('memberList');

        memberList.innerHTML = members.map(member => `
            <div class="member-item">
                <div class="member-info">
                    <h4>${member.name}</h4>
                    <p>${member.department} - ${member.position}</p>
                    <p>입사일: ${member.hireDate || '미등록'} | 연차: ${member.annualLeaveGranted}일 | 상태: ${member.isActive ? '활성' : '비활성'}</p>
                </div>
                <div class="member-actions">
                    <button class="btn btn-secondary" onclick="editMember(${member.id})">수정</button>
                    <button class="btn btn-danger" onclick="deleteMember(${member.id})">삭제</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Failed to load member list:', error);
        alert('멤버 목록을 불러오는데 실패했습니다.');
    }
}

// Calculate annual leave based on hire date
function calculateAnnualLeaveFromHireDate(hireDate) {
    if (!hireDate) {
        return 15;
    }

    const effectiveDate = new Date('2024-03-01');
    const today = new Date();
    const hire = new Date(hireDate);

    // 시행일 이전 입사자는 시행일을 기준으로 계산
    const calculationDate = hire < effectiveDate ? effectiveDate : hire;

    // 근속 년수 계산 (월 단위)
    const totalMonths = (today.getFullYear() - calculationDate.getFullYear()) * 12
                       + (today.getMonth() - calculationDate.getMonth());
    const yearsOfService = totalMonths / 12;

    let annualLeave;

    // 1년 미만인 경우: 남은 월수 기준 비례 계산
    if (yearsOfService < 1.0) {
        const currentMonth = today.getMonth() + 1;
        const remainingMonths = 12 - currentMonth + 1;
        annualLeave = remainingMonths;
    } else {
        // 기본 연차 15일
        annualLeave = 15;

        // 3년차 이상: 1일 가산
        if (yearsOfService >= 3.0) {
            annualLeave += 1;

            // 3년 이후 매 2년마다 1일 추가 가산
            const yearsAfterThree = yearsOfService - 3.0;
            const additionalDays = Math.floor(yearsAfterThree / 2.0);
            annualLeave += additionalDays;
        }

        // 최대 25일 제한
        if (annualLeave > 25) {
            annualLeave = 25;
        }
    }

    return annualLeave;
}

// Open add member modal
function openAddMemberModal() {
    const modal = document.getElementById('addMemberModal');
    const form = document.getElementById('memberForm');

    form.reset();
    document.getElementById('memberModalTitle').textContent = '멤버 추가';
    document.getElementById('memberId').value = '';
    document.getElementById('memberIsActive').checked = true;

    // 입사일 변경 시 연차 자동 계산
    const hireDateInput = document.getElementById('memberHireDate');
    const annualLeaveInput = document.getElementById('memberAnnualLeave');

    hireDateInput.addEventListener('change', function() {
        if (this.value) {
            const calculatedLeave = calculateAnnualLeaveFromHireDate(this.value);
            annualLeaveInput.value = calculatedLeave;
        }
    });

    populateMemberDeptSelects();
    modal.style.display = 'block';
}

// Close add member modal
function closeAddMemberModal() {
    document.getElementById('addMemberModal').style.display = 'none';
}

// Populate member department selects
function populateMemberDeptSelects(selectedParentId = null, selectedChildId = null) {
    const parentSelect = document.getElementById('memberParentDept');
    const childSelect = document.getElementById('memberChildDept');

    // Populate parent departments
    parentSelect.innerHTML = '<option value="">선택하세요</option>';
    const topLevelDepts = currentDepartments.filter(d => d.parentId === null || d.depth === 0);
    topLevelDepts.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.id;
        option.textContent = dept.name;
        parentSelect.appendChild(option);
    });

    if (selectedParentId) {
        parentSelect.value = selectedParentId;
        populateMemberChildDepts(selectedParentId, selectedChildId);
    } else {
        childSelect.innerHTML = '<option value="">상위 부서를 먼저 선택하세요</option>';
    }
}

// Populate member child departments
function populateMemberChildDepts(parentId, selectedChildId = null) {
    const childSelect = document.getElementById('memberChildDept');
    childSelect.innerHTML = '<option value="">선택하세요</option>';

    if (!parentId) return;

    const childDepts = currentDepartments.filter(d => d.parentId == parentId);
    childDepts.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.id;
        option.textContent = dept.name;
        childSelect.appendChild(option);
    });

    if (selectedChildId) {
        childSelect.value = selectedChildId;
    }
}

// Save member (add or update)
async function saveMember(e) {
    e.preventDefault();

    const memberData = {
        name: document.getElementById('memberName').value,
        email: document.getElementById('memberEmail').value,
        phone: document.getElementById('memberPhone').value || null,
        departmentId: parseInt(document.getElementById('memberChildDept').value),
        position: document.getElementById('memberPosition').value,
        hireDate: document.getElementById('memberHireDate').value || null,
        annualLeaveGranted: parseFloat(document.getElementById('memberAnnualLeave').value),
        smtpPassword: document.getElementById('memberSmtpPassword').value || null,
        isActive: document.getElementById('memberIsActive').checked
    };

    try {
        const memberId = document.getElementById('memberId').value;

        if (memberId) {
            await API.updateMember(parseInt(memberId), memberData);
            alert('멤버 정보가 수정되었습니다.');
        } else {
            await API.createMember(memberData);
            alert('멤버가 추가되었습니다.');
        }

        closeAddMemberModal();
        await loadMembers();
        await loadMemberList();
    } catch (error) {
        console.error('Failed to save member:', error);
        alert('멤버 저장에 실패했습니다: ' + error.message);
    }
}

// Edit member
async function editMember(id) {
    try {
        const member = await API.getMember(id);
        const modal = document.getElementById('addMemberModal');

        document.getElementById('memberModalTitle').textContent = '멤버 수정';
        document.getElementById('memberId').value = member.id;
        document.getElementById('memberName').value = member.name;
        document.getElementById('memberEmail').value = member.email || '';
        document.getElementById('memberPhone').value = member.phone || '';
        document.getElementById('memberPosition').value = member.position;
        document.getElementById('memberHireDate').value = member.hireDate || '';
        document.getElementById('memberAnnualLeave').value = member.annualLeaveGranted;
        document.getElementById('memberSmtpPassword').value = member.smtpPassword || '';
        document.getElementById('memberIsActive').checked = member.isActive;

        // 입사일 변경 시 연차 자동 계산
        const hireDateInput = document.getElementById('memberHireDate');
        const annualLeaveInput = document.getElementById('memberAnnualLeave');

        // 기존 이벤트 리스너 제거 후 새로 추가
        const newHireDateInput = hireDateInput.cloneNode(true);
        hireDateInput.parentNode.replaceChild(newHireDateInput, hireDateInput);

        newHireDateInput.addEventListener('change', function() {
            if (this.value) {
                const calculatedLeave = calculateAnnualLeaveFromHireDate(this.value);
                annualLeaveInput.value = calculatedLeave;
            }
        });

        // Find parent department
        const childDept = currentDepartments.find(d => d.id == member.departmentId);
        const parentId = childDept ? childDept.parentId : null;

        populateMemberDeptSelects(parentId, member.departmentId);

        modal.style.display = 'block';
    } catch (error) {
        console.error('Failed to edit member:', error);
        alert('멤버 수정에 실패했습니다: ' + error.message);
    }
}

// Delete member
async function deleteMember(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        await API.deleteMember(id);
        alert('멤버가 삭제되었습니다.');
        await loadMembers();
        await loadMemberList();
    } catch (error) {
        console.error('Failed to delete member:', error);
        alert('멤버 삭제에 실패했습니다: ' + error.message);
    }
}

// Open leave balance modal
async function openLeaveBalanceModal() {
    const modal = document.getElementById('leaveBalanceModal');
    const year = document.getElementById('balanceYear').value;
    await loadLeaveBalances(year);
    modal.style.display = 'block';
}

// Close leave balance modal
function closeLeaveBalanceModal() {
    document.getElementById('leaveBalanceModal').style.display = 'none';
}

// Load leave balances (filtered by selected member in "내 일정보기")
async function loadLeaveBalances(year) {
    try {
        const balances = await API.getAllLeaveBalances(year);
        const balanceList = document.getElementById('leaveBalanceList');

        // Get selected member from "내 일정보기"
        const selectedMemberId = sessionStorage.getItem('myScheduleMember');

        // Filter balances by selected member
        const filteredBalances = selectedMemberId
            ? balances.filter(b => b.memberId == selectedMemberId)
            : balances;

        if (filteredBalances.length === 0) {
            balanceList.innerHTML = '<div class="empty-approvals">선택한 멤버의 휴가 현황이 없습니다</div>';
            return;
        }

        balanceList.innerHTML = filteredBalances.map(balance => `
            <div class="balance-item">
                <h4>${balance.memberName} (${balance.department} - ${balance.position})</h4>
                <div class="balance-info">
                    <div class="balance-stat granted">
                        <span class="label">부여</span>
                        <span class="value">${balance.annualLeaveGranted}일</span>
                    </div>
                    <div class="balance-stat used">
                        <span class="label">사용</span>
                        <span class="value">${balance.usedLeave}일</span>
                    </div>
                    <div class="balance-stat remaining">
                        <span class="label">잔여</span>
                        <span class="value">${balance.remainingLeave}일</span>
                    </div>
                </div>
                <div style="margin-top: 10px; text-align: right;">
                    <button onclick="showLeaveUsageDetails(${balance.memberId}, ${year})"
                        style="padding: 6px 12px; background: #3b82f6; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px;">
                        사용 내역 보기
                    </button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Failed to load leave balances:', error);
        alert('휴가 현황을 불러오는데 실패했습니다.');
    }
}

// Show leave usage details
async function showLeaveUsageDetails(memberId, year) {
    try {
        // Get all events for the member in the specified year
        const memberEvents = currentEvents.filter(e =>
            e.memberId == memberId &&
            e.eventType === 'LEAVE' &&
            e.status === 'APPROVED' &&
            new Date(e.startDate).getFullYear() == year
        );

        if (memberEvents.length === 0) {
            alert('사용한 연차가 없습니다.');
            return;
        }

        // Sort by date
        memberEvents.sort((a, b) => new Date(a.startDate) - new Date(b.startDate));

        // Create detail list
        const detailsHtml = memberEvents.map(event => {
            const startDate = formatDate(event.startDate);
            const endDate = event.startDate === event.endDate ? '' : ` ~ ${formatDate(event.endDate)}`;
            const amount = event.leaveAmount || 0;
            const subtype = event.eventSubtype || '연차';

            return `
                <div style="padding: 10px; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between;">
                    <div>
                        <div style="font-weight: 600; color: #1f2937;">${subtype}</div>
                        <div style="font-size: 13px; color: #6b7280; margin-top: 4px;">📅 ${startDate}${endDate}</div>
                        ${event.description ? `<div style="font-size: 12px; color: #9ca3af; margin-top: 2px;">${event.description}</div>` : ''}
                    </div>
                    <div style="font-size: 16px; font-weight: 700; color: #e67e22; white-space: nowrap; margin-left: 10px;">
                        ${amount}일
                    </div>
                </div>
            `;
        }).join('');

        const totalUsed = memberEvents.reduce((sum, e) => sum + (e.leaveAmount || 0), 0);

        // Show in a modal or alert
        const member = currentMembers.find(m => m.id == memberId);
        const memberName = member ? member.name : '멤버';

        const detailModal = document.createElement('div');
        detailModal.style.cssText = `
            position: fixed;
            z-index: 1001;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
        `;

        detailModal.innerHTML = `
            <div style="background: white; border-radius: 8px; max-width: 600px; width: 90%; max-height: 80vh; overflow: hidden; display: flex; flex-direction: column;">
                <div style="padding: 20px; border-bottom: 1px solid #e5e7eb;">
                    <h3 style="margin: 0; color: #2c3e50;">${memberName}님의 ${year}년 연차 사용 내역</h3>
                </div>
                <div style="flex: 1; overflow-y: auto; padding: 10px;">
                    ${detailsHtml}
                </div>
                <div style="padding: 15px; border-top: 2px solid #e5e7eb; background: #f9fafb; display: flex; justify-content: space-between; align-items: center;">
                    <div style="font-size: 16px; font-weight: 700; color: #2c3e50;">총 사용: ${totalUsed}일</div>
                    <button id="closeLeaveDetailBtn"
                        style="padding: 8px 16px; background: #64748b; color: white; border: none; border-radius: 4px; cursor: pointer;">
                        닫기
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(detailModal);

        // Add close button event listener
        document.getElementById('closeLeaveDetailBtn').addEventListener('click', function() {
            detailModal.remove();
        });

        // Close on background click
        detailModal.addEventListener('click', function(e) {
            if (e.target === detailModal) {
                detailModal.remove();
            }
        });

    } catch (error) {
        console.error('Failed to load leave usage details:', error);
        alert('연차 사용 내역을 불러오는데 실패했습니다.');
    }
}

// Initialize event handlers
function initEventHandlers() {
    // Event form
    document.getElementById('eventForm').addEventListener('submit', saveEvent);
    document.getElementById('btnCloseEventModal').addEventListener('click', closeEventModal);
    document.getElementById('btnSubmitEvent').addEventListener('click', submitEvent);
    document.getElementById('btnDeleteEvent').addEventListener('click', deleteEvent);
    document.getElementById('btnRequestCancellation').addEventListener('click', requestCancellation);

    // Event type change
    document.getElementById('eventType').addEventListener('change', toggleLeaveAmountField);
    document.getElementById('eventSubtype').addEventListener('change', updateLeaveAmountBySubtype);

    // Date change - auto calculate leave amount
    document.getElementById('eventStartDate').addEventListener('change', autoCalculateLeaveAmount);
    document.getElementById('eventEndDate').addEventListener('change', autoCalculateLeaveAmount);

    // Event department select
    document.getElementById('eventParentDept').addEventListener('change', function() {
        const parentId = this.value;
        populateEventChildDepts(parentId);
    });

    document.getElementById('eventChildDept').addEventListener('change', function() {
        const parentId = document.getElementById('eventParentDept').value;
        const childId = this.value;
        populateEventMemberSelect(parentId, childId);
    });

    // Modal close buttons
    document.querySelectorAll('.close').forEach(closeBtn => {
        closeBtn.addEventListener('click', function() {
            this.closest('.modal').style.display = 'none';
        });
    });

    // View toggle buttons
    document.getElementById('btnCalendarView').addEventListener('click', () => switchView('calendar'));
    document.getElementById('btnTeamView').addEventListener('click', () => switchView('team'));

    // Header buttons
    document.getElementById('btnNewEvent').addEventListener('click', () => openEventModal());
    document.getElementById('btnManageMembers').addEventListener('click', openMemberModal);
    document.getElementById('btnLeaveBalance').addEventListener('click', openLeaveBalanceModal);
    document.getElementById('btnAddMember').addEventListener('click', openAddMemberModal);
    document.getElementById('btnLoadBalance').addEventListener('click', () => {
        const year = document.getElementById('balanceYear').value;
        loadLeaveBalances(year);
    });

    // Member form
    document.getElementById('memberForm').addEventListener('submit', saveMember);
    document.getElementById('btnCancelMember').addEventListener('click', closeAddMemberModal);
    document.getElementById('memberParentDept').addEventListener('change', function() {
        populateMemberChildDepts(this.value);
    });

    // Close modals on outside click
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            e.target.style.display = 'none';
        }
    });

    // Approval buttons
    document.getElementById('btnApprove').addEventListener('click', () => handleApproval('APPROVED'));
    document.getElementById('btnReject').addEventListener('click', () => handleApproval('REJECTED'));
}

// Load approval information for an event
async function loadApprovalInfo(eventId) {
    try {
        // Hide approval section for SCHEDULE events (회의)
        if (selectedEvent && selectedEvent.eventType === 'SCHEDULE') {
            document.getElementById('approvalSection').style.display = 'none';
            return;
        }

        const approvals = await API.getEventApprovals(eventId);
        const approvalSection = document.getElementById('approvalSection');
        const approvalInfo = document.getElementById('approvalInfo');
        const approvalActions = document.getElementById('approvalActions');

        if (!approvals || approvals.length === 0) {
            approvalSection.style.display = 'none';
            return;
        }

        approvalSection.style.display = 'block';

        // Display approval information
        const approvalHtml = approvals.map(approval => {
            const approver = currentMembers.find(m => m.id == approval.approverId);
            const approverName = approver ? approver.name : '알 수 없음';
            const decidedAt = approval.decidedAt ? new Date(approval.decidedAt).toLocaleString('ko-KR') : '-';
            const statusText = approval.decision === 'APPROVED' ? '승인' : approval.decision === 'REJECTED' ? '반려' : '대기 중';
            const statusColor = approval.decision === 'APPROVED' ? '#065f46' : approval.decision === 'REJECTED' ? '#991b1b' : '#0369a1';
            const stepLabel = approval.stepOrder === 1 ? '일정 승인' : '취소 승인';

            return `
                <div style="padding:12px; margin-bottom:8px; background:white; border-radius:6px; border-left:4px solid ${statusColor};">
                    <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
                        <span style="font-weight:700;">${stepLabel}: ${approverName}</span>
                        <span style="color:${statusColor}; font-weight:700;">${statusText}</span>
                    </div>
                    <div style="font-size:12px; color:var(--text-light);">
                        ${approval.decision ? `결재일: ${decidedAt}` : '결재 대기 중'}
                    </div>
                    ${approval.comment ? `<div style="margin-top:6px; font-size:12px; color:var(--text);">${approval.comment}</div>` : ''}
                </div>
            `;
        }).join('');

        approvalInfo.innerHTML = approvalHtml;

        // Show approval buttons if there's a pending approval and current member is the approver
        const pendingApproval = approvals.find(a => !a.decision);
        const currentMemberId = sessionStorage.getItem('myScheduleMember');
        const isApprover = pendingApproval && currentMemberId && pendingApproval.approverId == currentMemberId;

        if (pendingApproval && selectedEvent && selectedEvent.status === 'SUBMITTED' && isApprover) {
            approvalActions.style.display = 'flex';
            approvalActions.dataset.approvalId = pendingApproval.id;
            approvalActions.dataset.approverId = pendingApproval.approverId;
            approvalActions.dataset.stepOrder = pendingApproval.stepOrder;
        } else {
            approvalActions.style.display = 'none';
        }
    } catch (error) {
        console.error('Failed to load approval info:', error);
    }
}

// Handle approval/rejection
async function handleApproval(decision) {
    const approvalActions = document.getElementById('approvalActions');
    const approvalId = approvalActions.dataset.approvalId;
    const approverId = parseInt(approvalActions.dataset.approverId);
    const stepOrder = parseInt(approvalActions.dataset.stepOrder);

    if (!approvalId) {
        alert('결재 정보를 찾을 수 없습니다.');
        return;
    }

    const comment = prompt(decision === 'APPROVED' ? '승인 의견을 입력하세요 (선택)' : '반려 사유를 입력하세요');
    if (comment === null) return; // 사용자가 취소한 경우

    try {
        if (stepOrder === 1) {
            // 일정 승인/반려
            if (decision === 'APPROVED') {
                await API.approveEvent(approvalId, comment);
                alert('승인되었습니다.');
            } else {
                await API.rejectEvent(approvalId, comment);
                alert('반려되었습니다.');
            }
        } else if (stepOrder === 2) {
            // 취소 승인/반려
            if (decision === 'APPROVED') {
                await API.approveCancellation(selectedEvent.id, approverId, comment);
                alert('취소가 승인되었습니다.');
            } else {
                await API.rejectCancellation(selectedEvent.id, approverId, comment);
                alert('취소가 반려되었습니다.');
            }
        }

        // Reload events, pending approvals and close modal
        await loadEvents();
        await loadPendingApprovals();

        // Refresh leave balance if modal is open
        const leaveBalanceModal = document.getElementById('leaveBalanceModal');
        if (leaveBalanceModal && leaveBalanceModal.style.display === 'block') {
            const year = document.getElementById('balanceYear').value;
            await loadLeaveBalances(year);
        }

        closeEventModal();
    } catch (error) {
        console.error('Failed to process approval:', error);
        alert('결재 처리에 실패했습니다.');
    }
}

// Load pending approvals (for demonstration, showing all SUBMITTED events)
// In production, you'd fetch approvals for current user
async function loadPendingApprovals() {
    console.log('==== loadPendingApprovals 함수 호출됨 ====');
    try {
        // Get current selected member ID from myScheduleMember
        const currentMemberId = sessionStorage.getItem('myScheduleMember');
        console.log('현재 멤버 ID:', currentMemberId);

        // Filter SUBMITTED events: show all if no member selected, or filter by member
        const submittedEvents = currentEvents.filter(e => e.status === 'SUBMITTED');
        console.log('SUBMITTED 상태 이벤트 수:', submittedEvents.length);

        // Load approval info for each submitted event
        const allApprovals = await Promise.all(submittedEvents.map(async (event) => {
            try {
                const approvals = await API.getEventApprovals(event.id);
                const pendingApproval = approvals.find(a => !a.decision);
                console.log(`이벤트 ${event.id} (${event.title}):`, {
                    approvals: approvals,
                    pendingApproval: pendingApproval,
                    eventMemberId: event.memberId
                });
                return {
                    ...event,
                    approvals: approvals,
                    pendingApproval: pendingApproval
                };
            } catch (error) {
                console.error(`Failed to load approval for event ${event.id}:`, error);
                return {
                    ...event,
                    approvals: [],
                    pendingApproval: null
                };
            }
        }));

        // If no member is selected (전체 본부), show all submitted events
        if (!currentMemberId) {
            console.log('전체 본부 선택 - 모든 결재 대기 표시');
            pendingApprovals = allApprovals;
        } else {
            // Filter: only show if current member is the requester OR the pending approver
            pendingApprovals = allApprovals.filter(event => {
                const isRequester = event.memberId == currentMemberId;
                const isApprover = event.pendingApproval && event.pendingApproval.approverId == currentMemberId;

                console.log(`이벤트 ${event.id} 필터링:`, {
                    title: event.title,
                    isRequester,
                    isApprover,
                    currentMemberId,
                    eventMemberId: event.memberId,
                    pendingApproverId: event.pendingApproval?.approverId,
                    include: isRequester || isApprover
                });

                return isRequester || isApprover;
            });
        }

        console.log('최종 결재 대기 수:', pendingApprovals.length);

        displayPendingApprovals();
    } catch (error) {
        console.error('Failed to load pending approvals:', error);
    }
}

// Display pending approvals in sidebar
function displayPendingApprovals() {
    const approvalList = document.getElementById('approvalList');
    const approvalCount = document.getElementById('approvalCount');

    approvalCount.textContent = pendingApprovals.length;

    if (pendingApprovals.length === 0) {
        approvalList.innerHTML = '<div class="empty-approvals">결재 요청이 없습니다</div>';
        return;
    }

    const html = pendingApprovals.map(event => {
        const member = currentMembers.find(m => m.id == event.memberId);
        const memberName = member ? member.name : '알 수 없음';
        const department = member ? member.department : '';
        const dateStr = formatDate(event.startDate);

        // 결재자 정보
        let approverInfo = '';
        if (event.pendingApproval) {
            const approver = currentMembers.find(m => m.id == event.pendingApproval.approverId);
            const approverName = approver ? approver.name : '알 수 없음';
            const approverPosition = approver ? approver.position : '';
            const stepLabel = event.pendingApproval.stepOrder === 1 ? '승인 대기' : '취소 승인 대기';
            approverInfo = `<br><span style="color:#059669; font-weight:700;">→ ${approverName}${approverPosition ? ` (${approverPosition})` : ''} ${stepLabel}</span>`;
        }

        const typeColor = event.eventType === 'LEAVE' ? '#ec4899' : '#3b82f6';

        return `
            <div class="approval-item" onclick="openApprovalEvent(${event.id})" style="cursor: pointer; border-left-color: ${typeColor};">
                <div class="text-scroll-container">
                    <p class="text-scroll-content approval-item-title" style="margin: 0 0 6px 0;">${event.title}</p>
                </div>
                <p class="approval-item-info">
                    ${memberName}${department ? ` (${department})` : ''}<br>
                    ${dateStr} · ${event.eventSubtype}${approverInfo}
                </p>
            </div>
        `;
    }).join('');

    approvalList.innerHTML = html;
}

// Open approval event modal
async function openApprovalEvent(eventId) {
    const event = currentEvents.find(e => e.id == eventId);
    if (event) {
        openEventModal(event);
    }
}

// ==================== My Schedule View ====================

// Initialize My Schedule View
function initMyScheduleView() {
    // Populate department selects
    populateMyScheduleDepts();

    // Load saved selection from SessionStorage
    const savedParentDept = sessionStorage.getItem('myScheduleParentDept');
    const savedChildDept = sessionStorage.getItem('myScheduleChildDept');
    const savedMember = sessionStorage.getItem('myScheduleMember');

    if (savedParentDept) {
        document.getElementById('myScheduleParentDept').value = savedParentDept;
        populateMyScheduleChildDepts(savedParentDept);

        if (savedChildDept) {
            setTimeout(() => {
                document.getElementById('myScheduleChildDept').value = savedChildDept;
                populateMyScheduleMembers(savedParentDept, savedChildDept);

                if (savedMember) {
                    setTimeout(() => {
                        document.getElementById('myScheduleMember').value = savedMember;
                        loadMyScheduleList(savedMember);
                        loadPendingApprovals();
                    }, 100);
                } else {
                    // 멤버가 선택되지 않았지만 팀이 선택된 경우 팀 일정 표시
                    setTimeout(() => {
                        const teamMembers = currentMembers.filter(m => m.departmentId == savedChildDept);
                        const teamMemberIds = teamMembers.map(m => m.id);
                        const filteredEvents = currentEvents.filter(e => teamMemberIds.includes(e.memberId));

                        document.getElementById('scheduleListTitle').textContent = '📝 팀 일정';
                        if (filteredEvents.length > 0) {
                            displayScheduleList(filteredEvents);
                        } else {
                            document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">팀 일정이 없습니다</div>';
                        }
                    }, 100);
                }
            }, 50);
        } else if (!savedChildDept && !savedMember) {
            // 본부만 선택된 경우 본부 일정 표시
            setTimeout(() => {
                const childDepts = currentDepartments.filter(d => d.parentId == savedParentDept);
                const childDeptIds = childDepts.map(d => d.id);
                const deptMembers = currentMembers.filter(m => childDeptIds.includes(m.departmentId));
                const deptMemberIds = deptMembers.map(m => m.id);
                const filteredEvents = currentEvents.filter(e => deptMemberIds.includes(e.memberId));

                document.getElementById('scheduleListTitle').textContent = '📝 본부 일정';
                if (filteredEvents.length > 0) {
                    displayScheduleList(filteredEvents);
                } else {
                    document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">본부 일정이 없습니다</div>';
                }
            }, 100);
        }
    }

    // Event listeners
    document.getElementById('myScheduleParentDept').addEventListener('change', handleMyScheduleParentDeptChange);
    document.getElementById('myScheduleChildDept').addEventListener('change', handleMyScheduleChildDeptChange);
    document.getElementById('myScheduleMember').addEventListener('change', handleMyScheduleMemberChange);

    // Tab event listeners
    document.querySelectorAll('.schedule-tab').forEach(tab => {
        tab.addEventListener('click', handleScheduleTabChange);
    });
}

// Populate my schedule parent departments
function populateMyScheduleDepts() {
    const select = document.getElementById('myScheduleParentDept');
    select.innerHTML = '<option value="">전체 본부</option>';

    const topLevelDepts = currentDepartments.filter(d => d.parentId === null || d.depth === 0);
    topLevelDepts.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.id;
        option.textContent = dept.name;
        select.appendChild(option);
    });
}

// Populate my schedule child departments
function populateMyScheduleChildDepts(parentId) {
    const select = document.getElementById('myScheduleChildDept');
    select.innerHTML = '<option value="">전체 팀</option>';

    if (!parentId) {
        document.getElementById('myScheduleMember').innerHTML = '<option value="">전체 멤버</option>';
        return;
    }

    const childDepts = currentDepartments.filter(d => d.parentId == parentId);
    childDepts.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.id;
        option.textContent = dept.name;
        select.appendChild(option);
    });
}

// Populate my schedule members
function populateMyScheduleMembers(parentId, childId) {
    const select = document.getElementById('myScheduleMember');
    select.innerHTML = '<option value="">전체 멤버</option>';

    let filteredMembers = currentMembers.filter(m => m.isActive);

    if (childId) {
        filteredMembers = filteredMembers.filter(m => m.departmentId == childId);
    } else if (parentId) {
        const childDepts = currentDepartments.filter(d => d.parentId == parentId);
        const childDeptIds = childDepts.map(d => d.id);
        filteredMembers = filteredMembers.filter(m => childDeptIds.includes(m.departmentId));
    }

    filteredMembers.forEach(member => {
        const option = document.createElement('option');
        option.value = member.id;
        option.textContent = `${member.name} (${member.department || '부서 없음'})`;
        select.appendChild(option);
    });
}

// Handle my schedule parent dept change
function handleMyScheduleParentDeptChange(e) {
    const parentId = e.target.value;
    sessionStorage.setItem('myScheduleParentDept', parentId);
    sessionStorage.removeItem('myScheduleChildDept');
    sessionStorage.removeItem('myScheduleMember');

    populateMyScheduleChildDepts(parentId);

    // Update calendar filter
    selectedParentDeptId = parentId || null;
    selectedChildDeptId = null;
    displayEvents();

    // Show parent dept events when parent dept is selected
    if (parentId) {
        document.getElementById('scheduleListTitle').textContent = '📝 본부 일정';
        document.getElementById('scheduleTabs').style.display = 'none';

        // 선택된 본부의 모든 멤버 일정
        const childDepts = currentDepartments.filter(d => d.parentId == parentId);
        console.log('본부 선택 - childDepts:', childDepts);
        const childDeptIds = childDepts.map(d => d.id);
        console.log('본부 선택 - childDeptIds:', childDeptIds);
        const deptMembers = currentMembers.filter(m => childDeptIds.includes(m.departmentId));
        console.log('본부 선택 - deptMembers:', deptMembers);
        const deptMemberIds = deptMembers.map(m => m.id);
        console.log('본부 선택 - deptMemberIds:', deptMemberIds);
        const filteredEvents = currentEvents.filter(e => deptMemberIds.includes(e.memberId));
        console.log('본부 선택 - filteredEvents:', filteredEvents);

        if (filteredEvents.length > 0) {
            displayScheduleList(filteredEvents);
        } else {
            document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">본부 일정이 없습니다</div>';
        }
    } else {
        document.getElementById('scheduleListTitle').textContent = '📝 일정';
        document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">본부를 선택해주세요</div>';
    }
}

// Handle my schedule child dept change
function handleMyScheduleChildDeptChange(e) {
    const childId = e.target.value;
    const parentId = document.getElementById('myScheduleParentDept').value;
    sessionStorage.setItem('myScheduleChildDept', childId);
    sessionStorage.removeItem('myScheduleMember');

    populateMyScheduleMembers(parentId, childId);

    // Update calendar filter
    selectedChildDeptId = childId || null;
    displayEvents();

    // Show child dept (team) events when child dept is selected
    if (childId) {
        document.getElementById('scheduleListTitle').textContent = '📝 팀 일정';
        document.getElementById('scheduleTabs').style.display = 'none';

        // 선택된 팀의 모든 멤버 일정
        const teamMembers = currentMembers.filter(m => m.departmentId == childId);
        console.log('팀 선택 - teamMembers:', teamMembers);
        const teamMemberIds = teamMembers.map(m => m.id);
        console.log('팀 선택 - teamMemberIds:', teamMemberIds);
        const filteredEvents = currentEvents.filter(e => teamMemberIds.includes(e.memberId));
        console.log('팀 선택 - filteredEvents:', filteredEvents);

        if (filteredEvents.length > 0) {
            displayScheduleList(filteredEvents);
        } else {
            document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">팀 일정이 없습니다</div>';
        }
    } else if (parentId) {
        // 팀 선택을 해제했으면 본부 일정 표시
        document.getElementById('scheduleListTitle').textContent = '📝 본부 일정';
        document.getElementById('scheduleTabs').style.display = 'none';

        const childDepts = currentDepartments.filter(d => d.parentId == parentId);
        const childDeptIds = childDepts.map(d => d.id);
        const deptMembers = currentMembers.filter(m => childDeptIds.includes(m.departmentId));
        const deptMemberIds = deptMembers.map(m => m.id);
        const filteredEvents = currentEvents.filter(e => deptMemberIds.includes(e.memberId));

        if (filteredEvents.length > 0) {
            displayScheduleList(filteredEvents);
        } else {
            document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">본부 일정이 없습니다</div>';
        }
    } else {
        document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">팀을 선택해주세요</div>';
    }
}

// Handle my schedule member change
function handleMyScheduleMemberChange(e) {
    const memberId = e.target.value;
    console.log('handleMyScheduleMemberChange - memberId:', memberId);
    sessionStorage.setItem('myScheduleMember', memberId);

    // Reload pending approvals for the selected member
    loadPendingApprovals();

    // Update calendar filter
    displayEvents();

    if (memberId) {
        console.log('memberId 있음 - 개인 일정 로드');
        // Show tabs and update title
        const member = currentMembers.find(m => m.id == memberId);
        console.log('찾은 member:', member);
        if (member) {
            document.getElementById('scheduleListTitle').textContent = `📝 ${member.name}의 일정`;
            document.getElementById('scheduleTabs').style.display = 'flex';
        }

        // Load default tab (member)
        const savedTab = sessionStorage.getItem('myScheduleTab') || 'member';
        console.log('savedTab:', savedTab, '- loadScheduleByTab 호출 예정');
        loadScheduleByTab(savedTab);
    } else {
        // "전체 멤버" 선택 시 팀 전체 일정 표시
        const childDeptId = document.getElementById('myScheduleChildDept').value;
        const parentDeptId = document.getElementById('myScheduleParentDept').value;

        document.getElementById('scheduleListTitle').textContent = '📝 팀 일정';
        document.getElementById('scheduleTabs').style.display = 'none';

        // Show team events
        let filteredEvents = [];
        if (childDeptId) {
            // 선택된 팀의 모든 멤버 일정
            const teamMembers = currentMembers.filter(m => m.departmentId == childDeptId);
            const teamMemberIds = teamMembers.map(m => m.id);
            filteredEvents = currentEvents.filter(e => teamMemberIds.includes(e.memberId));
        } else if (parentDeptId) {
            // 선택된 본부의 모든 멤버 일정
            const childDepts = currentDepartments.filter(d => d.parentId == parentDeptId);
            const childDeptIds = childDepts.map(d => d.id);
            const deptMembers = currentMembers.filter(m => childDeptIds.includes(m.departmentId));
            const deptMemberIds = deptMembers.map(m => m.id);
            filteredEvents = currentEvents.filter(e => deptMemberIds.includes(e.memberId));
        }

        if (filteredEvents.length > 0) {
            displayScheduleList(filteredEvents);
        } else {
            document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">팀 일정이 없습니다</div>';
        }
    }
}

// Handle schedule tab change
function handleScheduleTabChange(e) {
    const tabType = e.target.dataset.tab;

    // Update active tab
    document.querySelectorAll('.schedule-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    e.target.classList.add('active');

    // Save to session storage
    sessionStorage.setItem('myScheduleTab', tabType);

    // Load schedule by tab
    loadScheduleByTab(tabType);
}

// Load schedule by tab type
function loadScheduleByTab(tabType) {
    console.log('loadScheduleByTab 호출됨 - tabType:', tabType);
    const memberId = sessionStorage.getItem('myScheduleMember');
    console.log('memberId:', memberId);
    if (!memberId) return;

    // Update active tab UI
    document.querySelectorAll('.schedule-tab').forEach(tab => {
        tab.classList.remove('active');
        if (tab.dataset.tab === tabType) {
            tab.classList.add('active');
        }
    });

    const member = currentMembers.find(m => m.id == memberId);
    if (!member) return;

    const parentDeptId = document.getElementById('myScheduleParentDept').value;
    const childDeptId = document.getElementById('myScheduleChildDept').value;

    let filteredEvents = [];

    if (tabType === 'member') {
        // 개인 일정: 선택한 멤버의 일정만
        filteredEvents = currentEvents.filter(e => e.memberId == memberId);
        console.log('개인 일정 필터링 결과:', filteredEvents.length);
    } else if (tabType === 'team') {
        // 팀 일정: 선택한 팀의 모든 멤버 일정
        if (childDeptId) {
            const teamMembers = currentMembers.filter(m => m.departmentId == childDeptId);
            const teamMemberIds = teamMembers.map(m => m.id);
            filteredEvents = currentEvents.filter(e => teamMemberIds.includes(e.memberId));
        } else {
            document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">팀을 선택해주세요</div>';
            return;
        }
    } else if (tabType === 'dept') {
        // 본부 일정: 선택한 본부의 모든 멤버 일정
        if (parentDeptId) {
            const childDepts = currentDepartments.filter(d => d.parentId == parentDeptId);
            const childDeptIds = childDepts.map(d => d.id);
            const deptMembers = currentMembers.filter(m => childDeptIds.includes(m.departmentId));
            const deptMemberIds = deptMembers.map(m => m.id);
            filteredEvents = currentEvents.filter(e => deptMemberIds.includes(e.memberId));
        } else {
            document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">본부를 선택해주세요</div>';
            return;
        }
    }

    displayScheduleList(filteredEvents);
}

// Load my schedule list for selected member (wrapper for backward compatibility)
async function loadMyScheduleList(memberId) {
    if (memberId) {
        // Show tabs and update title
        const member = currentMembers.find(m => m.id == memberId);
        if (member) {
            document.getElementById('scheduleListTitle').textContent = `📝 ${member.name}의 일정`;
            document.getElementById('scheduleTabs').style.display = 'flex';
        }

        // Load saved tab or default to member
        const savedTab = sessionStorage.getItem('myScheduleTab') || 'member';
        loadScheduleByTab(savedTab);
    } else {
        document.getElementById('scheduleListTitle').textContent = '📝 일정';
        document.getElementById('scheduleTabs').style.display = 'none';
        document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">멤버를 선택해주세요</div>';
    }
}

// Display schedule list with vertical scroll for more items
function displayScheduleList(events) {
    if (!events || events.length === 0) {
        document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">일정이 없습니다</div>';
        return;
    }

    // Filter events: only show events within 1 month before and after today
    const today = new Date();
    const oneMonthBefore = new Date(today.getFullYear(), today.getMonth() - 1, today.getDate());
    const oneMonthAfter = new Date(today.getFullYear(), today.getMonth() + 1, today.getDate());

    const filteredEvents = events.filter(event => {
        const eventDate = new Date(event.startDate);
        return eventDate >= oneMonthBefore && eventDate <= oneMonthAfter;
    });

    if (filteredEvents.length === 0) {
        document.getElementById('myScheduleList').innerHTML = '<div class="empty-approvals">최근 한 달 이내 일정이 없습니다</div>';
        return;
    }

    // Sort by start date (most recent first)
    filteredEvents.sort((a, b) => new Date(b.startDate) - new Date(a.startDate));

    const initialLimit = 5;
    const totalCount = filteredEvents.length;
    const showExpandButton = totalCount >= initialLimit;

    console.log('displayScheduleList - totalCount:', totalCount, 'showExpandButton:', showExpandButton);

    // Create vertical scroll container with flex: 1 to fill available space
       let html = `<div id="scheduleListItems" style="flex: 1; overflow-y: auto; padding-right: 5px;">`;

    filteredEvents.forEach((event, index) => {
        const isSchedule = event.eventType === 'SCHEDULE';
        const statusIcon = isSchedule ? '📅' : getStatusIcon(event.status);
        const statusColor = isSchedule ? '#3b82f6' : getStatusColor(event.status);
        const statusText = isSchedule ? '회의' : getStatusText(event.status);
        const typeColor = event.eventType === 'LEAVE' ? '#ec4899' : '#3b82f6';
        const startDate = formatDate(event.startDate);
        const endDate = event.startDate === event.endDate ? '' : ` ~ ${formatDate(event.endDate)}`;
        const timeInfo = event.startTime && event.endTime ? ` ${event.startTime}-${event.endTime}` : '';

        // Determine if we should show action buttons (only for LEAVE events)
        const isDraft = event.status === 'DRAFT';
        const isApproved = event.status === 'APPROVED';
        const isLeave = event.eventType === 'LEAVE';
        let actionButton = '';

        if (isDraft && isLeave) {
            // Show approval request button for DRAFT LEAVE events
            actionButton = `
                <button onclick="event.stopPropagation(); submitEventForApproval(${event.id});"
                    style="margin-top: 8px; padding: 4px 12px; background: #0369a1; color: white; border: none; border-radius: 4px; font-size: 12px; cursor: pointer;">
                    결재 요청
                </button>
            `;
        } else if (isApproved && isLeave) {
            // Show cancellation request button for APPROVED LEAVE events
            actionButton = `
                <button onclick="event.stopPropagation(); requestCancellationById(${event.id});"
                    style="margin-top: 8px; padding: 4px 12px; background: #dc2626; color: white; border: none; border-radius: 4px; font-size: 12px; cursor: pointer;">
                    취소 신청
                </button>
            `;
        }

        const displayTitle = event.title || '제목 없음';

        html += `
            <div class="approval-item" onclick="openApprovalEvent(${event.id})" style="cursor: pointer; border-left-color: ${typeColor};">
                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 6px;">
                    <div style="flex: 1; margin-right: 8px; overflow: hidden;">
                        <div title="${displayTitle}" style="font-weight: 600; color: #1f2937; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                            ${displayTitle}
                        </div>
                    </div>
                    <span style="background: ${statusColor}; color: white; font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 10px; white-space: nowrap;">
                        ${statusIcon} ${statusText}
                    </span>
                </div>
                <div style="font-size: 14px; font-weight: 700; color: #374151; margin: 4px 0;">
                    ${event.memberName || ''} / ${event.eventSubtype || event.eventType}
                </div>
                <div style="font-size: 14px; font-weight: 600; color: #1f2937; margin: 0; line-height: 1.5;">
                    📅 ${startDate}${endDate}${timeInfo}
                </div>
                ${actionButton}
            </div>
        `;
    });

    html += '</div>';

    // Add scroll info OUTSIDE the scroll container if there are more than 5 items
    if (showExpandButton) {
        html += `
            <div style="text-align: left; margin-top: 7px; color: #717171; font-size: 11px;">
                <span style="display: inline-block; width: 20px; text-align: center;">↕</span>
                <span>스크롤하여 ${totalCount}개 일정을 모두 확인하세요</span>
            </div>
        `;
    }

    document.getElementById('myScheduleList').innerHTML = html;
}

// Helper function to get status icon
function getStatusIcon(status) {
    const icons = {
        'DRAFT': '📝',
        'SUBMITTED': '📤',
        'APPROVED': '✅',
        'REJECTED': '❌',
        'CANCELED': '🚫'
    };
    return icons[status] || '📝';
}

// Helper function to get status color
function getStatusColor(status) {
    const colors = {
        'DRAFT': '#64748b',
        'SUBMITTED': '#0369a1',
        'APPROVED': '#065f46',
        'REJECTED': '#991b1b',
        'CANCELED': '#7c2d12'
    };
    return colors[status] || '#64748b';
}
