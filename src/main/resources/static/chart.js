let currentChart = null;

/**
 * Renders the tone chart in the modal.
 * @param {object} histogramData - The parsed histogram JSON from the API.
 * @param {string} title - The title of the article.
 */
export function displayToneChart(histogramData, title) {
    let parsedData = histogramData;

    if (histogramData && typeof histogramData.asString === 'string') {
        parsedData = JSON.parse(histogramData.asString);
    }

    if (!parsedData.histogram || parsedData.histogram.length === 0) {
        document.getElementById('chart-title').textContent = 'No tone data available.';
        return;
    }

    const labels = parsedData.histogram.map(item => item.bin);
    const data = parsedData.histogram.map(item => item.count);
    const queryDate = parsedData.query_date || 'Unknown Date';
    const timespan = parsedData.timespan || '?';

    const ctx = document.getElementById('tone-chart-canvas').getContext('2d');

    if (currentChart) {
        currentChart.destroy();
    }

    currentChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Article Count',
                data: data,
                backgroundColor: (context) => {
                    const bin = context.chart.data.labels[context.dataIndex];
                    return bin < 0 ? 'rgba(214, 40, 40, 0.6)' : 'rgba(44, 160, 44, 0.6)';
                },
                borderColor: (context) => {
                    const bin = context.chart.data.labels[context.dataIndex];
                    return bin < 0 ? 'rgba(214, 40, 40, 1)' : 'rgba(44, 160, 44, 1)';
                },
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Tone (higher ~= more positive)'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: '# of Articles'
                    },
                    beginAtZero: true
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            },
            responsive: true,
            maintainAspectRatio: true
        }
    });

    document.getElementById('chart-title').textContent = `Tone Chart: ${title} (Data from ${queryDate}, timespan: ${timespan})`;
}

/**
 * Destroys the current chart instance, if one exists.
 * Used when closing the modal or loading a new chart.
 */
export function destroyCurrentChart() {
    if (currentChart) {
        currentChart.destroy();
        currentChart = null;
    }
}
