// Mobile menu toggle
document.addEventListener('DOMContentLoaded', async function() {
    const menuToggle = document.querySelector('.menu-toggle');
    const navLinks = document.querySelector('.nav-links');

    menuToggle.addEventListener('click', function() {
        menuToggle.classList.toggle('active');
        navLinks.classList.toggle('active');
    });

    // Close menu when clicking outside
    document.addEventListener('click', function(event) {
        if (!event.target.closest('.nav-container')) {
            menuToggle.classList.remove('active');
            navLinks.classList.remove('active');
        }
    });

    // ---------------------------------------------
    // Modal functionality & dynamic updates loading
    // ---------------------------------------------
    if (typeof marked === 'undefined') {
        console.error('Marked library not loaded');
        return;
    }

    const modal = document.getElementById('contentModal');
    const closeButton = modal.querySelector('.close-button');
    const modalTitle = modal.querySelector('.modal-title');
    const modalBody = modal.querySelector('.modal-body');

    // Close modal handlers
    closeButton.addEventListener('click', () => (modal.style.display = 'none'));
    modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.style.display = 'none';
    });

    async function loadLatestUpdates() {
        try {
            // Meeting markdown files (add or remove as needed)
            const meetingFiles = [
                '2025-06-06-SecondMeeting.md',
                '2025-05-14-kickoff.md',
                '2025-05-16-officehour-john-mcnamara.md'
            ];

            const updates = [];

            // Fetch meetings
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
                    console.warn(`Error loading ${file}:`, err);
                }
            }

            // Fetch learning journal
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

            // Sort by date DESC
            updates.sort((a, b) => new Date(b.date) - new Date(a.date));

            // Render
            const updatesList = document.querySelector('.updates-list');
            updatesList.innerHTML = '';
            updates.forEach((u) => {
                const el = document.createElement('div');
                el.className = 'update-item';
                el.innerHTML = `
                    <div class="update-header">
                        <h3>${u.title}</h3>
                        <span class="date">${u.date}</span>
                    </div>
                    <p class="description">Click to view details</p>
                `;
                el.addEventListener('click', () => {
                    modalTitle.textContent = u.title;
                    modalBody.innerHTML = marked.parse(u.content);
                    modal.style.display = 'block';
                });
                updatesList.appendChild(el);
            });
        } catch (err) {
            console.error('Error loading updates:', err);
        }
    }

    loadLatestUpdates();
});

// ----------------------------
// Timezone clock (runs globally)
// ----------------------------
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

updateTime();
setInterval(updateTime, 1000); 