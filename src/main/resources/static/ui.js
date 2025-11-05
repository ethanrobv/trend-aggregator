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
 */
export function renderArticleList(articles) {
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

        const a = document.createElement('a');
        a.href = article.articleUrl;
        a.textContent = article.title;
        a.target = '_blank';
        a.rel = 'noopener noreferrer';

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

        li.appendChild(a);
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
