/**
 * Onboarding Guide System for Elderly Users
 * Provides step-by-step guidance for first-time users
 * 
 * @author Weihao Zeng
 * @version 1.0
 */

class OnboardingGuide {
    constructor() {
        this.currentStep = 0;
        this.totalSteps = 6;
        this.isActive = false;
        this.steps = [
            {
                icon: '👋',
                title: 'Welcome to AI Healthcare Assistant',
                description: 'This is an intelligent healthcare companion designed for elderly users, helping you better manage your daily health and wellness.',
                highlight: null,
                tooltip: null
            },
            {
                icon: '🤖',
                title: 'AI Healthcare Features',
                description: 'Get 24/7 health consultation, professional medical advice, and personalized health recommendations.',
                highlight: '.chat-container',
                tooltip: 'AI-powered healthcare assistance'
            },
            {
                icon: '🎤',
                title: 'Voice Interaction',
                description: 'Hold the microphone button to speak with the AI assistant using voice commands.',
                highlight: '.voice-controls',
                tooltip: 'Voice interaction capabilities'
            },
            {
                icon: '📱',
                title: 'Easy Navigation',
                description: 'Simple and intuitive interface designed specifically for elderly users with large buttons and clear text.',
                highlight: '.main-content',
                tooltip: 'User-friendly interface design'
            },
            {
                icon: '🔒',
                title: 'Privacy & Security',
                description: 'Your health information is protected with secure encryption and privacy controls.',
                highlight: '.privacy-info',
                tooltip: 'Data security and privacy'
            },
            {
                icon: '🎯',
                title: 'Ready to Start',
                description: 'You\'re now ready to use the AI Healthcare Assistant! Start by asking a health question or using voice commands.',
                highlight: null,
                tooltip: null
            }
        ];
        
        this.init();
    }

    init() {
        this.createOverlay();
        this.bindEvents();
        this.checkFirstTime();
    }

    createOverlay() {
        // Create overlay container
        this.overlay = document.createElement('div');
        this.overlay.className = 'onboarding-overlay';
        this.overlay.setAttribute('role', 'dialog');
        this.overlay.setAttribute('aria-label', 'AI Healthcare Assistant Guide');
        this.overlay.setAttribute('aria-describedby', 'onboarding-description');

        // Create main container
        this.container = document.createElement('div');
        this.container.className = 'onboarding-container';

        // Create header
        const header = document.createElement('div');
        header.className = 'onboarding-header';
        header.innerHTML = `
            <h1 class="onboarding-title">AI Healthcare Assistant Guide</h1>
            <p class="onboarding-subtitle">Let\'s explore the features of your intelligent healthcare companion</p>
        `;

        // Create content area
        this.content = document.createElement('div');
        this.content.className = 'onboarding-content';

        // Create progress bar
        const progress = document.createElement('div');
        progress.className = 'onboarding-progress';
        progress.innerHTML = `
            <div class="progress-bar">
                <div class="progress-fill" style="width: 0%"></div>
            </div>
            <div class="progress-text">Step 1 / ${this.totalSteps}</div>
        `;

        // Create navigation
        const navigation = document.createElement('div');
        navigation.className = 'onboarding-navigation';
        navigation.innerHTML = `
            <button class="onboarding-btn onboarding-btn-skip" id="onboarding-skip">
                Skip Guide
            </button>
            <div class="onboarding-btn-group">
                <button class="onboarding-btn onboarding-btn-secondary" id="onboarding-prev" style="display: none;">
                    Previous
                </button>
                <button class="onboarding-btn onboarding-btn-primary" id="onboarding-next">
                    Next
                </button>
            </div>
        `;

        // Create skip link for screen readers
        const skipLink = document.createElement('a');
        skipLink.href = './src/pages/ai-assistant.html';
        skipLink.className = 'onboarding-sr-only';
        skipLink.textContent = 'Skip guide and go to AI Assistant';

        // Assemble overlay
        this.container.appendChild(header);
        this.container.appendChild(this.content);
        this.container.appendChild(progress);
        this.container.appendChild(navigation);
        this.overlay.appendChild(this.container);
        this.overlay.appendChild(skipLink);

        // Add to page
        document.body.appendChild(this.overlay);

        // Store references
        this.progressFill = this.overlay.querySelector('.progress-fill');
        this.progressText = this.overlay.querySelector('.progress-text');
        this.prevBtn = this.overlay.querySelector('#onboarding-prev');
        this.nextBtn = this.overlay.querySelector('#onboarding-next');
        this.skipBtn = this.overlay.querySelector('#onboarding-skip');
    }

    bindEvents() {
        // Navigation buttons
        this.nextBtn.addEventListener('click', () => this.nextStep());
        this.prevBtn.addEventListener('click', () => this.prevStep());
        this.skipBtn.addEventListener('click', () => this.complete());

        // Keyboard navigation
        this.overlay.addEventListener('keydown', (e) => {
            switch(e.key) {
                case 'Escape':
                    this.complete();
                    break;
                case 'ArrowRight':
                case ' ':
                    e.preventDefault();
                    this.nextStep();
                    break;
                case 'ArrowLeft':
                    e.preventDefault();
                    this.prevStep();
                    break;
            }
        });

        // Click outside to close (optional)
        this.overlay.addEventListener('click', (e) => {
            if (e.target === this.overlay) {
                this.complete();
            }
        });
    }

    checkFirstTime() {
        // Don't auto-start onboarding, wait for user interaction
        // The onboarding will be triggered when user clicks "Try AI Healthcare Assistant"
    }

    start() {
        this.isActive = true;
        this.currentStep = 0;
        this.overlay.classList.add('active');
        this.showStep(0);
        this.updateProgress();
        
        // Focus management
        this.nextBtn.focus();
        
        // Announce to screen readers
        this.announceStep();
    }

    showStep(stepIndex) {
        // Hide all steps
        const steps = this.overlay.querySelectorAll('.onboarding-step');
        steps.forEach(step => step.classList.remove('active'));

        // Show current step
        if (this.content.children[stepIndex]) {
            this.content.children[stepIndex].classList.add('active');
        } else {
            // Create step content
            this.createStepContent(stepIndex);
        }

        // Update navigation buttons
        this.prevBtn.style.display = stepIndex === 0 ? 'none' : 'inline-flex';
        this.nextBtn.textContent = stepIndex === this.totalSteps - 1 ? 'Finish' : 'Next';

        // Handle highlights
        this.handleHighlights(stepIndex);
    }

    createStepContent(stepIndex) {
        const step = this.steps[stepIndex];
        const stepElement = document.createElement('div');
        stepElement.className = 'onboarding-step active';
        stepElement.innerHTML = `
            <span class="onboarding-step-icon" role="img" aria-label="${step.title}">${step.icon}</span>
            <h2 class="onboarding-step-title">${step.title}</h2>
            <p class="onboarding-step-description" id="onboarding-description">${step.description}</p>
        `;

        // Clear existing content and add new step
        this.content.innerHTML = '';
        this.content.appendChild(stepElement);
    }

    handleHighlights(stepIndex) {
        // Remove existing highlights
        this.removeHighlights();

        const step = this.steps[stepIndex];
        if (step.highlight) {
            const targetElement = document.querySelector(step.highlight);
            if (targetElement) {
                this.createHighlight(targetElement, step.tooltip);
            }
        }
    }

    createHighlight(element, tooltipText) {
        // Create highlight box
        const highlight = document.createElement('div');
        highlight.className = 'onboarding-highlight';
        
        // Position highlight
        const rect = element.getBoundingClientRect();
        highlight.style.top = rect.top + 'px';
        highlight.style.left = rect.left + 'px';
        highlight.style.width = rect.width + 'px';
        highlight.style.height = rect.height + 'px';
        
        document.body.appendChild(highlight);

        // Create tooltip if needed
        if (tooltipText) {
            const tooltip = document.createElement('div');
            tooltip.className = 'onboarding-tooltip';
            tooltip.textContent = tooltipText;
            
            // Position tooltip above highlight
            tooltip.style.top = (rect.top - 80) + 'px';
            tooltip.style.left = (rect.left + rect.width / 2 - 150) + 'px';
            
            document.body.appendChild(tooltip);
        }
    }

    removeHighlights() {
        // Remove highlights and tooltips
        const highlights = document.querySelectorAll('.onboarding-highlight, .onboarding-tooltip');
        highlights.forEach(el => el.remove());
    }

    nextStep() {
        if (this.currentStep < this.totalSteps - 1) {
            this.currentStep++;
            this.showStep(this.currentStep);
            this.updateProgress();
            this.announceStep();
        } else {
            this.complete();
        }
    }

    prevStep() {
        if (this.currentStep > 0) {
            this.currentStep--;
            this.showStep(this.currentStep);
            this.updateProgress();
            this.announceStep();
        }
    }

    updateProgress() {
        const percentage = ((this.currentStep + 1) / this.totalSteps) * 100;
        this.progressFill.style.width = percentage + '%';
        this.progressText.textContent = `Step ${this.currentStep + 1} / ${this.totalSteps}`;
    }

    announceStep() {
        const step = this.steps[this.currentStep];
        const announcement = document.createElement('div');
        announcement.setAttribute('aria-live', 'polite');
        announcement.setAttribute('aria-atomic', 'true');
        announcement.className = 'onboarding-sr-only';
        announcement.textContent = `Step ${this.currentStep + 1}: ${step.title}. ${step.description}`;
        
        document.body.appendChild(announcement);
        
        // Remove after announcement
        setTimeout(() => {
            announcement.remove();
        }, 1000);
    }

    complete() {
        this.isActive = false;
        this.overlay.classList.remove('active');
        this.removeHighlights();
        
        // Mark as completed
        localStorage.setItem('onboarding-completed', 'true');
        
        // Navigate to AI Assistant page after completion
        setTimeout(() => {
            window.location.href = './src/pages/ai-assistant.html';
        }, 500);
        
        // Announce completion
        this.announceCompletion();
    }

    announceCompletion() {
        const announcement = document.createElement('div');
        announcement.setAttribute('aria-live', 'polite');
        announcement.setAttribute('aria-atomic', 'true');
        announcement.className = 'onboarding-sr-only';
        announcement.textContent = 'Guide completed! You can now start using the AI Healthcare Assistant.';
        
        document.body.appendChild(announcement);
        
        setTimeout(() => {
            announcement.remove();
        }, 2000);
    }

    // Public method to restart onboarding
    restart() {
        localStorage.removeItem('onboarding-completed');
        this.start();
    }

    // Public method to check if onboarding is active
    getActive() {
        return this.isActive;
    }
}

// Initialize onboarding when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.onboardingGuide = new OnboardingGuide();
    
            // Add restart button to navigation (for testing)
        const nav = document.querySelector('.nav-links');
        if (nav) {
            const restartBtn = document.createElement('a');
            restartBtn.href = '#';
            restartBtn.className = 'nav-link';
            restartBtn.textContent = 'Restart Guide';
            restartBtn.addEventListener('click', (e) => {
                e.preventDefault();
                window.onboardingGuide.restart();
            });
            nav.appendChild(restartBtn);
        }
});

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = OnboardingGuide;
}
