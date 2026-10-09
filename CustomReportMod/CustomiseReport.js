/* GATLING-REPORT-MOD-JS-START */
document.addEventListener('DOMContentLoaded', function() {

    // OpenMRS logos are copied into the report's style/ directory by report-mod.sh.
    const OPENMRS_LOGO_LIGHT = 'style/openmrs_dark.webp';
    const OPENMRS_LOGO_DARK = 'style/openmrs_light.webp';

    function replaceGatlingLogo() {
        const logoLinks = document.querySelectorAll('a.gatling-logo');
        if (logoLinks.length === 0) {
            console.error('Gatling report modifier: Could not find the Gatling logo.');
            return;
        }

        logoLinks.forEach(function(link) {
            const img = link.querySelector('img');
            if (!img) {
                return;
            }
            // .gatling-logo-light shows on the dark theme, .gatling-logo-dark on the light theme.
            img.src = link.classList.contains('gatling-logo-light') ? OPENMRS_LOGO_LIGHT : OPENMRS_LOGO_DARK;
            img.alt = 'OpenMRS';
            link.href = 'https://openmrs.org';
            link.title = 'OpenMRS Home Page';
        });
        console.log('Gatling report modifier: Replaced Gatling logo with OpenMRS logo.');
    }

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

    replaceGatlingLogo();
    addCustomButtons();
    makeAssertionsCollapsible();

});
