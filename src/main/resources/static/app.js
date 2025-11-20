import { fetchTopArticles, fetchArticleToneChart, fetchLatestRedditPosts } from './api.js';
import { displayToneChart, destroyCurrentChart } from './chart.js';
import {
    renderArticleList,
    showArticleError,
    hideModal,
    showModalLoading,
    showModalError,
    renderRedditPosts,
    showMainContentLoading
} from './ui.js';

document.addEventListener('DOMContentLoaded', initializeApp);

/**
 * Initializes the application and sets up event listeners.
 */
function initializeApp() {
    const modalOverlay = document.getElementById('modal-overlay');
    const closeModalBtn = document.getElementById('close-modal');
    const articleList = document.getElementById('wiki-article-list');
    const themeToggleBtn = document.getElementById('theme-toggle');

    // Modal Listeners
    closeModalBtn.addEventListener('click', () => {
        hideModal();
        destroyCurrentChart();
    });

    modalOverlay.addEventListener('click', (event) => {
        if (event.target === modalOverlay) {
            hideModal();
            destroyCurrentChart();
        }
    });

    // Article Selection Listener
    articleList.addEventListener('click', handleArticleListClick);

    // Theme Toggle Listener
    themeToggleBtn.addEventListener('click', toggleTheme);

    // Initialize Theme from LocalStorage if available
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
    }

    // Load Initial Data
    loadTopArticles().then();
}

/**
 * Toggles the dark mode class and saves preference.
 */
function toggleTheme() {
    document.body.classList.toggle('dark-mode');
    const isDark = document.body.classList.contains('dark-mode');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
}

/**
 * Fetches and renders the top articles.
 * @returns {Promise<void>}
 */
async function loadTopArticles() {
    try {
        const articles = await fetchTopArticles();
        renderArticleList(articles, handleArticleSelection);
    } catch (error) {
        console.error('Failed to fetch articles:', error);
        showArticleError('Failed to load articles.');
    }
}

/**
 * Handles clicks within the article list, using event delegation.
 * @param {Event} event - The click event.
 * @returns {Promise<void>}
 */
async function handleArticleListClick(event) {
    if (!event.target.classList.contains('tone-link')) {
        return;
    }

    const link = event.target;
    const articleId = link.dataset.id;
    const articleTitle = link.dataset.title;

    showModalLoading(articleTitle);
    destroyCurrentChart();

    try {
        const histogramData = await fetchArticleToneChart(articleId);
        displayToneChart(histogramData, articleTitle);
    } catch (error) {
        console.error('Failed to fetch tone chart:', error);
        showModalError('Error loading tone data.');
    }
}

/**
 * Handles clicks on individual article items.
 * @param articleId
 * @param articleTitle
 * @returns {Promise<void>}
 */
async function handleArticleSelection(articleId, articleTitle) {
    showMainContentLoading();
    try {
        const posts = await fetchLatestRedditPosts(articleId);
        renderRedditPosts(posts, articleTitle);
    } catch (error) {
        console.error(error);
        document.getElementById('reddit-feed-container').innerHTML =
            `<div class="error">Failed to load posts: ${error.message}</div>`;
    }
}
