// API Configuration
const API_BASE_URL = '/api';
const USE_BACKEND = true; // Set to false to use localStorage

// API Service
const API = {
    // Helper function for API calls
    async request(endpoint, options = {}) {
        if (!USE_BACKEND) {
            return this.localStorage(endpoint, options);
        }

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Request failed');
            }

            // For DELETE requests or 204 No Content, don't try to parse JSON
            if (options.method === 'DELETE' || response.status === 204) {
                return null;
            }

            // Check if response has content before parsing JSON
            const text = await response.text();
            if (!text || text.trim() === '') {
                return null;
            }

            // Try to parse JSON
            try {
                return JSON.parse(text);
            } catch (e) {
                console.warn('Response is not JSON:', text);
                return null;
            }
        } catch (error) {
            console.error('API Error:', error);
            if (!USE_BACKEND) {
                return this.localStorage(endpoint, options);
            }
            throw error;
        }
    },

    // LocalStorage fallback
    localStorage(endpoint, options) {
        const storage = {
            members: JSON.parse(localStorage.getItem('members') || '[]'),
            events: JSON.parse(localStorage.getItem('events') || '[]'),
            approvals: JSON.parse(localStorage.getItem('approvals') || '[]')
        };

        const method = options.method || 'GET';
        const body = options.body ? JSON.parse(options.body) : null;

        if (endpoint.startsWith('/members')) {
            return this.handleMembersLocalStorage(endpoint, method, body, storage);
        } else if (endpoint.startsWith('/events')) {
            return this.handleEventsLocalStorage(endpoint, method, body, storage);
        } else if (endpoint.startsWith('/approvals')) {
            return this.handleApprovalsLocalStorage(endpoint, method, body, storage);
        }
    },

    handleMembersLocalStorage(endpoint, method, body, storage) {
        if (method === 'GET') {
            if (endpoint.includes('leave-balances')) {
                const year = new URL(window.location.origin + endpoint).searchParams.get('year');
                return storage.members.filter(m => m.isActive).map(member => {
                    const usedLeave = storage.events
                        .filter(e => e.memberId === member.id && e.eventType === 'LEAVE' &&
                                e.status === 'APPROVED' && new Date(e.startDate).getFullYear() == year)
                        .reduce((sum, e) => sum + parseFloat(e.leaveAmount || 0), 0);
                    return {
                        memberId: member.id,
                        memberName: member.name,
                        department: member.department,
                        position: member.position,
                        annualLeaveGranted: member.annualLeaveGranted,
                        usedLeave: usedLeave,
                        remainingLeave: member.annualLeaveGranted - usedLeave
                    };
                });
            }
            return storage.members.filter(m => !endpoint.includes('active=true') || m.isActive);
        } else if (method === 'POST') {
            const newMember = { ...body, id: Date.now(), isActive: true };
            storage.members.push(newMember);
            localStorage.setItem('members', JSON.stringify(storage.members));
            return newMember;
        } else if (method === 'PUT') {
            const id = parseInt(endpoint.split('/').pop());
            const index = storage.members.findIndex(m => m.id === id);
            if (index !== -1) {
                storage.members[index] = { ...storage.members[index], ...body };
                localStorage.setItem('members', JSON.stringify(storage.members));
                return storage.members[index];
            }
        } else if (method === 'DELETE') {
            const id = parseInt(endpoint.split('/').pop());
            storage.members = storage.members.filter(m => m.id !== id);
            localStorage.setItem('members', JSON.stringify(storage.members));
            return {};
        }
    },

    handleEventsLocalStorage(endpoint, method, body, storage) {
        if (method === 'GET') {
            let events = storage.events;
            if (endpoint.includes('startDate') && endpoint.includes('endDate')) {
                const params = new URL(window.location.origin + endpoint).searchParams;
                const startDate = params.get('startDate');
                const endDate = params.get('endDate');
                events = events.filter(e => e.startDate <= endDate && e.endDate >= startDate);
            }
            return events.map(e => ({
                ...e,
                memberName: storage.members.find(m => m.id === e.memberId)?.name
            }));
        } else if (method === 'POST') {
            if (endpoint.includes('/submit')) {
                const id = parseInt(endpoint.split('/')[2]);
                const event = storage.events.find(e => e.id === id);
                if (event) {
                    event.status = 'SUBMITTED';
                    const approval = {
                        id: Date.now(),
                        eventId: id,
                        stepOrder: 1,
                        approverId: body.approverId,
                        decision: null,
                        submittedAt: new Date().toISOString()
                    };
                    storage.approvals.push(approval);
                    localStorage.setItem('events', JSON.stringify(storage.events));
                    localStorage.setItem('approvals', JSON.stringify(storage.approvals));
                    return event;
                }
            } else if (endpoint.includes('/approve')) {
                const id = parseInt(endpoint.split('/')[2]);
                const event = storage.events.find(e => e.id === id);
                const approval = storage.approvals.find(a => a.eventId === id && a.stepOrder === 1);
                if (event && approval) {
                    event.status = 'APPROVED';
                    approval.decision = 'APPROVED';
                    approval.comment = body.comment;
                    approval.decidedAt = new Date().toISOString();
                    localStorage.setItem('events', JSON.stringify(storage.events));
                    localStorage.setItem('approvals', JSON.stringify(storage.approvals));
                    return event;
                }
            } else if (endpoint.includes('/reject')) {
                const id = parseInt(endpoint.split('/')[2]);
                const event = storage.events.find(e => e.id === id);
                const approval = storage.approvals.find(a => a.eventId === id && a.stepOrder === 1);
                if (event && approval) {
                    event.status = 'REJECTED';
                    approval.decision = 'REJECTED';
                    approval.comment = body.comment;
                    approval.decidedAt = new Date().toISOString();
                    localStorage.setItem('events', JSON.stringify(storage.events));
                    localStorage.setItem('approvals', JSON.stringify(storage.approvals));
                    return event;
                }
            } else {
                const newEvent = { ...body, id: Date.now(), status: 'DRAFT' };
                storage.events.push(newEvent);
                localStorage.setItem('events', JSON.stringify(storage.events));
                return newEvent;
            }
        } else if (method === 'PUT') {
            const id = parseInt(endpoint.split('/').pop());
            const index = storage.events.findIndex(e => e.id === id);
            if (index !== -1) {
                storage.events[index] = { ...storage.events[index], ...body };
                localStorage.setItem('events', JSON.stringify(storage.events));
                return storage.events[index];
            }
        } else if (method === 'DELETE') {
            const id = parseInt(endpoint.split('/').pop());
            storage.events = storage.events.filter(e => e.id !== id);
            localStorage.setItem('events', JSON.stringify(storage.events));
            return {};
        }
    },

    handleApprovalsLocalStorage(endpoint, method, body, storage) {
        if (method === 'GET') {
            if (endpoint.includes('/event/')) {
                const eventId = parseInt(endpoint.split('/')[3]);
                return storage.approvals.filter(a => a.eventId === eventId)
                    .map(a => ({
                        ...a,
                        approverName: storage.members.find(m => m.id === a.approverId)?.name
                    }));
            } else if (endpoint.includes('/pending')) {
                const approverId = parseInt(endpoint.split('/')[3]);
                return storage.approvals.filter(a => a.approverId === approverId && !a.decision)
                    .map(a => ({
                        ...a,
                        approverName: storage.members.find(m => m.id === a.approverId)?.name
                    }));
            }
        }
        return [];
    },

    // Members API
    getMembers(activeOnly = false) {
        const query = activeOnly ? '?active=true' : '';
        return this.request(`/members${query}`);
    },

    getMember(id) {
        return this.request(`/members/${id}`);
    },

    createMember(data) {
        return this.request('/members', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    updateMember(id, data) {
        return this.request(`/members/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    deleteMember(id) {
        return this.request(`/members/${id}`, {
            method: 'DELETE'
        });
    },

    getLeaveBalance(memberId, year) {
        return this.request(`/members/${memberId}/leave-balance?year=${year}`);
    },

    getAllLeaveBalances(year) {
        return this.request(`/members/leave-balances?year=${year}`);
    },

    // Events API
    getEvents(startDate = null, endDate = null) {
        let query = '';
        if (startDate && endDate) {
            query = `?startDate=${startDate}&endDate=${endDate}`;
        }
        return this.request(`/events${query}`);
    },

    getEvent(id) {
        return this.request(`/events/${id}`);
    },

    createEvent(data) {
        return this.request('/events', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    updateEvent(id, data) {
        return this.request(`/events/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    deleteEvent(id) {
        return this.request(`/events/${id}`, {
            method: 'DELETE'
        });
    },

    submitEvent(id, approverId) {
        return this.request(`/events/${id}/submit`, {
            method: 'POST',
            body: JSON.stringify({ approverId })
        });
    },

    approveEvent(id, approverId, comment = '') {
        return this.request(`/events/${id}/approve`, {
            method: 'POST',
            body: JSON.stringify({ approverId, comment })
        });
    },

    rejectEvent(id, approverId, comment = '') {
        return this.request(`/events/${id}/reject`, {
            method: 'POST',
            body: JSON.stringify({ approverId, comment })
        });
    },

    // Approvals API
    getEventApprovals(eventId) {
        return this.request(`/approvals/event/${eventId}`);
    },

    getPendingApprovals(approverId) {
        return this.request(`/approvals/approver/${approverId}/pending`);
    },

    approveEvent(approvalId, comment = '') {
        return this.request(`/approvals/${approvalId}/approve`, {
            method: 'POST',
            body: JSON.stringify({ comment })
        });
    },

    rejectEvent(approvalId, comment = '') {
        return this.request(`/approvals/${approvalId}/reject`, {
            method: 'POST',
            body: JSON.stringify({ comment })
        });
    },

    requestCancellation(eventId, approverId) {
        return this.request(`/events/${eventId}/request-cancellation`, {
            method: 'POST',
            body: JSON.stringify({ approverId })
        });
    },

    approveCancellation(eventId, approverId, comment = '') {
        return this.request(`/events/${eventId}/approve-cancellation`, {
            method: 'POST',
            body: JSON.stringify({ approverId, comment })
        });
    },

    rejectCancellation(eventId, approverId, comment = '') {
        return this.request(`/events/${eventId}/reject-cancellation`, {
            method: 'POST',
            body: JSON.stringify({ approverId, comment })
        });
    },

    // Departments API
    getDepartments() {
        return this.request('/departments');
    },

    getTopLevelDepartments() {
        return this.request('/departments/top-level');
    },

    getChildDepartments(parentId) {
        return this.request(`/departments/parent/${parentId}`);
    },

    getDepartment(id) {
        return this.request(`/departments/${id}`);
    },

    createDepartment(data) {
        return this.request('/departments', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    updateDepartment(id, data) {
        return this.request(`/departments/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    deleteDepartment(id) {
        return this.request(`/departments/${id}`, {
            method: 'DELETE'
        });
    }
};
