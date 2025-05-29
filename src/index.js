// Mobile menu toggle
document.addEventListener('DOMContentLoaded', function() {
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
});

// Function to update time
function updateTime() {
    const now = new Date();
    
    // UK time (BST)
    const ukTime = new Date(now.toLocaleString('en-US', { timeZone: 'Europe/London' }));
    document.getElementById('uk-time').textContent = ukTime.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        second: '2-digit',
        hour12: false 
    });

    // India time (IST)
    const indiaTime = new Date(now.toLocaleString('en-US', { timeZone: 'Asia/Kolkata' }));
    document.getElementById('india-time').textContent = indiaTime.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        second: '2-digit',
        hour12: false 
    });

    // US Eastern time (EDT)
    const usTime = new Date(now.toLocaleString('en-US', { timeZone: 'America/New_York' }));
    document.getElementById('us-time').textContent = usTime.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        second: '2-digit',
        hour12: false 
    });
}

// Update time immediately and then every second
updateTime();
setInterval(updateTime, 1000); 