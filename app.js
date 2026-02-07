/**
 * JATS - Job Application Tracking System
 * Core SPA Logic
 */

const STATE = {
    user: JSON.parse(localStorage.getItem('user')) || null,
    token: localStorage.getItem('token') || null,
    currentView: 'home'
};

const API_BASE = '/api';

const API = {
    async request(endpoint, method = 'GET', body = null, auth = true) {
        const headers = {
            'Content-Type': 'application/json'
        };
        if (auth && STATE.token) {
            headers['Authorization'] = `Bearer ${STATE.token}`;
        }

        const config = {
            method,
            headers
        };
        if (body) config.body = JSON.stringify(body);

        try {
            const response = await fetch(`${API_BASE}${endpoint}`, config);
            if (response.status === 401) {
                app.logout();
                throw new Error('Session expired');
            }

            const data = await response.json();
            if (!response.ok) {
                console.error('API Error:', data);
                throw new Error(data.message || data.error || 'API Error');
            }
            return data;
        } catch (err) {
            app.showToast(err.message, 'error');
            throw err;
        }
    }
};

const app = {
    init() {
        this.bindEvents();
        this.renderNav();
        this.navigateTo('home');
    },

    bindEvents() {
        document.addEventListener('click', e => {
            const link = e.target.closest('[data-link]');
            if (link) {
                e.preventDefault();
                this.navigateTo(link.dataset.link);
            }
        });
    },

    navigateTo(view) {
        STATE.currentView = view;
        this.renderView(view);
    },

    renderNav() {
        const nav = document.getElementById('nav-links');
        if (STATE.user) {
            nav.innerHTML = `
                <li><a href="#" data-link="jobs">Jobs</a></li>
                <li><a href="#" data-link="dashboard">Dashboard</a></li>
                <li><a href="#" data-link="profile">Profile</a></li>
                <li><a href="#" onclick="app.logout()" class="nav-btn">Logout</a></li>
            `;
        } else {
            nav.innerHTML = `
                <li><a href="#" data-link="jobs">Find Jobs</a></li>
                <li><a href="#" data-link="login" class="nav-btn">Login</a></li>
                <li><a href="#" data-link="register" class="nav-btn nav-btn-primary">Get Started</a></li>
            `;
        }
    },

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        STATE.user = null;
        STATE.token = null;
        location.reload();
    },

    showToast(message, type = 'success') {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerText = message;
        container.appendChild(toast);
        setTimeout(() => toast.remove(), 4000);
    },

    renderView(view) {
        const container = document.getElementById('view-container');
        container.innerHTML = '<div class="fade-in">Loading...</div>';

        switch (view) {
            case 'home':
                this.views.home(container);
                break;
            case 'login':
                this.views.auth(container, 'login');
                break;
            case 'register':
                this.views.auth(container, 'register');
                break;
            case 'jobs':
                this.views.jobs(container);
                break;
            case 'dashboard':
                this.views.dashboard(container);
                break;
            case 'profile':
                this.views.profile(container);
                break;
            default:
                container.innerHTML = `<div class="fade-in"><h2>404</h2><p>View "${view}" not found.</p></div>`;
        }
    },

    views: {
        home(container) {
            container.innerHTML = `
                <div class="hero-section fade-in">
                    <h1 style="font-size: 3.5rem; margin-bottom: 1rem;">Find Your Next <span style="background: linear-gradient(to right, var(--primary), var(--secondary)); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">Mission</span></h1>
                    <p style="font-size: 1.2rem; color: var(--text-muted); max-width: 600px; margin: 0 auto 2.5rem;">The only tool you need to track applications, shortlist missions, and land your dream role.</p>
                    <div style="display: flex; gap: 1rem; justify-content: center;">
                        <button data-link="jobs" class="btn btn-primary">Browse Jobs</button>
                        ${!STATE.user ? '<button data-link="register" class="btn glass-pane">Join Now</button>' : ''}
                    </div>
                </div>
            `;
        },

        async profile(container) {
            try {
                const profile = await API.request('/users/me');
                container.innerHTML = `
                    <div class="auth-container glass-pane fade-in">
                        <h2 style="margin-bottom: 2rem;">Manage Profile</h2>
                        <form id="profile-form">
                            <div class="form-group">
                                <label>Email (Immutable)</label>
                                <input type="email" value="${profile.email}" disabled style="opacity: 0.6;">
                            </div>
                            <div class="form-group">
                                <label>Full Name</label>
                                <input type="text" id="fullName" value="${profile.fullName}" required>
                            </div>
                            <div class="form-group">
                                <label>Phone Number</label>
                                <input type="text" id="phone" value="${profile.phone || ''}" placeholder="Not set">
                            </div>
                             <div class="form-group">
                                <label>Roles</label>
                                <p style="font-size: 0.8rem; color: var(--primary);">${profile.roles.join(', ')}</p>
                            </div>
                            <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center;">Update Profile</button>
                        </form>
                    </div>
                `;

                document.getElementById('profile-form').onsubmit = async (e) => {
                    e.preventDefault();
                    await API.request('/users/me', 'PUT', {
                        fullName: e.target.fullName.value,
                        phone: e.target.phone.value
                    });
                    app.showToast('Profile updated!');
                    setTimeout(() => location.reload(), 1000);
                };
            } catch (err) { }
        },

        async dashboard(container) {
            const isRecruiter = STATE.user && STATE.user.roles.includes('RECRUITER');

            try {
                let data;
                if (isRecruiter) {
                    data = await API.request('/jobs/my-jobs');
                } else {
                    data = await API.request('/applications/my-applications');
                }
                const items = data.content || [];

                container.innerHTML = `
                    <div class="fade-in">
                        <header style="background: none; border: none; padding: 0; margin-bottom: 2rem; height: auto; display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <h1>Welcome, ${STATE.user.fullName}</h1>
                                <p style="color: var(--text-muted);">Master Control Center | Role: ${STATE.user.roles[0]}</p>
                            </div>
                            ${isRecruiter ? '<button class="btn btn-primary" onclick="app.showPostJobModal()">Post New Mission</button>' : ''}
                        </header>
                        
                        <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem;">
                            <div class="glass-pane" style="padding: 2rem;">
                                <h3 style="margin-bottom: 1.5rem;">${isRecruiter ? 'Your Active Missions' : 'Your Applications'}</h3>
                                <div id="dashboard-list">
                                    ${items.length ? items.map(item => isRecruiter ? app.templates.jobRow(item) : app.templates.appRow(item)).join('') : '<p>No activity yet.</p>'}
                                </div>
                            </div>
                            
                            <div class="glass-pane" style="padding: 2rem;">
                                <h3>Platform Insight</h3>
                                <div style="margin-top: 1.5rem;">
                                    <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
                                        <span>System Status</span>
                                        <span style="color: var(--accent);">Active</span>
                                    </div>
                                    <div style="width: 100%; height: 4px; background: var(--border); border-radius: 2px; margin-bottom: 2rem;">
                                        <div style="width: 100%; height: 100%; background: var(--accent); border-radius: 2px;"></div>
                                    </div>
                                    
                                    <div style="background: rgba(255,255,255,0.03); padding: 1rem; border-radius: var(--radius-sm);">
                                        <p style="font-size: 0.8rem; color: var(--text-muted);">Logged in as:</p>
                                        <p style="font-weight: 600;">${STATE.user.email}</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            } catch (err) { }
        },

        async jobs(container) {
            try {
                const data = await API.request('/jobs');
                const jobs = data.content || [];

                container.innerHTML = `
                    <div class="fade-in">
                        <section style="margin-bottom: 3rem; display: flex; justify-content: space-between; align-items: flex-end; flex-wrap: wrap; gap: 2rem;">
                            <div>
                                <h2>Available Missions</h2>
                                <p style="color: var(--text-muted);">Discover and apply to technical roles.</p>
                            </div>
                            <div style="display: flex; gap: 0.5rem; background: rgba(255,255,255,0.05); padding: 0.5rem; border-radius: var(--radius-sm); border: 1px solid var(--border);">
                                <input type="text" id="job-search" placeholder="Search tech..." style="background: none; border: none; color: #fff; padding: 0.5rem; outline: none;">
                                <button class="btn btn-primary" style="padding: 0.5rem 1rem;" onclick="app.searchJobs()">Search</button>
                            </div>
                        </section>
                        <div id="jobs-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.5rem;">
                            ${jobs.length ? jobs.map(job => `
                                <div class="glass-pane" style="padding: 1.5rem; position: relative;">
                                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                                        <h3 style="font-size: 1.25rem;">${job.title}</h3>
                                        <span style="font-size: 0.75rem; padding: 0.25rem 0.5rem; background: var(--border); border-radius: 4px;">${job.jobType}</span>
                                    </div>
                                    <p style="color: var(--primary); font-weight: 600; margin-bottom: 0.5rem;">${job.company}</p>
                                    <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;">
                                        ${job.description}
                                    </p>
                                    <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border); padding-top: 1rem;">
                                        <span style="font-size: 0.85rem; color: var(--text-muted);">${job.location}</span>
                                        <button class="btn btn-primary" style="padding: 0.5rem 1rem; font-size: 0.85rem;" onclick="app.applyToJob(${job.id}, '${job.title}')">Quick Apply</button>
                                    </div>
                                </div>
                            `).join('') : '<p>No jobs available at the moment.</p>'}
                        </div>
                    </div>
                `;
            } catch (err) {
                container.innerHTML = `<p style="color: red;">Failed to load jobs: ${err.message}</p>`;
            }
        },

        auth(container, type) {
            const isLogin = type === 'login';
            container.innerHTML = `
                <div class="auth-container glass-pane fade-in">
                    <h2 style="margin-bottom: 2rem;">${isLogin ? 'Welcome Back' : 'Create Account'}</h2>
                    <form id="auth-form">
                        ${!isLogin ? `
                            <div class="form-group">
                                <label>Full Name</label>
                                <input type="text" name="fullName" required placeholder="John Doe">
                            </div>
                        ` : ''}
                        <div class="form-group">
                            <label>Email Address</label>
                            <input type="email" name="email" required placeholder="name@company.com">
                        </div>
                        <div class="form-group">
                            <label>Password</label>
                            <input type="password" name="password" required placeholder="••••••••">
                        </div>
                        ${!isLogin ? `
                            <div class="form-group">
                                <label>I am a...</label>
                                <select name="role">
                                    <option value="APPLICANT">Job Seeker</option>
                                    <option value="RECRUITER">Recruiter</option>
                                </select>
                            </div>
                        ` : ''}
                        <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; margin-top: 1rem;">
                            ${isLogin ? 'Sign In' : 'Create Account'}
                        </button>
                    </form>
                    <p style="margin-top: 2rem; font-size: 0.9rem; text-align: center; color: var(--text-muted);">
                        ${isLogin ? "Don't have an account?" : "Already have an account?"} 
                        <a href="#" data-link="${isLogin ? 'register' : 'login'}" style="color: var(--primary); font-weight: 600;">
                            ${isLogin ? 'Sign Up' : 'Sign In'}
                        </a>
                    </p>
                </div>
            `;

            document.getElementById('auth-form').onsubmit = async (e) => {
                e.preventDefault();
                const btn = e.target.querySelector('button');
                btn.disabled = true;
                btn.innerText = 'Processing...';

                const payload = {
                    email: e.target.email.value,
                    password: e.target.password.value
                };

                if (!isLogin) {
                    payload.fullName = e.target.fullName.value;
                    payload.roles = [e.target.role.value];
                }

                try {
                    const endpoint = isLogin ? '/auth/login' : '/auth/register';
                    const res = await API.request(endpoint, 'POST', payload, false);

                    if (isLogin) {
                        localStorage.setItem('token', res.token);
                        localStorage.setItem('user', JSON.stringify({
                            email: res.email,
                            fullName: res.fullName,
                            roles: res.roles
                        }));
                        STATE.user = JSON.parse(localStorage.getItem('user'));
                        STATE.token = res.token;
                        app.showToast('Login successful!');
                        app.navigateTo('dashboard');
                        app.renderNav();
                    } else {
                        app.showToast('Registration successful! Please login.');
                        app.navigateTo('login');
                    }
                } catch (err) {
                    btn.disabled = false;
                    btn.innerText = isLogin ? 'Sign In' : 'Create Account';
                }
            };
        }
    },

    templates: {
        jobRow(job) {
            return `
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 1.25rem; border-bottom: 1px solid var(--border);">
                    <div>
                        <p style="font-weight: 600;">${job.title}</p>
                        <p style="font-size: 0.8rem; color: var(--text-muted);">${job.jobType} • ${job.location}</p>
                    </div>
                    <div style="text-align: right;">
                        <span style="color: var(--accent); font-size: 0.8rem; font-weight: 600;">ACTIVE</span>
                        <p style="font-size: 0.7rem; color: var(--text-muted);">${new Date(job.createdAt).toLocaleDateString()}</p>
                    </div>
                </div>
            `;
        },
        appRow(appData) {
            const statusColors = {
                APPLIED: 'var(--text-muted)',
                SHORTLISTED: 'var(--primary)',
                INTERVIEW_SCHEDULED: 'var(--secondary)',
                OFFERED: 'var(--accent)',
                REJECTED: 'hsl(0, 80%, 60%)'
            };
            return `
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 1.25rem; border-bottom: 1px solid var(--border);">
                    <div>
                        <p style="font-weight: 600;">${appData.jobTitle}</p>
                        <p style="font-size: 0.8rem; color: var(--text-muted);">${appData.company}</p>
                    </div>
                    <div style="text-align: right;">
                        <span style="color: ${statusColors[appData.status] || '#fff'}; font-size: 0.8rem; font-weight: 700;">${appData.status.replace('_', ' ')}</span>
                        <p style="font-size: 0.7rem; color: var(--text-muted);">${new Date(appData.appliedAt).toLocaleDateString()}</p>
                    </div>
                </div>
            `;
        }
    },

    async applyToJob(jobId, jobTitle) {
        if (!STATE.user) {
            app.showToast('Please login to apply', 'error');
            app.navigateTo('login');
            return;
        }

        const coverLetter = prompt(`Apply for ${jobTitle}\n\nConfirming application as: ${STATE.user.email}\n\nEnter a brief message to the recruiter:`, "I'm interested in this position!");
        if (coverLetter === null) return;

        try {
            await API.request('/applications', 'POST', {
                jobId,
                coverLetter,
                resumeUrl: "https://jats.io/resumes/default.pdf"
            });
            app.showToast('Application sent successfully!');
            app.navigateTo('dashboard');
        } catch (err) { }
    },

    showPostJobModal() {
        const overlay = document.createElement('div');
        overlay.className = 'fade-in';
        overlay.style = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.8);z-index:1000;display:flex;align-items:center;justify-content:center;padding:2rem;';

        overlay.innerHTML = `
            <div class="glass-pane" style="max-width:600px; width:100%; padding:2.5rem; position:relative;">
                <button onclick="this.closest('.fade-in').remove()" style="position:absolute;top:1rem;right:1rem;background:none;border:none;color:#fff;font-size:1.5rem;cursor:pointer;">&times;</button>
                <h2 style="margin-bottom:2rem;">Post New Mission</h2>
                <form id="post-job-form">
                    <div style="display:grid; grid-template-columns:1fr 1fr; gap:1rem;">
                        <div class="form-group">
                            <label>Mission Title</label>
                            <input type="text" name="title" required placeholder="eg. Senior Java Dev">
                        </div>
                        <div class="form-group">
                            <label>Company</label>
                            <input type="text" name="company" required placeholder="eg. Tech Corp">
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" required style="width:100%; height:100px; background:rgba(255,255,255,0.05); border:1px solid var(--border); border-radius:var(--radius-sm); color:#fff; padding:0.8rem;"></textarea>
                    </div>
                    <div style="display:grid; grid-template-columns:1fr 1fr; gap:1rem;">
                        <div class="form-group">
                            <label>Location</label>
                            <input type="text" name="location" required placeholder="eg. Remote">
                        </div>
                        <div class="form-group">
                            <label>Experience</label>
                            <select name="experienceLevel">
                                <option value="ENTRY_LEVEL">Entry Level</option>
                                <option value="MID_LEVEL">Mid Level</option>
                                <option value="SENIOR_LEVEL">Senior Level</option>
                                <option value="LEAD">Lead</option>
                            </select>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary" style="width:100%; justify-content:center; margin-top:1rem;">Deploy Mission</button>
                </form>
            </div>
        `;

        document.body.appendChild(overlay);

        document.getElementById('post-job-form').onsubmit = async (e) => {
            e.preventDefault();
            const payload = {
                title: e.target.title.value,
                company: e.target.company.value,
                description: e.target.description.value,
                location: e.target.location.value,
                experienceLevel: e.target.experienceLevel.value,
                jobType: "FULL_TIME",
                salary: "Competitive",
                requirements: "Standard requirements apply."
            };

            try {
                await API.request('/jobs', 'POST', payload);
                app.showToast('Mission deployed successfully!');
                overlay.remove();
                app.navigateTo('dashboard');
            } catch (err) { }
        };
    },

    async searchJobs() {
        const keyword = document.getElementById('job-search').value;
        const grid = document.getElementById('jobs-grid');
        grid.innerHTML = '<p>Searching...</p>';
        try {
            const data = await API.request(`/jobs/search?keyword=${encodeURIComponent(keyword)}`);
            const jobs = data.content || [];
            grid.innerHTML = jobs.length ? jobs.map(job => `
                <div class="glass-pane" style="padding: 1.5rem; position: relative; display: flex; flex-direction: column;">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                        <h3 style="font-size: 1.25rem;">${job.title}</h3>
                        <span style="font-size: 0.75rem; padding: 0.25rem 0.5rem; background: var(--border); border-radius: 4px; color: var(--accent);">${job.jobType}</span>
                    </div>
                    <p style="color: var(--primary); font-weight: 600; margin-bottom: 0.5rem;">${job.company}</p>
                    <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem; flex-grow: 1; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;">
                        ${job.description}
                    </p>
                    <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border); padding-top: 1rem;">
                        <span style="font-size: 0.85rem; color: var(--text-muted);">${job.location}</span>
                        <button class="btn btn-primary" style="padding: 0.5rem 1rem; font-size: 0.85rem;" onclick="app.applyToJob(${job.id}, '${job.title}')">Quick Apply</button>
                    </div>
                </div>
            `).join('') : '<p>No matching missions found.</p>';
        } catch (err) { }
    }
};

window.onload = () => app.init();
