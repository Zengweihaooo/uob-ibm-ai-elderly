/**
 * Main JavaScript file for the IBM AI Elderly Project website
 * 
 * This file handles:
 * - Mobile menu functionality
 * - Dynamic content loading from markdown files
 * - Modal display for content
 * - Real-time clock updates for different timezones
 * 
 * @author Weihao Zeng
 * @version 1.0
 */

// Mobile menu toggle functionality
document.addEventListener('DOMContentLoaded', async function() {
    const menuToggle = document.querySelector('.menu-toggle');
    const navLinks = document.querySelector('.nav-links');

    // Toggle mobile menu when hamburger button is clicked
    menuToggle.addEventListener('click', function() {
        menuToggle.classList.toggle('active');
        navLinks.classList.toggle('active');
    });

    // Close mobile menu when clicking outside the navigation area
    document.addEventListener('click', function(event) {
        if (!event.target.closest('.nav-container')) {
            menuToggle.classList.remove('active');
            navLinks.classList.remove('active');
        }
    });

    // ---------------------------------------------
    // Modal functionality & dynamic updates loading
    // ---------------------------------------------
    
    // Check if marked library is loaded for markdown parsing
    if (typeof marked === 'undefined') {
        console.error('Marked library not loaded - markdown parsing unavailable');
        return;
    }

    const modal = document.getElementById('contentModal');
    const closeButton = modal.querySelector('.close-button');
    const modalTitle = modal.querySelector('.modal-title');
    const modalBody = modal.querySelector('.modal-body');

    // Close modal event handlers
    closeButton.addEventListener('click', () => (modal.style.display = 'none'));
    modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.style.display = 'none';
    });

    /**
     * Load and display latest project updates from markdown files
     * 
     * This function fetches meeting notes and learning journal content
     * from markdown files and displays them in a dynamic list.
     */
    async function loadLatestUpdates() {
        try {
            // List of meeting markdown files to load (add or remove as needed)
            const meetingFiles = [
                '2025-06-06-SecondMeeting.md',
                '2025-05-14-kickoff.md',
                '2025-05-16-officehour-john-mcnamara.md'
            ];

            const updates = [];

            // Fetch and process meeting files
            for (const file of meetingFiles) {
                try {
                    const res = await fetch(`./docs/meetings/${file}`);
                    if (!res.ok) continue;
                    const content = await res.text();
                    const date = file.split('-').slice(0, 3).join('-');
                    updates.push({
                        type: 'meeting',
                        title: file.replace('.md', ''),
                        date,
                        content
                    });
                } catch (err) {
                    console.warn(`Error loading meeting file ${file}:`, err);
                }
            }

            // Fetch learning journal content
            try {
                const journalRes = await fetch('./docs/LearningJournal/WeihaoZeng.md');
                if (journalRes.ok) {
                    const journalContent = await journalRes.text();
                    updates.push({
                        type: 'journal',
                        title: "Weihao Zeng's Learning Journal",
                        date: '2025-06-06',
                        content: journalContent
                    });
                }
            } catch (err) {
                console.error('Error loading learning journal:', err);
            }

            // Sort updates by date in descending order (newest first)
            updates.sort((a, b) => new Date(b.date) - new Date(a.date));

            // Render updates to the DOM
            const updatesList = document.querySelector('.updates-list');
            updatesList.innerHTML = '';
            updates.forEach((update) => {
                const el = document.createElement('div');
                el.className = 'update-item';
                el.innerHTML = `
                    <div class="update-header">
                        <h3>${update.title}</h3>
                        <span class="date">${update.date}</span>
                    </div>
                    <p class="description">Click to view details</p>
                `;
                el.addEventListener('click', () => {
                    modalTitle.textContent = update.title;
                    modalBody.innerHTML = marked.parse(update.content);
                    modal.style.display = 'block';
                });
                updatesList.appendChild(el);
            });
        } catch (err) {
            console.error('Error loading updates:', err);
        }
    }

    // Initialize dynamic content loading
    loadLatestUpdates();
});

// ----------------------------
// Real-time clock functionality (runs globally)
// ----------------------------

/**
 * Update time displays for different timezones
 * 
 * This function updates the clock displays for UK, India, and US timezones
 * to help team members coordinate across different time zones.
 */
function updateTime() {
    const now = new Date();
    const zones = [
        { id: 'uk-time', tz: 'Europe/London' },
        { id: 'india-time', tz: 'Asia/Kolkata' },
        { id: 'us-time', tz: 'America/New_York' }
    ];

    zones.forEach(({ id, tz }) => {
        const el = document.getElementById(id);
        if (!el) return;
        const time = new Date(now.toLocaleString('en-US', { timeZone: tz }));
        el.textContent = time.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        });
    });
}

// Initialize clock and update every second
updateTime();
setInterval(updateTime, 1000); 