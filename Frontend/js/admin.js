// Check auth and admin role
if (!checkAuth() || !hasRole('ADMIN')) {
    window.location.href = 'dashboard.html';
}

loadUsers();

// Load all users
async function loadUsers() {
    try {
        const users = await apiCall('/users');
        const tbody = document.getElementById('usersTableBody');

        if (users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center">No users found</td></tr>';
        } else {
            tbody.innerHTML = users.map(user => `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.firstName} ${user.lastName}</td>
                    <td>${user.email}</td>
                    <td>
                        <select class="form-select form-select-sm" onchange="updateUserRole(${user.id}, this.value)">
                            <option value="USER" ${user.role === 'USER' ? 'selected' : ''}>USER</option>
                            <option value="MANAGER" ${user.role === 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                            <option value="ADMIN" ${user.role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                        </select>
                    </td>
                    <td>
                        <span class="badge ${user.active ? 'bg-success' : 'bg-danger'}">
                            ${user.active ? 'Active' : 'Inactive'}
                        </span>
                    </td>
                    <td>${formatDate(user.createdAt)}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="editUser(${user.id})">Edit</button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteUser(${user.id})">Delete</button>
                    </td>
                </tr>
            `).join('');
        }
    } catch (error) {
        showAlert('Error loading users: ' + error.message);
    }
}

// User form submission
document.getElementById('userForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const userId = document.getElementById('userId').value;
    const password = document.getElementById('userPassword').value;

    const userData = {
        firstName: document.getElementById('userFirstName').value,
        lastName: document.getElementById('userLastName').value,
        email: document.getElementById('userEmail').value,
        role: document.getElementById('userRole').value,
        active: document.getElementById('userActive').checked
    };

    // Only include password if it is provided
    if (password) {
        userData.password = password;
    }

    try {
        if (userId) {
            await apiCall(`/users/${userId}`, 'PUT', userData);
            showAlert('User updated successfully', 'success');
        } else {
            // Password is required for new users
            if (!password) {
                showAlert('Password is required for new users');
                return;
            }
            userData.password = password;
            await apiCall('/users', 'POST', userData);
            showAlert('User created successfully', 'success');
        }

        // Close modal and reload users
        bootstrap.Modal.getInstance(document.getElementById('userModal')).hide();
        loadUsers();
    } catch (error) {
        showAlert('Error saving user: ' + error.message);
    }
});

// Update user role
async function updateUserRole(userId, newRole) {
    try {
        await apiCall(`/users/${userId}/role?role=${newRole}`, 'PUT');
        showAlert('Role updated successfully', 'success');
    } catch (error) {
        showAlert('Error updating role: ' + error.message);
        loadUsers(); // Reload to reset the select
    }
}

// Edit user
async function editUser(userId) {
    try {
        const user = await apiCall(`/users/${userId}`);

        document.getElementById('userModalTitle').textContent = 'Edit User';
        document.getElementById('userId').value = user.id;
        document.getElementById('userFirstName').value = user.firstName;
        document.getElementById('userLastName').value = user.lastName;
        document.getElementById('userEmail').value = user.email;
        document.getElementById('userRole').value = user.role;
        document.getElementById('userActive').checked = user.active;
        document.getElementById('userPassword').value = '';

        // Show modal
        new bootstrap.Modal(document.getElementById('userModal')).show();
    } catch (error) {
        showAlert('Error loading user: ' + error.message);
    }
}

// Delete user
async function deleteUser(userId) {
    if (!confirm('Are you sure you want to delete this user?')) {
        return;
    }

    try {
        await apiCall(`/users/${userId}`, 'DELETE');
        showAlert('User deleted successfully', 'success');
        loadUsers();
    } catch (error) {
        showAlert('Error deleting user: ' + error.message);
    }
}

// Reset form when modal is closed
document.getElementById('userModal').addEventListener('hidden.bs.modal', () => {
    document.getElementById('userForm').reset();
    document.getElementById('userId').value = '';
    document.getElementById('userModalTitle').textContent = 'Create User';
    document.getElementById('userPassword').required = true;
});

// Set password requirement based on whether it is new user or edit
document.getElementById('userModal').addEventListener('shown.bs.modal', () => {
    const userId = document.getElementById('userId').value;
    document.getElementById('userPassword').required = !userId;
});
