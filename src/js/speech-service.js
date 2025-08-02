/**
 * Speech Service - TTS and STT functionality using Web Speech API
 * Provides text-to-speech and speech-to-text capabilities for elderly-friendly applications
 */

class SpeechService {
    constructor() {
        this.isRecording = false;
        this.isSpeaking = false;
        this.recognition = null;
        this.synthesis = window.speechSynthesis;
        
        // Initialize Speech Recognition
        this.initializeSpeechRecognition();
        
        // Available voices (will be populated when voices are loaded)
        this.voices = [];
        this.loadVoices();
    }

    /**
     * Initialize Speech Recognition (STT)
     */
    initializeSpeechRecognition() {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        
        if (!SpeechRecognition) {
            console.warn('Speech Recognition not supported in this browser');
            return;
        }

        this.recognition = new SpeechRecognition();
        this.recognition.continuous = true;
        this.recognition.interimResults = true;
        this.recognition.lang = 'en-US';
        this.recognition.maxAlternatives = 1;
    }

    /**
     * Load available voices for TTS
     */
    loadVoices() {
        if (!this.synthesis) {
            console.warn('Speech Synthesis not supported in this browser');
            return;
        }

        const loadVoicesCallback = () => {
            this.voices = this.synthesis.getVoices();
            
            // Filter for English voices and prioritize local ones
            this.voices = this.voices.filter(voice => 
                voice.lang.startsWith('en-')
            ).sort((a, b) => {
                // Prioritize local voices
                if (a.localService && !b.localService) return -1;
                if (!a.localService && b.localService) return 1;
                return 0;
            });
        };

        // Load voices immediately if available
        loadVoicesCallback();
        
        // Also listen for voiceschanged event (some browsers load voices asynchronously)
        this.synthesis.addEventListener('voiceschanged', loadVoicesCallback);
    }

    /**
     * Check if Speech Recognition is supported
     */
    isSTTSupported() {
        return this.recognition !== null;
    }

    /**
     * Check if Speech Synthesis is supported
     */
    isTTSSupported() {
        return this.synthesis !== null;
    }

    /**
     * Start speech recognition
     * @param {Object} options - Recognition options
     * @param {Function} onResult - Callback for results
     * @param {Function} onError - Callback for errors
     * @param {Function} onEnd - Callback when recognition ends
     */
    startRecognition({ onResult, onError, onEnd, continuous = true } = {}) {
        if (!this.isSTTSupported()) {
            if (onError) onError('Speech recognition not supported');
            return false;
        }

        if (this.isRecording) {
            console.warn('Speech recognition already in progress');
            return false;
        }

        this.recognition.continuous = continuous;
        
        // Set up event handlers
        this.recognition.onstart = () => {
            this.isRecording = true;
            console.log('Speech recognition started');
        };

        this.recognition.onresult = (event) => {
            let finalTranscript = '';
            let interimTranscript = '';

            for (let i = event.resultIndex; i < event.results.length; i++) {
                const transcript = event.results[i][0].transcript;
                const confidence = event.results[i][0].confidence;

                if (event.results[i].isFinal) {
                    finalTranscript += transcript;
                } else {
                    interimTranscript += transcript;
                }
            }

            if (onResult) {
                onResult({
                    finalTranscript,
                    interimTranscript,
                    isFinal: finalTranscript.length > 0
                });
            }
        };

        this.recognition.onerror = (event) => {
            console.error('Speech recognition error:', event.error);
            this.isRecording = false;
            
            let errorMessage = 'Speech recognition error';
            switch (event.error) {
                case 'no-speech':
                    errorMessage = 'No speech detected. Please try again.';
                    break;
                case 'audio-capture':
                    errorMessage = 'Microphone not accessible. Please check permissions.';
                    break;
                case 'not-allowed':
                    errorMessage = 'Microphone access denied. Please allow microphone access.';
                    break;
                case 'network':
                    errorMessage = 'Network error occurred during speech recognition.';
                    break;
                default:
                    errorMessage = `Speech recognition error: ${event.error}`;
            }

            if (onError) onError(errorMessage);
        };

        this.recognition.onend = () => {
            this.isRecording = false;
            console.log('Speech recognition ended');
            if (onEnd) onEnd();
        };

        try {
            this.recognition.start();
            return true;
        } catch (error) {
            console.error('Failed to start speech recognition:', error);
            if (onError) onError('Failed to start speech recognition');
            return false;
        }
    }

    /**
     * Stop speech recognition
     */
    stopRecognition() {
        if (this.recognition && this.isRecording) {
            this.recognition.stop();
        }
    }

    /**
     * Abort speech recognition
     */
    abortRecognition() {
        if (this.recognition && this.isRecording) {
            this.recognition.abort();
        }
    }

    /**
     * Speak text using TTS
     * @param {string} text - Text to speak
     * @param {Object} options - Speech options
     */
    speak(text, { 
        voice = null, 
        rate = 1, 
        pitch = 1, 
        volume = 1,
        onStart = null,
        onEnd = null,
        onError = null 
    } = {}) {
        if (!this.isTTSSupported()) {
            if (onError) onError('Speech synthesis not supported');
            return false;
        }

        if (this.isSpeaking) {
            this.stopSpeaking();
        }

        const utterance = new SpeechSynthesisUtterance(text);
        
        // Set voice (use best available English voice if none specified)
        if (voice) {
            utterance.voice = voice;
        } else if (this.voices.length > 0) {
            // Use the first available English voice (prioritized local ones)
            utterance.voice = this.voices[0];
        }

        utterance.lang = 'en-US';
        utterance.rate = Math.max(0.1, Math.min(2, rate)); // Clamp between 0.1 and 2
        utterance.pitch = Math.max(0, Math.min(2, pitch)); // Clamp between 0 and 2
        utterance.volume = Math.max(0, Math.min(1, volume)); // Clamp between 0 and 1

        utterance.onstart = () => {
            this.isSpeaking = true;
            console.log('Speech synthesis started');
            if (onStart) onStart();
        };

        utterance.onend = () => {
            this.isSpeaking = false;
            console.log('Speech synthesis ended');
            if (onEnd) onEnd();
        };

        utterance.onerror = (event) => {
            this.isSpeaking = false;
            console.error('Speech synthesis error:', event.error);
            if (onError) onError(`Speech synthesis error: ${event.error}`);
        };

        try {
            this.synthesis.speak(utterance);
            return true;
        } catch (error) {
            console.error('Failed to start speech synthesis:', error);
            if (onError) onError('Failed to start speech synthesis');
            return false;
        }
    }

    /**
     * Stop current speech synthesis
     */
    stopSpeaking() {
        if (this.synthesis && this.isSpeaking) {
            this.synthesis.cancel();
            this.isSpeaking = false;
        }
    }

    /**
     * Pause speech synthesis
     */
    pauseSpeaking() {
        if (this.synthesis && this.isSpeaking) {
            this.synthesis.pause();
        }
    }

    /**
     * Resume speech synthesis
     */
    resumeSpeaking() {
        if (this.synthesis) {
            this.synthesis.resume();
        }
    }

    /**
     * Get available voices
     */
    getVoices() {
        return this.voices;
    }

    /**
     * Get speech recognition status
     */
    getRecognitionStatus() {
        return {
            isSupported: this.isSTTSupported(),
            isRecording: this.isRecording
        };
    }

    /**
     * Get speech synthesis status
     */
    getSynthesisStatus() {
        return {
            isSupported: this.isTTSSupported(),
            isSpeaking: this.isSpeaking,
            isPaused: this.synthesis ? this.synthesis.paused : false,
            isPending: this.synthesis ? this.synthesis.pending : false
        };
    }
}

// Create a global instance
window.speechService = new SpeechService();

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = SpeechService;
}