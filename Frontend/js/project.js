// Check auth and load data on page load
checkAuth();
loadProjects();
loadUsers();

// Hide create button if not authorized
if (!hasAnyRole('ADMIN', 'MANAGER')) {
    document.getElementById('createProjectBtn').style.display = 'none';
}

// Load all projects
async function loadProjects() {
    try {
        const projects = await apiCall('/projects');
        const container = document.getElementById('projectsContainer');

        if (projects.length === 0) {
            container.innerHTML = '<div class="col-12"><p class="text-center text-muted">No projects found</p></div>';
        } else {
            container.innerHTML = projects.map(project => `
                <div class="col-md-6 col-lg-4 mb-3">
                    <div class="card project-card h-100">
                        <div class="card-body">
                            <h5 class="card-title">${project.name}</h5>
                            <p class="card-text">${project.description || 'No description'}</p>
                            <div class="mb-2">
                                <small class="text-muted">Status: ${project.status}</small>
                            </div>
                            <div class="mb-2">
                                <small class="text-muted">Created by: ${project.createdByName}</small>
                            </div>
                            ${project.startDate ? `<div class="mb-2"><small class="text-muted">Start: ${formatDate(project.startDate)}</small></div>` : ''}
                            ${project.endDate ? `<div class="mb-2"><small class="text-muted">End: ${formatDate(project.endDate)}</small></div>` : ''}
                        </div>
                        <div class="card-footer bg-transparent">
                            <div class="btn-group btn-group-sm w-100" role="group">
                                <button class="btn btn-outline-primary" onclick="viewProject(${project.id})">View</button>
                                ${hasAnyRole('ADMIN', 'MANAGER') ? `
                                    <button class="btn btn-outline-secondary" onclick="editProject(${project.id})">Edit</button>
                                    <button class="btn btn-outline-danger" onclick="deleteProject(${project.id})">Delete</button>
                                ` : ''}
                            </div>
                        </div>
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        showAlert('Error loading projects: ' + error.message);
    }
}

// Load users for assignment
async function loadUsers() {
    try {
        const users = await apiCall('/users');
        const select = document.getElementById('projectUsers');
        select.innerHTML = users.map(user =>
            `<option value="${user.id}">${user.firstName} ${user.lastName} (${user.email})</option>`
        ).join('');
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

// Project form submission
document.getElementById('projectForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const projectId = document.getElementById('projectId').value;
    const selectedUsers = Array.from(document.getElementById('projectUsers').selectedOptions)
        .map(option => parseInt(option.value));

        const projectData = {
            name: document.getElementById('projectName').value,
            description: document.getElementById('projectDescription').value,
            status: 'ACTIVE',  // Add this
            startDate: document.getElementById('startDate').value || null,
            endDate: document.getElementById('endDate').value || null,
            userIds: selectedUsers
        };

    try {
        if (projectId) {
            await apiCall(`/projects/${projectId}`, 'PUT', projectData);
            showAlert('Project updated successfully', 'success');
        } else {
            await apiCall('/projects', 'POST', projectData);
            showAlert('Project created successfully', 'success');
        }

        // Close modal and reload projects
        bootstrap.Modal.getInstance(document.getElementById('projectModal')).hide();
        loadProjects();
    } catch (error) {
        showAlert('Error saving project: ' + error.message);
    }
});

// View project details
async function viewProject(id) {
    try {
        const project = await apiCall(`/projects/${id}`);
        alert(`Project: ${project.name}\nDescription: ${project.description}\nUsers: ${project.users.map(u => u.firstName + ' ' + u.lastName).join(', ')}`);
    } catch (error) {
        showAlert('Error viewing project: ' + error.message);
    }
}
        // Edit project
        async function editProject(id) {
            try {
                const project = await apiCall(`/projects/${id}`);

                document.getElementById('projectModalTitle').textContent = 'Edit Project';
                document.getElementById('projectId').value = project.id;
                document.getElementById('projectName').value = project.name;
                document.getElementById('projectDescription').value = project.description || '';

                document.getElementById('startDate').value = project.startDate ? project.startDate : '';
                document.getElementById('endDate').value = project.endDate ? project.endDate : '';

                // Select assigned users
                const userSelect = document.getElementById('projectUsers');
                Array.from(userSelect.options).forEach(option => {
                    option.selected = project.userIds && project.userIds.includes(parseInt(option.value));
                });

                // Show modal
                new bootstrap.Modal(document.getElementById('projectModal')).show();
            } catch (error) {
                showAlert('Error loading project: ' + error.message);
            }
        }

        // Delete project
        async function deleteProject(id) {
            if (!confirm('Are you sure you want to delete this project?')) {
                return;
            }

            try {
                await apiCall(`/projects/${id}`, 'DELETE');
                showAlert('Project deleted successfully', 'success');
                loadProjects();
            } catch (error) {
                showAlert('Error deleting project: ' + error.message);
            }
        }

        // Reset form when modal is closed
        document.getElementById('projectModal').addEventListener('hidden.bs.modal', () => {
            document.getElementById('projectForm').reset();
            document.getElementById('projectId').value = '';
            document.getElementById('projectModalTitle').textContent = 'Create Project';
        });


        // Helper function to format date for input fields
        function formatDateForInput(dateString) {
            if (!dateString) return '';
            // Handle both "2026-03-31" and "2026-03-31T00:00:00" formats
            if (dateString.includes('T')) {
                return dateString.split('T')[0];
            }
            return dateString;
        }