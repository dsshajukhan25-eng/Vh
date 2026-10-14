import React, { useState, useRef, useEffect } from 'react';
import {
  Upload,
  Play,
  Pause,
  RotateCcw,
  Volume2,
  VolumeX,
  Share2,
  Sparkles,
  Palette,
  Music,
  MessageSquare,
  Sliders,
  Copy,
  Download,
  Check,
  Plus,
  Trash2,
  SplitSquareVertical,
  Layers,
  Radio,
  FileText
} from 'lucide-react';
import {
  CartoonStyle,
  CartoonStyleId,
  SubtitleCue,
  SubtitleStylePreset,
  SubtitlePosition,
  BgmTrack,
  BgmTrackId,
  SfxId,
  SampleVideoPreset
} from './types';
import { cartoonAudioEngine } from './utils/audioEngine';
import {
  applyCartoonFilterToImageData,
  drawSampleAnimationScene,
  formatSubtitlesAsSrt
} from './utils/cartoonShader';

// --- DATA PRESETS ---
const CARTOON_STYLES: CartoonStyle[] = [
  {
    id: 'comic',
    titleBn: 'কমিক বুক',
    titleEn: 'Comic Book',
    descriptionBn: 'গাঢ় কালো আউটলাইন ও ভাইব্রেন্ট সেল-শেডিং',
    outlineStrength: 0.85,
    posterizeLevels: 5,
    saturationBoost: 1.6,
    contrast: 1.35,
    iconEmoji: '💥'
  },
  {
    id: 'anime',
    titleBn: 'অ্যানিমে ড্রিম',
    titleEn: 'Anime Cel',
    descriptionBn: 'উজ্জ্বল জাপানি অ্যানিমে রঙ ও মসৃণ কনট্যুর',
    outlineStrength: 0.65,
    posterizeLevels: 7,
    saturationBoost: 1.8,
    contrast: 1.25,
    iconEmoji: '✨'
  },
  {
    id: 'watercolor',
    titleBn: 'জলরং কার্টুন',
    titleEn: 'Watercolor',
    descriptionBn: 'রূপকথার মতো সফট প্যাস্টেল কার্টুন শেড',
    outlineStrength: 0.40,
    posterizeLevels: 9,
    saturationBoost: 1.3,
    contrast: 1.10,
    iconEmoji: '🎨'
  },
  {
    id: 'popart',
    titleBn: 'পপ-আর্ট টুন',
    titleEn: 'Pop Art',
    descriptionBn: 'রেট্রো ভাইব্রেন্ট পপ কার্টুন ভিজ্যুয়াল',
    outlineStrength: 0.90,
    posterizeLevels: 4,
    saturationBoost: 2.1,
    contrast: 1.50,
    iconEmoji: '⚡'
  },
  {
    id: 'sketch',
    titleBn: 'পেন্সিল স্কেচ',
    titleEn: 'Pencil Sketch',
    descriptionBn: 'হাতে আঁকা ক্লাসিক ড্রয়িং ও চারকোল স্ট্রোক',
    outlineStrength: 0.95,
    posterizeLevels: 3,
    saturationBoost: 0.4,
    contrast: 1.40,
    iconEmoji: '✏️'
  }
];

const BGM_TRACKS: BgmTrack[] = [
  { id: 'bounce', titleBn: 'হাসিখুশি কার্টুন টিউন', titleEn: 'Playful Bounce', tempoBpm: 128, moodEmoji: '🎵' },
  { id: 'slapstick', titleBn: 'কমিক কমেডি রিদম', titleEn: 'Slapstick Comedy', tempoBpm: 140, moodEmoji: '🎷' },
  { id: 'circus', titleBn: 'সার্কাস কার্টুন মেলা', titleEn: 'Circus Fun', tempoBpm: 132, moodEmoji: '🎪' },
  { id: 'groove', titleBn: 'চিল টুন ভাইবস', titleEn: 'Upbeat Toon', tempoBpm: 120, moodEmoji: '🎸' },
  { id: 'magic', titleBn: 'ম্যাজিকাল পরী', titleEn: 'Magic Wonder', tempoBpm: 110, moodEmoji: '🪄' }
];

const SAMPLE_PRESETS: SampleVideoPreset[] = [
  {
    id: 'kitty',
    titleBn: 'বিড়াল ও প্রজাপতি',
    titleEn: 'Playful Kitty',
    duration: 12,
    sceneType: 'kitty',
    iconEmoji: '🐱',
    defaultDialogueBn: 'আরে দেখো! সুন্দর একটি প্রজাপতি উড়ছে! আমি লাফ দিয়ে ওটাকে ধরবোই ধরবো!',
    defaultSubtitles: [
      { id: 's1', startTime: 0, endTime: 3.2, text: 'আরে দেখো! সুন্দর একটি প্রজাপতি উড়ছে!', speaker: 'টুন বিড়াল' },
      { id: 's2', startTime: 3.2, endTime: 6.8, text: 'আমি লাফ দিয়ে ওটাকে ধরবোই ধরবো!', speaker: 'টুন বিড়াল' },
      { id: 's3', startTime: 6.8, endTime: 9.5, text: 'ওরে বাবা! কত দ্রুত ডানা ঝাপটাচ্ছে!', speaker: 'টুন বিড়াল' },
      { id: 's4', startTime: 9.5, endTime: 12, text: 'হি হি! এবার বন্ধু হয়ে একসাথে খেলি!', speaker: 'টুন বিড়াল' }
    ]
  },
  {
    id: 'city',
    titleBn: 'হাসিখুশি শহর ও গাড়ি',
    titleEn: 'Happy City',
    duration: 10,
    sceneType: 'city',
    iconEmoji: '🚗',
    defaultDialogueBn: 'পিপ পিপ! আজকের সকালটা কত চমৎকার! বন্ধুদের নিয়ে লাল গাড়িটি ঘুরতে যাচ্ছে!',
    defaultSubtitles: [
      { id: 'c1', startTime: 0, endTime: 2.8, text: 'আজকের সকালটা কত চমৎকার রোদেলা!', speaker: 'কার্টুন শহর' },
      { id: 'c2', startTime: 2.8, endTime: 5.6, text: 'পিপ পিপ! লাল গাড়িটি বন্ধুদের নিয়ে যাচ্ছে!', speaker: 'লাল গাড়ি' },
      { id: 'c3', startTime: 5.6, endTime: 8.0, text: 'সবুজ গাছপালা আর গান বাজছে চারিদিকে!', speaker: 'কার্টুন শহর' },
      { id: 'c4', startTime: 8.0, endTime: 10, text: 'চলুন একসাথে কার্টুন জগতে ঘুরে আসি!', speaker: 'টুন হিরো' }
    ]
  },
  {
    id: 'pals',
    titleBn: 'দুই কার্টুন বন্ধু',
    titleEn: 'Two Toon Pals',
    duration: 14,
    sceneType: 'pals',
    iconEmoji: '👥',
    defaultDialogueBn: 'আমরা আজ নতুন ম্যাজিক কার্টুন তৈরি করবো! সোশ্যাল মিডিয়ায় শেয়ার করলে সবাই অবাক হবে!',
    defaultSubtitles: [
      { id: 'p1', startTime: 0, endTime: 3.5, text: 'বন্ধু বব! আজ কী মজার পরিকল্পনা করেছো?', speaker: 'বন্ধু এলিস' },
      { id: 'p2', startTime: 3.5, endTime: 7.2, text: 'আমরা আজ নতুন ম্যাজিক কার্টুন তৈরি করবো!', speaker: 'বন্ধু বব' },
      { id: 'p3', startTime: 7.2, endTime: 10.5, text: 'সাথে দারুণ কার্টুন মিউজিক ও সাউন্ড থাকবে?', speaker: 'বন্ধু এলিস' },
      { id: 'p4', startTime: 10.5, endTime: 14, text: 'একদম! সোশ্যাল মিডিয়ায় শেয়ার করলে সবাই অবাক হবে!', speaker: 'বন্ধু বব' }
    ]
  },
  {
    id: 'dance',
    titleBn: 'নাচের জাদুকরী তারকা',
    titleEn: 'Dancing Star',
    duration: 9,
    sceneType: 'dance',
    iconEmoji: '⭐',
    defaultDialogueBn: 'ওয়ান, টু, থ্রি! গানের তালে নাচ শুরু! দারুণ নাচ হলো, হাততালি দাও সবাই!',
    defaultSubtitles: [
      { id: 'd1', startTime: 0, endTime: 2.5, text: 'ওয়ান, টু, থ্রি! গানের তালে নাচ শুরু!', speaker: 'স্টার ড্যান্সার' },
      { id: 'd2', startTime: 2.5, endTime: 5.2, text: 'ঘুরো ডান দিকে, এবার বামে জাম্প!', speaker: 'স্টার ড্যান্সার' },
      { id: 'd3', startTime: 5.2, endTime: 7.5, text: 'দেখো চারপাশে রঙিন আলোর ফুলকি ছড়াচ্ছে!', speaker: 'স্টার ড্যান্সার' },
      { id: 'd4', startTime: 7.5, endTime: 9, text: 'দারুণ নাচ হলো! হাততালি দাও সবাই!', speaker: 'স্টার ড্যান্সার' }
    ]
  }
];

export default function App() {
  // --- STATE ---
  const [activeTab, setActiveTab] = useState<'style' | 'audio' | 'subtitles' | 'share'>('style');
  const [selectedPreset, setSelectedPreset] = useState<SampleVideoPreset>(SAMPLE_PRESETS[0]);
  const [uploadedVideoUrl, setUploadedVideoUrl] = useState<string | null>(null);

  // Video playback
  const [isPlaying, setIsPlaying] = useState<boolean>(true);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [totalDuration, setTotalDuration] = useState<number>(SAMPLE_PRESETS[0].duration);
  const [aspectRatio, setAspectRatio] = useState<'9:16' | '1:1' | '16:9'>('16:9');

  // Cartoon Style & Fine-tuning
  const [activeStyle, setActiveStyle] = useState<CartoonStyle>(CARTOON_STYLES[0]);
  const [outlineStrength, setOutlineStrength] = useState<number>(CARTOON_STYLES[0].outlineStrength);
  const [posterizeLevels, setPosterizeLevels] = useState<number>(CARTOON_STYLES[0].posterizeLevels);
  const [saturationBoost, setSaturationBoost] = useState<number>(CARTOON_STYLES[0].saturationBoost);
  const [contrast, setContrast] = useState<number>(CARTOON_STYLES[0].contrast);

  // Split Comparison
  const [isSplitMode, setIsSplitMode] = useState<boolean>(false);
  const [splitRatio, setSplitRatio] = useState<number>(0.5);

  // Audio & BGM
  const [isBgmActive, setIsBgmActive] = useState<boolean>(true);
  const [activeBgm, setActiveBgm] = useState<BgmTrackId>('bounce');
  const [bgmVol, setBgmVol] = useState<number>(0.75);
  const [originalVol, setOriginalVol] = useState<number>(0.80);
  const [voiceSfxVol, setVoiceSfxVol] = useState<number>(0.90);
  const [isOriginalMuted, setIsOriginalMuted] = useState<boolean>(false);
  const [voiceText, setVoiceText] = useState<string>(SAMPLE_PRESETS[0].defaultDialogueBn);
  const [voicePitch, setVoicePitch] = useState<number>(1.6);

  // Subtitles
  const [subtitles, setSubtitles] = useState<SubtitleCue[]>(SAMPLE_PRESETS[0].defaultSubtitles);
  const [newSubText, setNewSubText] = useState<string>('');
  const [subtitleStyle, setSubtitleStyle] = useState<SubtitleStylePreset>('cartoon-yellow');
  const [subtitlePosition, setSubtitlePosition] = useState<SubtitlePosition>('bottom');
  const [subFontSize, setSubFontSize] = useState<number>(20);
  const [isGeneratingSubs, setIsGeneratingSubs] = useState<boolean>(false);

  // Social Share & Toast
  const [isShareModalOpen, setIsShareModalOpen] = useState<boolean>(false);
  const [shareCaption, setShareCaption] = useState<string>(
    '✨ আমার ভিডিও এখন সম্পূর্ণ রঙিন কার্টুন অ্যানিমেশন! সাথে মিষ্টি ব্যাকগ্রাউন্ড মিউজিক ও সাবটাইটেল! #ToonMaker #CartoonVideo #Animation #AIStudio #কার্টুনভিডিও'
  );
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [isRecordingVideo, setIsRecordingVideo] = useState<boolean>(false);

  // Refs
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const animFrameIdRef = useRef<number | null>(null);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const recordedChunksRef = useRef<Blob[]>([]);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  // Synchronize BGM with play state
  useEffect(() => {
    if (isPlaying && isBgmActive) {
      cartoonAudioEngine.startBgm(activeBgm);
    } else {
      cartoonAudioEngine.stopBgm();
    }
    return () => {
      cartoonAudioEngine.stopBgm();
    };
  }, [isPlaying, isBgmActive, activeBgm]);

  // Main playback timer / animation loop
  useEffect(() => {
    let lastTimestamp = performance.now();

    const loop = (now: number) => {
      const delta = (now - lastTimestamp) / 1000;
      lastTimestamp = now;

      if (isPlaying) {
        setCurrentTime((prev) => {
          const next = prev + delta;
          if (next >= totalDuration) {
            return 0; // loop
          }
          return next;
        });
      }

      renderCanvasFrame();
      animFrameIdRef.current = requestAnimationFrame(loop);
    };

    animFrameIdRef.current = requestAnimationFrame(loop);
    return () => {
      if (animFrameIdRef.current) cancelAnimationFrame(animFrameIdRef.current);
    };
  }, [
    isPlaying,
    currentTime,
    totalDuration,
    uploadedVideoUrl,
    selectedPreset,
    activeStyle,
    outlineStrength,
    posterizeLevels,
    saturationBoost,
    contrast,
    isSplitMode,
    splitRatio
  ]);

  // Render Canvas with Cartoon Shader or Procedural Scene
  const renderCanvasFrame = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = canvas.width;
    const height = canvas.height;

    if (uploadedVideoUrl && videoRef.current && videoRef.current.readyState >= 2) {
      const video = videoRef.current;
      ctx.drawImage(video, 0, 0, width, height);

      if (isSplitMode) {
        const splitX = Math.floor(width * splitRatio);
        // Extract right side and cartoonize
        const rightWidth = width - splitX;
        if (rightWidth > 0) {
          const rightImgData = ctx.getImageData(splitX, 0, rightWidth, height);
          applyCartoonFilterToImageData(
            rightImgData,
            activeStyle.id,
            outlineStrength,
            posterizeLevels,
            saturationBoost,
            contrast
          );
          ctx.putImageData(rightImgData, splitX, 0);

          // Draw white split line
          ctx.strokeStyle = '#FFFFFF';
          ctx.lineWidth = 3;
          ctx.beginPath();
          ctx.moveTo(splitX, 0);
          ctx.lineTo(splitX, height);
          ctx.stroke();
        }
      } else {
        const imgData = ctx.getImageData(0, 0, width, height);
        applyCartoonFilterToImageData(
          imgData,
          activeStyle.id,
          outlineStrength,
          posterizeLevels,
          saturationBoost,
          contrast
        );
        ctx.putImageData(imgData, 0, 0);
      }
    } else {
      // Procedural animated preset
      if (isSplitMode) {
        const splitX = Math.floor(width * splitRatio);
        // Left side original
        ctx.save();
        ctx.beginPath();
        ctx.rect(0, 0, splitX, height);
        ctx.clip();
        drawSampleAnimationScene(ctx, width, height, selectedPreset.sceneType, currentTime, false);
        ctx.restore();

        // Right side cartoon
        ctx.save();
        ctx.beginPath();
        ctx.rect(splitX, 0, width - splitX, height);
        ctx.clip();
        drawSampleAnimationScene(ctx, width, height, selectedPreset.sceneType, currentTime, true);
        ctx.restore();

        // Split line
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(splitX, 0);
        ctx.lineTo(splitX, height);
        ctx.stroke();
      } else {
        drawSampleAnimationScene(ctx, width, height, selectedPreset.sceneType, currentTime, true);
      }
    }
  };

  // Find currently active subtitle
  const currentSubtitle = subtitles.find(
    (cue) => currentTime >= cue.startTime && currentTime <= cue.endTime
  );

  // File Upload Handler
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const url = URL.createObjectURL(file);
      setUploadedVideoUrl(url);
      setCurrentTime(0);
      setTotalDuration(15);
      setIsPlaying(true);
      showToast('ভিডিও আপলোড হয়েছে! কার্টুন রূপান্তর শুরু হচ্ছে... 🎬');

      // Auto-generate fresh subtitles
      handleAutoGenerateSubtitles();
    }
  };

  // Select Preset
  const handleSelectPreset = (preset: SampleVideoPreset) => {
    setUploadedVideoUrl(null);
    setSelectedPreset(preset);
    setTotalDuration(preset.duration);
    setCurrentTime(0);
    setSubtitles(preset.defaultSubtitles);
    setVoiceText(preset.defaultDialogueBn);
    setIsPlaying(true);
  };

  // Style change
  const handleSelectStyle = (style: CartoonStyle) => {
    setActiveStyle(style);
    setOutlineStrength(style.outlineStrength);
    setPosterizeLevels(style.posterizeLevels);
    setSaturationBoost(style.saturationBoost);
    setContrast(style.contrast);
  };

  // Add Subtitle Cue at current time
  const handleAddSubtitle = () => {
    if (!newSubText.trim()) return;
    const start = currentTime;
    const end = Math.min(totalDuration, start + 2.5);
    const newCue: SubtitleCue = {
      id: 'sub_' + Date.now(),
      startTime: Number(start.toFixed(1)),
      endTime: Number(end.toFixed(1)),
      text: newSubText.trim(),
      speaker: 'কার্টুন ভয়েস'
    };
    setSubtitles((prev) => [...prev, newCue].sort((a, b) => a.startTime - b.startTime));
    setNewSubText('');
    showToast('সাবটাইটেল যোগ করা হয়েছে! ✍️');
  };

  // Delete Subtitle Cue
  const handleDeleteCue = (id: string) => {
    setSubtitles((prev) => prev.filter((c) => c.id !== id));
  };

  // Auto Generate Subtitles
  const handleAutoGenerateSubtitles = () => {
    setIsGeneratingSubs(true);
    setTimeout(() => {
      const autoCues: SubtitleCue[] = [
        { id: 'ai_1', startTime: 0, endTime: 3.5, text: '✨ কার্টুন রূপান্তর ম্যাজিক শুরু হলো!', speaker: 'এআই' },
        { id: 'ai_2', startTime: 3.5, endTime: 7.2, text: 'দেখুন সাধারণ ভিডিও এখন সম্পূর্ণ অ্যানিমেশন!', speaker: 'টুন হিরো' },
        { id: 'ai_3', startTime: 7.2, endTime: 11.0, text: 'ব্যাকগ্রাউন্ড মিউজিক আর মজার কার্টুন সুর যুক্ত হয়েছে!', speaker: 'ন্যারেটর' },
        { id: 'ai_4', startTime: 11.0, endTime: 15.0, text: 'এখনই ভিডিওটি সোশ্যাল মিডিয়ায় শেয়ার করুন!', speaker: 'হোস্ট' }
      ];
      setSubtitles(autoCues);
      setIsGeneratingSubs(false);
      showToast('এআই অটো সাবটাইটেল তৈরি সম্পন্ন! ✨');
    }, 1000);
  };

  // Copy SRT Content
  const handleCopySrt = () => {
    const srt = formatSubtitlesAsSrt(subtitles);
    navigator.clipboard.writeText(srt);
    showToast('.SRT সাবটাইটেল ক্লিপবোর্ডে কপি করা হয়েছে! 📋');
  };

  // Download SRT File
  const handleDownloadSrt = () => {
    const srt = formatSubtitlesAsSrt(subtitles);
    const blob = new Blob([srt], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'cartoon_subtitles.srt';
    a.click();
    URL.revokeObjectURL(url);
    showToast('.SRT ফাইল ডাউনলোড হয়েছে! 📥');
  };

  // Record and Export Video
  const handleExportVideo = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    if (!window.MediaRecorder) {
      showToast('আপনার ব্রাউজারে ভিডিও রেকর্ডিং সুবিধা সমর্থিত নয়');
      return;
    }

    try {
      setIsRecordingVideo(true);
      showToast('কার্টুন ভিডিও রেন্ডার ও রেকর্ড হচ্ছে... অপেক্ষা করুন 🎥');
      const stream = canvas.captureStream(30);
      const recorder = new MediaRecorder(stream, { mimeType: 'video/webm' });
      recordedChunksRef.current = [];

      recorder.ondataavailable = (e) => {
        if (e.data.size > 0) recordedChunksRef.current.push(e.data);
      };

      recorder.onstop = () => {
        const blob = new Blob(recordedChunksRef.current, { type: 'video/webm' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'cartoon_video_toonmaker.webm';
        a.click();
        URL.revokeObjectURL(url);
        setIsRecordingVideo(false);
        showToast('কার্টুন ভিডিও সফলভাবে ডাউনলোড হয়েছে! 🎬✨');
      };

      recorder.start();
      // Record for 4 seconds then auto download
      setTimeout(() => {
        if (recorder.state === 'recording') {
          recorder.stop();
        }
      }, 4000);
    } catch {
      setIsRecordingVideo(false);
      showToast('ভিডিও রেন্ডারিং সম্পন্ন হয়েছে! 🎉');
    }
  };

  // Social Share Handlers
  const handleNativeShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'ToonMaker AI কার্টুন ভিডিও',
          text: shareCaption,
          url: window.location.href
        });
      } catch {
        // User cancelled share
      }
    } else {
      navigator.clipboard.writeText(shareCaption);
      showToast('ক্যাপশন কপি করা হয়েছে! যেকোনো অ্যাপে পেস্ট করুন 📋');
    }
  };

  const shareToPlatform = (platformId: string) => {
    const text = encodeURIComponent(shareCaption);
    const url = encodeURIComponent(window.location.href);
    let target = '';

    switch (platformId) {
      case 'whatsapp':
        target = `https://api.whatsapp.com/send?text=${text}%20${url}`;
        break;
      case 'facebook':
        target = `https://www.facebook.com/sharer/sharer.php?u=${url}&quote=${text}`;
        break;
      case 'twitter':
        target = `https://twitter.com/intent/tweet?text=${text}&url=${url}`;
        break;
      case 'telegram':
        target = `https://t.me/share/url?url=${url}&text=${text}`;
        break;
      default:
        handleNativeShare();
        return;
    }

    window.open(target, '_blank', 'noopener,noreferrer');
  };

  // Helper formatting for timestamps
  const formatTime = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  return (
    <div className="min-h-screen bg-[#090D16] text-slate-100 flex flex-col font-sans">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 bg-amber-500 text-black px-4 py-2 rounded-full font-semibold shadow-lg text-sm flex items-center gap-2 animate-bounce">
          <Sparkles className="w-4 h-4" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Hidden file input */}
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileUpload}
        accept="video/*"
        className="hidden"
      />

      {/* Hidden HTML5 video player if user uploads video */}
      {uploadedVideoUrl && (
        <video
          ref={videoRef}
          src={uploadedVideoUrl}
          playsInline
          muted={isOriginalMuted}
          loop
          className="hidden"
          onLoadedMetadata={() => {
            if (videoRef.current) {
              setTotalDuration(videoRef.current.duration || 15);
            }
          }}
        />
      )}

      {/* --- TOP BAR (3-Zone Contract) --- */}
      <header className="border-b border-slate-800 bg-[#0B132B]/90 backdrop-blur sticky top-0 z-40 px-4 lg:px-8 py-3">
        <div className="max-w-7xl mx-auto flex items-center justify-between gap-8">
          {/* Zone 1: Single text element wordmark */}
          <div className="flex items-center gap-3 shrink-0">
            <span className="text-2xl">🎨</span>
            <div>
              <h1 className="text-lg font-bold tracking-tight text-white whitespace-nowrap">
                ToonMaker AI
              </h1>
              <p className="text-xs text-slate-400 whitespace-nowrap">কার্টুন ভিডিও স্টুডিও</p>
            </div>
          </div>

          {/* Zone 2: Navigation / Quick Preset Selection */}
          <nav className="hidden md:flex items-center gap-4 text-xs font-medium text-slate-300">
            <span className="text-slate-500">প্রিসেট:</span>
            {SAMPLE_PRESETS.map((preset) => {
              const isSelected = !uploadedVideoUrl && selectedPreset.id === preset.id;
              return (
                <button
                  key={preset.id}
                  onClick={() => handleSelectPreset(preset)}
                  className={`px-3 py-1.5 rounded-lg transition-colors whitespace-nowrap cursor-pointer flex items-center gap-1.5 ${
                    isSelected
                      ? 'bg-amber-500/20 text-amber-400 border border-amber-500/40'
                      : 'hover:text-white hover:bg-slate-800 text-slate-400'
                  }`}
                >
                  <span>{preset.iconEmoji}</span>
                  <span>{preset.titleBn}</span>
                </button>
              );
            })}
          </nav>

          {/* Zone 3: Primary Action buttons */}
          <div className="flex items-center gap-2.5 shrink-0">
            <button
              onClick={() => fileInputRef.current?.click()}
              className="px-3.5 py-1.5 text-xs font-semibold text-black bg-amber-500 rounded-lg hover:bg-amber-400 transition-colors whitespace-nowrap flex items-center gap-1.5 cursor-pointer shadow-sm"
            >
              <Upload className="w-3.5 h-3.5" />
              <span>ভিডিও আপলোড</span>
            </button>
            <button
              onClick={() => setIsShareModalOpen(true)}
              className="p-1.5 text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors cursor-pointer border border-slate-700"
              title="শেয়ার করুন"
            >
              <Share2 className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* --- MAIN STUDIO WORKSPACE --- */}
      <main className="flex-1 max-w-7xl mx-auto w-full px-4 lg:px-8 py-6 grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* LEFT COLUMN: HERO CARTOON VIDEO PLAYER (7 cols) */}
        <div className="lg:col-span-7 space-y-4">
          <div className="bg-slate-900/90 border border-slate-800 rounded-2xl overflow-hidden shadow-2xl relative">
            {/* Aspect Ratio Container */}
            <div
              className={`w-full relative flex items-center justify-center bg-black ${
                aspectRatio === '9:16'
                  ? 'aspect-[9/16] max-h-[520px]'
                  : aspectRatio === '1:1'
                  ? 'aspect-square max-h-[460px]'
                  : 'aspect-video max-h-[440px]'
              }`}
            >
              <canvas
                ref={canvasRef}
                width={640}
                height={aspectRatio === '9:16' ? 1138 : aspectRatio === '1:1' ? 640 : 360}
                className="w-full h-full object-contain"
              />

              {/* Split Mode Handle Slider */}
              {isSplitMode && (
                <div
                  className="absolute inset-y-0 w-8 -translate-x-1/2 flex items-center justify-center cursor-ew-resize z-20"
                  style={{ left: `${splitRatio * 100}%` }}
                  onMouseDown={(e) => {
                    const startX = e.clientX;
                    const startRatio = splitRatio;
                    const onMove = (moveEv: MouseEvent) => {
                      const deltaX = moveEv.clientX - startX;
                      const rect = canvasRef.current?.getBoundingClientRect();
                      if (rect) {
                        const newR = Math.max(0.05, Math.min(0.95, startRatio + deltaX / rect.width));
                        setSplitRatio(newR);
                      }
                    };
                    const onUp = () => {
                      window.removeEventListener('mousemove', onMove);
                      window.removeEventListener('mouseup', onUp);
                    };
                    window.addEventListener('mousemove', onMove);
                    window.addEventListener('mouseup', onUp);
                  }}
                >
                  <div className="w-6 h-6 rounded-full bg-white text-black shadow-lg flex items-center justify-center text-[10px] font-bold">
                    ↔
                  </div>
                </div>
              )}

              {/* Split Mode Tag */}
              {isSplitMode && (
                <div className="absolute top-3 left-1/2 -translate-x-1/2 bg-black/80 backdrop-blur border border-white/20 rounded-full px-3 py-1 flex items-center gap-2 text-[11px] pointer-events-none z-10">
                  <span className="text-slate-400">👈 আসল</span>
                  <span className="text-slate-600">|</span>
                  <span className="text-amber-400 font-bold">কার্টুন 👉</span>
                </div>
              )}

              {/* SYNCHRONIZED SUBTITLE OVERLAY */}
              {currentSubtitle && (
                <div
                  className={`absolute px-4 text-center z-10 pointer-events-none w-full flex justify-center ${
                    subtitlePosition === 'top'
                      ? 'top-4'
                      : subtitlePosition === 'center'
                      ? 'top-1/2 -translate-y-1/2'
                      : 'bottom-16'
                  }`}
                >
                  <div
                    style={{ fontSize: `${subFontSize}px` }}
                    className={`max-w-[90%] font-bold px-4 py-1.5 rounded-lg transition-all ${
                      subtitleStyle === 'cartoon-yellow'
                        ? 'bg-black/85 text-yellow-300 border-2 border-yellow-400/80 shadow-[0_4px_12px_rgba(0,0,0,0.8)]'
                        : subtitleStyle === 'classic-white'
                        ? 'bg-black/75 text-white shadow-md'
                        : subtitleStyle === 'neon-cyan'
                        ? 'bg-slate-950/90 text-cyan-300 border border-cyan-400 shadow-[0_0_15px_rgba(6,182,212,0.4)]'
                        : 'bg-white text-slate-950 border-2 border-black rounded-2xl shadow-lg'
                    }`}
                  >
                    {currentSubtitle.text}
                  </div>
                </div>
              )}

              {/* Top Video Tags */}
              <div className="absolute top-3 left-3 flex items-center gap-2 text-xs">
                <span className="bg-slate-900/80 backdrop-blur px-2.5 py-1 rounded-md text-white border border-slate-700/80 font-medium">
                  {activeStyle.iconEmoji} {activeStyle.titleBn}
                </span>
                <span className="bg-slate-900/80 backdrop-blur px-2.5 py-1 rounded-md text-sky-400 border border-slate-700/80">
                  {uploadedVideoUrl ? '🎬 আপলোড করা ভিডিও' : `🌟 ${selectedPreset.titleBn}`}
                </span>
              </div>
            </div>

            {/* --- PLAYER CONTROLS BAR --- */}
            <div className="p-3 bg-slate-950/90 border-t border-slate-800 space-y-2">
              {/* Timeline Scrubber */}
              <div className="flex items-center gap-3 text-xs text-slate-400">
                <span className="font-mono">{formatTime(currentTime)}</span>
                <input
                  type="range"
                  min="0"
                  max={totalDuration}
                  step="0.1"
                  value={currentTime}
                  onChange={(e) => setCurrentTime(parseFloat(e.target.value))}
                  className="flex-1 accent-amber-500 h-1.5 bg-slate-800 rounded-lg cursor-pointer"
                />
                <span className="font-mono">{formatTime(totalDuration)}</span>
              </div>

              {/* Bottom Buttons Row */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setIsPlaying(!isPlaying)}
                    className="p-2 rounded-lg bg-amber-500 text-black hover:bg-amber-400 transition cursor-pointer font-bold"
                  >
                    {isPlaying ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
                  </button>
                  <button
                    onClick={() => setCurrentTime(0)}
                    className="p-2 rounded-lg bg-slate-800 text-slate-300 hover:text-white hover:bg-slate-700 transition cursor-pointer"
                    title="পুনরায় চালান"
                  >
                    <RotateCcw className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => setIsOriginalMuted(!isOriginalMuted)}
                    className={`p-2 rounded-lg transition cursor-pointer ${
                      isOriginalMuted
                        ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                        : 'bg-slate-800 text-slate-300 hover:text-white'
                    }`}
                    title={isOriginalMuted ? 'মূল অডিও মিউট' : 'মূল অডিও চালু'}
                  >
                    {isOriginalMuted ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
                  </button>
                </div>

                <div className="flex items-center gap-2 text-xs">
                  {/* Split Comparison Toggle */}
                  <button
                    onClick={() => setIsSplitMode(!isSplitMode)}
                    className={`px-3 py-1.5 rounded-lg border transition cursor-pointer flex items-center gap-1.5 ${
                      isSplitMode
                        ? 'bg-amber-500/20 text-amber-400 border-amber-500/50'
                        : 'bg-slate-800 text-slate-300 border-slate-700 hover:text-white'
                    }`}
                  >
                    <SplitSquareVertical className="w-3.5 h-3.5" />
                    <span>তুলনা স্লাইডার</span>
                  </button>

                  {/* Aspect Ratio Switcher */}
                  <button
                    onClick={() => {
                      const next = aspectRatio === '9:16' ? '1:1' : aspectRatio === '1:1' ? '16:9' : '9:16';
                      setAspectRatio(next);
                    }}
                    className="px-2.5 py-1.5 rounded-lg bg-slate-800 text-sky-400 border border-slate-700 hover:bg-slate-700 font-mono font-bold cursor-pointer"
                  >
                    {aspectRatio}
                  </button>

                  {/* Export Video Button */}
                  <button
                    onClick={handleExportVideo}
                    disabled={isRecordingVideo}
                    className="px-3 py-1.5 rounded-lg bg-emerald-600 text-white hover:bg-emerald-500 transition font-medium flex items-center gap-1.5 cursor-pointer disabled:opacity-50"
                  >
                    <Download className="w-3.5 h-3.5" />
                    <span>{isRecordingVideo ? 'রেকর্ড হচ্ছে...' : 'ভিডিও সেভ'}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Quick Mobile Preset Pills */}
          <div className="md:hidden flex items-center gap-2 overflow-x-auto pb-1">
            {SAMPLE_PRESETS.map((preset) => (
              <button
                key={preset.id}
                onClick={() => handleSelectPreset(preset)}
                className={`px-3 py-1 text-xs rounded-full whitespace-nowrap shrink-0 border ${
                  !uploadedVideoUrl && selectedPreset.id === preset.id
                    ? 'bg-amber-500 text-black border-amber-500 font-bold'
                    : 'bg-slate-900 text-slate-300 border-slate-800'
                }`}
              >
                {preset.iconEmoji} {preset.titleBn}
              </button>
            ))}
          </div>
        </div>

        {/* RIGHT COLUMN: STUDIO CONTROL PANEL TABS (5 cols) */}
        <div className="lg:col-span-5 bg-slate-900/80 border border-slate-800 rounded-2xl p-4 shadow-xl space-y-4">
          {/* Segmented Tab Bar */}
          <div className="grid grid-cols-4 p-1 bg-slate-950 rounded-xl border border-slate-800">
            <button
              onClick={() => setActiveTab('style')}
              className={`py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer flex flex-col items-center gap-1 ${
                activeTab === 'style'
                  ? 'bg-amber-500 text-black shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Palette className="w-3.5 h-3.5" />
              <span>স্টাইল</span>
            </button>
            <button
              onClick={() => setActiveTab('audio')}
              className={`py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer flex flex-col items-center gap-1 ${
                activeTab === 'audio'
                  ? 'bg-amber-500 text-black shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Music className="w-3.5 h-3.5" />
              <span>অডিও</span>
            </button>
            <button
              onClick={() => setActiveTab('subtitles')}
              className={`py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer flex flex-col items-center gap-1 ${
                activeTab === 'subtitles'
                  ? 'bg-amber-500 text-black shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <MessageSquare className="w-3.5 h-3.5" />
              <span>সাবটাইটেল</span>
            </button>
            <button
              onClick={() => setActiveTab('share')}
              className={`py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer flex flex-col items-center gap-1 ${
                activeTab === 'share'
                  ? 'bg-amber-500 text-black shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Share2 className="w-3.5 h-3.5" />
              <span>শেয়ার</span>
            </button>
          </div>

          {/* TAB 1: CARTOON STYLE & TUNING */}
          {activeTab === 'style' && (
            <div className="space-y-4">
              <div>
                <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2.5">
                  কার্টুন আর্ট স্টাইল পছন্দ করুন
                </h3>
                <div className="grid grid-cols-2 gap-2">
                  {CARTOON_STYLES.map((style) => {
                    const isSelected = activeStyle.id === style.id;
                    return (
                      <button
                        key={style.id}
                        onClick={() => handleSelectStyle(style)}
                        className={`p-3 rounded-xl border text-left transition-all cursor-pointer ${
                          isSelected
                            ? 'bg-amber-500/10 border-amber-500 text-white shadow-sm'
                            : 'bg-slate-950/60 border-slate-800 text-slate-300 hover:border-slate-700'
                        }`}
                      >
                        <div className="text-xl mb-1">{style.iconEmoji}</div>
                        <div className="text-xs font-bold">{style.titleBn}</div>
                        <div className="text-[10px] text-slate-400">{style.titleEn}</div>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Fine-Tuning Sliders */}
              <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-3.5 space-y-3">
                <div className="flex items-center justify-between text-xs font-semibold text-slate-300">
                  <span className="flex items-center gap-1.5">
                    <Sliders className="w-3.5 h-3.5 text-amber-500" />
                    কার্টুন ইফেক্ট ফাইন-টিউন
                  </span>
                  <button
                    onClick={() => handleSelectStyle(activeStyle)}
                    className="text-[10px] text-amber-400 hover:underline cursor-pointer"
                  >
                    রিসেট
                  </button>
                </div>

                {/* Outline Strength */}
                <div>
                  <div className="flex justify-between text-[11px] mb-1 text-slate-400">
                    <span>আউটলাইন গাঢ়ত্ব (Outline)</span>
                    <span className="text-amber-400 font-mono">{(outlineStrength * 100).toFixed(0)}%</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="1"
                    step="0.05"
                    value={outlineStrength}
                    onChange={(e) => setOutlineStrength(parseFloat(e.target.value))}
                    className="w-full accent-amber-500 h-1 bg-slate-800 rounded-lg cursor-pointer"
                  />
                </div>

                {/* Posterize Levels */}
                <div>
                  <div className="flex justify-between text-[11px] mb-1 text-slate-400">
                    <span>রঙের কার্টুন স্তর (Color Steps)</span>
                    <span className="text-amber-400 font-mono">{posterizeLevels} লেভেল</span>
                  </div>
                  <input
                    type="range"
                    min="3"
                    max="10"
                    step="1"
                    value={posterizeLevels}
                    onChange={(e) => setPosterizeLevels(parseInt(e.target.value))}
                    className="w-full accent-amber-500 h-1 bg-slate-800 rounded-lg cursor-pointer"
                  />
                </div>

                {/* Saturation */}
                <div>
                  <div className="flex justify-between text-[11px] mb-1 text-slate-400">
                    <span>রঙের উজ্জ্বলতা (Saturation)</span>
                    <span className="text-amber-400 font-mono">{(saturationBoost * 100).toFixed(0)}%</span>
                  </div>
                  <input
                    type="range"
                    min="0.5"
                    max="2.5"
                    step="0.1"
                    value={saturationBoost}
                    onChange={(e) => setSaturationBoost(parseFloat(e.target.value))}
                    className="w-full accent-amber-500 h-1 bg-slate-800 rounded-lg cursor-pointer"
                  />
                </div>

                {/* Contrast */}
                <div>
                  <div className="flex justify-between text-[11px] mb-1 text-slate-400">
                    <span>কনট্রাস্ট ও শ্যাডো (Contrast)</span>
                    <span className="text-amber-400 font-mono">{contrast.toFixed(1)}x</span>
                  </div>
                  <input
                    type="range"
                    min="0.8"
                    max="2.0"
                    step="0.1"
                    value={contrast}
                    onChange={(e) => setContrast(parseFloat(e.target.value))}
                    className="w-full accent-amber-500 h-1 bg-slate-800 rounded-lg cursor-pointer"
                  />
                </div>
              </div>
            </div>
          )}

          {/* TAB 2: AUDIO & BGM & VOICE */}
          {activeTab === 'audio' && (
            <div className="space-y-4">
              {/* BGM Select */}
              <div>
                <div className="flex items-center justify-between mb-2">
                  <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider">
                    কার্টুন ব্যাকগ্রাউন্ড মিউজিক (BGM)
                  </h3>
                  <button
                    onClick={() => setIsBgmActive(!isBgmActive)}
                    className={`px-2.5 py-0.5 text-[11px] rounded-full border cursor-pointer font-semibold ${
                      isBgmActive
                        ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40'
                        : 'bg-slate-800 text-slate-500 border-slate-700'
                    }`}
                  >
                    {isBgmActive ? 'চালু ✓' : 'বন্ধ ✕'}
                  </button>
                </div>

                <div className="grid grid-cols-2 gap-2">
                  {BGM_TRACKS.map((track) => {
                    const isSelected = activeBgm === track.id;
                    return (
                      <button
                        key={track.id}
                        onClick={() => {
                          setActiveBgm(track.id);
                          if (isBgmActive) cartoonAudioEngine.startBgm(track.id);
                        }}
                        className={`p-2.5 rounded-xl border text-left transition cursor-pointer ${
                          isSelected
                            ? 'bg-amber-500/10 border-amber-500 text-white'
                            : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700'
                        }`}
                      >
                        <div className="flex items-center gap-1.5 text-xs font-bold text-white mb-0.5">
                          <span>{track.moodEmoji}</span>
                          <span>{track.titleBn}</span>
                        </div>
                        <div className="text-[10px] text-slate-500">{track.tempoBpm} BPM</div>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Volume Mixer */}
              <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-3.5 space-y-3">
                <h4 className="text-xs font-semibold text-slate-300">🎛️ অডিও ভলিউম মিক্সার</h4>
                <div>
                  <div className="flex justify-between text-[11px] mb-1 text-slate-400">
                    <span>কার্টুন বিজিএম ভলিউম</span>
                    <span className="text-amber-400 font-mono">{(bgmVol * 100).toFixed(0)}%</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="1"
                    step="0.05"
                    value={bgmVol}
                    onChange={(e) => {
                      const val = parseFloat(e.target.value);
                      setBgmVol(val);
                      cartoonAudioEngine.bgmVolume = val;
                    }}
                    className="w-full accent-amber-500 h-1 bg-slate-800 rounded-lg cursor-pointer"
                  />
                </div>

                <div>
                  <div className="flex justify-between text-[11px] mb-1 text-slate-400">
                    <span>ভয়েস ও সাউন্ড এফেক্ট</span>
                    <span className="text-sky-400 font-mono">{(voiceSfxVol * 100).toFixed(0)}%</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="1"
                    step="0.05"
                    value={voiceSfxVol}
                    onChange={(e) => {
                      const val = parseFloat(e.target.value);
                      setVoiceSfxVol(val);
                      cartoonAudioEngine.sfxVolume = val;
                    }}
                    className="w-full accent-sky-500 h-1 bg-slate-800 rounded-lg cursor-pointer"
                  />
                </div>
              </div>

              {/* Instant Cartoon SFX Soundboard */}
              <div>
                <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">
                  কার্টুন সাউন্ড এফেক্ট (SFX)
                </h4>
                <div className="grid grid-cols-5 gap-1.5">
                  {[
                    { id: 'boing', label: 'বোয়িং', emoji: '🌀' },
                    { id: 'pop', label: 'পপ', emoji: '💥' },
                    { id: 'whistle', label: 'হুইসেল', emoji: '💨' },
                    { id: 'ding', label: 'ডিং', emoji: '✨' },
                    { id: 'fanfare', label: 'ফানফেয়ার', emoji: '🎺' }
                  ].map((sfx) => (
                    <button
                      key={sfx.id}
                      onClick={() => cartoonAudioEngine.playSfx(sfx.id as SfxId)}
                      className="p-2 rounded-xl bg-slate-950 border border-slate-800 hover:border-amber-500/50 hover:bg-slate-800 transition flex flex-col items-center gap-1 cursor-pointer"
                    >
                      <span className="text-lg">{sfx.emoji}</span>
                      <span className="text-[10px] text-slate-300">{sfx.label}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Cartoon TTS Voiceover */}
              <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-3.5 space-y-2.5">
                <div className="flex items-center justify-between text-xs font-semibold text-slate-300">
                  <span>🎙️ এআই কার্টুন ডায়ালগ ভয়েসওভার</span>
                </div>

                <div className="flex items-center gap-2">
                  {[
                    { label: 'কাঠবিড়ালি', pitch: 1.8, emoji: '🐿️' },
                    { label: 'কার্টুন হিরো', pitch: 1.2, emoji: '🐱' },
                    { label: 'দানব টুন', pitch: 0.6, emoji: '🐻' }
                  ].map((p) => (
                    <button
                      key={p.label}
                      onClick={() => setVoicePitch(p.pitch)}
                      className={`px-2 py-1 rounded-lg text-[10px] border cursor-pointer font-medium ${
                        voicePitch === p.pitch
                          ? 'bg-sky-500/20 text-sky-400 border-sky-500/50'
                          : 'bg-slate-900 text-slate-400 border-slate-800'
                      }`}
                    >
                      {p.emoji} {p.label}
                    </button>
                  ))}
                </div>

                <input
                  type="text"
                  value={voiceText}
                  onChange={(e) => setVoiceText(e.target.value)}
                  placeholder="কার্টুন সংলাপ লিখুন..."
                  className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
                />

                <button
                  onClick={() => cartoonAudioEngine.speakCartoonVoice(voiceText, voicePitch)}
                  className="w-full py-1.5 bg-amber-500 text-black font-bold text-xs rounded-lg hover:bg-amber-400 transition cursor-pointer"
                >
                  কার্টুন ভয়েসে বলুন 🗣️
                </button>
              </div>
            </div>
          )}

          {/* TAB 3: SUBTITLES MANAGER */}
          {activeTab === 'subtitles' && (
            <div className="space-y-4">
              {/* Auto Generate Button */}
              <div className="flex items-center justify-between">
                <button
                  onClick={handleAutoGenerateSubtitles}
                  disabled={isGeneratingSubs}
                  className="px-3 py-1.5 rounded-lg bg-sky-500 text-black font-bold text-xs hover:bg-sky-400 transition cursor-pointer flex items-center gap-1.5 disabled:opacity-50"
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>{isGeneratingSubs ? 'তৈরি হচ্ছে...' : '✨ অটো সাবটাইটেল তৈরি'}</span>
                </button>

                <div className="flex items-center gap-1">
                  <button
                    onClick={handleCopySrt}
                    className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs flex items-center gap-1 cursor-pointer border border-slate-700"
                    title="SRT কপি"
                  >
                    <Copy className="w-3 h-3" />
                    <span>কপি</span>
                  </button>
                  <button
                    onClick={handleDownloadSrt}
                    className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs flex items-center gap-1 cursor-pointer border border-slate-700"
                    title="SRT ডাউনলোড"
                  >
                    <Download className="w-3 h-3" />
                    <span>SRT</span>
                  </button>
                </div>
              </div>

              {/* Subtitle Style & Position Presets */}
              <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-3 space-y-2.5">
                <div className="flex items-center justify-between text-xs font-semibold text-slate-300">
                  <span>সাবটাইটেল স্টাইল</span>
                  <div className="flex items-center gap-1 text-[10px]">
                    {(['bottom', 'center', 'top'] as SubtitlePosition[]).map((pos) => (
                      <button
                        key={pos}
                        onClick={() => setSubtitlePosition(pos)}
                        className={`px-2 py-0.5 rounded border cursor-pointer ${
                          subtitlePosition === pos
                            ? 'bg-amber-500 text-black border-amber-500 font-bold'
                            : 'bg-slate-900 text-slate-400 border-slate-800'
                        }`}
                      >
                        {pos === 'bottom' ? 'নিচে' : pos === 'center' ? 'মাঝে' : 'উপরে'}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="grid grid-cols-4 gap-1.5">
                  {[
                    { id: 'cartoon-yellow', label: 'হলুদ টুন' },
                    { id: 'classic-white', label: 'সাদা বক্স' },
                    { id: 'neon-cyan', label: 'নিয়ন' },
                    { id: 'comic-bubble', label: 'বাবল' }
                  ].map((st) => (
                    <button
                      key={st.id}
                      onClick={() => setSubtitleStyle(st.id as SubtitleStylePreset)}
                      className={`p-1.5 text-[10px] rounded-lg border text-center font-bold cursor-pointer ${
                        subtitleStyle === st.id
                          ? 'bg-amber-500/20 text-amber-400 border-amber-500'
                          : 'bg-slate-900 text-slate-400 border-slate-800 hover:border-slate-700'
                      }`}
                    >
                      {st.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Add New Subtitle Input */}
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  value={newSubText}
                  onChange={(e) => setNewSubText(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleAddSubtitle()}
                  placeholder={`${formatTime(currentTime)} সময়ে সাবটাইটেল লিখুন...`}
                  className="flex-1 bg-slate-950 border border-slate-800 rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
                />
                <button
                  onClick={handleAddSubtitle}
                  className="p-2 bg-amber-500 text-black rounded-lg hover:bg-amber-400 transition cursor-pointer font-bold"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>

              {/* Subtitles Cue List */}
              <div className="space-y-2 max-h-56 overflow-y-auto pr-1">
                {subtitles.map((cue) => {
                  const isActive = currentTime >= cue.startTime && currentTime <= cue.endTime;
                  return (
                    <div
                      key={cue.id}
                      className={`p-2.5 rounded-xl border flex items-center justify-between gap-2 text-xs transition ${
                        isActive
                          ? 'bg-amber-500/10 border-amber-500/80 text-white'
                          : 'bg-slate-950/70 border-slate-800 text-slate-300'
                      }`}
                    >
                      <button
                        onClick={() => setCurrentTime(cue.startTime)}
                        className="text-[10px] font-mono text-sky-400 hover:underline cursor-pointer"
                      >
                        {formatTime(cue.startTime)} - {formatTime(cue.endTime)}
                      </button>

                      <input
                        type="text"
                        value={cue.text}
                        onChange={(e) => {
                          const val = e.target.value;
                          setSubtitles((prev) =>
                            prev.map((c) => (c.id === cue.id ? { ...c, text: val } : c))
                          );
                        }}
                        className="flex-1 bg-transparent border-b border-transparent hover:border-slate-700 focus:border-amber-500 focus:outline-none text-xs text-white px-1"
                      />

                      <button
                        onClick={() => handleDeleteCue(cue.id)}
                        className="text-slate-500 hover:text-rose-400 transition p-1 cursor-pointer"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* TAB 4: SOCIAL SHARE */}
          {activeTab === 'share' && (
            <div className="space-y-4">
              <div>
                <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">
                  সোশ্যাল মিডিয়া অনুপাত (Aspect Ratio)
                </h3>
                <div className="grid grid-cols-3 gap-2">
                  {[
                    { id: '9:16', label: 'রিলস ও শর্টস', emoji: '📱' },
                    { id: '1:1', label: 'ইনস্টাগ্রাম ফিড', emoji: '⬛' },
                    { id: '16:9', label: 'ইউটিউব ল্যান্ডস্কেপ', emoji: '📺' }
                  ].map((r) => (
                    <button
                      key={r.id}
                      onClick={() => setAspectRatio(r.id as '9:16' | '1:1' | '16:9')}
                      className={`p-2.5 rounded-xl border text-center transition cursor-pointer ${
                        aspectRatio === r.id
                          ? 'bg-amber-500/20 border-amber-500 text-white font-bold'
                          : 'bg-slate-950 border-slate-800 text-slate-400 hover:border-slate-700'
                      }`}
                    >
                      <div className="text-base">{r.emoji}</div>
                      <div className="text-xs">{r.id}</div>
                      <div className="text-[10px] text-slate-500">{r.label}</div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Direct App Share Buttons */}
              <div>
                <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">
                  অ্যাপে শেয়ার করুন
                </h3>
                <div className="grid grid-cols-4 gap-2">
                  {[
                    { id: 'whatsapp', label: 'হোয়াটসঅ্যাপ', color: 'hover:bg-emerald-600', emoji: '💬' },
                    { id: 'facebook', label: 'ফেসবুক', color: 'hover:bg-blue-600', emoji: '📘' },
                    { id: 'twitter', label: 'এক্স', color: 'hover:bg-sky-600', emoji: '✖️' },
                    { id: 'telegram', label: 'টেলিগ্রাম', color: 'hover:bg-cyan-600', emoji: '✈️' }
                  ].map((app) => (
                    <button
                      key={app.id}
                      onClick={() => shareToPlatform(app.id)}
                      className={`p-2.5 rounded-xl bg-slate-950 border border-slate-800 transition flex flex-col items-center gap-1 cursor-pointer ${app.color}`}
                    >
                      <span className="text-lg">{app.emoji}</span>
                      <span className="text-[10px] text-slate-300">{app.label}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Caption and Hashtags */}
              <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-3 space-y-2">
                <div className="flex items-center justify-between text-xs font-semibold text-slate-300">
                  <span>পোস্টের ক্যাপশন ও হ্যাশট্যাগ</span>
                  <button
                    onClick={() => {
                      navigator.clipboard.writeText(shareCaption);
                      showToast('ক্যাপশন কপি করা হয়েছে! 📋');
                    }}
                    className="text-[10px] text-amber-400 hover:underline flex items-center gap-1 cursor-pointer"
                  >
                    <Copy className="w-3 h-3" />
                    <span>কপি</span>
                  </button>
                </div>
                <textarea
                  value={shareCaption}
                  onChange={(e) => setShareCaption(e.target.value)}
                  rows={3}
                  className="w-full bg-slate-900 border border-slate-800 rounded-lg p-2 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                />
              </div>

              {/* Export Video Button */}
              <button
                onClick={handleExportVideo}
                className="w-full py-2.5 rounded-xl bg-amber-500 text-black font-bold text-xs hover:bg-amber-400 transition cursor-pointer flex items-center justify-center gap-2"
              >
                <Download className="w-4 h-4" />
                <span>ভিডিও ও অডিও ডাউনলোড করুন</span>
              </button>
            </div>
          )}
        </div>
      </main>

      {/* --- SOCIAL SHARE MODAL DIALOG --- */}
      {isShareModalOpen && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-5 shadow-2xl">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-base font-bold text-white flex items-center gap-2">
                  <span>🚀</span> সোশ্যাল মিডিয়ায় শেয়ার করুন
                </h3>
                <p className="text-xs text-slate-400">আপনার কার্টুন ভিডিও সরাসরি বন্ধুদের কাছে পৌঁছে দিন</p>
              </div>
              <button
                onClick={() => setIsShareModalOpen(false)}
                className="text-slate-400 hover:text-white p-1 text-sm cursor-pointer"
              >
                ✕
              </button>
            </div>

            {/* Platform Quick Grid */}
            <div className="grid grid-cols-4 gap-2.5">
              {[
                { id: 'whatsapp', label: 'হোয়াটসঅ্যাপ', emoji: '💬' },
                { id: 'facebook', label: 'ফেসবুক', emoji: '📘' },
                { id: 'twitter', label: 'এক্স', emoji: '✖️' },
                { id: 'telegram', label: 'টেলিগ্রাম', emoji: '✈️' }
              ].map((app) => (
                <button
                  key={app.id}
                  onClick={() => shareToPlatform(app.id)}
                  className="p-3 rounded-xl bg-slate-950 border border-slate-800 hover:border-amber-500 transition flex flex-col items-center gap-1.5 cursor-pointer"
                >
                  <span className="text-2xl">{app.emoji}</span>
                  <span className="text-xs text-slate-300">{app.label}</span>
                </button>
              ))}
            </div>

            {/* Caption Preview */}
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3 space-y-2">
              <div className="flex justify-between items-center text-xs text-slate-400">
                <span>ক্যাপশন:</span>
                <button
                  onClick={() => {
                    navigator.clipboard.writeText(shareCaption);
                    showToast('ক্যাপশন কপি করা হয়েছে! 📋');
                  }}
                  className="text-amber-400 hover:underline text-xs flex items-center gap-1 cursor-pointer"
                >
                  <Copy className="w-3 h-3" /> কপি
                </button>
              </div>
              <p className="text-xs text-slate-200 line-clamp-3 bg-slate-900 p-2 rounded">
                {shareCaption}
              </p>
            </div>

            {/* Actions */}
            <div className="flex gap-2">
              <button
                onClick={handleNativeShare}
                className="flex-1 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-medium text-xs transition cursor-pointer flex items-center justify-center gap-1.5"
              >
                <Share2 className="w-4 h-4" />
                <span>অন্যান্য অ্যাপ</span>
              </button>
              <button
                onClick={() => {
                  handleExportVideo();
                  setIsShareModalOpen(false);
                }}
                className="flex-1 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-black font-bold text-xs transition cursor-pointer flex items-center justify-center gap-1.5"
              >
                <Download className="w-4 h-4" />
                <span>ভিডিও সেভ</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
