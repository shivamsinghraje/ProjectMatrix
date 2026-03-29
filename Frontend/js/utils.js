// Check if user is authenticated
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'index.html';
        return false;
    }

    // Set user name in navbar
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userNameElements = document.querySelectorAll('#userName');
    userNameElements.forEach(el => {
        el.textContent = `${user.firstName} ${user.lastName}`;
    });

    // Show/hide admin menu based on role
    if (user.role === 'ADMIN') {
        const adminMenuItem = document.getElementById('adminMenuItem');
        if (adminMenuItem) {
            adminMenuItem.style.display = 'block';
        }
    }

    return true;
}

// Logout function
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'index.html';
}

// Load dashboard data
async function loadDashboard() {
    try {
        // Load projects
        const projects = await apiCall('/projects');
        const projectsList = document.getElementById('projectsList');

        if (projects.length === 0) {
            projectsList.innerHTML = '<p class="text-muted">No projects assigned</p>';
        } else {
            projectsList.innerHTML = projects.slice(0, 5).map(project => `
                <a href="projects.html" class="list-group-item list-group-item-action">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">${project.name}</h6>
                        <small>${project.status}</small>
                    </div>
                    <p class="mb-1">${project.description || 'No description'}</p>
                </a>
            `).join('');
        }

        // Load tasks
        const tasks = await apiCall('/tasks/my-tasks?size=5');
        const tasksList = document.getElementById('tasksList');

        if (tasks.content.length === 0) {
            tasksList.innerHTML = '<p class="text-muted">No tasks assigned</p>';
        } else {
            tasksList.innerHTML = tasks.content.map(task => `
                <a href="tasks.html" class="list-group-item list-group-item-action">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">${task.title}</h6>
                        <span class="badge badge-${task.status}">${task.status}</span>
                    </div>
                    <p class="mb-1">${task.projectName}</p>
                    <small>Priority: ${task.priority}</small>
                </a>
            `).join('');
        }
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

// Check if user has role
function hasRole(role) {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user.role === role;
}

// Check if user has any of the roles
function hasAnyRole(...roles) {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return roles.includes(user.role);
}