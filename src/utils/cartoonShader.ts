import { CartoonStyleId, SubtitleCue } from '../types';

/**
 * Apply fast cartoon pixel transformation on ImageData:
 * 1. Edge detection for dark comic ink outlines
 * 2. Color quantization (posterization) into cel-shaded levels
 * 3. Saturation and contrast boost
 */
export function applyCartoonFilterToImageData(
  imageData: ImageData,
  styleId: CartoonStyleId,
  outlineStrength: number,
  posterizeLevels: number,
  saturationBoost: number,
  contrast: number
) {
  const data = imageData.data;
  const width = imageData.width;
  const height = imageData.height;

  // Clone luminance buffer for edge detection
  const lum = new Float32Array(width * height);
  for (let i = 0, p = 0; i < data.length; i += 4, p++) {
    lum[p] = 0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2];
  }

  // Pre-calculate step quantization size
  const step = 255 / Math.max(2, posterizeLevels);

  // Sobel Edge Detection kernel offsets
  for (let y = 1; y < height - 1; y++) {
    const rowOffset = y * width;
    for (let x = 1; x < width - 1; x++) {
      const idx = rowOffset + x;
      const pixelIdx = idx * 4;

      // Sobel horizontal & vertical gradients
      const gx =
        -lum[idx - width - 1] + lum[idx - width + 1] -
        2 * lum[idx - 1] + 2 * lum[idx + 1] -
        lum[idx + width - 1] + lum[idx + width + 1];

      const gy =
        -lum[idx - width - 1] - 2 * lum[idx - width] - lum[idx - width + 1] +
        lum[idx + width - 1] + 2 * lum[idx + width] + lum[idx + width + 1];

      const edgeMag = Math.sqrt(gx * gx + gy * gy);
      const isEdge = edgeMag > (90 - outlineStrength * 45);

      let r = data[pixelIdx];
      let g = data[pixelIdx + 1];
      let b = data[pixelIdx + 2];

      if (styleId === 'sketch') {
        // Pencil sketch: light paper with dark charcoal lines
        const edgeShade = Math.max(0, 255 - edgeMag * outlineStrength * 2.2);
        data[pixelIdx] = edgeShade;
        data[pixelIdx + 1] = edgeShade;
        data[pixelIdx + 2] = edgeShade;
        continue;
      }

      if (isEdge && outlineStrength > 0.1) {
        // Dark cartoon ink line
        const inkFactor = Math.max(0, 1 - outlineStrength * 0.95);
        r = r * inkFactor;
        g = g * inkFactor;
        b = b * inkFactor;
      } else {
        // Posterization color clustering
        r = Math.floor(r / step) * step;
        g = Math.floor(g / step) * step;
        b = Math.floor(b / step) * step;

        // Saturation Boost
        const gray = 0.299 * r + 0.587 * g + 0.114 * b;
        r = gray + (r - gray) * saturationBoost;
        g = gray + (g - gray) * saturationBoost;
        b = gray + (b - gray) * saturationBoost;

        // Contrast boost
        r = ((r / 255 - 0.5) * contrast + 0.5) * 255;
        g = ((g / 255 - 0.5) * contrast + 0.5) * 255;
        b = ((b / 255 - 0.5) * contrast + 0.5) * 255;

        // Style nuances
        if (styleId === 'watercolor') {
          // Softer pastels with slight warm tint
          r = r * 1.05 + 10;
          g = g * 1.02 + 8;
          b = b * 0.95 + 5;
        } else if (styleId === 'popart') {
          // Neon Pop
          r = r > 128 ? Math.min(255, r * 1.2) : r * 0.7;
          g = g > 128 ? Math.min(255, g * 1.2) : g * 0.7;
          b = b > 128 ? Math.min(255, b * 1.3) : b * 0.7;
        }
      }

      data[pixelIdx] = Math.max(0, Math.min(255, r));
      data[pixelIdx + 1] = Math.max(0, Math.min(255, g));
      data[pixelIdx + 2] = Math.max(0, Math.min(255, b));
    }
  }
}

/**
 * Procedural sample cartoon animation renderer for presets
 */
export function drawSampleAnimationScene(
  ctx: CanvasRenderingContext2D,
  width: number,
  height: number,
  sceneType: 'kitty' | 'city' | 'pals' | 'dance',
  timeSeconds: number,
  isCartoon: boolean
) {
  const t = timeSeconds;

  ctx.save();

  if (sceneType === 'kitty') {
    // Sky
    ctx.fillStyle = isCartoon ? '#70C1FF' : '#87CEEB';
    ctx.fillRect(0, 0, width, height * 0.65);

    // Sun
    ctx.fillStyle = '#FFD93D';
    ctx.beginPath();
    ctx.arc(width * 0.85, height * 0.20, width * 0.08, 0, Math.PI * 2);
    ctx.fill();
    if (isCartoon) {
      ctx.lineWidth = 4;
      ctx.strokeStyle = '#000000';
      ctx.stroke();
    }

    // Grass ground
    ctx.fillStyle = isCartoon ? '#44D368' : '#4CAF50';
    ctx.fillRect(0, height * 0.65, width, height * 0.35);

    // Bouncing Cat
    const bounceY = Math.sin(t * 4) * (height * 0.04);
    const catX = width * 0.45;
    const catY = height * 0.68 + bounceY;

    // Body
    ctx.fillStyle = isCartoon ? '#FF9F43' : '#E67E22';
    ctx.beginPath();
    ctx.arc(catX, catY, width * 0.16, 0, Math.PI * 2);
    ctx.fill();
    if (isCartoon) {
      ctx.lineWidth = 5;
      ctx.strokeStyle = '#000000';
      ctx.stroke();
    }

    // Head
    const headR = width * 0.12;
    const headY = catY - width * 0.16 * 0.9;
    ctx.beginPath();
    ctx.arc(catX, headY, headR, 0, Math.PI * 2);
    ctx.fill();
    if (isCartoon) ctx.stroke();

    // Ears
    ctx.beginPath();
    ctx.moveTo(catX - headR * 0.8, headY - headR * 0.4);
    ctx.lineTo(catX - headR * 0.9, headY - headR * 1.5);
    ctx.lineTo(catX - headR * 0.2, headY - headR * 0.8);
    ctx.fill();
    if (isCartoon) ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(catX + headR * 0.2, headY - headR * 0.8);
    ctx.lineTo(catX + headR * 0.9, headY - headR * 1.5);
    ctx.lineTo(catX + headR * 0.8, headY - headR * 0.4);
    ctx.fill();
    if (isCartoon) ctx.stroke();

    // Big Eyes
    ctx.fillStyle = '#FFFFFF';
    ctx.beginPath();
    ctx.arc(catX - headR * 0.35, headY - headR * 0.1, headR * 0.25, 0, Math.PI * 2);
    ctx.arc(catX + headR * 0.35, headY - headR * 0.1, headR * 0.25, 0, Math.PI * 2);
    ctx.fill();
    if (isCartoon) {
      ctx.lineWidth = 3;
      ctx.stroke();
    }

    // Pupils
    const pupilShift = Math.sin(t * 3) * 3;
    ctx.fillStyle = '#000000';
    ctx.beginPath();
    ctx.arc(catX - headR * 0.35 + pupilShift, headY - headR * 0.1, headR * 0.12, 0, Math.PI * 2);
    ctx.arc(catX + headR * 0.35 + pupilShift, headY - headR * 0.1, headR * 0.12, 0, Math.PI * 2);
    ctx.fill();

    // Nose
    ctx.fillStyle = '#FF6B6B';
    ctx.beginPath();
    ctx.arc(catX, headY + headR * 0.2, headR * 0.08, 0, Math.PI * 2);
    ctx.fill();

    // Butterfly
    const bflyX = width * 0.72 + Math.cos(t * 3) * (width * 0.12);
    const bflyY = height * 0.35 + Math.sin(t * 4.5) * (height * 0.08);
    ctx.fillStyle = '#FF5252';
    ctx.beginPath();
    ctx.arc(bflyX - 10, bflyY, 12, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = '#FFD166';
    ctx.beginPath();
    ctx.arc(bflyX + 10, bflyY, 12, 0, Math.PI * 2);
    ctx.fill();
    if (isCartoon) {
      ctx.lineWidth = 2;
      ctx.strokeStyle = '#000000';
      ctx.stroke();
    }
  } else if (sceneType === 'city') {
    // City Sunset
    ctx.fillStyle = isCartoon ? '#FFA07A' : '#E59866';
    ctx.fillRect(0, 0, width, height * 0.7);

    // Buildings
    const colors = ['#2C3E50', '#34495E', '#1B2631', '#4A235A'];
    for (let i = 0; i < 5; i++) {
      ctx.fillStyle = colors[i % colors.length];
      const bx = i * (width * 0.22);
      const bh = height * (0.35 + (i % 3) * 0.08);
      ctx.fillRect(bx, height * 0.7 - bh, width * 0.20, height * 0.7);
      if (isCartoon) {
        ctx.lineWidth = 3;
        ctx.strokeStyle = '#000000';
        ctx.strokeRect(bx, height * 0.7 - bh, width * 0.20, height * 0.7);
      }

      // Yellow glowing windows
      ctx.fillStyle = '#F9E79F';
      for (let wy = 1; wy <= 4; wy++) {
        for (let wx = 1; wx <= 2; wx++) {
          const winX = bx + wx * (width * 0.06) - 6;
          const winY = height * 0.7 - bh + wy * (height * 0.06);
          ctx.fillRect(winX, winY, 10, 14);
        }
      }
    }

    // Road
    ctx.fillStyle = '#34495E';
    ctx.fillRect(0, height * 0.7, width, height * 0.3);

    // Cartoon Red Car
    const carX = ((t * 140) % (width + 200)) - 120;
    const carY = height * 0.82;
    ctx.fillStyle = '#E74C3C';
    ctx.beginPath();
    ctx.roundRect(carX, carY - 26, 120, 36, 10);
    ctx.roundRect(carX + 20, carY - 50, 75, 30, 8);
    ctx.fill();
    if (isCartoon) {
      ctx.lineWidth = 4;
      ctx.strokeStyle = '#000000';
      ctx.stroke();
    }

    // Wheels
    ctx.fillStyle = '#1A1A1A';
    ctx.beginPath();
    ctx.arc(carX + 30, carY + 12, 14, 0, Math.PI * 2);
    ctx.arc(carX + 90, carY + 12, 14, 0, Math.PI * 2);
    ctx.fill();
    if (isCartoon) ctx.stroke();
  } else if (sceneType === 'pals') {
    // Room / Studio
    ctx.fillStyle = '#85C1E9';
    ctx.fillRect(0, 0, width, height * 0.75);
    ctx.fillStyle = '#D5D8DC';
    ctx.fillRect(0, height * 0.75, width, height * 0.25);

    // Friend 1 (Left Blue Pal)
    const p1X = width * 0.32;
    const p1Y = height * 0.65 + Math.sin(t * 3.5) * 8;
    ctx.fillStyle = '#5DADE2';
    ctx.beginPath();
    ctx.arc(p1X, p1Y, width * 0.14, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = '#F5B7B1';
    ctx.beginPath();
    ctx.arc(p1X, p1Y - width * 0.16, width * 0.11, 0, Math.PI * 2);
    ctx.fill();
    if (isCartoon) {
      ctx.lineWidth = 4;
      ctx.strokeStyle = '#000000';
      ctx.stroke();
    }

    // Friend 2 (Right Orange Pal)
    const p2X = width * 0.68;
    const p2Y = height * 0.65 + Math.cos(t * 3.5) * 8;
    ctx.fillStyle = '#F5B041';
    ctx.beginPath();
    ctx.arc(p2X, p2Y, width * 0.14, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = '#FADBD8';
    ctx.beginPath();
    ctx.arc(p2X, p2Y - width * 0.16, width * 0.11, 0, Math.PI * 2);
    ctx.fill();
    if (isCartoon) {
      ctx.lineWidth = 4;
      ctx.strokeStyle = '#000000';
      ctx.stroke();
    }
  } else if (sceneType === 'dance') {
    // Dancing Star stage
    ctx.fillStyle = '#17202A';
    ctx.fillRect(0, 0, width, height);

    // Disco Spotlight
    ctx.fillStyle = Math.floor(t) % 2 === 0 ? '#9B59B6' : '#1ABC9C';
    ctx.beginPath();
    ctx.arc(width * 0.5, height * 0.52, width * 0.36, 0, Math.PI * 2);
    ctx.fill();

    // Dancing Star
    const starX = width * 0.5 + Math.sin(t * 4) * 24;
    const starY = height * 0.52 + Math.sin(t * 8) * 16;
    ctx.fillStyle = '#F4D03F';
    ctx.beginPath();
    ctx.arc(starX, starY, width * 0.16, 0, Math.PI * 2);
    ctx.fill();
    if (isCartoon) {
      ctx.lineWidth = 5;
      ctx.strokeStyle = '#000000';
      ctx.stroke();
    }

    // Star Face
    ctx.fillStyle = '#000000';
    ctx.beginPath();
    ctx.arc(starX - 20, starY - 12, 8, 0, Math.PI * 2);
    ctx.arc(starX + 20, starY - 12, 8, 0, Math.PI * 2);
    ctx.fill();

    ctx.beginPath();
    ctx.arc(starX, starY + 6, 22, 0, Math.PI);
    ctx.fill();
  }

  ctx.restore();
}

/**
 * Generate standard SRT subtitle formatted text
 */
export function formatSubtitlesAsSrt(subtitles: SubtitleCue[]): string {
  const pad = (n: number, z = 2) => ('00' + n).slice(-z);
  const formatSrtTime = (seconds: number) => {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    const millis = Math.floor((seconds % 1) * 1000);
    return `${pad(hrs)}:${pad(mins)}:${pad(secs)},${pad(millis, 3)}`;
  };

  return subtitles
    .map((cue, index) => {
      return `${index + 1}\n${formatSrtTime(cue.startTime)} --> ${formatSrtTime(cue.endTime)}\n${cue.text}\n`;
    })
    .join('\n');
}
