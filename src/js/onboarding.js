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
                title: '欢迎使用AI陪伴系统',
                description: '这是一个专为老年人设计的智能陪伴应用，帮助您更好地管理日常生活和健康。',
                highlight: null,
                tooltip: null
            },
            {
                icon: '🧭',
                title: '导航菜单介绍',
                description: '顶部导航栏包含所有主要功能：AI健康助手、日程管理、备忘录等。',
                highlight: '.nav-links',
                tooltip: '这里可以访问所有主要功能'
            },
            {
                icon: '🤖',
                title: 'AI健康助手',
                description: '点击"AI Assistant"按钮，获得24/7健康咨询和专业建议。',
                highlight: 'a[href="./src/pages/ai-assistant.html"]',
                tooltip: 'AI健康助手随时为您服务'
            },
            {
                icon: '📅',
                title: '日程管理功能',
                description: '点击"Daily Schedule"按钮，管理您的日常活动和提醒。',
                highlight: 'a[href="./src/pages/schedule.html"]',
                tooltip: '轻松管理您的日程安排'
            },
            {
                icon: '🎤',
                title: '语音交互功能',
                description: '在AI助手页面，您可以按住麦克风按钮进行语音对话。',
                highlight: '.cta-button.primary',
                tooltip: '体验语音交互功能'
            },
            {
                icon: '🎯',
                title: '开始使用',
                description: '现在您已经了解了主要功能，可以开始使用了！建议先尝试AI健康助手。',
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
        this.overlay.setAttribute('aria-label', '应用使用引导');
        this.overlay.setAttribute('aria-describedby', 'onboarding-description');

        // Create main container
        this.container = document.createElement('div');
        this.container.className = 'onboarding-container';

        // Create header
        const header = document.createElement('div');
        header.className = 'onboarding-header';
        header.innerHTML = `
            <h1 class="onboarding-title">应用使用引导</h1>
            <p class="onboarding-subtitle">让我们一起来了解这个智能陪伴系统</p>
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
            <div class="progress-text">步骤 1 / ${this.totalSteps}</div>
        `;

        // Create navigation
        const navigation = document.createElement('div');
        navigation.className = 'onboarding-navigation';
        navigation.innerHTML = `
            <button class="onboarding-btn onboarding-btn-skip" id="onboarding-skip">
                跳过引导
            </button>
            <div class="onboarding-btn-group">
                <button class="onboarding-btn onboarding-btn-secondary" id="onboarding-prev" style="display: none;">
                    上一步
                </button>
                <button class="onboarding-btn onboarding-btn-primary" id="onboarding-next">
                    下一步
                </button>
            </div>
        `;

        // Create skip link for screen readers
        const skipLink = document.createElement('a');
        skipLink.href = '#main-content';
        skipLink.className = 'onboarding-sr-only';
        skipLink.textContent = '跳过引导，直接进入主要内容';

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
        const hasSeenOnboarding = localStorage.getItem('onboarding-completed');
        if (!hasSeenOnboarding) {
            // Show after a short delay to let page load
            setTimeout(() => {
                this.start();
            }, 1000);
        }
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
        this.nextBtn.textContent = stepIndex === this.totalSteps - 1 ? '完成' : '下一步';

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
        this.progressText.textContent = `步骤 ${this.currentStep + 1} / ${this.totalSteps}`;
    }

    announceStep() {
        const step = this.steps[this.currentStep];
        const announcement = document.createElement('div');
        announcement.setAttribute('aria-live', 'polite');
        announcement.setAttribute('aria-atomic', 'true');
        announcement.className = 'onboarding-sr-only';
        announcement.textContent = `步骤 ${this.currentStep + 1}：${step.title}。${step.description}`;
        
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
        
        // Focus management
        document.querySelector('.main-content').focus();
        
        // Announce completion
        this.announceCompletion();
    }

    announceCompletion() {
        const announcement = document.createElement('div');
        announcement.setAttribute('aria-live', 'polite');
        announcement.setAttribute('aria-atomic', 'true');
        announcement.className = 'onboarding-sr-only';
        announcement.textContent = '引导完成，您现在可以开始使用应用了';
        
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
        restartBtn.textContent = '重新引导';
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
