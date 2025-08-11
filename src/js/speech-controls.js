/**
 * Speech Controls - UI components for TTS and STT
 * Provides elderly-friendly speech input and output controls
 */

/**
 * Speech Input Button Component
 * Provides speech-to-text functionality with visual feedback
 */
class SpeechInputButton {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        this.options = {
            buttonClass: 'speech-input-btn',
            activeClass: 'recording',
            onResult: null,
            onError: null,
            onStart: null,
            onEnd: null,
            continuous: true,
            showTranscript: true,
            ...options
        };
        
        this.isRecording = false;
        this.button = null;
        this.transcriptDisplay = null;
        
        this.init();
    }

    init() {
        this.createButton();
        this.attachEventListeners();
    }

    createButton() {
        // Create button HTML
        this.container.innerHTML = `
            <div class="speech-input-container">
                <button class="${this.options.buttonClass}" type="button" title="Click to start voice input (or press and hold Spacebar)">
                    <span class="speech-icon">🎤</span>
                    <span class="speech-text">Speak</span>
                    <span class="speech-status"></span>
                </button>
                ${this.options.showTranscript ? '<div class="speech-transcript" style="display: none;"></div>' : ''}
            </div>
        `;

        this.button = this.container.querySelector(`.${this.options.buttonClass}`);
        this.transcriptDisplay = this.container.querySelector('.speech-transcript');
        
        // Add CSS styles
        this.addStyles();
    }

    addStyles() {
        if (document.getElementById('speech-input-styles')) return;

        const styles = document.createElement('style');
        styles.id = 'speech-input-styles';
        styles.textContent = `
            .speech-input-container {
                display: inline-block;
                position: relative;
            }

            .speech-input-btn {
                background: linear-gradient(135deg, #4CAF50, #45a049);
                color: white;
                border: none;
                border-radius: 25px;
                padding: 12px 20px;
                font-size: 16px;
                font-weight: 500;
                cursor: pointer;
                display: flex;
                align-items: center;
                gap: 8px;
                transition: all 0.3s ease;
                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                min-width: 120px;
                justify-content: center;
            }

            .speech-input-btn:hover {
                background: linear-gradient(135deg, #45a049, #3d8b40);
                transform: translateY(-2px);
                box-shadow: 0 6px 12px rgba(0,0,0,0.15);
            }

            .speech-input-btn:active {
                transform: translateY(0);
            }

            .speech-input-btn.recording {
                background: linear-gradient(135deg, #f44336, #d32f2f);
                animation: pulse 1.5s infinite;
            }

            .speech-input-btn.recording .speech-icon {
                animation: microphone-active 0.8s infinite alternate;
            }

            .speech-input-btn:disabled {
                background: #cccccc;
                cursor: not-allowed;
                transform: none;
            }

            .speech-icon {
                font-size: 18px;
                display: inline-block;
            }

            .speech-text {
                font-size: 14px;
                font-weight: 500;
            }

            .speech-status {
                font-size: 12px;
                opacity: 0.8;
            }

            .speech-transcript {
                margin-top: 10px;
                padding: 10px;
                background: #f5f5f5;
                border-radius: 8px;
                font-size: 14px;
                line-height: 1.4;
                max-height: 100px;
                overflow-y: auto;
            }

            .speech-transcript .interim {
                color: #666;
                font-style: italic;
            }

            .speech-transcript .final {
                color: #333;
                font-weight: 500;
            }

            @keyframes pulse {
                0% { box-shadow: 0 0 0 0 rgba(244, 67, 54, 0.4); }
                70% { box-shadow: 0 0 0 10px rgba(244, 67, 54, 0); }
                100% { box-shadow: 0 0 0 0 rgba(244, 67, 54, 0); }
            }

            @keyframes microphone-active {
                0% { transform: scale(1); }
                100% { transform: scale(1.1); }
            }

            /* Elderly-friendly responsive design */
            @media (max-width: 768px) {
                .speech-input-btn {
                    padding: 16px 24px;
                    font-size: 18px;
                    min-width: 140px;
                }
                
                .speech-icon {
                    font-size: 22px;
                }
            }
        `;
        
        document.head.appendChild(styles);
    }

    attachEventListeners() {
        // Button click event
        this.button.addEventListener('click', () => {
            this.toggleRecording();
        });

        // Keyboard support (Spacebar)
        document.addEventListener('keydown', (e) => {
            if (e.code === 'Space' && !e.repeat && this.isInputFocused()) {
                e.preventDefault();
                this.startRecording();
            }
        });

        document.addEventListener('keyup', (e) => {
            if (e.code === 'Space' && this.isRecording) {
                e.preventDefault();
                this.stopRecording();
            }
        });
    }

    isInputFocused() {
        const activeElement = document.activeElement;
        return activeElement && (
            activeElement.tagName === 'INPUT' || 
            activeElement.tagName === 'TEXTAREA' || 
            activeElement.contentEditable === 'true'
        );
    }

    toggleRecording() {
        if (this.isRecording) {
            this.stopRecording();
        } else {
            this.startRecording();
        }
    }

    startRecording() {
        if (!window.speechService || !window.speechService.isSTTSupported()) {
            this.showError('Speech recognition is not supported in your browser');
            return;
        }

        if (this.isRecording) return;

        const success = window.speechService.startRecognition({
            continuous: this.options.continuous,
            onResult: (result) => this.handleResult(result),
            onError: (error) => this.handleError(error),
            onEnd: () => this.handleEnd()
        });

        if (success) {
            this.isRecording = true;
            this.updateUI('recording');
            if (this.options.onStart) this.options.onStart();
        }
    }

    stopRecording() {
        if (!this.isRecording) return;

        window.speechService.stopRecognition();
        this.isRecording = false;
        this.updateUI('idle');
    }

    handleResult(result) {
        if (this.transcriptDisplay && this.options.showTranscript) {
            this.transcriptDisplay.style.display = 'block';
            this.transcriptDisplay.innerHTML = `
                ${result.finalTranscript ? `<span class="final">${result.finalTranscript}</span>` : ''}
                ${result.interimTranscript ? `<span class="interim">${result.interimTranscript}</span>` : ''}
            `;
        }

        if (this.options.onResult) {
            this.options.onResult(result);
        }
    }

    handleError(error) {
        this.isRecording = false;
        this.updateUI('error');
        this.showError(error);
        
        if (this.options.onError) {
            this.options.onError(error);
        }
    }

    handleEnd() {
        this.isRecording = false;
        this.updateUI('idle');
        
        if (this.options.onEnd) {
            this.options.onEnd();
        }
    }

    updateUI(state) {
        const icon = this.button.querySelector('.speech-icon');
        const text = this.button.querySelector('.speech-text');
        const status = this.button.querySelector('.speech-status');

        this.button.classList.remove('recording', 'error');

        switch (state) {
            case 'recording':
                this.button.classList.add('recording');
                text.textContent = 'Recording...';
                status.textContent = 'Listening';
                this.button.title = 'Click to stop recording';
                break;
            case 'error':
                this.button.classList.add('error');
                text.textContent = 'Error';
                status.textContent = 'Try again';
                this.button.title = 'Click to try again';
                break;
            default: // idle
                text.textContent = 'Speak';
                status.textContent = '';
                this.button.title = 'Click to start voice input';
        }
    }

    showError(message) {
        // You can customize this to show errors in your preferred way
        console.error('Speech Input Error:', message);
        
        // Show temporary error message
        const status = this.button.querySelector('.speech-status');
        status.textContent = 'Error';
        status.style.color = '#f44336';
        
        setTimeout(() => {
            status.textContent = '';
            status.style.color = '';
        }, 3000);
    }

    destroy() {
        if (this.isRecording) {
            this.stopRecording();
        }
        this.container.innerHTML = '';
    }
}

/**
 * Speech Output Button Component
 * Provides text-to-speech functionality with visual feedback
 */
class SpeechOutputButton {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        this.options = {
            buttonClass: 'speech-output-btn',
            activeClass: 'speaking',
            text: '',
            voice: null,
            rate: 1,
            pitch: 1,
            volume: 1,
            onStart: null,
            onEnd: null,
            onError: null,
            ...options
        };
        
        this.isSpeaking = false;
        this.button = null;
        
        this.init();
    }

    init() {
        this.createButton();
        this.attachEventListeners();
    }

    createButton() {
        // Create button HTML
        this.container.innerHTML = `
            <button class="${this.options.buttonClass}" type="button" title="Click to hear this text read aloud">
                <span class="speech-icon">🔊</span>
                <span class="speech-text">Listen</span>
            </button>
        `;

        this.button = this.container.querySelector(`.${this.options.buttonClass}`);
        
        // Add CSS styles
        this.addStyles();
    }

    addStyles() {
        if (document.getElementById('speech-output-styles')) return;

        const styles = document.createElement('style');
        styles.id = 'speech-output-styles';
        styles.textContent = `
            .speech-output-btn {
                background: linear-gradient(135deg, #2196F3, #1976D2);
                color: white;
                border: none;
                border-radius: 25px;
                padding: 12px 20px;
                font-size: 16px;
                font-weight: 500;
                cursor: pointer;
                display: flex;
                align-items: center;
                gap: 8px;
                transition: all 0.3s ease;
                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                min-width: 120px;
                justify-content: center;
            }

            .speech-output-btn:hover {
                background: linear-gradient(135deg, #1976D2, #1565C0);
                transform: translateY(-2px);
                box-shadow: 0 6px 12px rgba(0,0,0,0.15);
            }

            .speech-output-btn:active {
                transform: translateY(0);
            }

            .speech-output-btn.speaking {
                background: linear-gradient(135deg, #FF9800, #F57C00);
                animation: speaking-pulse 1s infinite;
            }

            .speech-output-btn.speaking .speech-icon {
                animation: speaker-active 0.6s infinite alternate;
            }

            .speech-output-btn:disabled {
                background: #cccccc;
                cursor: not-allowed;
                transform: none;
            }

            @keyframes speaking-pulse {
                0% { box-shadow: 0 0 0 0 rgba(255, 152, 0, 0.4); }
                70% { box-shadow: 0 0 0 10px rgba(255, 152, 0, 0); }
                100% { box-shadow: 0 0 0 0 rgba(255, 152, 0, 0); }
            }

            @keyframes speaker-active {
                0% { transform: scale(1); }
                100% { transform: scale(1.1); }
            }

            /* Elderly-friendly responsive design */
            @media (max-width: 768px) {
                .speech-output-btn {
                    padding: 16px 24px;
                    font-size: 18px;
                    min-width: 140px;
                }
                
                .speech-output-btn .speech-icon {
                    font-size: 22px;
                }
            }
        `;
        
        document.head.appendChild(styles);
    }

    attachEventListeners() {
        this.button.addEventListener('click', () => {
            this.toggleSpeech();
        });
    }

    toggleSpeech() {
        if (this.isSpeaking) {
            this.stopSpeech();
        } else {
            this.startSpeech();
        }
    }

    startSpeech(text = null) {
        if (!window.speechService || !window.speechService.isTTSSupported()) {
            this.showError('Speech synthesis is not supported in your browser');
            return;
        }

        const textToSpeak = text || this.options.text;
        if (!textToSpeak || textToSpeak.trim() === '') {
            this.showError('No text to speak');
            return;
        }

        if (this.isSpeaking) {
            this.stopSpeech();
            return;
        }

        const success = window.speechService.speak(textToSpeak, {
            voice: this.options.voice,
            rate: this.options.rate,
            pitch: this.options.pitch,
            volume: this.options.volume,
            onStart: () => this.handleStart(),
            onEnd: () => this.handleEnd(),
            onError: (error) => this.handleError(error)
        });

        if (!success) {
            this.showError('Failed to start speech synthesis');
        }
    }

    stopSpeech() {
        if (window.speechService) {
            window.speechService.stopSpeaking();
        }
        this.isSpeaking = false;
        this.updateUI('idle');
    }

    handleStart() {
        this.isSpeaking = true;
        this.updateUI('speaking');
        
        if (this.options.onStart) {
            this.options.onStart();
        }
    }

    handleEnd() {
        this.isSpeaking = false;
        this.updateUI('idle');
        
        if (this.options.onEnd) {
            this.options.onEnd();
        }
    }

    handleError(error) {
        this.isSpeaking = false;
        this.updateUI('error');
        this.showError(error);
        
        if (this.options.onError) {
            this.options.onError(error);
        }
    }

    updateUI(state) {
        const text = this.button.querySelector('.speech-text');
        
        this.button.classList.remove('speaking', 'error');

        switch (state) {
            case 'speaking':
                this.button.classList.add('speaking');
                text.textContent = 'Speaking...';
                this.button.title = 'Click to stop speech';
                break;
            case 'error':
                this.button.classList.add('error');
                text.textContent = 'Error';
                this.button.title = 'Click to try again';
                break;
            default: // idle
                text.textContent = 'Listen';
                this.button.title = 'Click to hear this text read aloud';
        }
    }

    showError(message) {
        console.error('Speech Output Error:', message);
        
        // Show temporary error message
        const text = this.button.querySelector('.speech-text');
        const originalText = text.textContent;
        
        text.textContent = 'Error';
        text.style.color = '#f44336';
        
        setTimeout(() => {
            text.textContent = originalText;
            text.style.color = '';
        }, 3000);
    }

    setText(text) {
        this.options.text = text;
    }

    destroy() {
        if (this.isSpeaking) {
            this.stopSpeech();
        }
        this.container.innerHTML = '';
    }
}

// Export classes for use
if (typeof window !== 'undefined') {
    window.SpeechInputButton = SpeechInputButton;
    window.SpeechOutputButton = SpeechOutputButton;
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = { SpeechInputButton, SpeechOutputButton };
}