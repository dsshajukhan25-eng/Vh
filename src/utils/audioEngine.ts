import { BgmTrackId, SfxId } from '../types';

class CartoonWebAudioEngine {
  private ctx: AudioContext | null = null;
  private bgmIntervalId: number | null = null;
  private isBgmPlaying = false;
  private activeTrackId: BgmTrackId = 'bounce';

  public bgmVolume = 0.75;
  public sfxVolume = 0.90;
  public voicePitch = 1.6;

  private initContext() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      this.ctx = new AudioCtx();
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  // --- CARTOON BGM GENERATOR ---
  public startBgm(trackId: BgmTrackId = this.activeTrackId) {
    this.stopBgm();
    this.initContext();
    this.activeTrackId = trackId;
    this.isBgmPlaying = true;

    // Scale notes based on track
    const notePatterns: Record<BgmTrackId, number[]> = {
      bounce: [261.63, 329.63, 392.00, 523.25, 392.00, 329.63, 293.66, 349.23, 440.00, 587.33],
      slapstick: [196.00, 246.94, 329.63, 392.00, 493.88, 392.00, 220.00, 277.18, 349.23, 440.00],
      circus: [329.63, 392.00, 493.88, 587.33, 659.25, 587.33, 293.66, 369.99, 440.00, 554.37],
      groove: [220.00, 261.63, 329.63, 440.00, 523.25, 440.00, 174.61, 220.00, 261.63, 349.23],
      magic: [523.25, 659.25, 783.99, 1046.50, 783.99, 659.25, 587.33, 698.46, 880.00, 1174.66]
    };

    const notes = notePatterns[trackId] || notePatterns.bounce;
    let noteIndex = 0;
    const intervalMs = trackId === 'circus' ? 180 : 220;

    this.bgmIntervalId = window.setInterval(() => {
      if (!this.isBgmPlaying || !this.ctx) return;
      const freq = notes[noteIndex % notes.length];
      this.playSynthNote(freq, 0.20, this.bgmVolume * 0.35);
      noteIndex++;
    }, intervalMs);
  }

  public stopBgm() {
    this.isBgmPlaying = false;
    if (this.bgmIntervalId !== null) {
      clearInterval(this.bgmIntervalId);
      this.bgmIntervalId = null;
    }
  }

  private playSynthNote(freq: number, duration: number, volume: number) {
    if (!this.ctx) return;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();

    osc.type = 'triangle'; // cartoon bouncy tone
    osc.frequency.setValueAtTime(freq, this.ctx.currentTime);

    // Fast pluck attack and decay
    gain.gain.setValueAtTime(0, this.ctx.currentTime);
    gain.gain.linearRampToValueAtTime(volume, this.ctx.currentTime + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);

    osc.connect(gain);
    gain.connect(this.ctx.destination);

    osc.start();
    osc.stop(this.ctx.currentTime + duration);
  }

  // --- CARTOON SFX ---
  public playSfx(sfxId: SfxId) {
    this.initContext();
    if (!this.ctx) return;

    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();

    osc.connect(gain);
    gain.connect(this.ctx.destination);

    const baseVol = this.sfxVolume * 0.7;

    switch (sfxId) {
      case 'boing': {
        // Fast upward frequency bounce sweep
        osc.type = 'sine';
        osc.frequency.setValueAtTime(150, now);
        osc.frequency.exponentialRampToValueAtTime(800, now + 0.35);

        gain.gain.setValueAtTime(baseVol, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.40);

        osc.start(now);
        osc.stop(now + 0.42);
        break;
      }
      case 'pop': {
        // Quick high-pass click / comic bubble pop
        osc.type = 'sine';
        osc.frequency.setValueAtTime(600, now);
        osc.frequency.exponentialRampToValueAtTime(120, now + 0.12);

        gain.gain.setValueAtTime(baseVol * 1.2, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.14);

        osc.start(now);
        osc.stop(now + 0.15);
        break;
      }
      case 'whistle': {
        // Slide whistle
        osc.type = 'sine';
        osc.frequency.setValueAtTime(850, now);
        osc.frequency.linearRampToValueAtTime(1500, now + 0.25);
        osc.frequency.linearRampToValueAtTime(700, now + 0.50);

        gain.gain.setValueAtTime(0.01, now);
        gain.gain.linearRampToValueAtTime(baseVol * 0.8, now + 0.1);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.52);

        osc.start(now);
        osc.stop(now + 0.55);
        break;
      }
      case 'ding': {
        // Magic Bell Ding
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(1760, now);

        gain.gain.setValueAtTime(baseVol * 0.9, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.70);

        osc.start(now);
        osc.stop(now + 0.72);
        break;
      }
      case 'fanfare': {
        // Quick 3-note celebration trumpet
        const notes = [523.25, 659.25, 783.99, 1046.50];
        notes.forEach((f, idx) => {
          if (!this.ctx) return;
          const o = this.ctx.createOscillator();
          const g = this.ctx.createGain();
          o.type = 'sawtooth';
          o.frequency.setValueAtTime(f, now + idx * 0.12);
          g.gain.setValueAtTime(0.01, now + idx * 0.12);
          g.gain.linearRampToValueAtTime(baseVol * 0.5, now + idx * 0.12 + 0.02);
          g.gain.exponentialRampToValueAtTime(0.001, now + idx * 0.12 + 0.18);
          o.connect(g);
          g.connect(this.ctx.destination);
          o.start(now + idx * 0.12);
          o.stop(now + idx * 0.12 + 0.20);
        });
        break;
      }
    }
  }

  // --- CARTOON TTS VOICEOVER ---
  public speakCartoonVoice(text: string, pitch = this.voicePitch) {
    if (!('speechSynthesis' in window) || !text.trim()) return;

    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.pitch = pitch; // 1.8 = Chipmunk, 1.2 = Fun cartoon, 0.6 = Giant
    utterance.rate = 1.1;

    // Look for Bengali voice if possible, else default
    const voices = window.speechSynthesis.getVoices();
    const bnVoice = voices.find(v => v.lang.includes('bn') || v.lang.includes('BD') || v.lang.includes('IN'));
    if (bnVoice) {
      utterance.voice = bnVoice;
    }

    window.speechSynthesis.speak(utterance);
  }

  public stopSpeaking() {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
  }
}

export const cartoonAudioEngine = new CartoonWebAudioEngine();
