function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function formatTimestamp(timestamp) {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (isNaN(date.getTime())) return '';
    if (seconds < 60) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString();
}

class WakandaSocial {
    constructor() {
        this.baseUrl = 'http://localhost:8080';
        this.token = localStorage.getItem('wakanda_token');
        this.currentUser = JSON.parse(localStorage.getItem('wakanda_user') || 'null');
        this.currentTab = 'home';
        this.currentChatUser = null;
        this.autoRefreshInterval = null;
        this.messageRefreshInterval = null;
        this.init();
    }

    async init() {
        this.setupEventListeners();
        if (this.token && this.currentUser) {
            await this.showMainContent();
            this.startAutoRefresh();
        } else {
            this.showAuthContainer();
        }
    }

    setupEventListeners() {
        document.getElementById('showRegisterBtn').addEventListener('click', () => this.showRegisterForm());
        document.getElementById('showLoginBtn').addEventListener('click', () => this.showLoginForm());
        document.getElementById('loginBtn').addEventListener('click', () => this.login());
        document.getElementById('registerBtn').addEventListener('click', () => this.register());
        document.getElementById('homeBtn').addEventListener('click', () => this.loadPage('home', 'home.html'));
        document.getElementById('messagesBtn').addEventListener('click', () => this.loadPage('messages', 'messages.html'));
        document.getElementById('searchBtn').addEventListener('click', () => this.loadPage('search', 'search.html'));
        document.getElementById('profileBtn').addEventListener('click', () => this.loadPage('profile', 'profile.html'));
        document.getElementById('logoutBtn').addEventListener('click', () => this.logout());
    }

    async loadPage(tabName, pageUrl) {
        document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
        document.getElementById(tabName + 'Btn').classList.add('active');

        try {
            const response = await fetch(pageUrl);
            if (!response.ok) throw new Error(`Failed to load ${pageUrl}: ${response.status}`);
            const content = await response.text();
            document.getElementById('contentArea').innerHTML = content;

            this.currentTab = tabName;

            if (tabName === 'home') {
                this.setupHomeListeners();
                this.loadFeed();
            } else if (tabName === 'profile') {
                this.setupProfileListeners();
                this.loadUserProfile();
                this.loadUserPosts();
            } else if (tabName === 'messages') {
                this.setupMessagesListeners();
                this.loadConversations();
                this.showConversationsList();
            } else if (tabName === 'search') {
                this.setupSearchListeners();
            }

            await this.loadSuggestedUsers();
        } catch (error) {
            console.error('Error loading page:', error);
            this.showMessage('Failed to load page', 'error');
        }
    }

    setupHomeListeners() {
        const postContent = document.getElementById('postContent');
        const postBtn = document.getElementById('postBtn');
        if (postContent && postBtn) {
            postContent.addEventListener('input', () => {
                this.updateCharCounter();
                this.validatePostForm();
            });
            postBtn.addEventListener('click', () => this.createPost());
        }
    }

    setupProfileListeners() {
        const updateBtn = document.getElementById('updateProfileBtn');
        if (updateBtn) {
            updateBtn.addEventListener('click', () => this.updateProfile());
        }
    }

    setupMessagesListeners() {
        const backBtn = document.getElementById('backToConversations');
        const sendBtn = document.getElementById('sendMessageBtn');
        const chatInput = document.getElementById('chatInput');
        if (backBtn) {
            backBtn.addEventListener('click', () => this.showConversationsList());
        }
        if (sendBtn) {
            sendBtn.addEventListener('click', () => this.sendMessage());
        }
        if (chatInput) {
            chatInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.sendMessage();
            });
        }
    }

    setupSearchListeners() {
        let searchTimeout;
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    if (e.target.value.trim()) {
                        this.search(e.target.value.trim());
                    } else {
                        document.getElementById('searchResults').innerHTML =
                            '<p style="text-align: center; color: var(--wakanda-text-secondary); padding: 40px;">🔍 Enter your search to explore Wakanda\'s knowledge</p>';
                    }
                }, 300);
            });
        }
    }

    showAuthContainer() {
        document.getElementById('authContainer').classList.remove('hidden');
        document.getElementById('mainContent').classList.add('hidden');
    }

    async showMainContent() {
        document.getElementById('authContainer').classList.add('hidden');
        document.getElementById('mainContent').classList.remove('hidden');
        await this.loadPage('home', 'home.html');
    }

    showRegisterForm() {
        document.getElementById('loginForm').classList.add('hidden');
        document.getElementById('registerForm').classList.remove('hidden');
    }

    showLoginForm() {
        document.getElementById('registerForm').classList.add('hidden');
        document.getElementById('loginForm').classList.remove('hidden');
    }

    async login() {
        const username = document.getElementById('loginUsername').value.trim();
        const password = document.getElementById('loginPassword').value;

        if (!username || !password) {
            this.showMessage('Please fill in all fields, warrior!', 'error');
            return;
        }

        const loginBtn = document.getElementById('loginBtn');
        loginBtn.textContent = 'Entering Kingdom...';
        loginBtn.disabled = true;

        try {
            const response = await fetch(`${this.baseUrl}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ usernameOrEmail: username, password })
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Access denied to the kingdom');
            }

            const data = await response.json();
            this.token = data.token;
            this.currentUser = data.user;

            localStorage.setItem('wakanda_token', this.token);
            localStorage.setItem('wakanda_user', JSON.stringify(this.currentUser));

            this.showMessage('Welcome to Wakanda! 🌟', 'success');
            await this.showMainContent();
            this.startAutoRefresh();
        } catch (error) {
            console.error('Login error:', error);
            this.showMessage(error.message, 'error');
        } finally {
            loginBtn.textContent = 'Enter the Kingdom';
            loginBtn.disabled = false;
        }
    }

    async register() {
        const username = document.getElementById('registerUsername').value.trim();
        const email = document.getElementById('registerEmail').value.trim();
        const password = document.getElementById('registerPassword').value;

        if (!username || !email || !password) {
            this.showMessage('All fields are required to join Wakanda!', 'error');
            return;
        }

        if (password.length < 6) {
            this.showMessage('Your secret code must be at least 6 characters', 'error');
            return;
        }

        const registerBtn = document.getElementById('registerBtn');
        registerBtn.textContent = 'Joining Kingdom...';
        registerBtn.disabled = true;

        try {
            const response = await fetch(`${this.baseUrl}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password })
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Failed to join the kingdom');
            }

            const data = await response.json();
            this.token = data.token;
            this.currentUser = data.user;

            localStorage.setItem('wakanda_token', this.token);
            localStorage.setItem('wakanda_user', JSON.stringify(this.currentUser));

            this.showMessage('Welcome, new citizen of Wakanda! 🎉', 'success');
            await this.showMainContent();
            this.startAutoRefresh();
        } catch (error) {
            console.error('Registration error:', error);
            this.showMessage(error.message, 'error');
        } finally {
            registerBtn.textContent = 'Become a Citizen';
            registerBtn.disabled = false;
        }
    }

    logout() {
        this.token = null;
        this.currentUser = null;
        localStorage.removeItem('wakanda_token');
        localStorage.removeItem('wakanda_user');

        document.getElementById('loginUsername').value = '';
        document.getElementById('loginPassword').value = '';
        document.getElementById('registerUsername').value = '';
        document.getElementById('registerEmail').value = '';
        document.getElementById('registerPassword').value = '';

        this.stopAutoRefresh();
        this.showAuthContainer();
        this.showMessage('Farewell, warrior. Wakanda awaits your return! 👋', 'success');
    }

    async loadUserProfile() {
        if (!this.currentUser) return;

        try {
            const response = await this.fetchWithAuth(`/users/${this.currentUser.id}`);
            const user = await response.json();

            const avatar = document.getElementById('userAvatar');
            avatar.textContent = (user.displayName || user.username).charAt(0).toUpperCase();

            document.getElementById('userName').textContent = user.displayName || user.username;
            document.getElementById('userUsername').textContent = `@${user.username}`;
            document.getElementById('userBio').textContent = user.bio || 'A proud citizen of Wakanda';

            document.getElementById('postsCount').textContent = user.postsCount || 0;
            document.getElementById('followersCount').textContent = user.followersCount || 0;
            document.getElementById('followingCount').textContent = user.followingCount || 0;

            if (this.currentTab === 'profile') {
                const editDisplayName = document.getElementById('editDisplayName');
                const editBio = document.getElementById('editBio');
                if (editDisplayName && editBio) {
                    editDisplayName.value = user.displayName || '';
                    editBio.value = user.bio || '';
                }
            }
        } catch (error) {
            console.error('Error loading user profile:', error);
            this.showMessage('Failed to load profile', 'error');
        }
    }

    async loadFeed() {
        try {
            const container = document.getElementById('postsContainer');
            if (!container) return;
            container.innerHTML = `
                <div class="loading">
                    <div class="spinner"></div>
                    <p>Loading posts...</p>
                </div>
            `;

            const response = await this.fetchWithAuth('/posts/feed');
            if (!response.ok) throw new Error('Failed to load feed');
            const posts = await response.json();

            this.displayPosts(posts);
        } catch (error) {
            console.error('Error loading feed:', error);
            const container = document.getElementById('postsContainer');
            if (container) {
                container.innerHTML = `<div class="message error">Failed to load posts</div>`;
            }
        }
    }

    async loadUserPosts() {
        if (!this.currentUser) return;

        try {
            const container = document.getElementById('userPostsContainer');
            if (!container) return;
            const loadingDiv = container.querySelector('.loading');
            if (loadingDiv) {
                loadingDiv.innerHTML = `
                    <div class="spinner"></div>
                    <p>Loading posts...</p>
                `;
            }

            const response = await this.fetchWithAuth(`/posts/user/${this.currentUser.id}`);
            if (!response.ok) throw new Error('Failed to load user posts');
            const posts = await response.json();

            if (loadingDiv) loadingDiv.remove();

            const postsHtml = posts.length > 0 ?
                posts.map(post => this.createPostHTML(post, true)).join('') :
                '<p style="text-align: center; padding: 40px;">Your chronicles await</p>';

            container.innerHTML = `<div class="posts-list">${postsHtml}</div>`;
        } catch (error) {
            console.error('Error loading user posts:', error);
            this.showMessage('Failed to load posts', 'error');
        }
    }

    async loadConversations() {
        try {
            const container = document.getElementById('conversationsList');
            if (!container) {
                console.error('Conversations list container not found');
                return;
            }
            container.innerHTML = `
                <div class="loading">
                    <div class="spinner"></div>
                    <p>Loading conversations...</p>
                </div>
            `;

            const response = await this.fetchWithAuth('/messages/conversations');
            if (!response.ok) {
                const errorText = await response.text();
                console.error(`API error: ${response.status} ${response.statusText}`, errorText);
                throw new Error(`Failed to load conversations: ${response.status} ${response.statusText}`);
            }

            const conversations = await response.json();
            console.log('Conversations response:', conversations);

            if (!Array.isArray(conversations)) {
                console.error('Invalid conversations format:', conversations);
                throw new Error('Invalid conversations data');
            }

            conversations.sort((a, b) => {
                const timeA = a.lastMessageTime || a.createdAt || new Date();
                const timeB = b.lastMessageTime || b.createdAt || new Date();
                return new Date(timeB) - new Date(timeA);
            });

            if (conversations.length === 0) {
                container.innerHTML = `
                    <p style="text-align: center; padding: 40px;">
                        💬 No conversations yet. Start a new one!
                    </p>
                `;
                return;
            }

            const conversationsHtml = conversations.map(conv => {
                if (!conv.user || !conv.user.id) {
                    console.warn('Invalid conversation user:', conv);
                    return '';
                }
                return `
                    <div class="conversation-item" onclick="app.loadChat(${conv.user.id}, '${conv.user.username}', '${conv.user.displayName || conv.user.username}')">
                        <div class="conversation-avatar">${(conv.user.displayName || conv.user.username).charAt(0).toUpperCase()}</div>
                        <div class="conversation-info">
                            <div class="conversation-name">${conv.user.displayName || conv.user.username}</div>
                            <div class="conversation-last-message">${conv.lastMessage ? escapeHtml(conv.lastMessage.content) : 'No messages yet'}</div>
                        </div>
                        <div class="conversation-meta">
                            <div class="conversation-time">${conv.lastMessage ? formatTimestamp(conv.lastMessageTime || conv.lastMessage.createdAt) : ''}</div>
                            ${conv.unreadCount > 0 ? `<div class="unread-badge">${conv.unreadCount}</div>` : ''}
                        </div>
                    </div>
                `;
            }).filter(html => html).join('');

            container.innerHTML = conversationsHtml || `
                <p style="text-align: center; padding: 40px;">
                    💬 No valid conversations found.
                </p>
            `;

            if (!this.messageRefreshInterval) {
                this.messageRefreshInterval = setInterval(() => this.loadConversations(), 10000);
            }
        } catch (error) {
            console.error('Error loading conversations:', error);
            const container = document.getElementById('conversationsList');
            if (container) {
                container.innerHTML = `
                    <div class="message error">
                        Failed to load conversations. Please try again.
                        <button class="btn btn-small" onclick="app.loadConversations()" style="margin-top: 10px;">Retry</button>
                    </div>
                `;
            }
            this.showMessage('Unable to load conversations', 'error');
        }
    }

    async loadChat(userId, username, displayName) {
        this.currentChatUser = { id: userId, username, displayName };
        const chatArea = document.getElementById('chatArea');
        const conversationsList = document.getElementById('conversationsList');

        if (chatArea && conversationsList) {
            chatArea.classList.remove('hidden');
            conversationsList.classList.add('hidden');
        }

        const chatAvatar = document.getElementById('chatAvatar');
        const chatUsername = document.getElementById('chatUsername');
        const chatStatus = document.getElementById('chatStatus');

        if (chatAvatar && chatUsername && chatStatus) {
            chatAvatar.textContent = displayName.charAt(0).toUpperCase();
            chatUsername.textContent = displayName;
            chatStatus.textContent = 'Online';
        }

        try {
            const messagesContainer = document.getElementById('chatMessages');
            if (messagesContainer) {
                messagesContainer.innerHTML = `
                    <div class="loading">
                        <div class="spinner"></div>
                        <p>Loading messages...</p>
                    </div>
                `;
            }

            const response = await this.fetchWithAuth(`/messages/${userId}`);
            if (!response.ok) throw new Error('Failed to load messages');
            const messages = await response.json();

            this.displayMessages(messages);
            await this.fetchWithAuth(`/messages/${userId}/read`, { method: 'POST' });
            this.loadConversations();
        } catch (error) {
            console.error('Error loading chat:', error);
            this.showMessage('Failed to load messages', 'error');
        }
    }

    showConversationsList() {
        const chatArea = document.getElementById('chatArea');
        const conversationsList = document.getElementById('conversationsList');

        if (chatArea && conversationsList) {
            chatArea.classList.add('hidden');
            conversationsList.classList.remove('hidden');
        }

        this.currentChatUser = null;
        this.loadConversations();
    }

    async sendMessage() {
        if (!this.currentChatUser) return;

        const chatInput = document.getElementById('chatInput');
        const content = chatInput.value.trim();

        if (!content) {
            this.showMessage('Message cannot be empty!', 'error');
            return;
        }

        const sendBtn = document.getElementById('sendMessageBtn');
        sendBtn.disabled = true;

        try {
            const response = await this.fetchWithAuth('/messages', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ recipientId: this.currentChatUser.id, content })
            });

            if (!response.ok) throw new Error('Failed to send message');

            chatInput.value = '';
            await this.loadChat(this.currentChatUser.id, this.currentChatUser.username, this.currentChatUser.displayName);
        } catch (error) {
            console.error('Error sending message:', error);
            this.showMessage('Failed to send message', 'error');
        } finally {
            sendBtn.disabled = false;
        }
    }

    async createPost() {
        const content = document.getElementById('postContent').value.trim();
        const postBtn = document.getElementById('postBtn');

        if (!content) {
            this.showMessage('Post cannot be empty!', 'error');
            return;
        }

        postBtn.disabled = true;
        postBtn.textContent = 'Posting...';

        try {
            const response = await this.fetchWithAuth('/posts', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content })
            });

            if (!response.ok) throw new Error('Failed to post');

            document.getElementById('postContent').value = '';
            this.updateCharCounter();
            this.validatePostForm();
            await this.loadFeed();
            this.showMessage('Post shared!', 'success');
        } catch (error) {
            console.error('Error creating post:', error);
            this.showMessage('Failed to post', 'error');
        } finally {
            postBtn.disabled = false;
            postBtn.textContent = 'Share with Kingdom';
        }
    }

    async updateProfile() {
        const displayName = document.getElementById('editDisplayName').value.trim();
        const bio = document.getElementById('editBio').value.trim();
        const updateBtn = document.getElementById('updateProfileBtn');

        if (!displayName) {
            this.showMessage('Display name required!', 'error');
            return;
        }

        updateBtn.disabled = true;
        updateBtn.textContent = 'Updating...';

        try {
            const response = await this.fetchWithAuth(`/users/${this.currentUser.id}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ displayName, bio })
            });

            if (!response.ok) throw new Error('Failed to update profile');

            const updatedUser = await response.json();
            this.currentUser = updatedUser;
            localStorage.setItem('wakanda_user', JSON.stringify(updatedUser));

            await this.loadUserProfile();
            this.showMessage('Profile updated successfully!', 'success');
        } catch (error) {
            console.error('Error updating profile:', error);
            this.showMessage('Failed to update profile', 'error');
        } finally {
            updateBtn.disabled = false;
            updateBtn.textContent = 'Update Profile';
        }
    }

    async search(query) {
        try {
            const resultsContainer = document.getElementById('searchResults');
            resultsContainer.innerHTML = `
                <div class="loading">
                    <div class="spinner"></div>
                    <p>Searching...</p>
                </div>
            `;

            const response = await this.fetchWithAuth(`/search?q=${encodeURIComponent(query)}`);
            if (!response.ok) throw new Error('Search failed');
            const results = await response.json();

            let html = '';
            if (results.users && results.users.length) {
                html += `<h3 style="padding: 20px 0 10px;">Users</h3>`;
                html += results.users.map(user => `
                    <div class="user-item-wrapper">
                        <div class="user-item-avatar">${(user.displayName || user.username).charAt(0).toUpperCase()}</div>
                        <div class="user-item-info">
                            <div class="user-item-name">${user.displayName || user.username}</div>
                            <div class="user-item-username">@${user.username}</div>
                        </div>
                        <button class="follow-btn ${user.isFollowing ? 'following' : ''}"
                                onclick="app.toggleFollow(${user.id}, this)">
                            ${user.isFollowing ? 'Following' : 'Follow'}
                        </button>
                    </div>
                `).join('');
            }

            if (results.posts && results.posts.length) {
                html += `<h3 style="padding: 20px 0 10px;">Posts</h3>`;
                html += results.posts.map(post => this.createPostHTML(post)).join('');
            }

            resultsContainer.innerHTML = html || `
                <p style="text-align: center; padding: 20px;">
                    No results found
                </p>
            `;
        } catch (error) {
            console.error('Search error:', error);
            this.showMessage('Search failed', 'error');
        }
    }

    async toggleFollow(userId, button) {
        try {
            const isFollowing = button.classList.contains('following');
            const endpoint = isFollowing ? `/users/${userId}/unfollow` : `/users/${userId}/follow`;
            const response = await this.fetchWithAuth(endpoint, { method: 'POST' });

            if (!response.ok) throw new Error('Failed to toggle follow');

            button.classList.toggle('following');
            button.textContent = isFollowing ? 'Follow' : 'Following';
            await this.loadSuggestedUsers();
            if (this.currentTab === 'profile') {
                await this.loadUserProfile();
            }
        } catch (error) {
            console.error('Error toggling follow:', error);
            this.showMessage('Failed to toggle follow', 'error');
        }
    }

    async startChat(userId, username, displayName) {
        await this.loadPage('messages', 'messages.html');
        await this.loadChat(userId, username, displayName);
    }

    displayPosts(posts, containerId = 'postsContainer') {
        const container = document.getElementById(containerId);
        if (!container) return;

        const postsHtml = posts.length ? posts.map(post => this.createPostHTML(post)).join('') : '<p style="text-align: center; padding: 20px;">No posts found</p>';

        container.innerHTML = `<div class="posts-list">${postsHtml}</div>`;
    }

    createPostHTML(post, isUserPost = false) {
        return `
            <div class="post">
                <div class="post-header">
                    <div class="post-avatar">${(post.user.displayName || post.user.username).charAt(0).toUpperCase()}</div>
                    <div class="post-user-info">
                        <div class="post-username">${post.user.displayName || post.user.username}</div>
                        <div class="post-time">${formatTimestamp(post.createdAt)}</div>
                    </div>
                </div>
                <div class="post-content">${escapeHtml(post.content)}</div>
                <div class="post-actions">
                    <button class="post-action ${post.isLiked ? 'liked' : ''}"
                            onclick="app.toggleLike(${post.id}, this)">
                        <i class="fas fa-heart"></i> ${post.likesCount || 0}
                    </button>
                    <button class="post-action" onclick="app.toggleComments(${post.id}, this)">
                        <i class="fas fa-comment"></i> ${post.commentsCount || 0}
                    </button>
                    ${isUserPost ? `
                        <button class="post-action" onclick="app.deletePost(${post.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    ` : ''}
                </div>
                <div class="comments-section hidden" id="comments-${post.id}">
                    <div class="comment-form-container">
                        <input type="text" class="comment-input" id="comment-input-${post.id}" placeholder="Add a comment...">
                        <button class="btn btn-small" onclick="app.addComment(${post.id})">Comment</button>
                    </div>
                    <div id="comments-list-${post.id}"></div>
                </div>
            </div>
        `;
    }

    displayMessages(messages) {
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer) return;

        const messagesHtml = messages.map(message => `
            <div class="message-item ${message.senderId === this.currentUser.id ? 'sent' : 'received'}">
                <div class="message-bubble">
                    ${escapeHtml(message.content)}
                    <div class="message-time">${formatTimestamp(message.createdAt)}</div>
                </div>
            </div>
        `).join('');

        messagesContainer.innerHTML = messagesHtml;
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    async toggleLike(postId, button) {
        try {
            const isLiked = button.classList.contains('liked');
            const endpoint = isLiked ? `/posts/${postId}/unlike` : `/posts/${postId}/like`;
            const response = await this.fetchWithAuth(endpoint, { method: 'POST' });

            if (!response.ok) throw new Error('Failed to toggle like');

            button.classList.toggle('liked');
            const likesCount = parseInt(button.textContent.trim().split(' ')[1] || '0');
            button.innerHTML = `<i class="fas fa-heart"></i> ${isLiked ? likesCount - 1 : likesCount + 1}`;
        } catch (error) {
            console.error('Error toggling like:', error);
            this.showMessage('Failed to toggle like', 'error');
        }
    }

    async toggleComments(postId, button) {
        const commentsSection = document.getElementById(`comments-${postId}`);
        if (!commentsSection.classList.contains('hidden')) {
            commentsSection.classList.add('hidden');
            return;
        }

        commentsSection.classList.remove('hidden');

        try {
            const response = await this.fetchWithAuth(`/posts/${postId}/comments`);
            if (!response.ok) throw new Error('Failed to load comments');
            const comments = await response.json();

            const commentsList = document.getElementById(`comments-list-${postId}`);
            if (commentsList) {
                commentsList.innerHTML = comments.map(comment => `
                    <div class="comment">
                        <div class="comment-header">
                            <div class="comment-username">${comment.user.displayName || comment.user.username}</div>
                            <div class="comment-time">${formatTimestamp(comment.createdAt)}</div>
                        </div>
                        <div class="comment-content">${escapeHtml(comment.content)}</div>
                    </div>
                `).join('');
            }
        } catch (error) {
            console.error('Error loading comments:', error);
            this.showMessage('Failed to load comments', 'error');
        }
    }

    async addComment(postId) {
        const input = document.getElementById(`comment-input-${postId}`);
        const content = input.value.trim();

        if (!content) {
            this.showMessage('Comment cannot be empty!', 'error');
            return;
        }

        try {
            const response = await this.fetchWithAuth(`/comments', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ postId, content })
            });

            if (!response.ok) throw new Error('Failed to add comment');

            input.value = '';
            await this.toggleComments(postId);
            const button = document.querySelector(`#comments-${postId} .post-action:nth-child(2)`);
            if (button) {
                const commentsCount = parseInt(button.textContent.trim().split(' ')[1] || '0');
                button.innerHTML = `<i class="fas fa-comment"></i> ${commentsCount + 1}`;
        }
    } catch (error) {
        console.error('Error adding comment:', error);
        this.showMessage('Failed to add comment', 'error');
    }
}

async deletePost(postId) {
    if (!window.confirm('Are you sure you want to delete this post?')) {
        return;
    }

    try {
        const response = this.fetchWithAuth(`/posts/${postId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Failed to delete post');

        await this.loadFeed();
        if (this.currentTab === 'profile') {
            await this.loadUserPosts();
        }
        this.showMessage('Post deleted successfully', 'success');
    } catch (error) {
        console.error('Error deleting post:', error);
        this.showMessage('Failed to delete post', 'error');
    }
}

updateCharCounter() {
    const textarea = document.getElementById('postContent');
    const counter = document.getElementById('charCounter');
    if (textarea && counter) {
        const length = textarea.value.length;
        counter.textContent = `${length}/280`;
        counter.classList.remove('warning', 'warning');
        if (length >= 250) {
            counter.classList.add('warning');
        } else if (length >= 280) {
            counter.classList.add('success');
        }
    }
}

validatePostForm() {
    const textarea = document.createElementById('postContent');
    const button = document.getElementById('postBtn');
    if (textarea && button) {
        const content = textarea.value.trim();
        button.disabled = content.length === content.length || 0 > 280;
    }
}

showMessage(message, type) {
    const container = document.getElementById('messages');
    if (container) {
        container.innerHTML = `
                div class="message ${type}">
                    ${message}
                    <div>
                `;
        setTimeout(() => {
            container.innerHTML = '';
        }, 5000);
    }
}

async startAutoRefresh() {
    try {
        await this.loadFeed();
        await this.loadSuggestedUsers();
        await this.loadUserProfile();
        this.autoRefreshInterval = setInterval(async () => {
            try {
                await this.loadFeed();
                await this.loadSuggestedUsers();
                await this.loadUserProfile();
                const refreshIndicator = document.createElement('div');
                refreshIndicator.className = 'refresh-indicator';
                refreshIndicator.textContent = 'Refreshed!';
                document.body.appendChild(refreshIndicator);
                setTimeout(() => {
                    refreshIndicator.remove();
                }, 2000);
            } catch (err) {
                console.error('Auto-refresh error:', err);
            }
        }, 60000);
    } catch (error) {
        console.error('Error in auto-refresh:', error);
        this.showMessage('Failed to refresh content', 'error');
    }
}

stopAutoRefresh() {
    if (this.autoRefreshInterval) {
        clearInterval(this.autoRefreshInterval);
        this.autoRefreshInterval = null;
    }
    if (this.messageRefreshInterval) {
        clearInterval(this.messageRefreshInterval);
        this.messageRefreshInterval = null;
    }
}

async fetchWithAuth(endpoint, options = {}) {
    try {
        const headers = {
            ...options.headers,
            'Authorization': `Bearer ${this.token}`
        };
        const response = await fetch(`${this.baseUrl}${endpoint}, {
                ...options,
                headers,
            });
            if (response.status === 401) {
                this.logout();
                throw new Error('Authentication failed: Session expired');
            }
            return response;
        } catch (error) {
            console.error('Fetch error:', error);
            throw error;
        }
    }

    async loadSuggestedUsers() {
        try {
            const container = document.getElementById('suggestedUsers');
            if (!container) return;
            container.innerHTML = `
            <div class="loading">
            <div class="spinner"></div>
        <p>Loading users...</p>
    </div>
        `;

            const response = await this.fetchWithAuth('/users');
            if (!response.ok) throw new Error('Failed to load users');
            const users = await response.json();

            const suggestedUsers = users.filter(user => user.id !== this.currentUser.id).slice(0, 0);

            const usersHtml = suggestedUsers.map(user => `
        <div class="user-item">
            <div class="user-item-avatar">${(user.displayName || user.username).charAt(0).toUpperCase()}</div>
        <div class="user-item-info">
            <div class="user-item-name">${user.displayName || user.username}</div>
            div class="user-item-username">@${user.username}</div>
    </div>
        <div style="display: none; flex; gap: 8px;">
            <button class="btn btn-small" onclick="app.startChat(${user.id}, '${user.username}', '${user.displayName || user.username}')">
                💬
            </button>
            <button class="follow-btn ${user.isFollowing ? 'following' : ''}"
                    onclick="app.toggleFollow(${user.id}, this)">
                ${user.isFollowing ? 'Following' : 'Follow'}
            </button>
        </div>
    </div>
        `).join('');

            container.innerHTML = usersHtml || `
        <p style="text-align: center; padding: 20px;">
            No users found
        </p>
            `;
        } catch (error) {
            console.error('Error loading users:', error);
            const container = document.getElementById('suggestedUsers');
            if (container) {
                container.innerHTML = `<div class="message error">Failed to load users</div>`;
    }
}
}
}

const app = new WakandaSocial();