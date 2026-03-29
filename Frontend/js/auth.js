// Login functionality
document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const loginBtn = document.getElementById('loginBtn');
    const loginText = document.getElementById('loginText');
    const loginSpinner = document.getElementById('loginSpinner');

    // Disable button and show spinner
    loginBtn.disabled = true;
    loginText.textContent = 'Logging in...';
    loginSpinner.classList.remove('d-none');

    const loginData = {
        email: document.getElementById('email').value,
        password: document.getElementById('password').value
    };

    try {
        const response = await apiCall('/auth/login', 'POST', loginData);

        // Store token and user info
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify({
            email: response.email,
            firstName: response.firstName,
            lastName: response.lastName,
            role: response.role
        }));

        // Redirect to dashboard
        window.location.href = 'dashboard.html';
    } catch (error) {
        showAlert(error.message);
    } finally {
        // Re-enable button
        loginBtn.disabled = false;
        loginText.textContent = 'Login';
        loginSpinner.classList.add('d-none');
    }
});

// Register functionality
document.getElementById('registerForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const registerBtn = document.getElementById('registerBtn');
    const registerText = document.getElementById('registerText');
    const registerSpinner = document.getElementById('registerSpinner');

    // Disable button and show spinner
    registerBtn.disabled = true;
    registerText.textContent = 'Registering...';
    registerSpinner.classList.remove('d-none');

    const registerData = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        role: 'USER'
    };

    try {
        await apiCall('/auth/register', 'POST', registerData);
        showAlert('Registration successful! Please login.', 'success');

        // Redirect to login after 2 seconds
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 2000);
    } catch (error) {
        showAlert(error.message);
    } finally {
        // Re-enable button
        registerBtn.disabled = false;
        registerText.textContent = 'Register';
        registerSpinner.classList.add('d-none');
    }
});