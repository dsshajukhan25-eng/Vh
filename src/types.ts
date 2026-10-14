export interface SubtitleCue {
  id: string;
  startTime: number; // in seconds
  endTime: number;   // in seconds
  text: string;
  speaker?: string;
}

export type CartoonStyleId = 'comic' | 'anime' | 'watercolor' | 'popart' | 'sketch';

export interface CartoonStyle {
  id: CartoonStyleId;
  titleBn: string;
  titleEn: string;
  descriptionBn: string;
  outlineStrength: number;
  posterizeLevels: number;
  saturationBoost: number;
  contrast: number;
  iconEmoji: string;
}

export type SubtitleStylePreset = 'cartoon-yellow' | 'classic-white' | 'neon-cyan' | 'comic-bubble';
export type SubtitlePosition = 'bottom' | 'center' | 'top';

export type BgmTrackId = 'bounce' | 'slapstick' | 'circus' | 'groove' | 'magic';

export interface BgmTrack {
  id: BgmTrackId;
  titleBn: string;
  titleEn: string;
  tempoBpm: number;
  moodEmoji: string;
}

export type SfxId = 'boing' | 'pop' | 'whistle' | 'ding' | 'fanfare';

export interface SampleVideoPreset {
  id: string;
  titleBn: string;
  titleEn: string;
  duration: number; // in seconds
  defaultSubtitles: SubtitleCue[];
  defaultDialogueBn: string;
  iconEmoji: string;
  sceneType: 'kitty' | 'city' | 'pals' | 'dance';
}

export interface SocialPlatform {
  id: string;
  nameBn: string;
  color: string;
  iconEmoji: string;
  shareUrl: (text: string, url: string) => string;
}
