
// Check auth and load data on page load
checkAuth();
loadTasks();
loadProjectsForSelect();
loadUsersForSelect();

// Hide create button if not authorized
if (!hasAnyRole('ADMIN', 'MANAGER')) {
    document.getElementById('createTaskBtn').style.display = 'none';
}

let currentTaskId = null;

// Load all tasks
async function loadTasks() {
    try {
        const tasks = await apiCall('/tasks/my-tasks?size=100');

        // Clear columns
        document.getElementById('todoTasks').innerHTML = '';
        document.getElementById('inProgressTasks').innerHTML = '';
        document.getElementById('doneTasks').innerHTML = '';

        // Group tasks by status
        tasks.content.forEach(task => {
            const taskCard = createTaskCard(task);

            switch (task.status) {
                case 'TODO':
                    document.getElementById('todoTasks').appendChild(taskCard);
                    break;
                case 'IN_PROGRESS':
                    document.getElementById('inProgressTasks').appendChild(taskCard);
                    break;
                case 'DONE':
                    document.getElementById('doneTasks').appendChild(taskCard);
                    break;
            }
        });

        // Show empty message if no tasks
        ['todoTasks', 'inProgressTasks', 'doneTasks'].forEach(columnId => {
            const column = document.getElementById(columnId);
            if (column.children.length === 0) {
                column.innerHTML = '<p class="text-muted text-center">No tasks</p>';
            }
        });
    } catch (error) {
        showAlert('Error loading tasks: ' + error.message);
    }
}

// Create task card element
function createTaskCard(task) {
    const div = document.createElement('div');
    div.className = `task-card task-priority-${task.priority}`;
    div.innerHTML = `
        <div class="d-flex justify-content-between align-items-start">
            <h6 class="mb-1">${task.title}</h6>
            ${hasAnyRole('ADMIN', 'MANAGER') ? `
                <div class="dropdown">
                    <button class="btn btn-sm btn-link p-0" type="button" data-bs-toggle="dropdown" onclick="event.stopPropagation()">
                        <i class="bi bi-three-dots-vertical">⋮</i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item" href="#" onclick="event.stopPropagation(); editTask(${task.id})">Edit</a></li>
                        <li><a class="dropdown-item text-danger" href="#" onclick="event.stopPropagation(); deleteTask(${task.id})">Delete</a></li>
                    </ul>
                </div>
            ` : ''}
        </div>
        <p class="mb-1 small text-primary">${task.projectName}</p>
        ${task.assignedToName ? `<p class="mb-1 small"><i class="bi bi-person"></i> ${task.assignedToName}</p>` : ''}
        ${task.dueDate ? `<p class="mb-0 small text-muted"><i class="bi bi-calendar"></i> Due: ${formatDate(task.dueDate)}</p>` : ''}
        <div class="mt-2">
            <select class="form-select form-select-sm" onchange="updateTaskStatus(${task.id}, this.value)" onclick="event.stopPropagation()" ${!hasAnyRole('ADMIN', 'MANAGER') && task.assignedToId !== getCurrentUserId() ? 'disabled' : ''}>
                <option value="TODO" ${task.status === 'TODO' ? 'selected' : ''}>TODO</option>
                <option value="IN_PROGRESS" ${task.status === 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                <option value="DONE" ${task.status === 'DONE' ? 'selected' : ''}>DONE</option>
            </select>
        </div>
    `;

    div.onclick = (e) => {
        if (e.target.tagName !== 'SELECT' && !e.target.closest('.dropdown')) {
            viewTaskDetails(task.id);
        }
    };

    return div;
}

// Load projects for select
async function loadProjectsForSelect() {
    try {
        const projects = await apiCall('/projects');
        const select = document.getElementById('taskProject');
        select.innerHTML = '<option value="">Select Project</option>' +
            projects.map(project =>
                `<option value="${project.id}">${project.name}</option>`
            ).join('');
    } catch (error) {
        console.error('Error loading projects:', error);
    }
}

// Load users for select
async function loadUsersForSelect() {
    try {
        const users = await apiCall('/users');
        const select = document.getElementById('taskAssignee');
        select.innerHTML = '<option value="">Unassigned</option>' +
            users.map(user =>
                `<option value="${user.id}">${user.firstName} ${user.lastName}</option>`
            ).join('');
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

// Task form submission
document.getElementById('taskForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const taskId = document.getElementById('taskId').value;
    const assigneeId = document.getElementById('taskAssignee').value;
    const dueDateValue = document.getElementById('taskDueDate').value;

    const taskData = {
        title: document.getElementById('taskTitle').value,
        description: document.getElementById('taskDescription').value,
        projectId: parseInt(document.getElementById('taskProject').value),
        assignedToId: assigneeId ? parseInt(assigneeId) : null,
        status: document.getElementById('taskStatus').value,
        priority: document.getElementById('taskPriority').value,
        dueDate: dueDateValue || null
    };

    try {
        if (taskId) {
            await apiCall(`/tasks/${taskId}`, 'PUT', taskData);
            showAlert('Task updated successfully', 'success');
        } else {
            await apiCall('/tasks', 'POST', taskData);
            showAlert('Task created successfully', 'success');
        }

        // Close modal and reload tasks
        bootstrap.Modal.getInstance(document.getElementById('taskModal')).hide();
        loadTasks();
    } catch (error) {
        showAlert('Error saving task: ' + error.message);
    }
});

// Edit task
async function editTask(taskId) {
    try {
        const task = await apiCall(`/tasks/${taskId}`);

        document.getElementById('taskModalTitle').textContent = 'Edit Task';
        document.getElementById('taskId').value = task.id;
        document.getElementById('taskTitle').value = task.title;
        document.getElementById('taskDescription').value = task.description || '';
        document.getElementById('taskProject').value = task.projectId;
        document.getElementById('taskAssignee').value = task.assignedToId || '';
        document.getElementById('taskStatus').value = task.status;
        document.getElementById('taskPriority').value = task.priority;

        // Handle date - dates come as "yyyy-MM-dd" format from backend
        if (task.dueDate) {
            const dateValue = task.dueDate.includes('T') ? task.dueDate.split('T')[0] : task.dueDate;
            document.getElementById('taskDueDate').value = dateValue;
        } else {
            document.getElementById('taskDueDate').value = '';
        }

        // Show modal
        new bootstrap.Modal(document.getElementById('taskModal')).show();
    } catch (error) {
        showAlert('Error loading task: ' + error.message);
    }
}

// Delete task
async function deleteTask(taskId) {
    if (!confirm('Are you sure you want to delete this task?')) {
        return;
    }

    try {
        await apiCall(`/tasks/${taskId}`, 'DELETE');
        showAlert('Task deleted successfully', 'success');
        loadTasks();
    } catch (error) {
        showAlert('Error deleting task: ' + error.message);
    }
}

// Update task status
async function updateTaskStatus(taskId, newStatus) {
    try {
        await apiCall(`/tasks/${taskId}/status?status=${newStatus}`, 'PUT');
        loadTasks();
    } catch (error) {
        showAlert('Error updating task status: ' + error.message);
        loadTasks(); // Reload to reset the select
    }
}

// View task details
async function viewTaskDetails(taskId) {
    try {
        currentTaskId = taskId;
        const task = await apiCall(`/tasks/${taskId}`);

        document.getElementById('taskDetailsTitle').textContent = task.title;
        document.getElementById('taskDetailsContent').innerHTML = `
            <p><strong>Description:</strong> ${task.description || 'No description'}</p>
            <p><strong>Project:</strong> ${task.projectName}</p>
            <p><strong>Status:</strong> <span class="badge badge-${task.status}">${task.status}</span></p>
            <p><strong>Priority:</strong> <span class="badge bg-${getPriorityColor(task.priority)}">${task.priority}</span></p>
            <p><strong>Assigned to:</strong> ${task.assignedToName || 'Unassigned'}</p>
            <p><strong>Created by:</strong> ${task.createdByName}</p>
            ${task.dueDate ? `<p><strong>Due Date:</strong> ${formatDate(task.dueDate)}</p>` : ''}
            <p><strong>Created:</strong> ${formatDate(task.createdAt)}</p>
            <p><strong>Updated:</strong> ${formatDate(task.updatedAt)}</p>

            ${hasAnyRole('ADMIN', 'MANAGER') ? `
                <div class="mt-3">
                    <button class="btn btn-sm btn-primary me-2" onclick="editTaskFromDetails(${task.id})">Edit Task</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteTaskFromDetails(${task.id})">Delete Task</button>
                </div>
            ` : ''}
        `;

        // Load comments
        loadComments(taskId);

        // Show modal
        new bootstrap.Modal(document.getElementById('taskDetailsModal')).show();
    } catch (error) {
        showAlert('Error loading task details: ' + error.message);
    }
}

// Helper function to get priority color
function getPriorityColor(priority) {
    switch (priority) {
        case 'HIGH': return 'danger';
        case 'MEDIUM': return 'warning';
        case 'LOW': return 'success';
        default: return 'secondary';
    }
}

// Edit task from details modal
async function editTaskFromDetails(taskId) {
    // Close details modal
    bootstrap.Modal.getInstance(document.getElementById('taskDetailsModal')).hide();

    // Wait for modal to close
    setTimeout(() => {
        editTask(taskId);
    }, 300);
}

// Delete task from details modal
async function deleteTaskFromDetails(taskId) {
    bootstrap.Modal.getInstance(document.getElementById('taskDetailsModal')).hide();

    // Delete task
    deleteTask(taskId);
}

// Load comments for task
async function loadComments(taskId) {
    try {
        const comments = await apiCall(`/comments/task/${taskId}`);
        const commentsList = document.getElementById('commentsList');

        if (comments.length === 0) {
            commentsList.innerHTML = '<p class="text-muted">No comments yet</p>';
        } else {
            commentsList.innerHTML = comments.map(comment => `
                <div class="comment-item">
                    <div class="d-flex justify-content-between">
                        <span class="comment-author">${comment.userName}</span>
                        <span class="comment-time">${formatDate(comment.createdAt)}</span>
                    </div>
                    <div class="mt-1">${comment.content}</div>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Error loading comments:', error);
    }
}

// Comment form submission
document.getElementById('commentForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const content = document.getElementById('commentContent').value.trim();
    if (!content) return;

    try {
        await apiCall('/comments', 'POST', {
            content: content,
            taskId: currentTaskId
        });

        document.getElementById('commentContent').value = '';
        loadComments(currentTaskId);
    } catch (error) {
        showAlert('Error posting comment: ' + error.message);
    }
});

// Get current user ID
function getCurrentUserId() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.id || null;
}

// Reset form when modal is closed
document.getElementById('taskModal').addEventListener('hidden.bs.modal', () => {
    document.getElementById('taskForm').reset();
    document.getElementById('taskId').value = '';
    document.getElementById('taskModalTitle').textContent = 'Create Task';
});