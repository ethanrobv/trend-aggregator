const API_BASE = '/api';

/**
 * Fetches the list of top wikipedia articles from the backend.
 * @returns {Promise<Array<object>>} A promise that resolves to the array of articles.
 */
export async function fetchTopArticles() {
    const response = await fetch(`${API_BASE}/top-articles`);
    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    return await response.json();
}

/**
 * Fetches the tone chart data for a single wikipedia article.
 * @param {string} id - The ID of the article.
 * @returns {Promise<object>} A promise that resolves to the parsed tone chart JSON.
 */
export async function fetchArticleToneChart(id) {
    const response = await fetch(`${API_BASE}/article-tone-chart/${id}`);
    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    return await response.json();
}

/**
 * Fetches the list of latest reddit posts for some wikipedia article's subject.
 * @param {string} id - The ID of the wikipedia article.
 * @returns {Promise<Array<object>>} A promise that resolves to the array of posts.
 */
export async function fetchLatestRedditPosts(id) {
    const response = await fetch(`${API_BASE}/latest-reddit-posts/${id}`)
    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    return await response.json();
}
