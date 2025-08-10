package com.example.demo.controller;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5500"})
public class VoiceController {

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> info = new HashMap<>();
        String creds = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        info.put("GOOGLE_APPLICATION_CREDENTIALS", creds != null ? creds : "<not set>");
        if (creds != null) {
            try {
                Path p = Paths.get(creds);
                info.put("credentialsExists", Files.exists(p));
                info.put("credentialsReadable", Files.isReadable(p));
                info.put("credentialsSizeBytes", Files.exists(p) ? Files.size(p) : 0);
            } catch (Exception e) {
                info.put("credentialsCheckError", e.getMessage());
            }
        }
        info.put("success", true);
        return info;
    }

    @PostMapping("/stt")
    public ResponseEntity<?> speechToText(@RequestParam("audio") MultipartFile audio,
                                          @RequestParam(value = "languageCode", defaultValue = "en-GB") String languageCode)
            throws IOException {
        byte[] audioBytes = audio.getBytes();

        try (SpeechClient speechClient = SpeechClient.create()) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setLanguageCode(languageCode)
                    .setEnableAutomaticPunctuation(true)
                    .build();

            RecognitionAudio recognitionAudio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioBytes))
                    .build();

            RecognizeRequest request = RecognizeRequest.newBuilder()
                    .setConfig(config)
                    .setAudio(recognitionAudio)
                    .build();

            RecognizeResponse response = speechClient.recognize(request);
            StringBuilder transcriptBuilder = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                List<SpeechRecognitionAlternative> alternatives = result.getAlternativesList();
                if (!alternatives.isEmpty()) {
                    transcriptBuilder.append(alternatives.get(0).getTranscript());
                }
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("text", transcriptBuilder.toString());
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/tts")
    public ResponseEntity<?> textToSpeech(@RequestBody Map<String, Object> body) throws IOException {
        String text = String.valueOf(body.getOrDefault("text", ""));
        String languageCode = String.valueOf(body.getOrDefault("languageCode", "en-GB"));
        String voiceName = body.get("voiceName") == null ? null : String.valueOf(body.get("voiceName"));

        if (text == null || text.isBlank()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Text is required");
            return ResponseEntity.badRequest().body(error);
        }

        try (TextToSpeechClient ttsClient = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams.Builder voiceBuilder = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode);
            if (voiceName != null && !voiceName.isBlank()) {
                voiceBuilder.setName(voiceName);
            } else {
                voiceBuilder.setSsmlGender(SsmlVoiceGender.MALE);
            }
            VoiceSelectionParams voice = voiceBuilder.build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            var response = ttsClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContents = response.getAudioContent();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.set("Content-Disposition", "inline; filename=tts.mp3");
            return new ResponseEntity<>(audioContents.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}


