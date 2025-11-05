import { fetchTopArticles, fetchArticleToneChart } from './api.js';
import { displayToneChart, destroyCurrentChart } from './chart.js';
import {
    renderArticleList,
    showArticleError,
    hideModal,
    showModalLoading,
    showModalError
} from './ui.js';

document.addEventListener('DOMContentLoaded', initializeApp);

/**
 * Initializes the application and sets up event listeners.
 */
function initializeApp() {
    const modalOverlay = document.getElementById('modal-overlay');
    const closeModalBtn = document.getElementById('close-modal');
    const articleList = document.getElementById('wiki-article-list');

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

    articleList.addEventListener('click', handleArticleListClick);

    loadTopArticles().then();
}

/**
 * Fetches and renders the top articles.
 */
async function loadTopArticles() {
    try {
        const articles = await fetchTopArticles();
        renderArticleList(articles);
    } catch (error) {
        console.error('Failed to fetch articles:', error);
        showArticleError('Failed to load articles.');
    }
}

/**
 * Handles clicks within the article list, using event delegation.
 * @param {Event} event - The click event.
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
