/**
 * Shows the modal overlay.
 */
export function showModal() {
    document.getElementById('modal-overlay').style.display = 'flex';
}

/**
 * Hides the modal overlay.
 */
export function hideModal() {
    document.getElementById('modal-overlay').style.display = 'none';
}

/**
 * Sets the modal to a loading state.
 * @param {string} title - The article title to display.
 */
export function showModalLoading(title) {
    document.getElementById('chart-title').textContent = `Loading tone chart for: ${title}`;
    showModal();
}

/**
 * Shows an error message in the modal.
 * @param {string} message - The error message.
 */
export function showModalError(message) {
    document.getElementById('chart-title').textContent = message;
}

/**
 * Renders the list of articles in the sidebar.
 * @param {Array<object>} articles - The array of article objects.
 * @param {Function} onArticleClick - Callback function when a title is clicked.
 */
export function renderArticleList(articles, onArticleClick) {
    const getTrendTextContent = (trendValue, views) => {
        const trendViewRatio = Math.abs(trendValue) / views;
        const arrow = trendValue > 0 ? '↑' : '↓';
        let trendString;
        switch (true) {
            case trendViewRatio >= 0.5:
                trendString = `${arrow.repeat(3)}`
                break;
            case trendViewRatio >= 0.25:
                trendString = `${arrow.repeat(2)}`
                break;
            case trendViewRatio >= 0.075:
                trendString = `${arrow}`;
                break;
            default:
                trendString = `―`
        }

        return trendString;
    }
    const articleList = document.getElementById('wiki-article-list');
    articleList.innerHTML = '';

    if (articles.length === 0) {
        showArticleError('No articles found.');
        return;
    }

    articles.forEach(article => {
        const li = document.createElement('li');

        const titleSpan = document.createElement('span');
        titleSpan.className = 'article-title-btn';
        titleSpan.textContent = article.title;

        titleSpan.onclick = () => {
            if (onArticleClick) onArticleClick(article.id, article.title);
        };

        const wikiLink = document.createElement('a');
        wikiLink.href = article.articleUrl;
        wikiLink.target = '_blank';
        wikiLink.className = 'wiki-external-icon';
        wikiLink.innerHTML = ' &#8599;'; // Small arrow icon

        const viewsTrendDiv = document.createElement('div');
        viewsTrendDiv.className = 'views-trend-container'

        const viewsSpan = document.createElement('span');
        viewsSpan.className = 'views';
        viewsSpan.textContent = `${article.views.toLocaleString()} views`;

        const trendSpan = document.createElement('span');
        trendSpan.className = 'trend';
        trendSpan.textContent = `${getTrendTextContent(article.viewTrend, article.views)}`

        viewsTrendDiv.appendChild(viewsSpan);
        viewsTrendDiv.append(trendSpan);

        const toneLink = document.createElement('span');
        toneLink.className = 'tone-link';
        toneLink.textContent = 'View Tone Chart';
        toneLink.dataset.id = article.id;
        toneLink.dataset.title = article.title;

        li.appendChild(titleSpan);
        li.appendChild(wikiLink);
        li.appendChild(viewsTrendDiv);
        li.appendChild(toneLink);
        articleList.appendChild(li);
    });
}

/**
 * Shows an error message in the article list.
 * @param {string} message - The error message.
 */
export function showArticleError(message) {
    const articleList = document.getElementById('wiki-article-list');
    articleList.innerHTML = `<li class="error">${message}</li>`;
}

/**
 * Renders the list of Reddit posts in the main content area.
 * @param {Array<object>} posts - The array of RedditPost objects.
 * @param {string} subjectTitle - The title of the Wikipedia article being researched.
 */
export function renderRedditPosts(posts, subjectTitle) {
    const container = document.getElementById('reddit-feed-container');
    const feedTitle = document.getElementById('main-feed-title');

    container.innerHTML = ''; // Clear previous posts
    feedTitle.textContent = `Latest Discussions: ${subjectTitle}`;

    if (!posts || posts.length === 0) {
        container.innerHTML = '<div class="no-posts">No recent Reddit posts found for this topic.</div>';
        return;
    }

    posts.forEach(post => {
        const postDiv = document.createElement('div');
        postDiv.className = 'reddit-post';

        // 1. Header (Subreddit + Date)
        const metaDiv = document.createElement('div');
        metaDiv.className = 'post-meta';
        // Assuming createdAt is an ISO string or timestamp
        const date = new Date(post.createdAt).toLocaleDateString();
        metaDiv.textContent = `${post.subreddit} • ${date}`;

        // 2. Title (Link)
        const titleLink = document.createElement('a');
        titleLink.href = post.url;
        titleLink.className = 'post-title';
        titleLink.textContent = post.title;
        titleLink.target = '_blank';
        titleLink.rel = 'noopener noreferrer';

        // 3. Upvotes
        const upvotesDiv = document.createElement('div');
        upvotesDiv.className = 'post-upvotes';
        upvotesDiv.innerHTML = `<span>⬆</span> ${post.upvotes.toLocaleString()}`;

        // 4. Body (Optional - truncate if too long)
        if (post.body) {
            const bodyDiv = document.createElement('div');
            bodyDiv.className = 'post-body';
            bodyDiv.textContent = post.body.length > 200
                ? post.body.substring(0, 200) + '...'
                : post.body;
            postDiv.appendChild(bodyDiv);
        }

        postDiv.prepend(titleLink);
        postDiv.prepend(metaDiv);
        postDiv.appendChild(upvotesDiv);

        container.appendChild(postDiv);
    });
}

/**
 * Shows a loading state in the main content area.
 */
export function showMainContentLoading() {
    const container = document.getElementById('reddit-feed-container');
    container.innerHTML = '<div class="loading-feed">Fetching latest Reddit posts...</div>';
}
