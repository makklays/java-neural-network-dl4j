package com.techmatrix18;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.client.MaryClient;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        //System.out.println("Hello world!");

        LibVosk.setLogLevel(LogLevel.INFO);
        Model model = new Model("models/vosk-model-small-en-us-0.15"); // английская модель
        Recognizer recognizer = new Recognizer(model, 16000);

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        System.out.println("Говорите что-нибудь...");
        System.out.println("Говорите одну фразу...");

        /*byte[] buffer = new byte[4096];
        while (true) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                String result = recognizer.getResult();
                System.out.println("Распознано: " + result);

                //String text = extractText(result);
                String text = extractTextFromJson(result);
                System.out.println("Распознано text: " + text);
                if (text != null) {
                    String response = processCommand(text);
                    speak(response);
                }
            }
        }*/

        byte[] buffer = new byte[4096];
        String finalResultText = null;

        while (true) {
            while (finalResultText == null) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                // acceptWaveForm возвращает true, когда фраза завершена
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    String resultJson = recognizer.getResult();
                    finalResultText = extractTextFromJson(resultJson);
                } else {
                    // Частичные результаты можно игнорировать или вывести при отладке
                    // System.out.println("Partial: " + recognizer.getPartialResult());
                }
            }

            System.out.println("Распознано: " + finalResultText);
            String response = processCommand(finalResultText);

            if (response.equals("stop")) break;
            // Выключаем микрофон, чтобы не слышал себя
            microphone.close(); // ?
            microphone.flush(); // ?

            // Отвечает
            speak(response);

            // Включаем микрофон
            microphone.start(); // ?
        }

        microphone.close();
        recognizer.close();
        model.close();
    }

    public static void main2(String[] args) throws Exception {
        // start recorder
        // Настройка формата аудио: 16 kHz, 16 бит, 1 канал (моно), little-endian
        /*AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Микрофон не поддерживается.");
            return;
        }

        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        System.out.println("🎙 Запись началась... (5 секунд)");

        AudioInputStream audioStream = new AudioInputStream(microphone);
        File wavFile = new File("output.wav");

        // Запись 5 секунд
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(5000);
                microphone.stop();
                microphone.close();
                System.out.println("✅ Запись завершена. Файл: output.wav");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        stopper.start();
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, wavFile);*/
        // end recorder

        LibVosk.setLogLevel(LogLevel.INFO); // Отключить логирование

        // en
        Model model = new Model("models/vosk-model-small-en-us-0.15");
        // ru
        //Model model = new Model("models/vosk-model-small-ru-0.22");
        // es
        //Model model = new Model("models/vosk-model-small-es-0.42");

        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        Recognizer recognizer = new Recognizer(model, 16000.0f);
        byte[] buffer = new byte[4096];

        // list microphones
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        System.out.println("Доступные аудиоустройства ввода (микрофоны):");
        for (int i = 0; i < mixers.length; i++) {
            Mixer mixer = AudioSystem.getMixer(mixers[i]);
            Line.Info[] targetLineInfo = mixer.getTargetLineInfo(); // Target = input

            if (targetLineInfo.length > 0) {
                System.out.println(i + ": " + mixers[i].getName() + " - " + mixers[i].getDescription());
            }
        }
        // end list

        System.out.println("🎙 Запись началась... (5 секунд)");
        System.out.println("Микрофон открыт: " + microphone.isOpen());
        System.out.println("Говорите что-нибудь...");

        //// Запись в файл
        AudioInputStream audioStream = new AudioInputStream(microphone);
        File wavFile = new File("output.wav");
        // Запись 5 секунд
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(5000);
                microphone.stop();
                microphone.close();
                System.out.println("✅ Запись завершена. Файл: output.wav");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        stopper.start();
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, wavFile);
        //// END запись в файл

        //// Читаем и распознаем текст из файла
        // Открываем аудио файл с речью (16 kHz, mono, 16 bit)
        try (InputStream ais = new FileInputStream("output.wav")) {
            byte[] buffer1 = new byte[4096];
            int bytesRead;

            while ((bytesRead = ais.read(buffer1)) >= 0) {
                if (recognizer.acceptWaveForm(buffer1, bytesRead)) {
                    // распознавание закончено, можем получить результат
                    break;
                }
            }
        }

        // Получаем распознанный текст
        String resultJson = recognizer.getResult();
        // Можно распарсить JSON, но для простоты вытащим поле "text"
        String text = extractTextFromJson(resultJson);
        System.out.println("Распознанный текст: " + text);
        //// END

        //// Вопрос - Ответ
        text = processCommand(text);


        /*while (true) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                System.out.println(recognizer.getResult());
            } else {
                System.out.println(recognizer.getPartialResult());
            }
        }*/

        //// Воспроизведение из wav файла
        // Загрузи подходящую модель
        /*Model model1 = new Model("models/vosk-model-small-ru-0.22");

        // Открываем WAV-файл
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File("output.wav"));

        AudioFormat baseFormat = ais.getFormat();

        // Проверим, что формат — 16 kHz, 16 бит, моно
        AudioFormat targetFormat = new AudioFormat(16000.0f, 16, 1, true, false);
        AudioInputStream convertedAis = AudioSystem.getAudioInputStream(targetFormat, ais);

        Recognizer recognizer1 = new Recognizer(model1, 16000);
        byte[] buffer1 = new byte[4096];
        int bytesRead;

        while ((bytesRead = convertedAis.read(buffer1)) != -1) {
            if (recognizer1.acceptWaveForm(buffer1, bytesRead)) {
                System.out.println(recognizer1.getResult());
            } else {
                System.out.println(recognizer1.getPartialResult());
            }
        }

        System.out.println("Результат: " + recognizer1.getFinalResult());*/
        //// END Воспроизведение из wav файла

        //VoiceManager voiceManager = VoiceManager.getInstance();
        //Voice voice = voiceManager.getVoice("kevin");

        // Указание голосового каталога
        /*System.setProperty("freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_time_awb.AwbVoiceDirectory");
        Voice voice = VoiceManager.getInstance().getVoice("awb");*/
        /*System.setProperty("freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        Voice voice = VoiceManager.getInstance().getVoice("kevin16");

        if (voice != null) {
            voice.allocate();
            voice.speak("Hello, Alexander. This is Free TTS.");
            voice.deallocate();
        } else {
            System.out.println("Voice not found.");
        }*/

        //// MaryTTS
        MaryInterface marytts1 = new LocalMaryInterface();
        Set<String> voices = marytts1.getAvailableVoices();
        for (String voice : voices) {
            System.out.println("Available voice: " + voice);
        }

        /*MaryClient client = new MaryClient("http://localhost:59125");
        AudioInputStream audio = client.speak("Hello world", "cmu-slt-hsmm");*/

        MaryInterface marytts = new LocalMaryInterface();

        // Выбираем голос
        marytts.setVoice("cmu-slt-hsmm");
        //String text = "Hello, Alexander! How are you? Did you fuck anybody this night ?";

        // Выбираем русский голос
        //marytts.setLocale(Locale.forLanguageTag("ru"));
        //marytts.setVoice("voxforge-ru-nsh");
        //String text = "Привет, Александр! Это тест синтеза речи на русском языке.";

        // Синтез речи в аудио поток
        AudioInputStream audio = marytts.generateAudio(text);

        // Проигрываем аудио
        Clip clip = AudioSystem.getClip();
        clip.open(audio);
        clip.start();

        // Ждём окончания воспроизведения
        Thread.sleep(clip.getMicrosecondLength() / 1000);
        //// END MaryTTS
    }

    private static String extractTextFromJson(String json) {
        // Примерный парсинг из результата Vosk (json вида {"text":"..."} )
        int idx = json.indexOf("\"text\"");
        if (idx == -1) return "";
        int start = json.indexOf("\"", idx + 6) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    // Извлечь распознанный текст из JSON результата Vosk
    private static String extractText(String jsonResult) {
        // простой парсинг или используйте JSON библиотеку
        int start = jsonResult.indexOf("\"text\" : \"") + 9;
        int end = jsonResult.indexOf("\"", start);
        if (start >= 9 && end > start) {
            return jsonResult.substring(start, end);
        }
        return null;
    }

    // Логика обработки команд
    private static String processCommand(String text) {
        text = text.toLowerCase();
        if (text.contains("hello")) {
            return "Hello! How can I help you?";
        } else if (text.contains("time")) {
            return "The current time is " + java.time.LocalTime.now().toString();
        } else if (text.contains("up") || text.contains("stop")) {
            return "stop";
        } else {
            return "Sorry";
        }
    }

    // Синтез речи с MaryTTS
    private static void speak(String text) throws Exception {
        marytts.LocalMaryInterface marytts = new marytts.LocalMaryInterface();
        marytts.setVoice("cmu-slt-hsmm"); // английский голос
        javax.sound.sampled.AudioInputStream audio = marytts.generateAudio(text);

        javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
        clip.open(audio);
        clip.start();

        Thread.sleep(clip.getMicrosecondLength() / 1000);
    }
}