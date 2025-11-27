const API_BASE = '/api';

/**
 * Fetches the list of top wikipedia articles for the current date.
 * @returns {Promise<Array<object>>} A promise that resolves to the array of articles.
 */
export async function fetchTopArticles() {
    // 1. Get current date in UTC to match backend logic
    const now = new Date();
    const year = now.getUTCFullYear();
    // Month is 0-indexed in JS, so add 1. Pad with '0' if needed.
    const month = String(now.getUTCMonth() + 1).padStart(2, '0');
    const day = String(now.getUTCDate()).padStart(2, '0');

    // 2. Call the new endpoint structure
    const response = await fetch(`${API_BASE}/trending-topics/${year}/${month}/${day}`);

    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    return await response.json();
}

/**
 * Fetches the tone chart data for a single wikipedia article.
 * @param {string} id - The ID of the article (Topic ID).
 * @returns {Promise<object>} A promise that resolves to the parsed tone chart JSON.
 */
export async function fetchArticleToneChart(id) {
    // No changes needed here, assuming backend still accepts Topic ID
    const response = await fetch(`${API_BASE}/article-tone-chart/${id}`);
    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }
    return await response.json();
}

export async function fetchLatestRedditPosts(id) {
    // No changes needed here
    const response = await fetch(`${API_BASE}/latest-reddit-posts/${id}`)
    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }
    return await response.json();
}
