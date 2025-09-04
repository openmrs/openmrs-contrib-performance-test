document.addEventListener('DOMContentLoaded', function() {

    function addCustomButtons() {
        const docLink = document.querySelector('a.gatling-documentation');
        if (!docLink) {
            console.error('Gatling report modifier: Could not find the "Documentation" button.');
            return;
        }

        const performanceTrendsButton = `
            <a class="gatling-documentation" href="https://o3-performance.openmrs.org/performance-trends" target="_blank">
                Performance Trends
            </a>`;
        const reportSizesButton = `
            <a class="gatling-documentation" href="https://o3-performance.openmrs.org/response-sizes" target="_blank">
                Response Sizes
            </a>`;

        docLink.insertAdjacentHTML('afterend', performanceTrendsButton);
        docLink.insertAdjacentHTML('afterend', reportSizesButton);
        console.log('Gatling report modifier: Added custom buttons.');
    }

    function makeAssertionsCollapsible() {
        const assertionsContainer = document.querySelector('div.statistics.extensible-geant');
        if (!assertionsContainer) {
            console.error('Gatling report modifier: Could not find the assertions container.');
            return;
        }

        assertionsContainer.classList.add('assertions-wrapper');
        const assertionsTitle = assertionsContainer.querySelector('.title');

        if (assertionsTitle) {
            assertionsTitle.addEventListener('click', () => {
                assertionsContainer.classList.toggle('collapsed');
            });
            console.log('Gatling report modifier: Enabled collapsible assertions.');
        }
    }

    addCustomButtons();
    makeAssertionsCollapsible();

});
